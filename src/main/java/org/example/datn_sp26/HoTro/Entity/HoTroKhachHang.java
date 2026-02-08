package org.example.datn_sp26.HoTro.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Entity.NhanVien;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
public class HoTroKhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idKhachHang")
    private KhachHang idKhachHang;

    @Nationalized
    @Column(name = "noiDung")
    private String noiDung;

    @Column(name = "thoiGian")
    private Instant thoiGian;

    @Column(name = "trangThai")
    private Integer trangThai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idNhanVienTraLoi")
    private NhanVien idNhanVienTraLoi;

}