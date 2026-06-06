package com.catering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.common.BusinessException;
import com.catering.entity.SensitiveWord;
import com.catering.mapper.SensitiveWordMapper;
import com.catering.util.DfaFilter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class SensitiveWordService {

    /** 内置营销/引流类敏感词，审核时额外扫描 */
    private static final List<String> BUILTIN_SPAM_WORDS = Arrays.asList(
            "微信", "加微信", "微信号", "优惠券", "扫码", "兼职", "返利",
            "转账", "贷款", "办证", "代理", "vx", "VX", "威信", "qq", "QQ"
    );

    private final SensitiveWordMapper sensitiveWordMapper;
    private volatile DfaFilter dfa = new DfaFilter();

    public SensitiveWordService(SensitiveWordMapper sensitiveWordMapper) {
        this.sensitiveWordMapper = sensitiveWordMapper;
    }

    @PostConstruct
    public void load() {
        reload();
    }

    public synchronized void reload() {
        DfaFilter filter = new DfaFilter();
        List<SensitiveWord> list = sensitiveWordMapper.selectList(
                new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getStatus, 1));
        for (SensitiveWord w : list) {
            filter.addWord(w.getContent());
        }
        this.dfa = filter;
    }

    public boolean containsSensitiveWord(String text) {
        return dfa.contains(text);
    }

    public String findHit(String text) {
        List<String> hits = findAllHits(text);
        return hits.isEmpty() ? null : hits.get(0);
    }

    /**
     * 扫描文本中所有敏感词（词库 + 内置引流词）
     */
    public List<String> findAllHits(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }
        Set<String> hits = new LinkedHashSet<>(dfa.findAll(text));
        String lower = text.toLowerCase();
        for (String word : BUILTIN_SPAM_WORDS) {
            if (text.contains(word) || lower.contains(word.toLowerCase())) {
                hits.add(word);
            }
        }
        return new ArrayList<>(hits);
    }

    /**
     * 格式化敏感词命中结果，供审核列表展示
     */
    public String formatHits(String text) {
        List<String> hits = findAllHits(text);
        return hits.isEmpty() ? null : String.join(", ", hits);
    }

    public List<SensitiveWord> listAll() {
        return sensitiveWordMapper.selectList(
                new LambdaQueryWrapper<SensitiveWord>().orderByDesc(SensitiveWord::getWordId));
    }

    public List<String> listCategories() {
        List<SensitiveWord> list = sensitiveWordMapper.selectList(
                new LambdaQueryWrapper<SensitiveWord>().select(SensitiveWord::getCategory));
        return list.stream()
                .map(SensitiveWord::getCategory)
                .filter(c -> c != null && !c.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }

    public void addWord(String content, String category) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(1001, "敏感词不能为空");
        }
        SensitiveWord w = new SensitiveWord();
        w.setContent(content.trim());
        w.setCategory(category != null ? category : "default");
        w.setStatus(1);
        sensitiveWordMapper.insert(w);
        reload();
    }

    public void deleteWord(Long wordId) {
        sensitiveWordMapper.deleteById(wordId);
        reload();
    }
}
