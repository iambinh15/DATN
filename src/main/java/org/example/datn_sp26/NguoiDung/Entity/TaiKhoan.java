package org.example.datn_sp26.NguoiDung.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TaiKhoan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaiKhoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String tenDangNhap;

    @Column(name = "password")
    private String matKhau;

    @ManyToOne
    @JoinColumn(name = "idVaiTro")
    private VaiTro vaiTro;

    @Column(name = "trangThai")
    private Integer trangThai;
}
