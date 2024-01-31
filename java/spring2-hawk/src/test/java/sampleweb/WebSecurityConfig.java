package sampleweb;

import kr.jclab.hawk.core.BasicVerifyContext;
import kr.jclab.hawk.core.HawkVerifier;
import kr.jclab.hawk.spring2.HawkAuthenticationFilter;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configurable
@EnableWebSecurity
public class WebSecurityConfig {
    private final HawkVerifier hawkVerifier = new HawkVerifier((id) -> {
        if ("helloworld".equals(id)) {
            return new BasicVerifyContext(id, "secret");
        }
        return null;
    });

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        http.csrf().disable();
        http.httpBasic().disable();
        http.formLogin().disable();
        http.sessionManagement().disable();
        http.addFilterAfter(
                new HawkAuthenticationFilter(hawkVerifier),
                BasicAuthenticationFilter.class
        );
        http.authorizeRequests().anyRequest().permitAll();
        return http.build();
    }
}
