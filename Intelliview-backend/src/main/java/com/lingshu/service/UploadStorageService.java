package com.lingshu.service;

import com.lingshu.exception.BusinessException;
import com.lingshu.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadStorageService {

    private static final Set<String> AVATAR_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");
    private static final Set<String> RESUME_EXTENSIONS = Set.of(".pdf", ".docx", ".txt", ".md");
    private final Path uploadRoot = Paths.get("uploads").toAbsolutePath().normalize();

    public StoredFile storeAvatar(Long userId, MultipartFile file) {
        return store("avatars/" + userId, file, AVATAR_EXTENSIONS);
    }

    public StoredFile storeResume(Long userId, MultipartFile file) {
        return store("resumes/" + userId, file, RESUME_EXTENSIONS);
    }

    public Path getUploadRoot() {
        return uploadRoot;
    }

    public void deleteStoredFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl) || !fileUrl.startsWith("/uploads/")) {
            return;
        }
        try {
            String relativePath = fileUrl.substring("/uploads/".length());
            Path target = uploadRoot.resolve(relativePath).normalize();
            if (target.startsWith(uploadRoot)) {
                Files.deleteIfExists(target);
            }
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "文件删除失败");
        }
    }

    private StoredFile store(String relativeDir, MultipartFile file, Set<String> allowedExtensions) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "上传文件不能为空");
        }
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());
        String extension = extensionOf(originalName);
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "不支持的文件格式");
        }
        try {
            Path dir = uploadRoot.resolve(relativeDir).normalize();
            if (!dir.startsWith(uploadRoot)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "上传路径不合法");
            }
            Files.createDirectories(dir);
            String storedName = UUID.randomUUID().toString().replace("-", "") + extension;
            Path target = dir.resolve(storedName).normalize();
            file.transferTo(target);
            String url = "/uploads/" + relativeDir.replace("\\", "/") + "/" + storedName;
            return new StoredFile(storedName, originalName, url, extension.substring(1), file.getSize());
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "文件保存失败");
        }
    }

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot).toLowerCase(Locale.ROOT) : "";
    }

    public record StoredFile(String fileName, String originalFileName, String url, String fileType, Long fileSize) {
    }
}
