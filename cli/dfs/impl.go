package dfs

import (
	"context"
	"fmt"
	"io"
	"net"
	"os"

	"strconv"

	"github.com/heroku/docker-registry-client/Godeps/_workspace/src/github.com/Sirupsen/logrus"
	"google.golang.org/grpc"
	"google.golang.org/grpc/metadata"
	"nju.edu.cn/ds/lab2/cli/file"
	"nju.edu.cn/ds/lab2/cli/utils"
)

type fileSystemError interface {
	GetError() *file.FileSystemError
}

func combineErrors(fserr fileSystemError, err error) error {
	if err != nil {
		return err
	}
	if fserr.GetError() != nil {
		return fmt.Errorf(fserr.GetError().ErrorMessage)
	}
	return nil
}

type DistributedFileSystemImpl struct {
	url      string
	conn     *grpc.ClientConn
	fsclient file.FileSystemClient
	context  context.Context
}

func New(url string, token string) DistributedFileSystem {
	return &DistributedFileSystemImpl{
		url:      url,
		conn:     nil,
		fsclient: nil,
		context:  metadata.NewContext(context.Background(), metadata.New(map[string]string{"TOKEN": token})),
	}
}

func (fs *DistributedFileSystemImpl) Open() error {
	var err error
	if fs.conn == nil {
		fs.conn, err = grpc.Dial(fs.url, grpc.WithInsecure())
		if err == nil {
			fs.fsclient = file.NewFileSystemClient(fs.conn)
		}
		return err
	}
	return fmt.Errorf("already connected")
}

func (fs *DistributedFileSystemImpl) Close() error {
	if fs.fsclient != nil {
		err := fs.conn.Close()
		fs.conn = nil
		fs.fsclient = nil
		return err
	}
	return fmt.Errorf("not connected")
}

func (fs *DistributedFileSystemImpl) List(path string) ([]string, error) {
	if fs.fsclient == nil {
		return nil, fmt.Errorf("not connected")
	}
	if !utils.ValidatePath(path) {
		return nil, utils.InvalidPathError(path)
	}
	resp, err := fs.fsclient.List(fs.context, &file.Path{
		Path: path,
	}, grpc.FailFast(true))
	if err = combineErrors(resp, err); err != nil {
		return nil, err
	}
	return resp.Name, nil
}

func (fs *DistributedFileSystemImpl) Mkdir(path string) error {
	if fs.fsclient == nil {
		return fmt.Errorf("not connected")
	}
	if !utils.ValidatePath(path) {
		return utils.InvalidPathError(path)
	}
	resp, err := fs.fsclient.CreateDirectory(fs.context, &file.Path{
		Path: path,
	}, grpc.FailFast(true))
	if err = combineErrors(resp, err); err != nil {
		return err
	}
	return nil
}

func (fs *DistributedFileSystemImpl) Move(src, dest string) error {
	if fs.fsclient == nil {
		return fmt.Errorf("not connected")
	}
	if !utils.ValidatePath(src) {
		return utils.InvalidPathError(src)
	}
	if !utils.ValidatePath(dest) {
		return utils.InvalidPathError(dest)
	}
	resp, err := fs.fsclient.Move(fs.context, &file.MoveRequest{
		Src: &file.Path{
			Path: src,
		},
		Dest: &file.Path{
			Path: dest,
		},
	}, grpc.FailFast(true))
	if err = combineErrors(resp, err); err != nil {
		return err
	}
	return nil
}

func (fs *DistributedFileSystemImpl) Remove(path string) error {
	if fs.fsclient == nil {
		return fmt.Errorf("not connected")
	}
	if !utils.ValidatePath(path) {
		return utils.InvalidPathError(path)
	}
	resp, err := fs.fsclient.Delete(fs.context, &file.Path{
		Path: path,
	}, grpc.FailFast(true))
	if err = combineErrors(resp, err); err != nil {
		return err
	}
	return nil
}

func (fs *DistributedFileSystemImpl) getFileMeta(path string) (*file.File, error) {
	resp, err := fs.fsclient.GetFileMeta(fs.context, &file.Path{
		Path: path,
	}, grpc.FailFast(true))
	if err = combineErrors(resp, err); err != nil {
		return nil, err
	}
	return resp.GetFile(), nil
}

func JoinHostPort(url *file.FileStoreURL) string {
	if url == nil {
		return ""
	}
	return net.JoinHostPort(url.Host, strconv.Itoa(int(url.Port)))
}

