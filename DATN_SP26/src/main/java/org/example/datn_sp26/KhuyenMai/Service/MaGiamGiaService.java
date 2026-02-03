package org.example.datn_sp26.KhuyenMai.Service;

import lombok.RequiredArgsConstructor;
import org.example.datn_sp26.KhuyenMai.Entity.MaGiamGia;
import org.example.datn_sp26.KhuyenMai.Repository.MaGiamGiaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaGiamGiaService {

    private final MaGiamGiaRepository maGiamGiaRepository;

    public List<MaGiamGia> layMaDangHoatDong() {
        return maGiamGiaRepository.findByTrangThai(1);
    }
}
