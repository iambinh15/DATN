package org.example.datn_sp26.SanPham.Service;

import org.example.datn_sp26.SanPham.Entity.MauSac;
import org.example.datn_sp26.SanPham.Repository.MauSacRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MauSacService {

    private final MauSacRepository repository;

    public MauSacService(MauSacRepository repository) {
        this.repository = repository;
    }

    public List<MauSac> findAll() {
        return repository.findAll();
    }

    public MauSac findById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public void save(MauSac mauSac) {
        repository.save(mauSac);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
