package org.example.datn_sp26.SanPham.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "SanPham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"chiTietList"})
public class SanPham {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "maSanPham", length = 20)
    private String maSanPham;
    
    @Column(name = "tenSanPham", length = 150)
    private String tenSanPham;
    
    @Column(name = "moTa", length = 255)
    private String moTa;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idThuongHieu")
    private ThuongHieu thuongHieu;
    
    @Column(name = "trangThai")
    private Integer trangThai;
    
    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SanPhamChiTiet> chiTietList;
}
