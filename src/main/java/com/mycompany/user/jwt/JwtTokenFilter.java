package com.mycompany.user.jwt;

import com.mycompany.user.entity.Role;
import com.mycompany.user.entity.User;
import com.mycompany.user.repository.RoleRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(value = 1)

public class JwtTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RoleRepository roleRepository; // Use repository to load roles

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        System.out.println(header);

        if (!hasAuthorizationHeader(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = getAccessToken(request);

        if (!jwtTokenUtil.validateAccessToken(accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        setAuthenticationContext(accessToken, request);
        filterChain.doFilter(request, response);
    }

    private boolean hasAuthorizationHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return header != null && header.startsWith("Bearer ");
    }

    private String getAccessToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return header.split(" ")[1].trim();
    }

    private void setAuthenticationContext(String token, HttpServletRequest request) {
        UserDetails userDetails = getUserDetails(token);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public UserDetails getUserDetails(String token) {
        Claims claims = jwtTokenUtil.parseClaims(token);
        String subject = (String) claims.get(Claims.SUBJECT);

        // Extract roles from JWT claims
        List<Map<String, Object>> roles = (List<Map<String, Object>>) claims.get("roles");
        Set<Role> rolesSet = roles.stream()
                .map(roleMap -> new Role(
                        Long.parseLong((String) roleMap.get("id")), // Convert String to Long
                        (String) roleMap.get("name")))
                .collect(Collectors.toSet());

        String[] jwtSubject = subject.split(",");
        User userDetails = new User();
        userDetails.setId(Long.parseLong(jwtSubject[0])); // Convert String to Long
        userDetails.setEmail(jwtSubject[1]);
        userDetails.setFirstName(jwtSubject[2]);
        userDetails.setLastName(jwtSubject[3]);
        userDetails.setRoles(rolesSet);

        return userDetails;
    }
}

