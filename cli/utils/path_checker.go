package utils

import (
	"fmt"
	"regexp"
)

var (
	pathPattern = regexp.MustCompile("^(/[^/]+)*/([^/]+)$")
)

func ValidatePath(path string) bool {
	return pathPattern.MatchString(path)
}

func InvalidPathError(path string) error {
	return fmt.Errorf(path + " isn't a valid path")
}
