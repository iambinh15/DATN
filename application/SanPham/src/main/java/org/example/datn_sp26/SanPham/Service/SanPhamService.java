package org.example.datn_sp26.SanPham.Service;

import org.example.datn_sp26.SanPham.Entity.SanPham;
import org.example.datn_sp26.SanPham.Repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SanPhamService {
    
    @Autowired
    private SanPhamRepository sanPhamRepository;
    
    public List<SanPham> getAllSanPham() {
        return sanPhamRepository.findAll();
    }
    
    public List<SanPham> getSanPhamActive() {
        return sanPhamRepository.findByTrangThai(1);
    }
    
    public Optional<SanPham> getSanPhamById(Integer id) {
        return sanPhamRepository.findById(id);
    }
    
    public SanPham getSanPhamByMa(String ma) {
        return sanPhamRepository.findByMaSanPham(ma);
    }
    
    public List<SanPham> searchSanPham(String keyword) {
        return sanPhamRepository.searchByKeyword(keyword);
    }
    
    @Transactional
    public SanPham saveSanPham(SanPham sanPham) {
        return sanPhamRepository.save(sanPham);
    }
    
    @Transactional
    public void deleteSanPham(Integer id) {
        sanPhamRepository.deleteById(id);
    }
    
    @Transactional
    public void updateTrangThai(Integer id, Integer trangThai) {
        Optional<SanPham> optional = sanPhamRepository.findById(id);
        if (optional.isPresent()) {
            SanPham sanPham = optional.get();
            sanPham.setTrangThai(trangThai);
            sanPhamRepository.save(sanPham);
        }
    }
}
