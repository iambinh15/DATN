package org.example.datn_sp26.NguoiDung.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "NhanVien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "maNhanVien")
    private String maNhanVien;

    @Column(name = "tenNhanVien")
    private String ten;

    @Column(name = "sdt")
    private String sdt;

    @Column(name = "email")
    private String email;

    @Column(name = "diaChi")
    private String diaChi;

    @Column(name = "trangThai")
    private Integer trangThai;

    @OneToOne
    @JoinColumn(name = "idTaiKhoan")
    private TaiKhoan taiKhoan;
}
