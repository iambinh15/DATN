package org.example.datn_sp26.NguoiDung.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "VaiTro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VaiTro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "maVaiTro")
    private String maVaiTro;

    @Column(name = "tenVaiTro")
    private String tenVaiTro;

    @Column(name = "trangThai")
    private Integer trangThai;
}
