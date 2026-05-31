package com.pms.clinicalservice.service.storage;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@ConditionalOnProperty(name = "pdf.storage.type", havingValue = "filesystem", matchIfMissing = true)
public class FileSystemStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(FileSystemStorageService.class);

    private final Path storagePath;

    public FileSystemStorageService(@Value("${pdf.storage.path}") String path) {
        this.storagePath = Path.of(path);
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory: " + storagePath, e);
        }
    }

    @Override
    public void store(String key, byte[] data) {
        try {
            Files.write(storagePath.resolve(key), data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + key, e);
        }
    }

    @Override
    public byte[] retrieve(String key) {
        try {
            return Files.readAllBytes(storagePath.resolve(key));
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve file: " + key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        return Files.exists(storagePath.resolve(key));
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(storagePath.resolve(key));
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", key, e);
        }
    }
}
