package org.example.datn_sp26.NguoiDung.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.example.datn_sp26.HoTro.Entity.HoTroKhachHang;
import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.hibernate.annotations.Nationalized;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(
        name = "NhanVien",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "maNhanVien"),
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "sdt")
        }
)
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTaiKhoan")
    private TaiKhoan idTaiKhoan;

    @NotBlank(message = "Mã nhân viên không được để trống")
    @Size(max = 20, message = "Mã nhân viên tối đa 20 ký tự")
    @Column(name = "maNhanVien", length = 20, nullable = false)
    private String maNhanVien;

    @NotBlank(message = "Tên nhân viên không được để trống")
    @Size(max = 100)
    @Nationalized
    @Column(name = "tenNhanVien", length = 100, nullable = false)
    private String tenNhanVien;

    @NotBlank(message = "SĐT không được để trống")
    @Pattern(regexp = "0[0-9]{9}", message = "SĐT phải 10 số, bắt đầu bằng 0")
    @Column(name = "sdt", length = 15, nullable = false)
    private String sdt;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Nationalized
    @Column(name = "diaChi", nullable = false)
    private String diaChi;

    @NotNull(message = "Vui lòng chọn trạng thái")
    @Column(name = "trangThai", nullable = false)
    private Integer trangThai; // 1: Hoạt động | 0: Ngưng

    @OneToMany(mappedBy = "idNhanVienTraLoi")
    private Set<HoTroKhachHang> hoTroKhachHangs = new LinkedHashSet<>();
    @OneToMany(mappedBy = "idNhanVien")
    private Set<HoaDon> hoaDons = new LinkedHashSet<>();
}
