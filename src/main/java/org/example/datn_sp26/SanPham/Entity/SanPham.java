package org.example.datn_sp26.SanPham.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "maSanPham", length = 20)
    private String maSanPham;

    @Nationalized
    @Column(name = "tenSanPham", length = 150)
    private String tenSanPham;

    @Nationalized
    @Column(name = "moTa")
    private String moTa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idThuongHieu")
    private ThuongHieu idThuongHieu;

    @Column(name = "trangThai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idSanPham")
    private Set<HinhAnh> hinhAnhs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idSanPham")
    private Set<SanPhamChiTiet> sanPhamChiTiets = new LinkedHashSet<>();

    // Transient field để lưu tên ảnh đầu tiên (không lưu vào DB)
    @Transient
    private String firstImage;

}