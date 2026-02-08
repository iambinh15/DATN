package org.example.datn_sp26.BanHang.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.datn_sp26.NguoiDung.Entity.KhachHang;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class GioHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idKhachHang")
    private KhachHang idKhachHang;

    @Column(name = "ngayTao")
    private Instant ngayTao;

    @Column(name = "trangThai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idGioHang")
    private Set<GioHangChiTiet> gioHangChiTiets = new LinkedHashSet<>();

}