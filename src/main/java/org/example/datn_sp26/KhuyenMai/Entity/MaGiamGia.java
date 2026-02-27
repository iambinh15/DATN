package org.example.datn_sp26.KhuyenMai.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "MaGiamGia") // Tên bảng trong SQL của bạn
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MaGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String ma;
    private Double giaTri;
    private Integer trangThai;
    private String tenGiamGia;
    private Integer loaiGiam; // 0: Tiền mặt, 1: Phần trăm
    private Double giamToiDa;
    private String dieuKienGiam;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayBatDau;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayKetThuc;

    private Integer soLuong;
    private Double giamToiThieu; // Điều kiện đơn hàng tối thiểu
}