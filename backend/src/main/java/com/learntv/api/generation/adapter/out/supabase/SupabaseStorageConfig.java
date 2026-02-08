package com.learntv.api.generation.adapter.out.supabase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile({"production", "local-supabase"})
public class SupabaseStorageConfig {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${supabase.storage.bucket-name}")
    private String bucketName;

    @Bean
    public WebClient supabaseStorageWebClient() {
        return WebClient.builder()
                .baseUrl(supabaseUrl + "/storage/v1")
                .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
                .build();
    }

    public String getSupabaseUrl() {
        return supabaseUrl;
    }

    public String getBucketName() {
        return bucketName;
    }
}
