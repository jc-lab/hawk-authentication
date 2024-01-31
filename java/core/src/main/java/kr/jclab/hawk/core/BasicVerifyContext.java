package kr.jclab.hawk.core;

import lombok.Getter;

@Getter
public class BasicVerifyContext implements VerifyContext {
    private final String id;
    private final String secret;

    public BasicVerifyContext(String id, String secret) {
        this.id = id;
        this.secret = secret;
    }
}
