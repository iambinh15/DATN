package org.example.datn_sp26.ThongKe.Controller;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.datn_sp26.ThongKe.Service.ThongKeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/thong-ke")
public class ThongKeController {

    private final ThongKeService thongKeService;

    public ThongKeController(ThongKeService thongKeService) {
        this.thongKeService = thongKeService;
    }

    @GetMapping
    public String thongKe(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime tuNgay,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime denNgay,

            @RequestParam(required = false) String type,

            Model model) {

        LocalDateTime now = LocalDateTime.now();

        if (type != null) {
            switch (type) {
                case "today":
                    tuNgay = now.toLocalDate().atStartOfDay();
                    denNgay = now;
                    break;
                case "7days":
                    tuNgay = now.minusDays(7);
                    denNgay = now;
                    break;
                case "month":
                    tuNgay = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
                    denNgay = now;
                    break;
                case "year":
                    tuNgay = now.withDayOfYear(1).toLocalDate().atStartOfDay();
                    denNgay = now;
                    break;
            }
        }

        Instant tu = tuNgay != null
                ? tuNgay.atZone(ZoneId.systemDefault()).toInstant()
                : null;

        Instant den = denNgay != null
                ? denNgay.atZone(ZoneId.systemDefault()).toInstant()
                : null;

        BigDecimal doanhThu = thongKeService.getDoanhThu(tu, den);

        List<BigDecimal> dataThang =
                thongKeService.getDoanhThuTheoThang(now.getYear());

        model.addAttribute("doanhThu", doanhThu);
        model.addAttribute("dataThang", dataThang);
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);

        return "ThongKe/form";
    }

    // ================= EXPORT EXCEL =================

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response)
            throws IOException {

        List<BigDecimal> data =
                thongKeService.getDoanhThuTheoThang(
                        LocalDate.now().getYear());

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("ThongKe");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Tháng");
        header.createCell(1).setCellValue("Doanh thu");

        for (int i = 0; i < 12; i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue("Tháng " + (i + 1));
            row.createCell(1).setCellValue(data.get(i).doubleValue());
        }

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        response.setHeader("Content-Disposition",
                "attachment; filename=thong-ke.xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}