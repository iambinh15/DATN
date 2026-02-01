package org.example.datn_sp26.NguoiDung.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class TaiKhoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "password", length = 100)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idVaiTro")
    private VaiTro idVaiTro;

    @Column(name = "trangThai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idTaiKhoan")
    private Set<KhachHang> khachHangs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idTaiKhoan")
    private Set<NhanVien> nhanViens = new LinkedHashSet<>();

}