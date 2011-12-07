package com.tomakehurst.wiremock.common;

import java.util.Random;

public class VeryShortIdGenerator implements IdGenerator {
    
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789$#&+@!()-{}";

    public String generate() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(randomChar());
        }
        
        return sb.toString();
    }
    
    private static char randomChar() {
        Random random = new Random();
        int index = random.nextInt(CHARS.length());
        return CHARS.charAt(index);
    }
}
