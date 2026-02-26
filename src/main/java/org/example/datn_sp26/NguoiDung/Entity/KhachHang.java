package org.example.datn_sp26.NguoiDung.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.datn_sp26.BanHang.Entity.GioHang;
import org.example.datn_sp26.HoTro.Entity.HoTroKhachHang;
import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTaiKhoan")
    private TaiKhoan idTaiKhoan;


    // Trong class KhachHang
    @OneToMany(mappedBy = "idKhachHang", fetch = FetchType.LAZY)
    private List<DiaChi> danhSachDiaChi;


    @Column(name = "maKhachHang", length = 20)
    private String maKhachHang;

    @Nationalized
    @Column(name = "tenKhachHang", length = 100)
    private String tenKhachHang;

    @Column(name = "sdt", length = 15)
    private String sdt;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "gioiTinh")
    private Boolean gioiTinh;

    @Column(name = "ngayTao")
    private Instant ngayTao;

    @Column(name = "trangThai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idKhachHang")
    private Set<DiaChi> diaChis = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idKhachHang")
    private Set<GioHang> gioHangs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idKhachHang")
    private Set<HoTroKhachHang> hoTroKhachHangs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idKhachHang")
    private Set<HoaDon> hoaDons = new LinkedHashSet<>();

}
