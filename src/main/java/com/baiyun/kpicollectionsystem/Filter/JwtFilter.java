package com.baiyun.kpicollectionsystem.Filter;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baiyun.kpicollectionsystem.controller.UserController;
import com.baiyun.kpicollectionsystem.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //获取token
        String token = request.getHeader("token");

        //没有token，可能是登录请求，放行
        if (StrUtil.isBlank(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        //有token，校验token
        try {
            Claims claims = jwtUtil.parseToken(token);
            int uid = claims.get("id") instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(claims.get("id")));
            String name = (String) claims.get("name");
            String role = (String) claims.get("role");
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(name + "#" + uid, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        //放行
        filterChain.doFilter(request, response);
    }
}
