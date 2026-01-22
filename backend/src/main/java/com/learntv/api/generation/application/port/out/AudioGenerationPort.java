package com.learntv.api.generation.application.port.out;

public interface AudioGenerationPort {
    byte[] generateWav(String text);
    byte[] convertToMp3(byte[] wavData);
}
