package org.example.datn_sp26.DangNhap.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Collection;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            // Kiểm tra role có prefix ROLE_ do UserDetailsService thêm vào
            if (role.equals("ROLE_ADMIN") || role.equals("ROLE_STAFF")) {
                response.sendRedirect("/admin");
                return;
            } else if (role.equals("ROLE_USER")) {
                response.sendRedirect("/home");
                return;
            }
        }
        // Mặc định về trang chủ nếu không khớp role nào
        response.sendRedirect("/home");
    }
}
