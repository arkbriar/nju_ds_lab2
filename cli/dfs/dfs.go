package dfs

import (
	"context"
	"fmt"
	"io"
	"os"

	"net"

	"google.golang.org/grpc"
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

type DistributedFileSystem struct {
	url      string
	conn     *grpc.ClientConn
	fsclient file.FileSystemClient
}

func New(url string) *DistributedFileSystem {
	return &DistributedFileSystem{
		url:      url,
		conn:     nil,
		fsclient: nil,
	}
}

func (fs *DistributedFileSystem) Open() error {
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

func (fs *DistributedFileSystem) Close() error {
	if fs.fsclient != nil {
		err := fs.conn.Close()
		fs.conn = nil
		fs.fsclient = nil
		return err
	}
	return fmt.Errorf("not connected")
}

func (fs *DistributedFileSystem) List(path string) ([]string, error) {
	if fs.fsclient == nil {
		return nil, fmt.Errorf("not connected")
	}
	if !utils.ValidatePath(path) {
		return utils.InvalidPathError(path)
	}
	resp, err := fs.fsclient.List(context.Background(), &file.Path{
		Path: path,
	})
	if err = combineErrors(resp, err); err != nil {
		return nil, err
	}
	return resp.Name, nil
}

func (fs *DistributedFileSystem) Mkdir(path string) error {
	if fs.fsclient == nil {
		return fmt.Errorf("not connected")
	}
	if !utils.ValidatePath(path) {
		return utils.InvalidPathError(path)
	}
	resp, err := fs.fsclient.CreateDirectory(context.Background(), &file.Path{
		Path: path,
	})
	if err = combineErrors(resp, err); err != nil {
		return err
	}
	return nil
}

func (fs *DistributedFileSystem) Move(src, dest string) error {
	if fs.fsclient == nil {
		return fmt.Errorf("not connected")
	}
	if !utils.ValidatePath(src) {
		return utils.InvalidPathError(src)
	}
	if !utils.ValidatePath(dest) {
		return utils.InvalidPathError(dest)
	}
	resp, err := fs.fsclient.Move(context.Background, &file.MoveRequest{
		Src: &file.Path{
			Path: src,
		},
		Dest: &file.Path{
			Path: dest,
		},
	})
	if err = combineErrors(resp, err); err != nil {
		return err
	}
	return nil
}

func (fs *DistributedFileSystem) Remove(path string) error {
	if fs.fsclient == nil {
		return fmt.Errorf("not connected")
	}
	if !utils.ValidatePath(path) {
		return utils.InvalidPathError(path)
	}
	resp, err := fs.fsclient.Delete(context.Background(), &file.Path{
		Path: path,
	})
	if err = combineErrors(resp, err); err != nil {
		return err
	}
	return nil
}

func (fs *DistributedFileSystem) getFileMeta(path string) (*file.File, error) {
	resp, err := fs.fsclient.GetFileMeta(context.Background(), &file.Path{
		Path: path,
	})
	if err = combineErrors(resp, err); err != nil {
		return nil, err
	}
	return resp.GetFile(), nil
}

func (fs *DistributedFileSystem) createFileMeta(local string, remote string) (*file.File, error) {
	localFileInfo, err := os.Stat(local)
	if err != nil {
		return nil, err
	}
	fileMD5Hash, err := utils.HashFileMD5(local)
	resp, err := fs.fsclient.CreateFileMeta(context.Background(), &file.File{
		Name:     localFileInfo.Name(),
		Path:     remote,
		Size:     localFileInfo.Size(),
		Checksum: []byte(fileMD5Hash),
	})
	if err = combineErrors(resp, err); err != nil {
		return nil, err
	}
	return resp.GetFile(), nil
}

func (fs *DistributedFileSystem) Get(remote, local string) error {
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
	dfsconn, err := grpc.Dial(net.JoinHostPort(fileMeta.GetFileStoreUrl().Host, fileMeta.GetFileStoreUrl().Port), grpc.WithInsecure())
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
		Uuid:    fileMeta.Uuid,
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
		case *file.ControlledPacket_FileToChap:
			fallthrough
		case *file.ControlledPacket_Checksum:
			fallthrough
		default:
			panic("You should never receive this type of packet here.")
		}
	}
	return nil
}

func (fs *DistributedFileSystem) Put(local, remote string) error {
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
	dfsconn, err := grpc.Dial(net.JoinHostPort(remoteFile.GetFileStoreUrl().Host, remoteFile.GetFileStoreUrl().Port), grpc.WithInsecure())
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
	defer putFileClient.CloseSend()
	if err != nil {
		return err
	}
	err = putFileClient.Send(&file.ControlledPacket{
		Data: &file.ControlledPacket_FileToChap{
			FileToChap: remoteFile,
		},
	})
	if err != nil {
		return err
	}
	buffer := make([]byte, 1024)
	for {
		readSize, err := localFile.Read(buffer)
		if err != nil {
			if err == io.EOF {
				break
			}
			return err
		}
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
	return nil
}
