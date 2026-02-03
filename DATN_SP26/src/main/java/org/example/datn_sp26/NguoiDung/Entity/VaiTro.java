package org.example.datn_sp26.NguoiDung.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class VaiTro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "maVaiTro", length = 20)
    private String maVaiTro;

    @Nationalized
    @Column(name = "tenVaiTro", length = 50)
    private String tenVaiTro;

    @Column(name = "trangThai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idVaiTro")
    private Set<TaiKhoan> taiKhoans = new LinkedHashSet<>();

}