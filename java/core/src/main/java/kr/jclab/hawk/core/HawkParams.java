package kr.jclab.hawk.core;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Map;


@Getter
@ToString
@SuperBuilder
public class HawkParams extends HawkRequestParams {
    private final Long ts;
    private final String nonce;
    private final String ext;
    private final Map<String, String> others;
}
