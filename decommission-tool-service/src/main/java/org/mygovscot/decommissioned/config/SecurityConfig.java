package org.mygovscot.decommissioned.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import scot.mygov.authentication.spring.AuthenticationConfigurer;

@Configuration
@EnableWebSecurity
@Import(AuthenticationConfigurer.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationConfigurer configurer;

    @Autowired
    @Value("${authentication.enabled}")
    private boolean enabled;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(r -> !enabled).permitAll()
                .anyRequest().hasRole("ADMIN")
                .and()
                .apply(configurer);
    }

}
