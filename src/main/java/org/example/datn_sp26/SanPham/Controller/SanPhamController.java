package org.example.datn_sp26.SanPham.Controller;


import org.example.datn_sp26.SanPham.Entity.SanPham;
import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.SanPhamChiTietRepository;
import org.example.datn_sp26.SanPham.Repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/khach-hang")
public class SanPhamController {

    @Autowired
    private SanPhamRepository sanPhamRepo;

    @Autowired
    private SanPhamChiTietRepository spctRepo;

    // 1. TRANG CHỦ: Chỉ hiển thị danh sách sản phẩm đang hoạt động (trangThai = 1)
    @GetMapping("/trang-chu")
    public String trangChu(Model model) {
        //1 là trạng thái sản phẩm đang hoat đông nếu đổi là 0 thì sex là sản phẩm không hoạt động
        model.addAttribute("listSanPham", sanPhamRepo.findByTrangThai(1));
        return "khachhang/trang-chu";
    }
    // 2. TRANG CHI TIẾT: Hiển thị 1 sản phẩm và các biến thể của nó
    @GetMapping("/san-pham/{id}")
    public String chiTiet(@PathVariable("id") Integer id, Model model) {
        // Lấy thông tin sản phẩm chính
        SanPham sp = sanPhamRepo.findById(id).orElse(null);

        // Lấy danh sách các biến thể (SPCT) của sản phẩm đó
        // Lưu ý: Bạn cần viết hàm findByIdSanPham_Id trong Repository của SPCT
        List<SanPhamChiTiet> listBienThe = spctRepo.findAllWithDetailsBySanPhamId(id);

        model.addAttribute("sp", sp);
        model.addAttribute("listBienThe", listBienThe);
        return "khachhang/chi-tiet";
    }
}
