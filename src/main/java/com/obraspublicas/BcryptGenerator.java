package com.obraspublicas;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin123";
        String encoded = encoder.encode(rawPassword);
        System.out.println("BCRYPT_HASH:" + encoded);
        System.out.println("VERIFY:" + encoder.matches(rawPassword, encoded));
    }
}
