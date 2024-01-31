package hawk

import "crypto/rand"

const nonceCharPool = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

func GenerateNonce(n int) string {
	var output string
	buf := make([]byte, n)
	rand.Read(buf)
	for _, b := range buf {
		p := int(b) % len(nonceCharPool)
		output += string(nonceCharPool[p])
	}
	return output
}
