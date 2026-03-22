package com.enlace.shared;

import java.text.Normalizer;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugGenerator {
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String generate(String title, Instant scheduledAt) {
        String nowhitespace = WHITESPACE.matcher(title).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        
        int year = scheduledAt.atZone(ZoneId.of("UTC")).getYear();
        
        return slug.toLowerCase(Locale.ENGLISH) + "-" + year;
    }
}
