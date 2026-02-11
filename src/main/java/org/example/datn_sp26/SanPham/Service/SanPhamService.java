package org.example.datn_sp26.SanPham.Service;


import org.example.datn_sp26.SanPham.Entity.SanPham;
import org.example.datn_sp26.SanPham.Repository.SanPhamRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SanPhamService {

    private final SanPhamRepository sanPhamRepository;

    public SanPhamService(SanPhamRepository sanPhamRepository) {
        this.sanPhamRepository = sanPhamRepository;
    }

    public List<SanPham> getAllSanPhamActive() {
        return sanPhamRepository.findByTrangThai(1); // chỉ lấy sản phẩm đang hiển thị
    }
}
