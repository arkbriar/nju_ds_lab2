PROTO_INCLUDES=../proto
PROTO_FILES=../proto/*.proto
TARGET_BIN=dfscli
GOFILES=*.go cmd/*.go dfs/*.go

all: $(TARGET_BIN)

$(TARGET_BIN): generate_proto_gofiles $(GOFILES)
	go build -o $(TARGET_BIN) .

generate_proto_gofiles:
	@echo "Generating from proto files"
	@mkdir -p file
	@protoc -I $(PROTO_INCLUDES) $(PROTO_FILES) --go_out=plugins=grpc:file

clean_generated:
	@echo "Cleaning generated files"
	@rm -rf file

clean: clean_generated
	@echo "Cleaning binaries"
	@rm -f $(TARGET_BIN)
