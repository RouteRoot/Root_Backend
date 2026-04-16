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

    public List<String> storeImages(List<MultipartFile> images) {
        // 파일 개수 제한 (최대 5장)
        if (images.size() > 5) {
            throw new IllegalArgumentException("이미지는 최대 5장까지 첨부 가능합니다.");
        }

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile image : images) {
            // 빈 파일은 무시
            if (image.isEmpty()) continue;

            // 파일명 및 확장자 추출
            String originalFilename = image.getOriginalFilename();
            String extension = getExtension(originalFilename);

            // 허용된 확장자인지 검증
            if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + extension);
            }

            // UUID로 고유 파일명 생성 (중복 방지)
            String savedFileName = UUID.randomUUID() + "." + extension;
            Path savePath = Paths.get(uploadDir, savedFileName);

            try {
                // 디렉토리 없으면 생성 후 파일 저장
                Files.createDirectories(savePath.getParent());
                image.transferTo(savePath.toFile());
            } catch (IOException e) {
                throw new RuntimeException("파일 저장에 실패했습니다.", e);
            }

            // 접근 가능한 URL 형태로 변환하여 저장
            imageUrls.add("/images/" + savedFileName);
        }

        return imageUrls;
    }

    public String storeSingleImage(MultipartFile image) {
        // 파일 비어있는지 검증
        if (image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어있습니다.");
        }

        // 파일명 및 확장자 추출
        String originalFilename = image.getOriginalFilename();
        String extension = getExtension(originalFilename);

        // 허용된 확장자인지 검증
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + extension);
        }

        // UUID로 고유 파일명 생성 (중복 방지)
        String savedFileName = UUID.randomUUID() + "." + extension;
        Path savePath = Paths.get(uploadDir, savedFileName);

        try {
            // 디렉토리 없으면 생성 후 파일 저장
            Files.createDirectories(savePath.getParent());
            image.transferTo(savePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }

        // 저장된 파일의 URL 반환
        return "/images/" + savedFileName;
    }

    public void deleteImage(String imageUrl) {
        // URL에서 파일명 추출
        String fileName = imageUrl.replace("/images/", "");
        Path filePath = Paths.get(uploadDir, fileName);

        try {
            // 파일 존재 시 삭제
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제에 실패했습니다.", e);
        }
    }

    private String getExtension(String filename) {
        // 파일명 유효성 검사
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("올바르지 않은 파일명입니다.");
        }

        // 확장자 반환
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}