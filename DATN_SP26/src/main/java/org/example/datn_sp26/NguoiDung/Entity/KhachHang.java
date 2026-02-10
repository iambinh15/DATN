package org.example.datn_sp26.NguoiDung.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.datn_sp26.BanHang.Entity.GioHang;
import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.example.datn_sp26.HoTro.Entity.HoTroKhachHang;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class KhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTaiKhoan")
    private TaiKhoan idTaiKhoan;

    @NotBlank(message = "Mã khách hàng không được để trống")
    @Column(length = 20, unique = true)
    private String maKhachHang;

    @NotBlank(message = "Tên khách hàng không được để trống")
    @Nationalized
    @Column(length = 100)
    private String tenKhachHang;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "0[0-9]{9}", message = "Số điện thoại không hợp lệ")
    @Column(length = 15, unique = true)
    private String sdt;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Column(length = 100, unique = true)
    private String email;

    @NotNull(message = "Vui lòng chọn giới tính")
    private Boolean gioiTinh; // true = Nam, false = Nữ

    @Column(updatable = false)
    private Instant ngayTao;

    @NotNull(message = "Vui lòng chọn trạng thái")
    private Integer trangThai; // 1 = Hoạt động, 0 = Ngừng


    @OneToMany(mappedBy = "idKhachHang")
    private Set<DiaChi> diaChis = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idKhachHang")
    private Set<GioHang> gioHangs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idKhachHang")
    private Set<HoTroKhachHang> hoTroKhachHangs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idKhachHang")
    private Set<HoaDon> hoaDons = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        ngayTao = Instant.now();
    }
}
