package sampleweb;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class TestController {
    @RequestMapping(
            path = "/api/test"
    )
    public String test(
            HttpServletRequest request,
            @RequestBody(required = false) String body
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("TEST API");
        System.out.println("\tAuthentication: " + authentication);
        System.out.println("\tMethod     : " + request.getMethod());
        System.out.println("\tURI        : " + request.getRequestURI());
        System.out.println("\tQueryString: " + request.getQueryString());
        System.out.println("\tBODY: " + body);
        return "GOOD";
    }
}
