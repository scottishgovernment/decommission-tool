package org.mygovscot.decommissioned.config;

import org.mygovscot.authentication.client.AuthenticationContext;
import org.mygovscot.authentication.spring.SpringAuthenticationFilter;
import org.mygovscot.basic.authentication.web.resource.representation.UserResource;
import org.springframework.stereotype.Component;

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
