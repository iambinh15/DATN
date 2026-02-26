package org.example.datn_sp26.SanPham.Service;

import org.example.datn_sp26.SanPham.Entity.SanPhamChiTiet;
import org.example.datn_sp26.SanPham.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SanPhamChiTietService {

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private MauSacRepository mauSacRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private ChatLieuRepository chatLieuRepository;

    public List<SanPhamChiTiet> getAll() {
        return sanPhamChiTietRepository.findAll();
    }

    public Optional<SanPhamChiTiet> getById(Integer id) {
        return sanPhamChiTietRepository.findById(id);
    }

    public SanPhamChiTiet save(SanPhamChiTiet sanPhamChiTiet) {
        return sanPhamChiTietRepository.save(sanPhamChiTiet);
    }

    public void deleteById(Integer id) {
        sanPhamChiTietRepository.deleteById(id);
    }

    public List getAllSanPham() {
        return sanPhamRepository.findAll();
    }

    public List getAllMauSac() {
        return mauSacRepository.findByTrangThai(1);
    }

    public List getAllSize() {
        return sizeRepository.findByTrangThai(1);
    }

    public List getAllChatLieu() {
        return chatLieuRepository.findAll();
    }
}
