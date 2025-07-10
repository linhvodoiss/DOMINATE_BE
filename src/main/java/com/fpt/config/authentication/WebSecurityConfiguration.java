package com.fpt.config.authentication;

import com.fpt.authentication.CustomAuthenticationEntryPoint;
import com.fpt.config.CustomAuthenticationFailureHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.google.common.collect.ImmutableList;
import com.fpt.service.IUserService;

@Component
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private IUserService service;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

	@Autowired
	private CustomAuthenticationFailureHandler failureHandler;
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(service).passwordEncoder(passwordEncoder);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		.exceptionHandling()
		.authenticationEntryPoint(customAuthenticationEntryPoint) // 👈 Thêm dòng này
		.and()
		.authorizeRequests()
		.antMatchers("/api/v1/login").anonymous()
		.antMatchers("/api/v1/users/profile").authenticated()
		.antMatchers("/api/v1/users/**", "/api/v1/categories/**","/api/v1/products/**"
				,"/api/v1/licenses/**","/api/v1/subscriptions/**","/api/v1/orders/**"
				,"/api/v1/versions/**","/api/v1/docs/**").permitAll()
		.antMatchers( "/api/v1/admin/**").hasRole("ADMIN")
//		.antMatchers( "/api/v1/products/**").hasAnyRole("ADMIN", "CUSTOMER")
		.anyRequest().permitAll()
		.and()
		.httpBasic()
		.and()
		.cors()
		.and()
		.csrf().disable()
		.addFilterBefore(
				new JWTAuthenticationFilter("/api/v1/login", authenticationManager(), service,failureHandler),
				UsernamePasswordAuthenticationFilter.class) 
		.addFilterBefore(
				new JWTAuthorizationFilter(), 
				UsernamePasswordAuthenticationFilter.class);
	}
	
	@Bean
    CorsConfigurationSource corsConfigurationSource() {
		final CorsConfiguration configuration = new CorsConfiguration();
	    configuration.setAllowedMethods(ImmutableList.of("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
	    configuration.applyPermitDefaultValues();
	    
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}