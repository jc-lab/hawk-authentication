package sampleweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SampleWebApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(SampleWebApplication.class);
        springApplication.run(args);
    }
}