func (fs *DistributedFileSystemImpl) createFileMeta(local string, remote string) (*file.File, error) {
	localFileInfo, err := os.Stat(local)
	if err != nil {
		return nil, err
	}
	fileMD5Hash, err := utils.HashFileMD5(local)
	resp, err := fs.fsclient.CreateFileMeta(fs.context, &file.File{
		Name: localFileInfo.Name(),
		Path: &file.Path{
			Path: remote,
		},
		Size:     localFileInfo.Size(),
		Checksum: []byte(fileMD5Hash),
	}, grpc.FailFast(true))
	if err = combineErrors(resp, err); err != nil {
		return nil, err
	}
	return resp.GetFile(), nil
}

func (fs *DistributedFileSystemImpl) Get(remote, local string) error {
	if fs.fsclient == nil {
		return fmt.Errorf("not connected")
	}
	if !utils.ValidatePath(remote) {
		return utils.InvalidPathError(remote)
	}
	if !utils.ValidatePath(local) {
		return utils.InvalidPathError(local)
	}
	fileMeta, err := fs.getFileMeta(remote)
	if err != nil {
		return err
	}
	dfsconn, err := grpc.Dial(JoinHostPort(fileMeta.GetFileStoreUrl()), grpc.WithInsecure())
	if err != nil {
		return err
	}
	defer dfsconn.Close()
	fsdclient := file.NewFileStoreDeviceClient(dfsconn)
	getFileClient, err := fsdclient.GetFile(context.Background())
	if err != nil {
		return err
	}
	defer getFileClient.CloseSend()
	err = getFileClient.Send(&file.GetFileRequest{
		Command: file.TransferCommand_INIT,
		Token:   nil,
		Uuid:    []byte(fileMeta.Uuid),
	})
	if err != nil {
		return err
	}
	localFile, err := os.Create(local)
	if err != nil {
		return err
	}
	defer localFile.Close()
	// Start transformation
	for {
		resp, err := getFileClient.Recv()
		if err != nil {
			if err == io.EOF {
				return nil
			}
			return err
		}
		switch resp.Data.(type) {
		case *file.ControlledPacket_FileBlock:
			block := resp.GetFileBlock().Block
			_, err := localFile.Write(block)
			if err != nil {
				return err
			}
		case *file.ControlledPacket_Command:
			command := resp.GetCommand()
			if command == file.TransferCommand_CLOSE {
				getFileClient.Send(&file.GetFileRequest{
					Command: file.TransferCommand_CLOSE,
				})
			}
			return nil
		//case *file.ControlledPacket_FileToChap:
		//case *file.ControlledPacket_Checksum:
		default:
			panic("You should never receive this type of packet here.")
		}
	}
	return nil
}

func (fs *DistributedFileSystemImpl) Put(local, remote string) error {
	if fs.fsclient == nil {
		return fmt.Errorf("not connected")
	}
	if !utils.ValidatePath(local) {
		return utils.InvalidPathError(local)
	}
	if !utils.ValidatePath(remote) {
		return utils.InvalidPathError(remote)
	}
	remoteFile, err := fs.createFileMeta(local, remote)
	if err != nil {
		return err
	}
	dfsconn, err := grpc.Dial(JoinHostPort(remoteFile.GetFileStoreUrl()), grpc.WithInsecure())
	if err != nil {
		return err
	}
	defer dfsconn.Close()
	fsdclient := file.NewFileStoreDeviceClient(dfsconn)
	localFile, err := os.Open(local)
	if err != nil {
		return err
	}
	defer localFile.Close()
	// Start transformation
	putFileClient, err := fsdclient.PutFile(context.Background())
	if err != nil {
		return err
	}
	defer putFileClient.CloseSend()
	err = putFileClient.Send(&file.ControlledPacket{
		Data: &file.ControlledPacket_FileToChap{
			FileToChap: remoteFile,
		},
	})
	if err != nil {
		return err
	}
	buffer := make([]byte, 1024, 1024)
	totalSent := 0
	for {
		readSize, err := localFile.Read(buffer)
		if err != nil {
			if err == io.EOF {
				break
			}
			return err
		}
		totalSent += readSize
		err = putFileClient.Send(&file.ControlledPacket{
			Data: &file.ControlledPacket_FileBlock{
				FileBlock: &file.FileBlock{
					Block: buffer[:readSize],
				},
			},
		})
		if err != nil {
			return err
		}
	}
	err = putFileClient.Send(&file.ControlledPacket{
		Data: &file.ControlledPacket_Command{
			Command: file.TransferCommand_CLOSE,
		},
	})
	if err != nil {
		return err
	}
	putFileClient.Recv()
	logrus.Infof("File sent, total bytes is %d", totalSent)
	return nil
}
