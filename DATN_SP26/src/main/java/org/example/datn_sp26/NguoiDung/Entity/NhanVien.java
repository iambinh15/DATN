package org.example.datn_sp26.NguoiDung.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.datn_sp26.HoTro.Entity.HoTroKhachHang;
import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.hibernate.annotations.Nationalized;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTaiKhoan")
    private TaiKhoan idTaiKhoan;

    @Column(name = "maNhanVien", length = 20)
    private String maNhanVien;

    @Nationalized
    @Column(name = "tenNhanVien", length = 100)
    private String tenNhanVien;

    @Column(name = "sdt", length = 15)
    private String sdt;

    @Column(name = "email", length = 100)
    private String email;

    @Nationalized
    @Column(name = "diaChi")
    private String diaChi;

    @Column(name = "trangThai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idNhanVienTraLoi")
    private Set<HoTroKhachHang> hoTroKhachHangs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idNhanVien")
    private Set<HoaDon> hoaDons = new LinkedHashSet<>();

}