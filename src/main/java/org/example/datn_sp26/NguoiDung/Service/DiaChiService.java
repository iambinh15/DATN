package org.example.datn_sp26.NguoiDung.Service;

import org.example.datn_sp26.NguoiDung.Entity.DiaChi;
import org.example.datn_sp26.NguoiDung.Repository.DiaChiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiaChiService {

    @Autowired
    private DiaChiRepository diaChiRepository;

    // GIỮ NGUYÊN logic cũ để lấy danh sách
    public List<DiaChi> layDiaChiCuaKhach(Integer idKhachHang) {
        return diaChiRepository.findByIdKhachHang_Id(idKhachHang);
    }

    // GIỮ NGUYÊN logic cũ để lấy mặc định
    public DiaChi layDiaChiMacDinh(Integer idKhachHang) {
        return diaChiRepository
                .findByIdKhachHang_IdAndTrangThai(idKhachHang, 1)
                .orElse(null);
    }

    /**
     * LOGIC THIẾT LẬP MẶC ĐỊNH DUY NHẤT
     * Đảm bảo dọn sạch mọi địa chỉ mặc định cũ trước khi đặt cái mới.
     */
    @Transactional
    public void capNhatMacDinhDuyNhat(Integer idKhachHang, Integer idDiaChiDuocChon) {
        // 1. Lấy tất cả địa chỉ của khách hàng đó
        List<DiaChi> tatCaDiaChi = diaChiRepository.findByIdKhachHang_Id(idKhachHang);

        // 2. Duyệt qua danh sách và cập nhật trạng thái
        for (DiaChi dc : tatCaDiaChi) {
            if (dc.getId().equals(idDiaChiDuocChon)) {
                dc.setTrangThai(0); // Địa chỉ được chọn
            } else {
                dc.setTrangThai(1); // Tất cả những cái còn lại phải là 0
            }
        }

        // 3. Lưu toàn bộ thay đổi
        diaChiRepository.saveAll(tatCaDiaChi);
    }
}