package org.example.datn_sp26.SanPham.Service;

import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SanPhamChiTietService {
    
    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;
    
    public List<SanPhamChiTiet> getAllSanPhamChiTiet() {
        return sanPhamChiTietRepository.findAll();
    }
    
    public List<SanPhamChiTiet> getSanPhamChiTietActive() {
        return sanPhamChiTietRepository.findByTrangThai(1);
    }
    
    public Optional<SanPhamChiTiet> getSanPhamChiTietById(Integer id) {
        return sanPhamChiTietRepository.findById(id);
    }
    
    public List<SanPhamChiTiet> getChiTietBySanPhamId(Integer sanPhamId) {
        return sanPhamChiTietRepository.findBySanPhamId(sanPhamId);
    }
    
    public List<SanPhamChiTiet> getActiveChiTietBySanPhamId(Integer sanPhamId) {
        return sanPhamChiTietRepository.findActiveBySanPhamId(sanPhamId);
    }
    
    @Transactional
    public SanPhamChiTiet saveSanPhamChiTiet(SanPhamChiTiet sanPhamChiTiet) {
        return sanPhamChiTietRepository.save(sanPhamChiTiet);
    }
    
    @Transactional
    public void deleteSanPhamChiTiet(Integer id) {
        sanPhamChiTietRepository.deleteById(id);
    }
    
    @Transactional
    public void updateTrangThai(Integer id, Integer trangThai) {
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findById(id);
        if (optional.isPresent()) {
            SanPhamChiTiet chiTiet = optional.get();
            chiTiet.setTrangThai(trangThai);
            sanPhamChiTietRepository.save(chiTiet);
        }
    }
    
    @Transactional
    public void updateSoLuong(Integer id, Integer soLuong) {
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findById(id);
        if (optional.isPresent()) {
            SanPhamChiTiet chiTiet = optional.get();
            chiTiet.setSoLuong(soLuong);
            sanPhamChiTietRepository.save(chiTiet);
        }
    }
}
