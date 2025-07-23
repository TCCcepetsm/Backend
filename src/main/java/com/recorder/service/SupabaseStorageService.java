package com.recorder.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service.role.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucketName;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public String uploadFile(MultipartFile file, String folder) throws IOException, InterruptedException {
        // Gerar nome único para o arquivo
        String fileName = folder + "/" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // URL da API do Supabase Storage
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;

        // Criar requisição HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Authorization", "Bearer " + supabaseKey)
                .header("Content-Type", file.getContentType())
                .POST(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                .build();

        // Enviar requisição
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            // Retornar URL pública do arquivo
            return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + fileName;
        } else {
            throw new RuntimeException("Erro ao fazer upload do arquivo: " + response.body());
        }
    }

    public void deleteFile(String fileUrl) throws IOException, InterruptedException {
        // Extrair o caminho do arquivo da URL
        String filePath = fileUrl.replace(supabaseUrl + "/storage/v1/object/public/" + bucketName + "/", "");

        // URL da API do Supabase Storage para deletar
        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath;

        // Criar requisição HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(deleteUrl))
                .header("Authorization", "Bearer " + supabaseKey)
                .DELETE()
                .build();

        // Enviar requisição
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new RuntimeException("Erro ao deletar arquivo: " + response.body());
        }
    }
}
