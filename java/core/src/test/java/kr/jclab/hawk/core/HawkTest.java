package kr.jclab.hawk.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HawkTest {
    String id = "dh37fgj492je";
    String secret = "werxhqb98rpaxn39848xrunpaw3489ruxnpa98w4rxn";

    @Test
    public void signAndVerify_success() {
        Long ts = 1353832234L;
        String nonce = "j4h3g2";
        HawkSigner signer = new HawkSigner(id, secret);

        HawkRequestParams requestParams = HawkRequestParams.builder()
                .url("/resource/1?b=1&a=2")
                .method("POST")
                .host("example.com")
                .port(8000)
                .payloadHash(HawkUtils.hashPayload("text/plain", "Thank you for flying Hawk".getBytes()))
                .build();
        HawkHeader header = signer.sign(ts, nonce, requestParams, "some-app-ext-data", null);
        assertThat(header.serialize()).isEqualTo("Hawk id=\"dh37fgj492je\", ts=\"1353832234\", nonce=\"j4h3g2\", hash=\"Yi9LfIIFRtBEPt74PVmbTF/xVAwPn7ub15ePICfgnuY=\", ext=\"some-app-ext-data\", mac=\"aSe1DERmZuRl3pI36/9BdZmnErTw3sNzOOAUlfeKjVw=\"");
    }
}
