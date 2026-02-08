package org.example.datn_sp26.KhuyenMai.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.datn_sp26.BanHang.Entity.HoaDon;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class MaGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "ma", length = 20)
    private String ma;

    @Column(name = "giaTri", precision = 18, scale = 2)
    private BigDecimal giaTri;

    @Column(name = "trangThai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idMaGiamGia")
    private Set<HoaDon> hoaDons = new LinkedHashSet<>();

}