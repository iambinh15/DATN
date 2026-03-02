package org.example.datn_sp26.SanPhamAdmin.Controller;

import org.example.datn_sp26.SanPham.Entity.SanPham;
import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.math.BigDecimal;
import java.util.Optional;

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

        return "SanPhamCT/create";
    }

    // ===============================
    // SAVE
    // ===============================
    @PostMapping("/save")
    public String save(@ModelAttribute("spct") SanPhamChiTiet spct,
                       Model model) {

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

        // ===== Không cho trùng biến thể =====
        Optional<SanPhamChiTiet> existing =
                spctRepository.findByIdSanPham_IdAndIdMauSac_IdAndIdSize_Id(
                        sanPhamId, mauSacId, sizeId);

        if (existing.isPresent()) {

            // Nếu đã tồn tại → cộng số lượng
            SanPhamChiTiet old = existing.get();
            old.setSoLuong(old.getSoLuong() + spct.getSoLuong());
            old.setDonGia(spct.getDonGia());

            // đảm bảo trạng thái vẫn = 1
            if (old.getTrangThai() == null) {
                old.setTrangThai(1);
            }

            spctRepository.save(old);

        } else {

            // ===== THÊM MỚI → MẶC ĐỊNH TRẠNG THÁI = 1 =====
            if (spct.getTrangThai() == null) {
                spct.setTrangThai(1);
            }

            spctRepository.save(spct);
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