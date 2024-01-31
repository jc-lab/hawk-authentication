package hawk

import (
	"crypto"
	"encoding/base64"
	"strings"
)

func HashPayload(contentType string, body []byte) string {
	hash := crypto.SHA256.New()
	hash.Write([]byte("hawk.1.payload\n"))
	hash.Write([]byte(strings.TrimSpace(strings.Split(contentType, ";")[0]) + "\n"))
	hash.Write(body)
	hash.Write([]byte("\n"))
	return base64.StdEncoding.EncodeToString(hash.Sum(nil))
}
