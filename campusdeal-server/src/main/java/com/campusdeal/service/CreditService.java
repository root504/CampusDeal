package com.campusdeal.service;

import com.campusdeal.common.PageResult;
import com.campusdeal.entity.CreditRecord;
import com.campusdeal.entity.CreditRule;
import com.campusdeal.entity.UserCredit;

import java.util.List;
import java.util.Map;

public interface CreditService {
    UserCredit getUserCredit(Long userId);
    PageResult<CreditRecord> getCreditRecords(Long userId, int page, int size);
    void changeCredit(Long userId, Integer amount, String reason, Long operatorId);
    PageResult<UserCredit> getAllUserCredits(int page, int size);
    PageResult<Map<String, Object>> getAllUserCreditsWithName(int page, int size);
    List<CreditRule> getRules();
    CreditRule createRule(CreditRule rule);
    CreditRule updateRule(Long id, CreditRule rule);
    void deleteRule(Long id);
    void applyRule(Long id);
    void unapplyRule(Long id);
}
