package org.example.datn_sp26.SanPham.Service;

import org.example.datn_sp26.SanPham.Entity.SanPham;
import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.SanPhamChiTietRepository;
import org.example.datn_sp26.SanPham.Repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SanPhamChiTietService {

    @Autowired
    SanPhamChiTietRepository sanPhamChiTietRepository;
    @Autowired
    SanPhamRepository sanPhamRepository;
    // Giả sử đây là hàm cập nhật sau khi bán hàng hoặc chỉnh sửa kho
    public void checkAndDisableSanPham(Integer idSanPham) {
        // 1. Lấy danh sách tất cả chi tiết của sản phẩm đó
        List<SanPhamChiTiet> listDetails = sanPhamChiTietRepository.findAllByIdSanPham_Id(idSanPham);

        // 2. Tính tổng số lượng của tất cả biến thể
        int totalSoLuong = listDetails.stream()
                .mapToInt(SanPhamChiTiet::getSoLuong)
                .sum();

        // 3. Nếu tổng bằng 0, cập nhật trạng thái Sản phẩm tổng thành 0 (Ngưng hoạt động)
        if (totalSoLuong <= 0) {
            SanPham sp = sanPhamRepository.findById(idSanPham).orElse(null);
            if (sp != null) {
                sp.setTrangThai(0); // 0 là Ngưng hoạt động
                sanPhamRepository.save(sp);
            }
        }
    }
}
