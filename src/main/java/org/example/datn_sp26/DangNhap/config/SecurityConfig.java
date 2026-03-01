package org.example.datn_sp26.DangNhap.config;

import org.example.datn_sp26.DangNhap.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private CustomUserDetailsService userDetailsService;

        @Autowired
        private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(requests -> requests
                                                // Trang công khai
                                        .requestMatchers("/", "/trang-chu", "/home", "/login", "/css/**",
                                                "/js/**", "/images/**",
                                                "/dang-ky", "/register", "/forgot-password")
                                        .permitAll()

// 🔥 CHỈ ADMIN
                                        .requestMatchers("/nhan-vien/**").hasRole("ADMIN")
                                        .requestMatchers("/san-pham/**").hasRole("ADMIN")

// 🔥 ADMIN + STAFF
                                        .requestMatchers("/chat-lieu/**", "/hoa-don/**")
                                        .hasAnyRole("ADMIN", "STAFF")

// Admin area
                                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "STAFF")

// USER
                                        .requestMatchers("/khach-hang/trang-chu", "/khach-hang/san-pham/**",
                                                "/khach-hang/gio-hang/**", "/khach-hang/thanh-toan/**",
                                                "/khach-hang/don-hang/**")
                                        .hasRole("USER")

// Quản lý khách hàng
                                        .requestMatchers("/khach-hang", "/khach-hang/add",
                                                "/khach-hang/edit/**",
                                                "/khach-hang/save", "/khach-hang/delete/**")
                                        .hasAnyRole("ADMIN", "STAFF")

                                        .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/perform_login")
                                                .successHandler(customAuthenticationSuccessHandler)
                                                .permitAll())
                                .logout(logout -> logout.permitAll())
                                .csrf(csrf -> csrf.disable());

                return http.build();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setUserDetailsService(userDetailsService);
                provider.setPasswordEncoder(passwordEncoder());
                return provider;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return NoOpPasswordEncoder.getInstance();
        }
}
