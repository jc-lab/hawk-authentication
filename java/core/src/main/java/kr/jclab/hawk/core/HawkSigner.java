package kr.jclab.hawk.core;

import java.security.SecureRandom;
import java.util.Map;

public class HawkSigner {
    private final SecureRandom random = new SecureRandom();

    private final String id;
    private final String secret;
    public HawkSigner(String id, String secret) {
        this.id = id;
        this.secret = secret;
    }

    public HawkHeader sign(
            Long ts,
            String nonce,
            HawkRequestParams requestParams,
            String ext,
            Map<String, String> others
    ) {
        byte[] mac = HawkUtils.computeMac(secret, ts, nonce, ext, requestParams);
        return new HawkHeader(id, ts, nonce, requestParams.getPayloadHash(), ext, mac, others);
    }

    public HawkHeader sign(
            HawkRequestParams requestParams,
            String ext,
            Map<String, String> others
    ) {
        long ts = System.currentTimeMillis() / 1000;
        String nonce = HawkUtils.generateNonce(random, 16);
        byte[] mac = HawkUtils.computeMac(secret, ts, nonce, ext, requestParams);
        return new HawkHeader(id, ts, nonce, requestParams.getPayloadHash(), ext, mac, others);
    }
}
