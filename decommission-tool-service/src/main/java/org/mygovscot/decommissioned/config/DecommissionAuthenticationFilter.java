package org.mygovscot.decommissioned.config;

import scot.mygov.authentication.client.AuthenticationContext;
import scot.mygov.authentication.api.UserResource;
import org.springframework.stereotype.Component;
import scot.mygov.authentication.spring.SpringAuthenticationFilter;

@Component
public class DecommissionAuthenticationFilter extends SpringAuthenticationFilter {

    @Override
    public boolean isAuthorized(AuthenticationContext context) {
        UserResource user = context.getSession().getUserResource();
        return user.getRoles().stream()
                .filter(r -> "admin".equals(r.getName()))
                .findFirst()
                .isPresent();
    }

    @Override
    public void onAuthenticatedRequest(AuthenticationContext context) {
        SecurityContextHolder.setUser(context.getSession().getUserResource().getName());
    }

}
