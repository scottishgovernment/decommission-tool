package org.mygovscot.decommissioned.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import scot.mygov.authentication.spring.SpringAuthenticationClient;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private Configurer configurer;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.apply(configurer);
        http.csrf().disable();
    }

    @Component
    @Import({SpringAuthenticationClient.class})
    static class Configurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

        @Autowired
        DecommissionAuthenticationFilter filter;

        @Override
        public void init(HttpSecurity builder) throws Exception {
            builder.addFilterAfter(filter, UsernamePasswordAuthenticationFilter.class);
        }
    }

}
