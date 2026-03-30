package org.example.datn_sp26.SanPham.Service;

import org.example.datn_sp26.SanPham.Entity.Size;
import org.example.datn_sp26.SanPham.Repository.SizeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SizeService {

    private final SizeRepository repository;

    public SizeService(SizeRepository repository) {
        this.repository = repository;
    }

    public List<Size> findAll() {
        return repository.findAll();
    }

    public Size findById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public void save(Size size) {
        repository.save(size);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
