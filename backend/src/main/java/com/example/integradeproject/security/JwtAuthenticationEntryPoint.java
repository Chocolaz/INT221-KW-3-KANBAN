package com.example.integradeproject.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

//AuthenticationEntryPoint: อินเทอร์เฟซที่ให้เรากำหนดวิธีจัดการกรณีที่ผู้ใช้ไม่ได้รับอนุญาต (Unauthorized)
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        String error = (String) request.getAttribute("jwt_error");
        if (error == null) {
            error = "Unauthorized";
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", error));
    }
}
