package com.campusdeal.service.impl;

import com.campusdeal.common.BusinessException;
import com.campusdeal.entity.SensitiveWord;
import com.campusdeal.repository.SensitiveWordRepository;
import com.campusdeal.service.SensitiveWordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordRepository sensitiveWordRepository;

    public SensitiveWordServiceImpl(SensitiveWordRepository sensitiveWordRepository) {
        this.sensitiveWordRepository = sensitiveWordRepository;
    }

    @Override
    public List<SensitiveWord> getAllWords() {
        return sensitiveWordRepository.findAllByOrderByUpdatedAtDesc();
    }

    @Override
    public List<String> getAppliedWords() {
        return sensitiveWordRepository.findByAppliedTrue().stream()
                .map(SensitiveWord::getWord)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SensitiveWord createWord(SensitiveWord word) {
        if (sensitiveWordRepository.existsByWord(word.getWord())) {
            throw new BusinessException("该敏感词已存在");
        }
        return sensitiveWordRepository.save(word);
    }

    @Override
    @Transactional
    public SensitiveWord updateWord(Long id, SensitiveWord word) {
        SensitiveWord existing = sensitiveWordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("敏感词不存在"));
        if (!existing.getWord().equals(word.getWord()) && sensitiveWordRepository.existsByWord(word.getWord())) {
            throw new BusinessException("该敏感词已存在");
        }
        existing.setWord(word.getWord());
        return sensitiveWordRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteWord(Long id) {
        sensitiveWordRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void applyWord(Long id) {
        SensitiveWord word = sensitiveWordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("敏感词不存在"));
        word.setApplied(true);
        sensitiveWordRepository.save(word);
    }

    @Override
    @Transactional
    public void unapplyWord(Long id) {
        SensitiveWord word = sensitiveWordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("敏感词不存在"));
        word.setApplied(false);
        sensitiveWordRepository.save(word);
    }

    @Override
    public void checkContent(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        List<SensitiveWord> appliedWords = sensitiveWordRepository.findByAppliedTrue();
        for (SensitiveWord sw : appliedWords) {
            if (text.contains(sw.getWord())) {
                throw new BusinessException("内容包含敏感词，请修改后重新提交");
            }
        }
    }
}
