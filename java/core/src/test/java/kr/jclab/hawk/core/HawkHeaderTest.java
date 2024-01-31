package kr.jclab.hawk.core;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class HawkHeaderTest {
    @Test
    public void parse_clientHeader() {
        String input = "Hawk id=\"dh37fgj492je\", ts=\"1353832234\", nonce=\"j4h3g2\", hash=\"Yi9LfIIFRtBEPt74PVmbTF/xVAwPn7ub15ePICfgnuY=\", ext=\"some-app-ext-data\", mac=\"aSe1DERmZuRl3pI36/9BdZmnErTw3sNzOOAUlfeKjVw=\"";
        HawkHeader header = HawkHeader.parse(input, false);
        assertThat(header.getId()).isEqualTo("dh37fgj492je");
        assertThat(header.getTs()).isEqualTo(1353832234L);
        assertThat(header.getNonce()).isEqualTo("j4h3g2");
        assertThat(header.getHash()).isEqualTo(Base64.getDecoder().decode("Yi9LfIIFRtBEPt74PVmbTF/xVAwPn7ub15ePICfgnuY="));
        assertThat(header.getExt()).isEqualTo("some-app-ext-data");
        assertThat(header.getMac()).isEqualTo(Base64.getDecoder().decode("aSe1DERmZuRl3pI36/9BdZmnErTw3sNzOOAUlfeKjVw="));
    }

    @Test
    public void parse_serverHeader() {
        String input = "Hawk mac=\"XIJRsMl/4oL+nn+vKoeVZPdCHXB4yJkNnBbTbHFZUYE=\", hash=\"f9cDF/TDm7TkYRLnGwRMfeDzT6LixQVLvrIKhh0vgmM=\", ext=\"response-specific\"";
        HawkHeader header = HawkHeader.parse(input, true);
        assertThat(header.getHash()).isEqualTo(Base64.getDecoder().decode("f9cDF/TDm7TkYRLnGwRMfeDzT6LixQVLvrIKhh0vgmM="));
        assertThat(header.getExt()).isEqualTo("response-specific");
        assertThat(header.getMac()).isEqualTo(Base64.getDecoder().decode("XIJRsMl/4oL+nn+vKoeVZPdCHXB4yJkNnBbTbHFZUYE="));
    }
}