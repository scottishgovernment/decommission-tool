package org.mygovscot.decommissioned.config;

import org.junit.Before;
import org.junit.Test;
import scot.mygov.authentication.client.AuthenticationClient;
import scot.mygov.authentication.api.RoleResource;
import scot.mygov.authentication.api.SessionResource;
import scot.mygov.authentication.api.UserResource;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by z419300 on 30/03/2015.
 */
public class DecommissionAuthenticationFilterTest {

    private DecommissionAuthenticationFilter filter;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private AuthenticationClient client;

    private FilterChain chain;

    @Before
    public void setUp() throws Exception {
        filter = new DecommissionAuthenticationFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        client = mock(AuthenticationClient.class);
        filter.setAuthenticationClient(client);
    }

    @Test
    public void passThroughIfAuthenticationDisabled() throws Exception {
        filter.setAuthenticationEnabled(false);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    public void unauthenticatedGetReturns401() throws Exception {
        filter.setAuthenticationEnabled(true);
        when(request.getMethod()).thenReturn("GET");
        filter.doFilter(request, response, chain);
        verify(response).sendError(401);
        verifyNoMoreInteractions(chain);
    }

    @Test
    public void authenticatedGet() throws Exception {
        filter.setAuthenticationEnabled(true);
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer 0");
        List<RoleResource> roles = new ArrayList<>();
        RoleResource role = new RoleResource();
        role.setName("admin");
        roles.add(role);
        UserResource user = new UserResource();
        user.setActive(true);
        user.setRoles(roles);
        SessionResource session = new SessionResource();
        session.setSessionAlive(true);
        session.setUserResource(user);
        when(client.getSession("0")).thenReturn(session);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

}
