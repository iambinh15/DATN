package org.example.datn_sp26.BanHang.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;

@Getter
@Setter
@Entity
public class GioHangChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idGioHang")
    private GioHang idGioHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSanPhamChiTiet")
    private SanPhamChiTiet idSanPhamChiTiet;

    @Column(name = "soLuong")
    private Integer soLuong;

}