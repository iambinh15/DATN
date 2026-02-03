package org.example.datn_sp26.SanPham.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ThuongHieu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ThuongHieu {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "tenThuongHieu", length = 100)
    private String tenThuongHieu;
    
    @Column(name = "trangThai")
    private Integer trangThai;
}
