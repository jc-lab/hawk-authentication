package kr.jclab.hawk.core;

import com.google.common.hash.Hashing;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

public class HawkUtils {
    public static byte[] normalizeHeader(
            Long ts,
            String nonce,
            String ext,
            HawkRequestParams params
    ) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(String.format(
                    "hawk.1.header\n" +
                            "%d\n" +
                            "%s\n" +
                            "%s\n" +
                            "%s\n" +
                            "%s\n" +
                            "%d\n"
                    ,
                    ts,
                    nonce,
                    params.getMethod(),
                    params.getUrl(),
                    params.getHost(),
                    params.getPort()
            ).getBytes(StandardCharsets.UTF_8));
            if (params.getPayloadHash() != null) {
                bos.write((Base64.getEncoder().encodeToString(params.getPayloadHash()) + "\n").getBytes(StandardCharsets.US_ASCII));
            } else {
                bos.write("\n".getBytes(StandardCharsets.US_ASCII));
            }
            if (ext != null) {
                bos.write((ext + "\n").getBytes(StandardCharsets.US_ASCII));
            } else {
                bos.write("\n".getBytes(StandardCharsets.US_ASCII));
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] hashPayload(
            String contentType,
            byte[] payload
    ) {
        try {
            if (contentType == null) {
                contentType = "";
            }
            contentType = contentType.split(";")[0].trim();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(String.format("hawk.1.payload\n%s\n", contentType).getBytes(StandardCharsets.US_ASCII));
            if (payload != null) {
                bos.write(payload);
            }
            bos.write("\n".getBytes(StandardCharsets.US_ASCII));
            return Hashing.sha256().hashBytes(bos.toByteArray()).asBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] computeMac(
            String secret,
            Long ts,
            String nonce,
            String ext,
            HawkRequestParams requestParams
    ) {
        byte[] normalized = HawkUtils.normalizeHeader(ts, nonce, ext, requestParams);
        return Hashing.hmacSha256(secret.getBytes(StandardCharsets.UTF_8))
                .hashBytes(normalized)
                .asBytes();
    }

    private static final String NONCE_CHAR_POOL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static String generateNonce(Random random, int length) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i=0; i<length; i++) {
            stringBuffer.append(NONCE_CHAR_POOL.charAt(random.nextInt(NONCE_CHAR_POOL.length())));
        }
        return stringBuffer.toString();
    }
}
