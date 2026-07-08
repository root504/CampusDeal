package com.campusdeal.service;

import com.campusdeal.entity.SensitiveWord;

import java.util.List;

public interface SensitiveWordService {
    List<SensitiveWord> getAllWords();
    List<String> getAppliedWords();
    SensitiveWord createWord(SensitiveWord word);
    SensitiveWord updateWord(Long id, SensitiveWord word);
    void deleteWord(Long id);
    void applyWord(Long id);
    void unapplyWord(Long id);

    /**
     * 检测文本是否包含已应用的敏感词。
     * 命中时抛出 BusinessException；未命中时正常返回。
     */
    void checkContent(String text);
}
