package com.learntv.api.learning.application.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Looks up IPA phonemes for English words using the CMU Pronouncing Dictionary.
 * The dictionary maps words to ARPAbet symbols, which are then converted to IPA.
 */
@Service
@Slf4j
public class PhonemeService {

    private final Map<String, List<String>> dictionary = new HashMap<>();

    private static final Map<String, String> ARPABET_TO_IPA = Map.ofEntries(
            Map.entry("AA", "ɑː"),
            Map.entry("AE", "æ"),
            Map.entry("AH0", "ə"),   // unstressed → schwa
            Map.entry("AH", "ʌ"),    // stressed
            Map.entry("AO", "ɔː"),
            Map.entry("AW", "aʊ"),
            Map.entry("AY", "aɪ"),
            Map.entry("B", "b"),
            Map.entry("CH", "tʃ"),
            Map.entry("D", "d"),
            Map.entry("DH", "ð"),
            Map.entry("EH", "ɛ"),
            Map.entry("ER", "ɜː"),
            Map.entry("EY", "eɪ"),
            Map.entry("F", "f"),
            Map.entry("G", "g"),
            Map.entry("HH", "h"),
            Map.entry("IH", "ɪ"),
            Map.entry("IY", "iː"),
            Map.entry("JH", "dʒ"),
            Map.entry("K", "k"),
            Map.entry("L", "l"),
            Map.entry("M", "m"),
            Map.entry("N", "n"),
            Map.entry("NG", "ŋ"),
            Map.entry("OW", "əʊ"),
            Map.entry("OY", "ɔɪ"),
            Map.entry("P", "p"),
            Map.entry("R", "r"),
            Map.entry("S", "s"),
            Map.entry("SH", "ʃ"),
            Map.entry("T", "t"),
            Map.entry("TH", "θ"),
            Map.entry("UH", "ʊ"),
            Map.entry("UW", "uː"),
            Map.entry("V", "v"),
            Map.entry("W", "w"),
            Map.entry("Y", "j"),
            Map.entry("Z", "z"),
            Map.entry("ZH", "ʒ")
    );

    @PostConstruct
    void loadDictionary() {
        try {
            var resource = new ClassPathResource("cmudict.txt");
            try (var reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank() || line.startsWith(";;;")) continue;

                    int space = line.indexOf(' ');
                    if (space < 0) continue;

                    String word = line.substring(0, space).toLowerCase();
                    // Skip alternate pronunciations like "a(2)"
                    int paren = word.indexOf('(');
                    if (paren > 0) continue;

                    String[] arpabetTokens = line.substring(space).trim().split("\\s+");
                    List<String> ipaPhonemes = new ArrayList<>();

                    for (String token : arpabetTokens) {
                        String ipa = convertArpabetToIpa(token);
                        if (ipa != null && !ipaPhonemes.contains(ipa)) {
                            ipaPhonemes.add(ipa);
                        }
                    }

                    if (!ipaPhonemes.isEmpty()) {
                        dictionary.put(word, List.copyOf(ipaPhonemes));
                    }
                }
            }
            log.info("Loaded CMU dictionary with {} words", dictionary.size());
        } catch (Exception e) {
            log.error("Failed to load CMU dictionary: {}", e.getMessage());
        }
    }

    /**
     * Look up the unique IPA phonemes for a word or phrase.
     * For multi-word phrases, phonemes from all words are combined (deduplicated).
     */
    public List<String> lookup(String text) {
        if (text == null || text.isBlank()) return List.of();

        String[] words = text.toLowerCase().replaceAll("[^a-z\\s']", "").trim().split("\\s+");
        LinkedHashSet<String> phonemes = new LinkedHashSet<>();

        for (String word : words) {
            List<String> wordPhonemes = dictionary.get(word);
            if (wordPhonemes != null) {
                phonemes.addAll(wordPhonemes);
            }
        }

        return List.copyOf(phonemes);
    }

    private String convertArpabetToIpa(String arpabet) {
        // Check for AH0 specifically (schwa) before stripping stress
        if (arpabet.equals("AH0")) {
            return ARPABET_TO_IPA.get("AH0");
        }

        // Strip stress digits (0, 1, 2) from vowels
        String base = arpabet.replaceAll("[012]$", "");
        return ARPABET_TO_IPA.get(base);
    }
}
