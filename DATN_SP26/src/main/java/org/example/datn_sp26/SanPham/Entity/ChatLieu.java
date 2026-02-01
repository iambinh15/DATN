package org.example.datn_sp26.SanPham.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class ChatLieu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "tenChatLieu", length = 50)
    private String tenChatLieu;

    @Column(name = "trangThai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idChatLieu")
    private Set<SanPhamChiTiet> sanPhamChiTiets = new LinkedHashSet<>();

}