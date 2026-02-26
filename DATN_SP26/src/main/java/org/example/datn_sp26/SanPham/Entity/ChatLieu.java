package org.example.datn_sp26.SanPham.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ChatLieu")
public class ChatLieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Nationalized
    @NotBlank(message = "Tên chất liệu không được để trống")
    @Size(min = 2, max = 50, message = "Tên chất liệu từ 2 đến 50 ký tự")
    @Column(name = "tenChatLieu", length = 50)
    private String tenChatLieu;

    @NotNull(message = "Vui lòng chọn trạng thái")
    @Column(name = "trangThai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idChatLieu")
    private Set<SanPhamChiTiet> sanPhamChiTiets = new LinkedHashSet<>();
}
