package org.example.datn_sp26.DangNhap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String username; // Thêm trường này
    private String hoTen;
    private String email;
    private String sdt;
    private String password;
    private String confirmPassword;
}
