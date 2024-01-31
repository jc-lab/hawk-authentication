package hawk_authentication

import (
	"bytes"
	"crypto"
	"crypto/hmac"
	"encoding/base64"
	"fmt"
	"github.com/jc-lab/hawk-authentication/pkg/hawk"
	"io"
	"net/http"
	"strconv"
	"strings"
	"time"
)

type Session struct {
	httpClient *http.Client
	HawkExt    string
	id         string
	secret     string
}

func New(httpClient *http.Client) *Session {
	return &Session{
		httpClient: httpClient,
	}
}

func (c *Session) SetAuth(id string, secret string) {
	c.id = id
	c.secret = secret
}

func (c *Session) Do(request *http.Request) (*http.Response, error) {
	var body []byte
	var err error
	var payloadHash string

	ts := time.Now().Unix()
	nonce := hawk.GenerateNonce(16)

	if request.Method != "GET" && request.Body != nil {
		body, err = io.ReadAll(request.Body)
		if err != nil {
			return nil, err
		}
		payloadHash = hawk.HashPayload(request.Header.Get("content-type"), body)
	}

	port := request.URL.Port()
	if port == "" {
		scheme := strings.ToLower(request.URL.Scheme)
		if strings.HasPrefix(scheme, "https") {
			port = "443"
		} else {
			port = "80"
		}
	}
	portNum, err := strconv.Atoi(port)
	if err != nil {
		return nil, err
	}

	path := request.URL.Path
	if request.URL.RawQuery != "" {
		path += "?" + request.URL.RawQuery
	}

	signer := hmac.New(crypto.SHA256.New, []byte(c.secret))
	hawk.NormalizeHeader(signer, ts, nonce, request.Method, path, request.URL.Hostname(), portNum, payloadHash, c.HawkExt)
	mac := base64.StdEncoding.EncodeToString(signer.Sum(nil))

	hawkHeader := fmt.Sprintf("Hawk id=\"%s\",ts=\"%d\",nonce=\"%s\"", c.id, ts, nonce)
	if payloadHash != "" {
		hawkHeader += fmt.Sprintf(",hash=\"%s\"", payloadHash)
	}
	hawkHeader += fmt.Sprintf(",mac=\"%s\"", mac)
	if c.HawkExt != "" {
		hawkHeader += fmt.Sprintf(",ext=\"%s\"", c.HawkExt)
	}

	request.Header.Set("Authorization", hawkHeader)

	newRequest := c.copyRequest(request, body)

	return c.httpClient.Do(newRequest)
}

func (c *Session) copyRequest(request *http.Request, body []byte) *http.Request {
	newRequest := &http.Request{}
	*newRequest = *request
	newRequest.RequestURI = ""

	if body != nil {
		newRequest.ContentLength = int64(len(body))
		newRequest.Body = io.NopCloser(bytes.NewReader(body))
	}

	return newRequest
}
