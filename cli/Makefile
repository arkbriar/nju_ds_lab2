PROTO_INCLUDES=../proto
PROTO_FILES_FOR_FILE=../proto/*.proto
PROTO_FILES_FOR_AUTH=../proto/auth/*.proto
TARGET_BIN=dfscli
GOFILES=*.go cmd/*.go dfs/*.go

all: $(TARGET_BIN)

$(TARGET_BIN): generate_proto_gofiles $(GOFILES)
	go build -o $(TARGET_BIN) .

generate_proto_gofiles: $(PROTO_FILES_FOR_AUTH) $(PROTO_FILES_FOR_FILE)
	@echo "Generating from proto files"
	@mkdir -p file
	@protoc -I $(PROTO_INCLUDES) $(PROTO_FILES_FOR_FILE) --go_out=plugins=grpc:file
	@protoc -I $(PROTO_INCLUDES) $(PROTO_FILES_FOR_AUTH) --go_out=plugins=grpc:.

clean_generated:
	@echo "Cleaning generated files"
	@rm -rf auth
	@rm -rf file

clean: clean_generated
	@echo "Cleaning binaries"
	@rm -f $(TARGET_BIN)
