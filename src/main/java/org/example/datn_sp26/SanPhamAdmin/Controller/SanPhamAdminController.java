package org.example.datn_sp26.SanPhamAdmin.Controller;

import lombok.RequiredArgsConstructor;
import org.example.datn_sp26.SanPham.Entity.HinhAnh;
import org.example.datn_sp26.SanPham.Entity.SanPham;
import org.example.datn_sp26.SanPham.Repository.HinhAnhRepository;
import org.example.datn_sp26.SanPham.Repository.SanPhamRepository;
import org.example.datn_sp26.SanPham.Repository.ThuongHieuRepository;
import org.example.datn_sp26.BanHang.Repository.HoaDonChiTietRepository;
import org.example.datn_sp26.BanHang.Repository.GioHangChiTietRepository;
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
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.List;

@Controller
@RequestMapping("/admin/san-pham")
@RequiredArgsConstructor
public class SanPhamAdminController {

    private final SanPhamRepository sanPhamRepository;
    private final ThuongHieuRepository thuongHieuRepository; // ✅ thêm dòng này
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final HinhAnhRepository hinhAnhRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final GioHangChiTietRepository gioHangChiTietRepository;
    private static final Pattern FILENAME_SAFE = Pattern.compile("[^a-zA-Z0-9._-]");

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
        model.addAttribute("cacheBust", System.currentTimeMillis());
        return "SanPham/create";
    }

    // ================== FORM SỬA ==================
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        SanPham sanPham = sanPhamRepository.findByIdWithImages(id).orElseThrow();
        model.addAttribute("sanPham", sanPham);
        model.addAttribute("listThuongHieu",
                thuongHieuRepository.findByTrangThai(1));
        model.addAttribute("cacheBust", System.currentTimeMillis());
        return "SanPham/create";
    }

    // ================== LƯU ==================
    @PostMapping("/save")
    public String save(@ModelAttribute SanPham sanPham,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                       @RequestParam(value = "selectedImageId", required = false) Integer selectedImageId) throws IOException {

        boolean isEdit = sanPham.getId() != null;

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
            String safeOriginal = originalFileName != null ? FILENAME_SAFE.matcher(originalFileName).replaceAll("_") : "image";
            String fileName = savedSanPham.getId() + "_" + System.currentTimeMillis() + "_" + safeOriginal;

            Path destination = Paths.get(uploadDir).resolve(fileName);
            Files.copy(imageFile.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = "/images/" + fileName;

            // Khi sửa: cập nhật đúng ảnh được chọn (không thêm mới). Khi thêm: tạo ảnh mới.
            HinhAnh target = null;
            if (isEdit && selectedImageId != null) {
                Optional<HinhAnh> selected = hinhAnhRepository.findById(selectedImageId);
                if (selected.isPresent()
                        && selected.get().getIdSanPham() != null
                        && selected.get().getIdSanPham().getId() != null
                        && selected.get().getIdSanPham().getId().equals(savedSanPham.getId())) {
                    target = selected.get();
                }
            }
            if (target == null && isEdit) {
                target = hinhAnhRepository.findTopByIdSanPham_IdOrderByIdAsc(savedSanPham.getId())
                        .orElse(null);
            }
            if (target == null) {
                target = new HinhAnh();
                target.setIdSanPham(savedSanPham);
                target.setTrangThai(1);
            }
            target.setHinhAnh(imageUrl);
            if (target.getTrangThai() == null) target.setTrangThai(1);
            hinhAnhRepository.save(target);
        }

        return "redirect:/admin/san-pham";
    }

    // ================== XOÁ ==================
    @GetMapping("/delete/{id}")
    @Transactional
    public String delete(@PathVariable Integer id) {

        // Nếu SP đã phát sinh giỏ hàng / hóa đơn -> không xóa cứng, chuyển sang ngừng bán
        List<Integer> spctIds = sanPhamChiTietRepository.findByIdSanPham_Id(id)
                .stream()
                .map(spct -> spct.getId())
                .collect(Collectors.toList());

        boolean hasRefs = spctIds.stream().anyMatch(spctId ->
                hoaDonChiTietRepository.existsByIdSanPhamChiTiet_Id(spctId)
                        || gioHangChiTietRepository.existsByIdSanPhamChiTiet_Id(spctId)
        );

        if (hasRefs) {
            SanPham sp = sanPhamRepository.findById(id).orElseThrow();
            sp.setTrangThai(0);
            sanPhamRepository.save(sp);

            // Ngừng bán toàn bộ biến thể
            sanPhamChiTietRepository.findByIdSanPham_Id(id).forEach(ct -> {
                ct.setTrangThai(0);
                spctIds.add(ct.getId());
            });
            // saveAll
            sanPhamChiTietRepository.saveAll(sanPhamChiTietRepository.findByIdSanPham_Id(id));
            return "redirect:/admin/san-pham";
        }

        // Chưa phát sinh -> xóa cứng theo thứ tự FK
        if (!spctIds.isEmpty()) {
            spctIds.forEach(gioHangChiTietRepository::deleteByIdSanPhamChiTiet_Id);
            hoaDonChiTietRepository.deleteByIdSanPhamChiTiet_IdIn(spctIds);
        }
        sanPhamChiTietRepository.deleteByIdSanPham_Id(id);
        hinhAnhRepository.deleteByIdSanPham_Id(id);
        sanPhamRepository.deleteById(id);

        return "redirect:/admin/san-pham";
    }
}