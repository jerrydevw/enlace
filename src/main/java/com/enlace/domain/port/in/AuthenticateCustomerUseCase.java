package com.enlace.domain.port.in;

import com.enlace.domain.model.Customer;
import java.util.UUID;

public interface AuthenticateCustomerUseCase {
    
    Customer register(RegisterCommand command);
    
    LoginResult login(LoginCommand command);
    
    record RegisterCommand(String name, String email, String password) {}
    
    record LoginCommand(String email, String password) {}
    
    record LoginResult(String accessToken, String refreshToken, Customer customer) {}
}
