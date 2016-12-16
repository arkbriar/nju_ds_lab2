package dfs

import (
	"context"

	"github.com/Sirupsen/logrus"
	"google.golang.org/grpc"
	"nju.edu.cn/ds/lab2/cli/auth"
)

func Login(username, password, url string) (token string, err error) {
	conn, err := grpc.Dial(url, grpc.WithInsecure())
	if err != nil {
		return "", err
	}
	client := auth.NewAuthClient(conn)
	resp, err := client.Login(context.Background(), &auth.LoginRequest{
		Username: username,
		Password: password,
	})
	if err != nil {
		return "", err
	}
	logrus.Infof("Time on remote server is %s", resp.Time)
	logrus.Infof("It's the %d-th time login.", resp.LoginTimes)
	return resp.Token, nil
}
