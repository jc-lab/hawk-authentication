package sampleweb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class RequestTest {
    @LocalServerPort
    public int randomPort;

    @Test
    public void test() {
        System.out.println("ASDASd");
    }
}
