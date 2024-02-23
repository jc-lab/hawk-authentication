package main

import (
	"bytes"
	"flag"
	hawk_authentication "github.com/jc-lab/hawk-authentication"
	"io"
	"log"
	"net/http"
)

func main() {
	var id string
	var secret string
	var method string
	var data string
	var contentType string

	flag.StringVar(&id, "id", "", "id")
	flag.StringVar(&secret, "secret", "", "secret")
	flag.StringVar(&method, "method", "GET", "method")
	flag.StringVar(&data, "data", "", "request body")
	flag.StringVar(&contentType, "content-type", "", "content type")
	flag.Parse()

	s := hawk_authentication.New(http.DefaultClient)
	s.SetAuth(id, secret)

	req, err := http.NewRequest(method, flag.Arg(0), bytes.NewReader([]byte(data)))
	if err != nil {
		log.Fatalln(err)
	}

	if contentType != "" {
		req.Header.Set("content-type", contentType)
	}

	res, err := s.Do(req)
	if err != nil {
		log.Fatalln(err)
	}
	responseBody, err := io.ReadAll(res.Body)
	if err != nil {
		log.Fatalln(err)
	}
	println(string(responseBody))
}
