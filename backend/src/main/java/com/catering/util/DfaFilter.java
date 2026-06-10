package com.catering.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * 找出文本中所有命中的敏感词（去重，按出现顺序）
     */
    public List<String> findAll(String text) {
        if (text == null || text.isEmpty()) return Collections.emptyList();
        Set<String> hits = new LinkedHashSet<>();
        for (int i = 0; i < text.length(); i++) {
            Map<Character, Object> node = root;
            int j = i;
            while (j < text.length()) {
                node = (Map<Character, Object>) node.get(text.charAt(j));
                if (node == null) break;
                if (node.containsKey('\0')) {
                    hits.add(text.substring(i, j + 1));
                }
                j++;
            }
        }
        return new ArrayList<>(hits);
    }
}
