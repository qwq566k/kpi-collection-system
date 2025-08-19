package com.baiyun.kpicollectionsystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
		http.csrf(csrf -> csrf.disable());
		http.sessionManagement(mgr -> mgr.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.authorizeHttpRequests(registry -> registry
				.requestMatchers("/health", "/api/user/login", "/api/uploadFile").permitAll()
				.requestMatchers("/upload/**", "/export/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/getAllField", "/api/getKeyIndicators", "/api/getStandards").permitAll()
				.anyRequest().authenticated());
		http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Component
	public static class JwtAuthFilter extends OncePerRequestFilter {
		private final JwtUtil jwtUtil;

		public JwtAuthFilter(JwtUtil jwtUtil) {
			this.jwtUtil = jwtUtil;
		}

		@Override
		protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
			String auth = request.getHeader("Authorization");
			if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
				String token = auth.substring(7);
				try {
					var claims = jwtUtil.parseToken(token);
					Object idObj = claims.get("id");
					String name = (String) claims.get("name");
					String role = (String) claims.get("role");
					int uid = idObj instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(idObj));
					var authToken = new UsernamePasswordAuthenticationToken(name + "#" + uid, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				} catch (Exception ignored) { }
			}
			filterChain.doFilter(request, response);
		}
	}
}


