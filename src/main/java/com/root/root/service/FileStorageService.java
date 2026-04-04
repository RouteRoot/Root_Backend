package com.root.root.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp", "JPG", "JPEG", "PNG");

    //이미지 파일 목록을 로컬 디렉토리에 저장하고 URL 목록을 반환
    public List<String> storeImages(List<MultipartFile> images) {
        if (images.size() > 5) {
            throw new IllegalArgumentException("이미지는 최대 5장까지 첨부 가능합니다.");
        }

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile image : images) {
            if (image.isEmpty()) continue;

            String originalFilename = image.getOriginalFilename();
            String extension = getExtension(originalFilename);

            if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + extension);
            }

            // UUID로 고유한 파일명 생성 (파일명 중복 방지)
            String savedFileName = UUID.randomUUID() + "." + extension;
            Path savePath = Paths.get(uploadDir, savedFileName);

            try {
                // 디렉토리 없으면 자동 생성
                Files.createDirectories(savePath.getParent());
                image.transferTo(savePath.toFile());
            } catch (IOException e) {
                throw new RuntimeException("파일 저장에 실패했습니다.", e);
            }

            imageUrls.add("/images/" + savedFileName);
        }

        return imageUrls;
    }

    // 로컬 디렉토리에서 이미지 파일 삭제
     public void deleteImage(String imageUrl) {
        String fileName = imageUrl.replace("/images/", "");
        Path filePath = Paths.get(uploadDir, fileName);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제에 실패했습니다.", e);
        }
    }

    // 파일명에서 확장자 추출
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("올바르지 않은 파일명입니다.");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}