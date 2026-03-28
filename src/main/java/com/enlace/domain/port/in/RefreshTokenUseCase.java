package com.enlace.domain.port.in;

public interface RefreshTokenUseCase {
    
    RefreshResult refresh(RefreshCommand command);
    
    record RefreshCommand(String refreshToken) {}
    
    record RefreshResult(String accessToken) {}
}
