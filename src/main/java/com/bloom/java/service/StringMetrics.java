package com.bloom.java.service;


// Really just a string comparison class
// TODO: Measure quality of response and explore phonetic comparators
public class StringMetrics {
    
    public static int levenshteinDistance(String s, String t) {
        int lenS = s.length();
        int lenT = t.length();
        int[][] dist = new int[lenS + 1][lenT + 1];

        // Initialize the distance matrix
        for (int i = 0; i <= lenS; i++) {
            dist[i][0] = i;
        }
        for (int j = 0; j <= lenT; j++) {
            dist[0][j] = j;
        }

        // Compute the Levenshtein distance
        for (int i = 1; i <= lenS; i++) {
            for (int j = 1; j <= lenT; j++) {
                int cost = (s.charAt(i - 1) == t.charAt(j - 1)) ? 0 : 1;

                dist[i][j] = Math.min(
                        Math.min(dist[i - 1][j] + 1, dist[i][j - 1] + 1),
                        dist[i - 1][j - 1] + cost
                );
            }
        }

        // Return the Levenshtein distance
        return dist[lenS][lenT];
    }
}