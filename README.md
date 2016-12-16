# Setup

Setup project using hardlink.
```bash
mkdir -p $GOPATH/nju.edu.cn/ds/lab2
if [ $(uname -s) == "Darwin" ];
    brew install hardlink-osx
    hln cli $GOPATH/nju.edu.cn/ds/lab2/cli
else
    ln -d cli $GOPATH/nju.edu.cn/ds/lab2/cli
fi
```

# Build

```bash
cd nodes && gradle jar && cd ..
cd cli && make && mv dfscli ../ && cd ..
mv nodes/build/libs/nodes-1.0-SNAPSHOT.jar nodes.jar
```

# Run

Prepare a redis server on localhost:6379
```bash
java -cp nodes.jar MasterNode -p port
java -cp nodes.jar DataNode -p port -n name -d store_dir
```
