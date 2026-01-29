package com.learntv.api.generation.application.port.out;

public interface AudioStoragePort {
    String upload(String key, byte[] data, String contentType);
    void delete(String key);
    String getPublicUrl(String key);
}
