package org.example.datn_sp26.NguoiDung.Service;

import org.example.datn_sp26.NguoiDung.Entity.DiaChi;
import org.example.datn_sp26.NguoiDung.Repository.DiaChiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiaChiService {

    @Autowired
    private DiaChiRepository diaChiRepository;

    public List<DiaChi> layDiaChiCuaKhach(Integer idKhachHang) {
        return diaChiRepository.findByIdKhachHang_Id(idKhachHang);
    }

    public DiaChi layDiaChiMacDinh(Integer idKhachHang) {
        return diaChiRepository
                .findByIdKhachHang_IdAndTrangThai(idKhachHang, 1)
                .orElse(null);
    }
}
