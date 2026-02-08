package org.example.datn_sp26.SanPham.Service;


import org.example.datn_sp26.SanPham.Entity.HinhAnh;
import org.example.datn_sp26.SanPham.Entity.SanPham;
import org.example.datn_sp26.SanPham.Repository.HinhAnhRepository;
import org.example.datn_sp26.SanPham.Repository.SanPhamRepository;
import org.example.datn_sp26.SanPham.Repository.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SanPhamService {

    private final SanPhamRepository sanPhamRepository;
    
    @Autowired
    private ThuongHieuRepository thuongHieuRepository;
    
    @Autowired
    private HinhAnhRepository hinhAnhRepository;
    
    // Sử dụng đường dẫn tuyệt đối dựa trên user.dir để đảm bảo hoạt động trên mọi môi trường
    private final Path rootLocation = Paths.get(System.getProperty("user.dir") + "/uploads/images");

    public SanPhamService(SanPhamRepository sanPhamRepository) {
        this.sanPhamRepository = sanPhamRepository;
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public List<SanPham> getAllSanPhamActive() {
        return sanPhamRepository.findByTrangThai(1); // chỉ lấy sản phẩm đang hiển thị
    }
    
    // Admin methods
    public List<SanPham> getAll() {
        return sanPhamRepository.findAll();
    }

    public Optional<SanPham> getById(Integer id) {
        return sanPhamRepository.findById(id);
    }

    public SanPham save(SanPham sanPham) {
        return sanPhamRepository.save(sanPham);
    }

    public void deleteById(Integer id) {
        sanPhamRepository.deleteById(id);
    }

    public List getAllThuongHieu() {
        return thuongHieuRepository.findByTrangThai(1);
    }
    
    public String saveImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }
        
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path destinationFile = this.rootLocation.resolve(
                Paths.get(fileName))
                .normalize().toAbsolutePath();
                
        Files.copy(file.getInputStream(), destinationFile);
        
        return fileName;
    }
    
    public void saveProductWithImage(SanPham sanPham, MultipartFile file) throws IOException {
        SanPham savedProduct = sanPhamRepository.save(sanPham);
        
        if (file != null && !file.isEmpty()) {
            // Tìm ảnh cũ của sản phẩm này
            List<HinhAnh> hinhAnhCu = hinhAnhRepository.findByIdSanPham_Id(savedProduct.getId());
            
            // Xóa file ảnh cũ khỏi thư mục nếu có
            if (!hinhAnhCu.isEmpty()) {
                String oldFileName = hinhAnhCu.get(0).getHinhAnh();
                if (oldFileName != null) {
                    try {
                        Path oldFile = this.rootLocation.resolve(oldFileName);
                        Files.deleteIfExists(oldFile);
                    } catch (IOException e) {
                        // Ignore nếu không xóa được file cũ
                    }
                }
            }
            
            // Lưu file ảnh mới
            String fileName = saveImage(file);
            
            // Update hoặc tạo mới record HinhAnh
            HinhAnh hinhAnh;
            if (!hinhAnhCu.isEmpty()) {
                // Update ảnh cũ
                hinhAnh = hinhAnhCu.get(0);
                hinhAnh.setHinhAnh(fileName);
            } else {
                // Tạo mới nếu chưa có
                hinhAnh = new HinhAnh();
                hinhAnh.setIdSanPham(savedProduct);
                hinhAnh.setHinhAnh(fileName);
                hinhAnh.setTrangThai(1);
            }
            hinhAnhRepository.save(hinhAnh);
        }
    }
}
