package dfs

type DistributedFileSystem interface {
	Open() error
	Close() error
	List(path string) ([]string, error)
	Mkdir(path string) error
	Move(src, dest string) error
	Remove(path string) error
	Get(remote, local string) error
	Put(local, remote string) error
}
