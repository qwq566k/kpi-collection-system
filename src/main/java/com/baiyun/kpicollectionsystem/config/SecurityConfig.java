package com.baiyun.kpicollectionsystem.config;

import com.baiyun.kpicollectionsystem.Filter.JwtFilter;
import com.baiyun.kpicollectionsystem.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Autowired
	private JwtFilter jwtFilter;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new PasswordEncoder() {
			@Override
			public String encode(CharSequence rawPassword) {
				return rawPassword.toString();
			}

			@Override
			public boolean matches(CharSequence rawPassword, String encodedPassword) {
				return encode(rawPassword).equals(encodedPassword);
			}
		};
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.formLogin(form -> form.disable())
			.logout(logout -> logout.disable())
			.csrf(csrf -> csrf.disable())
			.sessionManagement(mgr -> mgr.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//		http.authorizeHttpRequests(registry -> registry
//				.requestMatchers("/health", "/api/user/login", "/api/uploadFile").permitAll()
//				.requestMatchers("/upload/**", "/export/**").permitAll()
//				.requestMatchers(HttpMethod.GET, "/api/getAllField", "/api/getKeyIndicators", "/api/getStandards").permitAll()
//				.anyRequest().authenticated());
		http.authorizeHttpRequests(auth ->auth.anyRequest().permitAll());
		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}


}


