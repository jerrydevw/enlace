package com.enlace.domain.port.in;

import java.util.UUID;

public interface ManageEventPhotoUseCase {

    /** Faz upload de uma foto e salva a chave S3 no evento. */
    void upload(UploadPhotoCommand command);

    /** Remove uma foto do S3 e limpa a chave no evento. */
    void delete(DeletePhotoCommand command);

    record UploadPhotoCommand(
        UUID eventId,
        UUID customerId,
        int index,
        byte[] bytes,
        String contentType
    ) {}

    record DeletePhotoCommand(
        UUID eventId,
        UUID customerId,
        int index
    ) {}
}
