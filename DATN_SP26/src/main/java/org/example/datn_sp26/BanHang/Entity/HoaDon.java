package org.example.datn_sp26.BanHang.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.datn_sp26.KhuyenMai.Entity.MaGiamGia;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;
import org.example.datn_sp26.NguoiDung.Entity.NhanVien;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "maHoaDon", length = 20)
    private String maHoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idNhanVien")
    private NhanVien idNhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idKhachHang")
    private KhachHang idKhachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idMaGiamGia")
    private MaGiamGia idMaGiamGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idLoaiThanhToan")
    private LoaiThanhToan idLoaiThanhToan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTrangThaiHoaDon")
    private TrangThaiHoaDon idTrangThaiHoaDon;

    @Nationalized
    @Column(name = "diaChi")
    private String diaChi;

    @Column(name = "ngayTao")
    private Instant ngayTao;

    @Column(name = "tongThanhToan", precision = 18, scale = 2)
    private BigDecimal tongThanhToan;

    @OneToMany(mappedBy = "idHoaDon")
    private Set<HoaDonChiTiet> hoaDonChiTiets = new LinkedHashSet<>();

}