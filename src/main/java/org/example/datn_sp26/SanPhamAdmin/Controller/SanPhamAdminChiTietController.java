package org.example.datn_sp26.SanPhamAdmin.Controller;

import org.example.datn_sp26.SanPham.Entity.HinhAnh;
import org.example.datn_sp26.SanPham.Entity.SanPham;
import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.*;

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
import java.util.Objects;

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
            // ===== SỬA: cập nhật đúng bản ghi, không gộp số lượng =====
            Optional<SanPhamChiTiet> duplicate =
                    spctRepository.findByIdSanPham_IdAndIdMauSac_IdAndIdSize_IdAndIdNot(
                            sanPhamId, mauSacId, sizeId, spct.getId());

            if (duplicate.isPresent()) {
                model.addAttribute("error", "Biến thể (màu/size) đã tồn tại.");
                return reloadForm(spct, model);
            }

            if (spct.getTrangThai() == null) {
                spct.setTrangThai(1);
            }

            spctRepository.save(spct);

        } else {
            // ===== THÊM MỚI: không cho trùng biến thể =====
            Optional<SanPhamChiTiet> existing =
                    spctRepository.findByIdSanPham_IdAndIdMauSac_IdAndIdSize_Id(
                            sanPhamId, mauSacId, sizeId);

            if (existing.isPresent()) {
                // Nếu đã tồn tại → cộng số lượng (giữ logic cũ)
                SanPhamChiTiet old = existing.get();
                old.setSoLuong(old.getSoLuong() + spct.getSoLuong());
                old.setDonGia(spct.getDonGia());

                if (old.getTrangThai() == null) {
                    old.setTrangThai(1);
                }

                spctRepository.save(old);
            } else {
                if (spct.getTrangThai() == null) {
                    spct.setTrangThai(1);
                }
                spctRepository.save(spct);
            }
        }

        // ================== ẢNH: THÊM MỚI hoặc REPLACE khi SỬA ==================
        if (moreImages != null && moreImages.stream().anyMatch(f -> f != null && !f.isEmpty())) {
            String uploadDir = "src/main/resources/static/images";
            Files.createDirectories(Paths.get(uploadDir));

            if (isEdit) {
                // PHƯƠNG ÁN B: Khi sửa chỉ cập nhật ảnh đại diện (ảnh đầu tiên), không thêm ảnh mới
                MultipartFile firstFile = moreImages.stream()
                        .filter(Objects::nonNull)
                        .filter(f -> !f.isEmpty())
                        .findFirst()
                        .orElse(null);

                if (firstFile != null) {
                    String originalFileName = firstFile.getOriginalFilename();
                    String safeOriginal = originalFileName != null ? FILENAME_SAFE.matcher(originalFileName).replaceAll("_") : "image";
                    String fileName = sanPhamId + "_" + System.currentTimeMillis() + "_" + safeOriginal;

                    var destination = Paths.get(uploadDir).resolve(fileName);
                    Files.copy(firstFile.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

                    HinhAnh hinhAnh = null;

                    // Ưu tiên cập nhật đúng ảnh được chọn trên UI
                    if (selectedImageId != null) {
                        Optional<HinhAnh> selected = hinhAnhRepository.findById(selectedImageId);
                        if (selected.isPresent()
                                && selected.get().getIdSanPham() != null
                                && selected.get().getIdSanPham().getId() != null
                                && selected.get().getIdSanPham().getId().equals(sanPhamId)) {
                            hinhAnh = selected.get();
                        }
                    }

                    if (hinhAnh == null) {
                        hinhAnh = hinhAnhRepository
                                .findTopByIdSanPham_IdOrderByIdAsc(sanPhamId)
                                .orElseGet(() -> {
                                    HinhAnh ha = new HinhAnh();
                                    ha.setIdSanPham(spct.getIdSanPham());
                                    ha.setTrangThai(1);
                                    return ha;
                                });
                    }

                    hinhAnh.setHinhAnh("/images/" + fileName);
                    hinhAnhRepository.save(hinhAnh);
                }
            } else {
                // Thêm mới biến thể: vẫn cho phép thêm nhiều ảnh
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