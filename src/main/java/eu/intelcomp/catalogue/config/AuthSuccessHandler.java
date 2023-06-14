package eu.intelcomp.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;

@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthSuccessHandler.class);

    private final IntelcompProperties intelcompProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public AuthSuccessHandler(IntelcompProperties intelcompProperties) {
        this.intelcompProperties = intelcompProperties;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        AuthenticationSuccessHandler.super.onAuthenticationSuccess(request, response, chain, authentication);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Cookie cookie = new Cookie("AccessToken", ((OidcUser) authentication.getPrincipal()).getIdToken().getTokenValue());
        cookie.setMaxAge(createCookieMaxAge(authentication));
        cookie.setPath("/");
//        cookie.setSecure(true);

        logger.debug("Assigning Cookie: {}", objectMapper.writeValueAsString(cookie));
        response.addCookie(cookie);
        logger.debug("Authentication Successful - Redirecting to: {}", intelcompProperties.getLoginRedirect());
        response.sendRedirect(intelcompProperties.getLoginRedirect());
    }

    private int createCookieMaxAge(Authentication authentication) {
        Integer age = getExp(authentication);
        return age != null ? age : 3600;
    }

    private Integer getExp(Authentication authentication) {
        OidcUser user = ((OidcUser) authentication.getPrincipal());
        if (user.getAttribute("exp") instanceof Instant) {
            Instant exp = user.getAttribute("exp");
            int age = (int) (exp.getEpochSecond() - (new Date().getTime() / 1000));
            return age;
        }
        return null;
    }
}

