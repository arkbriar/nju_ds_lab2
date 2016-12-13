#!/bin/bash

mkdir -p file
protoc -I ../proto ../proto/*.proto --go_out=plugins=grpc:file
