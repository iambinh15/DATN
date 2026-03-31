package org.example.datn_sp26.SanPhamAdmin.Controller;

import org.example.datn_sp26.SanPham.Entity.HinhAnh;
import org.example.datn_sp26.SanPham.Entity.SanPham;
import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.*;
import org.example.datn_sp26.BanHang.Repository.HoaDonChiTietRepository;
import org.example.datn_sp26.BanHang.Repository.GioHangChiTietRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/admin/spct")
public class SanPhamAdminChiTietController {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private SanPhamChiTietRepository spctRepository;

    @Autowired
    private MauSacRepository mauSacRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private ChatLieuRepository chatLieuRepository;

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    @Autowired
    private HinhAnhRepository hinhAnhRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private GioHangChiTietRepository gioHangChiTietRepository;

    private static final Pattern FILENAME_SAFE = Pattern.compile("[^a-zA-Z0-9._-]");

    // ===============================
    // FORM CREATE
    // ===============================
    @GetMapping("/create/{sanPhamId}")
    public String showCreateForm(@PathVariable Integer sanPhamId, Model model) {

        SanPhamChiTiet spct = new SanPhamChiTiet();
        SanPham sanPham = sanPhamRepository.findById(sanPhamId).orElseThrow();

        spct.setIdSanPham(sanPham);

        model.addAttribute("spct", spct);
        model.addAttribute("mauSacs", mauSacRepository.findAll());
        model.addAttribute("sizes", sizeRepository.findAll());
        model.addAttribute("chatLieus", chatLieuRepository.findAll());
        model.addAttribute("thuongHieus", thuongHieuRepository.findAll());
        model.addAttribute("cacheBust", System.currentTimeMillis());

        return "SanPhamCT/create";
    }

