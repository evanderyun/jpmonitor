package com.jpm.erp.platform.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        System.out.println("LoggingFilter: Request received: " + req.getMethod() + " " + req.getRequestURI());
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            System.err.println("LoggingFilter: Exception in filter chain: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
