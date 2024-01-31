package kr.jclab.hawk.spring2;

import kr.jclab.hawk.core.VerifyContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class HawkAuthentication extends UsernamePasswordAuthenticationToken {
    public HawkAuthentication(Object principal, VerifyContext verifyContext) {
        super(principal, verifyContext);
    }

    public HawkAuthentication(Object principal, VerifyContext verifyContext, Collection<? extends GrantedAuthority> authorities) {
        super(principal, verifyContext, authorities);
    }

    public VerifyContext getVerifyContext() {
        return (VerifyContext) this.getCredentials();
    }
}
