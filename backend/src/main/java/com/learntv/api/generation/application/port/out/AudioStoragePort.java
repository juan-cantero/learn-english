package com.learntv.api.generation.application.port.out;

public interface AudioStoragePort {
    void upload(String key, byte[] data, String contentType);
    String getPublicUrl(String key);
}
