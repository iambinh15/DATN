package org.example.datn_sp26.BanHang.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class HoaDonChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idHoaDon")
    private HoaDon idHoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSanPhamChiTiet")
    private SanPhamChiTiet idSanPhamChiTiet;

    @Column(name = "soLuong")
    private Integer soLuong;

    @Column(name = "donGia", precision = 18, scale = 2)
    private BigDecimal donGia;

}