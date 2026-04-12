package com.enlace.domain.port.in;

public interface ServeEventPhotoUseCase {

    PhotoResult serve(String slug, int index);

    record PhotoResult(
        byte[] bytes,
        String contentType
    ) {}
}
