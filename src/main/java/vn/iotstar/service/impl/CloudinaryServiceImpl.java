package vn.iotstar.service.impl;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.CloudinaryUploadResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;

    @Override
    public CloudinaryUploadResult upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn file hình ảnh cho sản phẩm.");
        }

        // Lấy phần mở rộng của file
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        // Kiểm tra loại file hợp lệ
        boolean isImage = (file.getContentType() != null && file.getContentType().toLowerCase().startsWith("image/"))
                || ext.matches("\\.(jpg|jpeg|png|gif|webp|bmp)");

        if (!isImage) {
            throw new IllegalArgumentException("Chỉ chấp nhận các định dạng hình ảnh (.jpg, .png, .webp, .jpeg).");
        }

        // 1. Luôn lưu bản sao cục bộ vào static/uploads để đảm bảo ảnh luôn hiển thị được ngay lập tức
        String savedFileName = UUID.randomUUID().toString() + (ext.isBlank() ? ".jpg" : ext);
        String localUrl = "/uploads/" + savedFileName;
        try {
            saveLocal(file, savedFileName);
        } catch (Exception e) {
            System.err.println("Không thể lưu ảnh cục bộ: " + e.getMessage());
        }

        // 2. Thử tải lên Cloudinary nếu có cấu hình Cloudinary hợp lệ
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of("folder", "shop/products")
            );
            String secureUrl = String.valueOf(result.get("secure_url"));
            String publicId = String.valueOf(result.get("public_id"));
            System.out.println(">>> ĐÃ UPLOAD LÊN CLOUDINARY THÀNH CÔNG: " + secureUrl);
            return new CloudinaryUploadResult(secureUrl, publicId);
        } catch (Exception e) {
            System.err.println("Upload Cloudinary không thành công (" + e.getMessage() + ") -> Sử dụng đường dẫn ảnh cục bộ: " + localUrl);
            return new CloudinaryUploadResult(localUrl, "local_" + savedFileName);
        }
    }

    private void saveLocal(MultipartFile file, String fileName) throws IOException {
        String[] locations = {
                "src/main/resources/static/uploads",
                "target/classes/static/uploads"
        };
        for (String loc : locations) {
            Path dir = Paths.get(loc);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path target = dir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) return;

        // Xóa file cục bộ nếu là ảnh local
        if (publicId.startsWith("local_")) {
            String fileName = publicId.substring("local_".length());
            try {
                Files.deleteIfExists(Paths.get("src/main/resources/static/uploads").resolve(fileName));
                Files.deleteIfExists(Paths.get("target/classes/static/uploads").resolve(fileName));
            } catch (Exception ignored) {}
            return;
        }

        // Xóa trên Cloudinary
        try {
            cloudinary.uploader().destroy(publicId, Map.of("resource_type", "image"));
        } catch (Exception e) {
            System.err.println("Xóa ảnh Cloudinary thất bại: " + e.getMessage());
        }
    }
}
