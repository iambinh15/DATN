package org.example.datn_sp26.SanPhamAdmin.Controller;

import lombok.RequiredArgsConstructor;
import org.example.datn_sp26.SanPham.Entity.HinhAnh;
import org.example.datn_sp26.SanPham.Entity.SanPham;
import org.example.datn_sp26.SanPham.Repository.HinhAnhRepository;
import org.example.datn_sp26.SanPham.Repository.SanPhamRepository;
import org.example.datn_sp26.SanPham.Repository.ThuongHieuRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.example.datn_sp26.SanPham.Repository.SanPhamChiTietRepository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/admin/san-pham")
@RequiredArgsConstructor
public class SanPhamAdminController {

    private final SanPhamRepository sanPhamRepository;
    private final ThuongHieuRepository thuongHieuRepository; // ✅ thêm dòng này
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final HinhAnhRepository hinhAnhRepository;

    // ================== HIỂN THỊ DANH SÁCH ==================
    @GetMapping
    public String index(Model model) {
        model.addAttribute("list", sanPhamRepository.findByTrangThai(1));
        return "SanPham/index";
    }

    // ================== FORM THÊM ==================
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("sanPham", new SanPham());
        model.addAttribute("listThuongHieu",
                thuongHieuRepository.findByTrangThai(1)); // ✅ gọi đúng cách
        return "SanPham/create";
    }

    // ================== FORM SỬA ==================
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        SanPham sanPham = sanPhamRepository.findById(id).orElseThrow();
        model.addAttribute("sanPham", sanPham);
        model.addAttribute("listThuongHieu",
                thuongHieuRepository.findByTrangThai(1));
        return "SanPham/create";
    }

    // ================== LƯU ==================
    @PostMapping("/save")
    public String save(@ModelAttribute SanPham sanPham,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {

        // Nếu chưa chọn trạng thái thì mặc định = 1
        if (sanPham.getTrangThai() == null) {
            sanPham.setTrangThai(1);
        }

        // Lưu sản phẩm trước để có ID
        SanPham savedSanPham = sanPhamRepository.save(sanPham);

        // Nếu có chọn file ảnh thì lưu vào thư mục và bảng HinhAnh
        if (imageFile != null && !imageFile.isEmpty()) {
            String uploadDir = "src/main/resources/static/images";
            Files.createDirectories(Paths.get(uploadDir));

            String originalFileName = imageFile.getOriginalFilename();
            String fileName = savedSanPham.getId() + "_" + (originalFileName != null ? originalFileName : "image");

            Path destination = Paths.get(uploadDir).resolve(fileName);
            Files.copy(imageFile.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            HinhAnh hinhAnh = new HinhAnh();
            hinhAnh.setIdSanPham(savedSanPham);
            // Đường dẫn dùng để hiển thị trên web
            hinhAnh.setHinhAnh("/images/" + fileName);
            hinhAnh.setTrangThai(1);
            hinhAnhRepository.save(hinhAnh);
        }

        return "redirect:/admin/san-pham";
    }

    // ================== XOÁ ==================
    @GetMapping("/delete/{id}")
    @Transactional
    public String delete(@PathVariable Integer id) {

        sanPhamChiTietRepository.deleteByIdSanPham_Id(id);

        sanPhamRepository.deleteById(id);

        return "redirect:/admin/san-pham";
    }
    // acb
}