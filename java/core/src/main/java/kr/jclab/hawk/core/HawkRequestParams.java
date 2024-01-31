package kr.jclab.hawk.core;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Getter
@ToString
@SuperBuilder
public class HawkRequestParams {
    private final String method;
    private final String url;
    private final String host;
    private final int port;
    private final byte[] payloadHash;
}
