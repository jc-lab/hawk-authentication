package hawk

import (
	"io"
	"strconv"
)

func NormalizeHeader(hash io.Writer, ts int64, nonce string, method string, url string, host string, port int, payloadHash string, ext string) {
	hash.Write([]byte("hawk.1.header\n"))

	hash.Write(strconv.AppendInt(nil, ts, 10))
	hash.Write([]byte("\n"))

	hash.Write([]byte(nonce + "\n"))
	hash.Write([]byte(method + "\n"))
	hash.Write([]byte(url + "\n"))
	hash.Write([]byte(host + "\n"))
	hash.Write([]byte(strconv.Itoa(port) + "\n"))
	hash.Write([]byte(payloadHash + "\n"))
	hash.Write([]byte(ext + "\n"))
}
