package org.example.datn_sp26.BanHang.Service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.datn_sp26.BanHang.Entity.HoaDon;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class HoaDonExcelExporter {

    private static final String[] HEADERS = {
            "ID", "Mã hóa đơn", "Nhân viên", "Khách hàng",
            "Mã giảm giá", "Loại thanh toán", "Trạng thái",
            "Địa chỉ", "Ngày tạo", "Tổng thanh toán"
    };

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    private static final DecimalFormat MONEY_FMT = new DecimalFormat("#,##0");

    public void export(List<HoaDon> danhSach, HttpServletResponse response) throws IOException {
        // Setup response headers
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=DanhSachHoaDon.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Hóa đơn");

            // ===== HEADER STYLE =====
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // ===== DATA STYLE =====
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // ===== MONEY STYLE =====
            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.cloneStyleFrom(dataStyle);
            moneyStyle.setAlignment(HorizontalAlignment.RIGHT);
            Font moneyFont = workbook.createFont();
            moneyFont.setBold(true);
            moneyFont.setColor(IndexedColors.RED.getIndex());
            moneyStyle.setFont(moneyFont);

            // ===== WRITE HEADER ROW =====
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // ===== WRITE DATA ROWS =====
            int rowIdx = 1;
            for (HoaDon hd : danhSach) {
                Row row = sheet.createRow(rowIdx++);

                createCell(row, 0, hd.getId() != null ? hd.getId().toString() : "", dataStyle);
                createCell(row, 1, hd.getMaHoaDon() != null ? hd.getMaHoaDon() : "", dataStyle);
                createCell(row, 2, hd.getNhanVien() != null ? hd.getNhanVien().getTenNhanVien() : "—", dataStyle);
                createCell(row, 3, hd.getIdKhachHang() != null ? hd.getIdKhachHang().getTenKhachHang() : "—",
                        dataStyle);
                createCell(row, 4, hd.getIdMaGiamGia() != null ? hd.getIdMaGiamGia().getMa() : "Không có", dataStyle);
                createCell(row, 5, hd.getIdLoaiThanhToan() != null ? hd.getIdLoaiThanhToan().getTenLoai() : "—",
                        dataStyle);
                createCell(row, 6,
                        hd.getIdTrangThaiHoaDon() != null ? hd.getIdTrangThaiHoaDon().getTenTrangThai() : "—",
                        dataStyle);
                createCell(row, 7, hd.getDiaChi() != null ? hd.getDiaChi() : "—", dataStyle);
                createCell(row, 8, hd.getNgayTao() != null ? DATE_FMT.format(hd.getNgayTao()) : "—", dataStyle);

                String tien = hd.getTongThanhToan() != null
                        ? MONEY_FMT.format(hd.getTongThanhToan()) + " ₫"
                        : "—";
                createCell(row, 9, tien, moneyStyle);
            }

            // ===== AUTO-SIZE COLUMNS =====
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
                // Add a little extra padding
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512);
            }

            workbook.write(response.getOutputStream());
        }
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
