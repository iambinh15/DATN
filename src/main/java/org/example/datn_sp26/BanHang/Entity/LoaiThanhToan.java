package org.example.datn_sp26.BanHang.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class LoaiThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "tenLoai", length = 50)
    private String tenLoai;

    @OneToMany(mappedBy = "idLoaiThanhToan")
    private Set<HoaDon> hoaDons = new LinkedHashSet<>();

}