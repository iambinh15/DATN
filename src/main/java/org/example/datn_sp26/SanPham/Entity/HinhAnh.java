package org.example.datn_sp26.SanPham.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
public class HinhAnh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSanPham")
    private SanPham idSanPham;

    @Nationalized
    @Column(name = "hinhAnh")
    private String hinhAnh;

    @Column(name = "trangThai")
    private Integer trangThai;

}