package com.catering.util;

import java.util.HashMap;
import java.util.Map;

/**
 * DFA 敏感词过滤
 */
public class DfaFilter {

    private final Map<Character, Object> root = new HashMap<>();

    public void addWord(String word) {
        if (word == null || word.isEmpty()) return;
        Map<Character, Object> node = root;
        for (char c : word.toCharArray()) {
            node = (Map<Character, Object>) node.computeIfAbsent(c, k -> new HashMap<Character, Object>());
        }
        node.put('\0', Boolean.TRUE);
    }

    public String findFirst(String text) {
        if (text == null || text.isEmpty()) return null;
        for (int i = 0; i < text.length(); i++) {
            Map<Character, Object> node = root;
            int j = i;
            while (j < text.length()) {
                node = (Map<Character, Object>) node.get(text.charAt(j));
                if (node == null) break;
                if (node.containsKey('\0')) {
                    return text.substring(i, j + 1);
                }
                j++;
            }
        }
        return null;
    }

    public boolean contains(String text) {
        return findFirst(text) != null;
    }
}
