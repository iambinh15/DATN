package org.example.datn_sp26.SanPham.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MauSac")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MauSac {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "tenMau", length = 50)
    private String tenMau;
    
    @Column(name = "trangThai")
    private Integer trangThai;
}
