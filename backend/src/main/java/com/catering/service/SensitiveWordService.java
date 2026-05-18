package com.catering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.common.BusinessException;
import com.catering.entity.SensitiveWord;
import com.catering.mapper.SensitiveWordMapper;
import com.catering.util.DfaFilter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class SensitiveWordService {

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
        return dfa.findFirst(text);
    }

    public List<SensitiveWord> listAll() {
        return sensitiveWordMapper.selectList(
                new LambdaQueryWrapper<SensitiveWord>().orderByDesc(SensitiveWord::getWordId));
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
