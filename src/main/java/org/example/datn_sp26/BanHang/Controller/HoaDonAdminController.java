package org.example.datn_sp26.BanHang.Controller;

import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.example.datn_sp26.BanHang.Entity.TrangThaiHoaDon;
import org.example.datn_sp26.BanHang.Service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Controller
@RequestMapping("/hoa-don")
public class HoaDonAdminController {

    @Autowired
    private HoaDonService hoaDonService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String tenKH,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay,
            Model model) {

        List<HoaDon> danhSach;

        // Parse date strings to Instant
        Instant tuNgayInstant = null;
        Instant denNgayInstant = null;

        if (tuNgay != null && !tuNgay.isBlank()) {
            tuNgayInstant = LocalDate.parse(tuNgay)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant();
        }

        if (denNgay != null && !denNgay.isBlank()) {
            denNgayInstant = LocalDate.parse(denNgay)
                    .plusDays(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant();
        }

        // Check if any filter is active
        boolean hasFilter = (tenKH != null && !tenKH.isBlank())
                || (trangThai != null && !trangThai.isBlank())
                || tuNgayInstant != null
                || denNgayInstant != null;

        if (hasFilter) {
            String tenKHParam = (tenKH != null && !tenKH.isBlank()) ? tenKH : null;
            String trangThaiParam = (trangThai != null && !trangThai.isBlank()) ? trangThai : null;
            danhSach = hoaDonService.filterHoaDon(tenKHParam, trangThaiParam,
                    tuNgayInstant, denNgayInstant);
        } else {
            danhSach = hoaDonService.findAll();
        }

        // Load dropdown options
        List<TrangThaiHoaDon> dsTrangThai = hoaDonService.getAllTrangThai();

        model.addAttribute("list", danhSach);
        model.addAttribute("dsTrangThai", dsTrangThai);

        // Preserve filter values in form
        model.addAttribute("tenKH", tenKH);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);

        return "HoaDon/list";
    }
}
