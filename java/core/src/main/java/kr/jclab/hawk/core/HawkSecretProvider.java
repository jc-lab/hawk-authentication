package kr.jclab.hawk.core;

@FunctionalInterface
public interface HawkSecretProvider {
    VerifyContext findAccount(String id) throws Exception;
}
