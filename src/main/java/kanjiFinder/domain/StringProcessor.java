package kanjiFinder.domain;

import java.util.*;

public class StringProcessor {
    private static final String REGEX_DETECT_KANJI = "([一-龯])";
    private static final String REGEX_SENTENCE_SEPERATORS = "[「」『』\"'.,!?．。！？…︒︕︖︙\n]";

    public StringProcessor() {
    }

    public Set<String> extractUniqueKanji(String text) {
        Set<String> uniqueKanji = new HashSet<>();
        for(int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            String charAsString = Character.toString(c);
            if(isKanji(charAsString)) {
                uniqueKanji.add(Character.toString(c));
            }
        }
        return uniqueKanji;
    }

    private boolean isKanji(String string) {
        return string.matches(REGEX_DETECT_KANJI);
    }

    public Map<String,String> extractKanjiWithSentences(String text, Set<String> uniqueKanji) {
        Map<String,String> kanjiSentence = new HashMap<>();
        String[] sentences = text.split(REGEX_SENTENCE_SEPERATORS);

        for(String sentence : sentences) {
            for(int i=0; i<sentence.length(); i++) {
                char c = sentence.charAt(i);
                String charAsString = Character.toString(c);
                if(uniqueKanji.contains(charAsString)) {
                    kanjiSentence.put(charAsString,sentence);
                }
            }
        }
        return kanjiSentence;
    }

}
