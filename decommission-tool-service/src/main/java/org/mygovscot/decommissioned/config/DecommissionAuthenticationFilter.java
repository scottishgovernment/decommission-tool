package org.mygovscot.decommissioned.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import scot.mygov.authentication.client.AuthenticationContext;
import scot.mygov.authentication.spring.SpringAuthenticationFilter;

@Component
public class DecommissionAuthenticationFilter extends SpringAuthenticationFilter {

    @Override
    public boolean isAuthorized(AuthenticationContext context) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter("ROLE_ADMIN"::equals)
                .findFirst()
                .isPresent();
    }

}
