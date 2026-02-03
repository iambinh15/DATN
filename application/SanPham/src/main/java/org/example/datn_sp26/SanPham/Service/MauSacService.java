package org.example.datn_sp26.SanPham.Service;

import org.example.datn_sp26.SanPham.Repository.MauSacRepository;
import org.example.datn_sp26.SanPham.Entity.MauSac;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MauSacService {
    
    @Autowired
    private MauSacRepository mauSacRepository;
    
    public List<MauSac> getAllMauSac() {
        return mauSacRepository.findAll();
    }
    
    public List<MauSac> getMauSacActive() {
        return mauSacRepository.findByTrangThai(1);
    }
}
