
package com.notsoold.maisec.bank.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
	http.authorizeRequests().anyRequest().authenticated()
			.and().formLogin()
			.loginProcessingUrl("/login")
			.defaultSuccessUrl("/homepage", true)
			.and().csrf().disable();
    }

}
