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
