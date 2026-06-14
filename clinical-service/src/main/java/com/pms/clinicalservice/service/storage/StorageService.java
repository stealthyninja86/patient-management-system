package com.pms.clinicalservice.service.storage;

public interface StorageService {
    void store(String key, byte[] data);
    byte[] retrieve(String key);
    boolean exists(String key);
    void delete(String key);
}
