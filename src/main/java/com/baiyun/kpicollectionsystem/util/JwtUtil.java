package com.baiyun.kpicollectionsystem.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

	@Value("${app.jwt.secret}")
	private String secret;

	@Value("${app.jwt.expire-seconds:2592000}")
	private long expireSeconds;

	private SecretKey getKey() {
		byte[] keyBytes = secret.length() > 64 ? Decoders.BASE64.decode(secret) : secret.getBytes();
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateToken(Integer userId, String name, String role) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("id", userId);
		claims.put("name", name);
		claims.put("role", role);
		Date now = new Date();
		Date exp = new Date(now.getTime() + expireSeconds * 1000);
		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now)
				.setExpiration(exp)
				.signWith(getKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	public Claims parseToken(String token) {
		return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
	}
}