    // ===============================
    // SAVE
    // ===============================
    @PostMapping("/save")
    public String save(@ModelAttribute("spct") SanPhamChiTiet spct,
                       @RequestParam(value = "moreImages", required = false) List<MultipartFile> moreImages,
                       @RequestParam(value = "selectedImageId", required = false) Integer selectedImageId,
                       Model model) throws IOException {

        // ===== Validate an toàn =====
        if (spct.getSoLuong() == null || spct.getSoLuong() <= 0) {
            model.addAttribute("error", "Số lượng phải > 0");
            return reloadForm(spct, model);
        }

        if (spct.getDonGia() == null ||
                spct.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
            model.addAttribute("error", "Giá không hợp lệ");
            return reloadForm(spct, model);
        }

        Integer sanPhamId = spct.getIdSanPham().getId();
        Integer mauSacId = spct.getIdMauSac().getId();
        Integer sizeId = spct.getIdSize().getId();

        boolean isEdit = spct.getId() != null;

        if (isEdit) {
            // ===== SỬA: update đúng bản ghi theo ID (KHÔNG tạo biến thể mới) =====
            SanPhamChiTiet existing = spctRepository.findById(spct.getId()).orElseThrow();

            // Không cho trùng màu/size với bản ghi khác
            Optional<SanPhamChiTiet> duplicate =
                    spctRepository.findByIdSanPham_IdAndIdMauSac_IdAndIdSize_IdAndIdNot(
                            sanPhamId, mauSacId, sizeId, spct.getId());
            if (duplicate.isPresent()) {
                model.addAttribute("error", "Biến thể (màu/size) đã tồn tại.");
                return reloadForm(spct, model);
            }

            existing.setIdMauSac(spct.getIdMauSac());
            existing.setIdSize(spct.getIdSize());
            existing.setIdChatLieu(spct.getIdChatLieu());
            existing.setDonGia(spct.getDonGia());
            existing.setSoLuong(spct.getSoLuong());
            existing.setTrangThai(spct.getTrangThai() != null ? spct.getTrangThai() : 1);

            spctRepository.save(existing);

        } else {
            // ===== THÊM MỚI: giữ logic cũ (trùng thì gộp số lượng) =====
            Optional<SanPhamChiTiet> existing =
                    spctRepository.findByIdSanPham_IdAndIdMauSac_IdAndIdSize_Id(
                            sanPhamId, mauSacId, sizeId);

            if (existing.isPresent()) {
                SanPhamChiTiet old = existing.get();
                old.setSoLuong(old.getSoLuong() + spct.getSoLuong());
                old.setDonGia(spct.getDonGia());
                if (old.getTrangThai() == null) old.setTrangThai(1);
                spctRepository.save(old);
            } else {
                if (spct.getTrangThai() == null) spct.setTrangThai(1);
                spctRepository.save(spct);
            }
        }

        // ================== LƯU THÊM NHIỀU ẢNH (NẾU CÓ) ==================
        if (moreImages != null && moreImages.stream().anyMatch(f -> f != null && !f.isEmpty())) {
            String uploadDir = "src/main/resources/static/images";
            Files.createDirectories(Paths.get(uploadDir));

            // Khi sửa: chỉ cập nhật 1 ảnh được chọn (không thêm ảnh mới)
            if (isEdit) {
                MultipartFile firstFile = moreImages.stream()
                        .filter(f -> f != null && !f.isEmpty())
                        .findFirst()
                        .orElse(null);

                if (firstFile != null) {
                    String originalFileName = firstFile.getOriginalFilename();
                    String safeOriginal = originalFileName != null ? FILENAME_SAFE.matcher(originalFileName).replaceAll("_") : "image";
                    String fileName = sanPhamId + "_" + System.currentTimeMillis() + "_" + safeOriginal;

                    var destination = Paths.get(uploadDir).resolve(fileName);
                    Files.copy(firstFile.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

                    HinhAnh target = null;
                    if (selectedImageId != null) {
                        Optional<HinhAnh> selected = hinhAnhRepository.findById(selectedImageId);
                        if (selected.isPresent()
                                && selected.get().getIdSanPham() != null
                                && selected.get().getIdSanPham().getId() != null
                                && selected.get().getIdSanPham().getId().equals(sanPhamId)) {
                            target = selected.get();
                        }
                    }
                    if (target == null) {
                        target = hinhAnhRepository.findTopByIdSanPham_IdOrderByIdAsc(sanPhamId)
                                .orElseGet(() -> {
                                    HinhAnh ha = new HinhAnh();
                                    ha.setIdSanPham(spct.getIdSanPham());
                                    ha.setTrangThai(1);
                                    return ha;
                                });
                    }

                    target.setHinhAnh("/images/" + fileName);
                    if (target.getTrangThai() == null) target.setTrangThai(1);
                    hinhAnhRepository.save(target);
                }
            } else {
                // Thêm mới: cho phép thêm nhiều ảnh
                for (MultipartFile file : moreImages) {
                    if (file == null || file.isEmpty()) continue;

                    String originalFileName = file.getOriginalFilename();
                    String safeOriginal = originalFileName != null ? FILENAME_SAFE.matcher(originalFileName).replaceAll("_") : "image";
                    String fileName = sanPhamId + "_" + System.currentTimeMillis() + "_" + safeOriginal;

                    var destination = Paths.get(uploadDir).resolve(fileName);
                    Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

                    HinhAnh hinhAnh = new HinhAnh();
                    hinhAnh.setIdSanPham(spct.getIdSanPham());
                    hinhAnh.setHinhAnh("/images/" + fileName);
                    hinhAnh.setTrangThai(1);
                    hinhAnhRepository.save(hinhAnh);
                }
            }
        }

        return "redirect:/admin/spct/list/" + sanPhamId;
    }

    // ===============================
    // LIST
    // ===============================
    @GetMapping("/list/{sanPhamId}")
    public String list(@PathVariable Integer sanPhamId, Model model) {

        model.addAttribute("list",
                spctRepository.findAllWithDetailsBySanPhamId(sanPhamId));

        model.addAttribute("sanPhamId", sanPhamId);

        return "SanPhamCT/index";
    }

    // ===============================
    // EDIT FORM
    // ===============================
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {

        SanPhamChiTiet spct = spctRepository.findById(id).orElseThrow();

        model.addAttribute("spct", spct);
        model.addAttribute("mauSacs", mauSacRepository.findAll());
        model.addAttribute("sizes", sizeRepository.findAll());
        model.addAttribute("chatLieus", chatLieuRepository.findAll());
        model.addAttribute("thuongHieus", thuongHieuRepository.findAll());
        model.addAttribute("cacheBust", System.currentTimeMillis());

        return "SanPhamCT/create";
    }

    // ===============================
    // DELETE
    // ===============================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {

        SanPhamChiTiet spct = spctRepository.findById(id).orElseThrow();
        Integer sanPhamId = spct.getIdSanPham().getId();

        // Nếu đã nằm trong giỏ hàng / hóa đơn -> không xóa cứng, chuyển trạng thái ngừng bán
        boolean hasRefs = hoaDonChiTietRepository.existsByIdSanPhamChiTiet_Id(id)
                || gioHangChiTietRepository.existsByIdSanPhamChiTiet_Id(id);

        if (hasRefs) {
            spct.setTrangThai(0);
            spctRepository.save(spct);
            return "redirect:/admin/spct/list/" + sanPhamId;
        }

        // Chưa phát sinh -> xóa cứng
        gioHangChiTietRepository.deleteByIdSanPhamChiTiet_Id(id);
        hoaDonChiTietRepository.deleteByIdSanPhamChiTiet_Id(id);
        spctRepository.deleteById(id);

        return "redirect:/admin/spct/list/" + sanPhamId;
    }

    // ===============================
    // LOAD LẠI FORM KHI LỖI
    // ===============================
    private String reloadForm(SanPhamChiTiet spct, Model model) {

        model.addAttribute("spct", spct);
        model.addAttribute("mauSacs", mauSacRepository.findAll());
        model.addAttribute("sizes", sizeRepository.findAll());
        model.addAttribute("chatLieus", chatLieuRepository.findAll());
        model.addAttribute("thuongHieus", thuongHieuRepository.findAll());

        return "SanPhamCT/create";
    }
}