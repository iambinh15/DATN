package org.example.datn_sp26.SanPham.Service;

import org.example.datn_sp26.SanPham.Entity.Size;
import org.example.datn_sp26.SanPham.Repository.SizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SizeService {
    
    @Autowired
    private SizeRepository sizeRepository;
    
    public List<Size> getAllSize() {
        return sizeRepository.findAll();
    }
    
    public List<Size> getSizeActive() {
        return sizeRepository.findByTrangThai(1);
    }
}
