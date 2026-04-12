package com.enlace.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoupleStory {
    private String partner1Name;   // max 100 chars, nullable
    private String partner2Name;   // max 100 chars, nullable
    private String message;        // max 400 chars, nullable
    private String photoKey1;      // S3 key, nullable
    private String photoKey2;      // S3 key, nullable
    private String photoKey3;      // S3 key, nullable

    /** Construtor sem fotos para compatibilidade */
    public CoupleStory(String partner1Name, String partner2Name, String message) {
        this.partner1Name = partner1Name;
        this.partner2Name = partner2Name;
        this.message = message;
    }

    /** Retorna true se ao menos um campo de texto está preenchido */
    public boolean hasContent() {
        return (partner1Name != null && !partner1Name.isBlank())
            || (partner2Name != null && !partner2Name.isBlank())
            || (message != null && !message.isBlank())
            || hasPhotos();
    }

    /** Retorna true se há ao menos uma foto */
    public boolean hasPhotos() {
        return isNotBlank(photoKey1) || isNotBlank(photoKey2) || isNotBlank(photoKey3);
    }

    /** Retorna os índices (1, 2, 3) que têm foto, em ordem */
    public List<Integer> getPhotoIndices() {
        List<Integer> indices = new ArrayList<>();
        if (isNotBlank(photoKey1)) indices.add(1);
        if (isNotBlank(photoKey2)) indices.add(2);
        if (isNotBlank(photoKey3)) indices.add(3);
        return indices;
    }

    /** Retorna a chave S3 para o índice dado (1–3), ou null */
    public String getPhotoKey(int index) {
        return switch (index) {
            case 1 -> photoKey1;
            case 2 -> photoKey2;
            case 3 -> photoKey3;
            default -> null;
        };
    }

    /** Define a chave S3 para o índice dado (1–3) */
    public void setPhotoKey(int index, String key) {
        switch (index) {
            case 1 -> photoKey1 = key;
            case 2 -> photoKey2 = key;
            case 3 -> photoKey3 = key;
        }
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}
