/**
 * Project : OSS, mygov.scot
 * Copyright (C) 2014 Scottish Government Online Services & Strategy
 * OSS PROPRIETARY/CONFIDENTIAL
 */
package org.mygovscot.decommissioned.config;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * The Class CorsFilter.
 *
 */
@Component
@Order(10)
public class CorsFilter  extends OncePerRequestFilter {

    private static final String ACAO = "Access-Control-Allow-Origin";
    private static final String ACAM = "Access-Control-Allow-Methods";
    private static final String ACMA = "Access-Control-Max-Age";
    private static final String ACAH = "Access-Control-Allow-Headers";
    private static final String ACEH = "Access-Control-Expose-Headers";

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        response.setHeader(ACAO, "*");
        response.setHeader(ACAM, "PUT, POST, GET, OPTIONS, DELETE");
        response.setHeader(ACMA, "3600");
        response.addHeader(ACAH, "x-requested-with");
        response.addHeader(ACAH, "x-requested-with");
        response.addHeader(ACEH, "Location");
        response.addHeader(ACAH, "Content-Type");
        response.addHeader(ACAH, "Authorization");
        response.addHeader(ACAH, "Bearer");

        filterChain.doFilter(request, response);
    }
}