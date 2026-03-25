package com.enlace.shared;

import java.security.SecureRandom;

public class InviteCodeGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public String generate() {
        char a = LETTERS.charAt(RANDOM.nextInt(26));
        char b = LETTERS.charAt(RANDOM.nextInt(26));
        char c = LETTERS.charAt(RANDOM.nextInt(26));
        int digits = RANDOM.nextInt(10000);
        return String.format("%c%c%c-%04d", a, b, c, digits);
    }
}
