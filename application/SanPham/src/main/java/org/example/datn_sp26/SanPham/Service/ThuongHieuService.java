package org.example.datn_sp26.SanPham.Service;

import org.example.datn_sp26.SanPham.Entity.ThuongHieu;
import org.example.datn_sp26.SanPham.Repository.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThuongHieuService {
    
    @Autowired
    private ThuongHieuRepository thuongHieuRepository;
    
    public List<ThuongHieu> getAllThuongHieu() {
        return thuongHieuRepository.findAll();
    }
    
    public List<ThuongHieu> getThuongHieuActive() {
        return thuongHieuRepository.findByTrangThai(1);
    }
}
