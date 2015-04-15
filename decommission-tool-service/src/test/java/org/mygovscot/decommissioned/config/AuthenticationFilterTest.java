package org.mygovscot.decommissioned.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mygovscot.decommissioned.config.AuthenticationFilter.Role;
import static org.mygovscot.decommissioned.config.AuthenticationFilter.Session;
import static org.mygovscot.decommissioned.config.AuthenticationFilter.User;

/**
 * Created by z419300 on 30/03/2015.
 */
public class AuthenticationFilterTest {

    private AuthenticationFilter filter;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private RestOperations rest;

    private FilterChain chain;

    @Before
    public void setUp() throws Exception {
        filter = new AuthenticationFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        rest = mock(RestOperations.class);
        Field field = AuthenticationFilter.class.getDeclaredField("rest");
        field.setAccessible(true);
        field.set(filter, rest);
    }

    @Test
    public void passThroughIfAuthenticationDisabled() throws Exception {
        setEnabled(false);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    public void unauthenticatedGetReturns401() throws Exception {
        setEnabled(true);
        when(request.getMethod()).thenReturn("GET");
        filter.doFilter(request, response, chain);
        verify(response).sendError(401);
        verifyNoMoreInteractions(chain);
    }

    @Test
    public void authenticatedGet() throws Exception {
        setEnabled(true);
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer 0");
        List<Role> roles = new ArrayList<>();
        Role role = new Role();
        role.setName("admin");
        roles.add(role);
        User user = new User();
        user.setActive(true);
        user.setRoles(roles);
        Session session = new Session();
        session.setAlive(true);
        session.setUser(user);
        ResponseEntity<Session> entity = new ResponseEntity<>(session, HttpStatus.OK);
        when(rest.getForEntity(anyString(), eq(Session.class), eq("0"))).thenReturn(entity);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    private void setEnabled(boolean enabled) throws NoSuchFieldException, IllegalAccessException {
        Field field = AuthenticationFilter.class.getDeclaredField("authenticationEnabled");
        field.setAccessible(true);
        field.set(filter, enabled);
    }


}
