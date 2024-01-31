package kr.jclab.hawk.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

public class HawkVerifier {
    private final HawkSecretProvider secretProvider;

    public HawkVerifier(HawkSecretProvider secretProvider) {
        this.secretProvider = secretProvider;
    }

    public Result verify(HawkRequestParams request, HawkHeader header) throws Exception {
        VerifyContext verifyContext = secretProvider.findAccount(header.getId());
        if (verifyContext == null) {
            return new Result(false, null);
        }
        if (!checkTs(verifyContext, header)) {
            return new Result(false, verifyContext);
        }
        byte[] mac = HawkUtils.computeMac(verifyContext.getSecret(), header.getTs(), header.getNonce(), header.getExt(), request);
        if (!Arrays.equals(mac, header.getMac())) {
            return new Result(false, verifyContext);
        }
        return new Result(true, verifyContext);
    }

    public boolean checkTs(VerifyContext verifyContext, HawkHeader header) {
        long delta = (System.currentTimeMillis() / 1000) - header.getTs();
        return (delta < 3600);
    }

    @Getter
    @AllArgsConstructor
    public static class Result {
        private final boolean verified;
        private final VerifyContext verifyContext;
    }
}
