package org.example.datn_sp26.DangNhap.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendNewPasswordEmail(String toEmail, String newPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("CotiT Store <noreply@cotit.vn>");
        message.setTo(toEmail);
        message.setSubject("Cấp lại mật khẩu mới - CotiT Store");
        message.setText("Chào bạn,\n\n"
                + "Mật khẩu mới của bạn là: " + newPassword + "\n\n"
                + "Vui lòng đăng nhập và đổi lại mật khẩu ngay lập tức để bảo mật tài khoản.\n\n"
                + "Trân trọng,\n"
                + "Đội ngũ CotiT Store");

        mailSender.send(message);
    }
}
