package kr.jclab.hawk.spring2;

import kr.jclab.hawk.core.*;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class HawkAuthenticationFilter extends OncePerRequestFilter {
    private AuthenticationEntryPoint authenticationEntryPoint;
    private AuthenticationManager authenticationManager;
    private SecurityContextRepository securityContextRepository = new NullSecurityContextRepository();

    private final HawkVerifier verifier;

    public HawkAuthenticationFilter(
            HawkVerifier verifier
    ) {
        Assert.notNull(verifier, "verifier cannot be null");
        this.verifier = verifier;
    }

    public HawkAuthenticationFilter(
            HawkVerifier verifier,
            AuthenticationManager authenticationManager
    ) {
        Assert.notNull(verifier, "verifier cannot be null");
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        this.verifier = verifier;
        this.authenticationManager = authenticationManager;
    }

    public HawkAuthenticationFilter(
            HawkVerifier verifier,
            AuthenticationManager authenticationManager,
            AuthenticationEntryPoint authenticationEntryPoint
    ) {
        Assert.notNull(verifier, "verifier cannot be null");
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        Assert.notNull(authenticationEntryPoint, "authenticationEntryPoint cannot be null");
        this.verifier = verifier;
        this.authenticationManager = authenticationManager;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    /**
     * Sets the {@link SecurityContextRepository} to save the {@link SecurityContext} on
     * authentication success. The default action is not to save the
     * {@link SecurityContext}.
     * @param securityContextRepository the {@link SecurityContextRepository} to use.
     * Cannot be null.
     */
    public void setSecurityContextRepository(SecurityContextRepository securityContextRepository) {
        Assert.notNull(securityContextRepository, "securityContextRepository cannot be null");
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        RequestHolder requestHolder = new RequestHolder(request);

        try {
            HawkAuthentication authRequest = verification(requestHolder);
            if (authRequest == null) {
                this.logger.trace("Did not process authentication request since failed to find "
                        + "username and signature in Hawk Authorization header");
                chain.doFilter(requestHolder.request, response);
                return;
            }

            this.logger.trace(LogMessage.format("Found username '%s' in Hawk Authorization header", authRequest.getName()));

            if (true) {
                Authentication authResult = authentication(authRequest);
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authResult);
                SecurityContextHolder.setContext(context);
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(LogMessage.format("Set SecurityContextHolder to %s", authResult));
                }
                this.securityContextRepository.saveContext(context, request, response);
                onSuccessfulAuthentication(request, response, authResult);
            }
        }
        catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            this.logger.debug("Failed to process authentication request", ex);
            onUnsuccessfulAuthentication(requestHolder.request, response, ex);
            if (this.authenticationEntryPoint != null) {
                this.authenticationEntryPoint.commence(request, response, ex);
            } else {
                throw ex;
            }
        }

        chain.doFilter(requestHolder.request, response);
    }

    protected void onSuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authResult
    ) throws IOException {
    }

    protected void onUnsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed
    ) throws IOException {
    }

    protected AuthenticationEntryPoint getAuthenticationEntryPoint() {
        return this.authenticationEntryPoint;
    }

    protected AuthenticationManager getAuthenticationManager() {
        return this.authenticationManager;
    }

    protected Authentication authentication(HawkAuthentication authentication) {
        return new HawkAuthentication(authentication.getPrincipal(), authentication.getVerifyContext(), Collections.emptyList());
    }

    protected HawkAuthentication verification(RequestHolder requestHolder) throws AuthenticationException, IOException {
        HttpServletRequest request = requestHolder.request;
        String header = request.getHeader("Authorization");
        if (header == null || !header.toLowerCase().startsWith("hawk ")) {
            return null;
        }
        HawkHeader hawkHeader = HawkHeader.parse(header, false);

        ContentPreCachingRequestWrapper requestWrapper = new ContentPreCachingRequestWrapper(request);
        requestHolder.request = requestWrapper;

        String method = request.getMethod().toUpperCase();

        byte[] payloadHash = null;
        if ("POST".equals(method) || "PUT".equals(method)) {
            payloadHash = HawkUtils.hashPayload(request.getContentType(), requestWrapper.getBody());
        }

        HawkRequestParams requestParams = HawkRequestParams.builder()
                .url(getFullURL(request))
                .method(method)
                .host(getHost(request))
                .port(getPort(request))
                .payloadHash(payloadHash)
                .build();

        HawkVerifier.Result result;
        try {
            result = this.verifier.verify(requestParams, hawkHeader);
        } catch (Exception e) {
            throw new BadCredentialsException("hawk decode failed", e);
        }
        if (!result.isVerified()) {
            throw new BadCredentialsException("hawk verification failed");
        }

        return new HawkAuthentication(hawkHeader, result.getVerifyContext());
    }

    public String getFullURL(HttpServletRequest request) {
        StringBuilder requestURL = new StringBuilder(request.getRequestURI().toString());
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

    public String getHost(HttpServletRequest request) {
        return request.getHeader("host").split(":")[0];
    }

    public int getPort(HttpServletRequest request) {
        String portHeader = request.getHeader("port");
        if (portHeader == null || portHeader.isEmpty()) {
            return request.getServerPort();
        }
        String[] hostHeader = request.getHeader("host").split(":");
        if (hostHeader.length == 2) {
            return Integer.parseInt(hostHeader[1]);
        }
        return Integer.parseInt(portHeader);
    }

    static class RequestHolder {
        HttpServletRequest request;

        RequestHolder(HttpServletRequest request) {
            this.request = request;
        }
    }
}
