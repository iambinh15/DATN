package org.example.datn_sp26.SanPham.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "\"Size\"")
public class Size {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "tenSize", length = 10)
    private String tenSize;

    @Column(name = "trangThai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idSize")
    private Set<SanPhamChiTiet> sanPhamChiTiets = new LinkedHashSet<>();

}