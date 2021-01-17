/* 
 * Copyright (C) 2020 Dejan Stojanovic <dejanstojanovich@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ds.foldercompare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class which defines access limitations based on application.properties parameters
 * the application can allow access only from defined IP addresses or
 * use basic authentication to allow access to specific user
 * @author Dejan Stojanovic
 */
@Configuration
@EnableWebSecurity
//@EnableWebSecurity(debug = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    //collect the parameters defined in application.properties into the variables
    @Value("${diff.allowedClients:127.0.0.1}")
    private String[] allowedClients;
    @Value("${diff.authenticate:false}")
    private Boolean authenticate;
    @Value("${diff.pass:pass}")
    private String pass;
    @Value("${diff.user:user}")
    private String user;

    /**
     * Configure in-memory authentication for the user/pass specified in the application.properties
     * @param auth SecurityBuilder used to create an AuthenticationManager
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        auth.inMemoryAuthentication()
                .withUser(user)
                .password(encoder.encode(pass))
                .roles("USER");
    }

    /**
     * Configure authentication or IP address filter for http requests
     * @param http It allows configuring web based security for specific http requests
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        if (authenticate) {
            http
                    .csrf().disable()
                    .authorizeRequests()
                    .antMatchers("/**")
                    .hasRole("USER")
                    .anyRequest().authenticated()
                    .and().formLogin();
        } else {
            StringBuilder allowed = new StringBuilder();
            for (int i = 0; i < allowedClients.length; i++) {
                if (i > 0) {
                    allowed.append(" or ");
                }
                allowed.append("hasIpAddress('").append(allowedClients[i]).append("')");
                  if (allowedClients[i].equals("127.0.0.1")) {
                    allowed.append(" or hasIpAddress('::1')");
                }
            }
            http
                    .csrf().disable()
                    .httpBasic().disable()
                    .authorizeRequests()
                    .antMatchers("/**").access(allowed.toString());
        }
    }

}
