package com.sharmaji.spideystream.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdParser {

    public static String extractImdbId(String url) {
        // Regular expression pattern to match IMDb movie ID
        Pattern pattern = Pattern.compile("/title/(tt\\d+)/");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1); // Return the matched group (movie ID)
        } else {
            return null; // Return null if no match found
        }
    }

    public static String extractTmdbId(String url) {
        // Regular expression pattern to match TMDB movie or series ID
        Pattern pattern = Pattern.compile("/(movie|tv)/(\\d+)-");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(2); // Return the second matched group (ID)
        } else {
            return null; // Return null if no match found
        }
    }
}

