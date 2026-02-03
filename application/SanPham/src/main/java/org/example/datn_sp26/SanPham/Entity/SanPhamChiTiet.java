package org.example.datn_sp26.SanPham.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "SanPhamChiTiet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"sanPham"})
public class SanPhamChiTiet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idSanPham")
    private SanPham sanPham;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idMauSac")
    private MauSac mauSac;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idSize")
    private Size size;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idChatLieu")
    private ChatLieu chatLieu;
    
    @Column(name = "donGia", precision = 18, scale = 2)
    private BigDecimal donGia;
    
    @Column(name = "soLuong")
    private Integer soLuong;
    
    @Column(name = "trangThai")
    private Integer trangThai;
}
