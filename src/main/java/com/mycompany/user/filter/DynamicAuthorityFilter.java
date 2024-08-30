package com.mycompany.user.filter;

import com.mycompany.user.entity.Role;
import com.mycompany.user.entity.User;
import com.mycompany.user.service.IRolePermissionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@Order(value = 2)
@RequiredArgsConstructor

public class DynamicAuthorityFilter extends OncePerRequestFilter {
    private final IRolePermissionService rolePermissionService;
    private static final Logger logger = LoggerFactory.getLogger(DynamicAuthorityFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ) {
            filterChain.doFilter(request, response);
            return;
        }

        User user = (User) authentication.getPrincipal();
        System.out.println(user);

        Set<Role> roles = user.getRoles();
        for (Role role : roles) {
            System.out.println("role.getName(): " + role.getName());
            System.out.println("role.getId(): " + role.getId());
        }

        // Check if user has an admin role
        boolean isAdmin = roles.stream()
                .anyMatch(role -> Role.ADMIN.equals(role.getName()));

        logger.info("Is Admin: {}", isAdmin);

        if (isAdmin) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestPath = request.getRequestURI();
        boolean hasPermission = roles.stream()
                .filter(role -> role.getId() != null)  // Ensure roleId is not null
                .flatMap(role -> rolePermissionService.findAllByRoleId((long) Math.toIntExact(role.getId())).stream())
                .anyMatch(rolePermission -> requestPath.equals(rolePermission.getPermission().getUrl()));

        /*
        Endpoint URL: This is the base URL for your endpoint.

        /loans/findLoan

        Full URL:

        /loans/findLoan?id=2&status=active
        */

        logger.info("Requested Path: {}", requestPath);
        logger.info("Has Permission: {}", hasPermission);

        if (hasPermission) {
            filterChain.doFilter(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
        }
    }
}




