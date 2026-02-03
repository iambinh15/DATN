package org.example.datn_sp26.SanPham.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ChatLieu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatLieu {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "tenChatLieu", length = 50)
    private String tenChatLieu;
    
    @Column(name = "trangThai")
    private Integer trangThai;
}
