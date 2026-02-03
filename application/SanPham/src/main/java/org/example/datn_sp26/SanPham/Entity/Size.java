package org.example.datn_sp26.SanPham.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Size")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Size {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "tenSize", length = 10)
    private String tenSize;
    
    @Column(name = "trangThai")
    private Integer trangThai;
}
