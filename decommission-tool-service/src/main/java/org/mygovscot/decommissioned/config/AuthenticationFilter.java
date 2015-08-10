package org.mygovscot.decommissioned.config;

/**
 * Project : OSS, mygov.scot
 * Copyright (C) 2014 Scottish Government Online Services & Strategy
 * OSS PROPRIETARY/CONFIDENTIAL
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Value("${authentication.endpoint}")
    private String authenticationEndpoint;

    @Value("${authentication.enabled}")
    private boolean authenticationEnabled;

    @Autowired
    private RestOperations rest;

    private static final String TOKEN_HEADER = "Authorization";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!authenticationEnabled) {
            filterChain.doFilter(request, response);
        } else if (!"OPTIONS".equals(request.getMethod())) {
            String sessionId = getSessionId(request);
            try {
                Session session = getSession(sessionId);
                if (!isAdmin(session)) {
                    response.sendError(HttpStatus.UNAUTHORIZED.value());
                    return;
                }

                SecurityContextHolder.setUser(session.getUser().getName());
                keepAlive(sessionId);
                filterChain.doFilter(request, response);
            } catch (HttpClientErrorException e) {
                LOG.warn("Received http error.", e);
                response.sendError(HttpStatus.UNAUTHORIZED.value(), e.getStatusText());
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isAdmin(Session session) {

        if (session == null) {
            return false;
        }
        for (Role role : session.getUser().getRoles()) {
            if ("admin".equals(role.getName())) {
                return true;
            }
        }
        return false;
    }

    private String getSessionId(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);
        if (StringUtils.isNotBlank(token)) {
            String[] authToken = StringUtils.split(token);
            if (authToken.length == 2) {
                token = authToken[1];
            }
        }
        return token;
    }

    private Session getSession(String sessionId) {
        if (StringUtils.isBlank(sessionId)) {
            return null;
        }

        ResponseEntity<Session> result = rest.getForEntity(getSessionEndpoint(), Session.class, sessionId);
        if (result.getStatusCode() == HttpStatus.OK && result.getBody().isAlive()
                && result.getBody().getUser().isActive()) {
            return result.getBody();
        } else {
            return null;
        }
    }

    private void keepAlive(String sessionId) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        rest.exchange(getSessionEndpoint(), HttpMethod.PUT, entity, byte[].class, sessionId);
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.rest = restTemplate;
    }

    public void setAuthenticationEnabled(boolean authenticationEnabled) {
        this.authenticationEnabled = authenticationEnabled;
    }

    public String getSessionEndpoint() {
        return String.format("%s%s", authenticationEndpoint, "sessions/{sessionId}");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Session {
        private String sessionId;

        @JsonProperty("userResource")
        private User user;

        @JsonProperty("sessionAlive")
        private boolean alive;

        public Session() {

        }

        public String getSessionId() {
            return sessionId;
        }

        public boolean isAlive() {
            return alive;
        }

        public void setAlive(boolean alive) {
            this.alive = alive;
        }

        public void setSessionId(String sessionId) {

            this.sessionId = sessionId;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {

        private String userId;
        private String name;
        private List<Role> roles;
        private boolean active;

        public User() {

        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getName() {
            return name;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Role> getRoles() {
            return roles;
        }

        public void setRoles(List<Role> roles) {
            this.roles = roles;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Role {
        private String name;

        private String description;

        private boolean active;

        public Role() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}