package com.jtdev.umak;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StringAnalyzer {

    private final Context context;

    public StringAnalyzer(Context context) {
        this.context = context;
    }

    public String[] getNounsFromAssets() {
        AssetManager assetManager = context.getAssets();
        ArrayList<String> nounsList = new ArrayList<>();
        try {
            InputStream inputStream = assetManager.open("nouns.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split(",");
                for (String word : words) {
                    nounsList.add(word.trim().toLowerCase());
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nounsList.toArray(new String[0]);
    }

    public String[] tokenize(String words) {
        ArrayList<String> tokens = new ArrayList<>();
        for (String word : words.split("\\s+")) {
            String cleanedWord = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
            tokens.add(cleanedWord);
        }
        return tokens.toArray(new String[0]);
    }

    // Method for processing the string, finding nouns, and excluding them from remaining terms
    public String extract_terms_and_definitions(String string, Set<String> nouns) {
        Map<String, String> termsMap = new HashMap<>();

        String[] sentences = string.split("\\.");
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) continue;

            ArrayList<String> tokensExcludingTerm = new ArrayList<>();
            String term = null;

            String[] tokens = tokenize(sentence);

            // Check if the term is "Both"
            if (sentence.contains("Both")) {
                term = "Both";
            } else {
                for (String token : tokens) {
                    if (nouns.contains(token)) {
                        term = token;
                        break;
                    }
                }
            }

            // If the term is "Both", find the two nouns in the sentence
            if ("Both".equalsIgnoreCase(term)) {
                ArrayList<String> foundNouns = new ArrayList<>();
                for (String token : tokens) {
                    if (nouns.contains(token)) {
                        foundNouns.add(token);
                        if (foundNouns.size() == 3) break; // Find two nouns
                    }
                }
                if (foundNouns.size() == 3) {
                    term = "Both " + foundNouns.get(1) + " and the " + foundNouns.get(2);
                }
            }

            // Process tokens excluding the term
            for (String token : tokens) {
                if (term == null || !token.equals(term)) {
                    tokensExcludingTerm.add(token);
                }
            }
            String remainingTerms = String.join(" ", tokensExcludingTerm);

            if (term != null) {
                if(term.toLowerCase().endsWith("s")){
                    term = term.substring(0, term.length() - 1);
                }
                if (termsMap.containsKey(term)) {
                    String existingDefinition = termsMap.get(term);
                    termsMap.put(term, existingDefinition + ", " + remainingTerms + ".");
                } else {
                    termsMap.put(term, remainingTerms);
                }
            } else {
                String noTermLabel = "";
                termsMap.put(noTermLabel, remainingTerms);
            }
        }

        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : termsMap.entrySet()) {
            String definition = entry.getValue();
            if (definition.toLowerCase().startsWith("the ")){
                definition = definition.substring(4); // Remove "The" from the beginning
            }
            if (definition.toLowerCase().startsWith("mighty ")) {
                definition = definition.substring(7);
            }

            result.append(entry.getKey().toUpperCase()).append(": ").append(definition).append("\n");
        }

        return result.toString().trim(); // Remove any trailing newline
    }

}
