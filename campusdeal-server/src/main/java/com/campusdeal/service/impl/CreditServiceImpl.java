package com.campusdeal.service.impl;

import com.campusdeal.common.BusinessException;
import com.campusdeal.common.PageResult;
import com.campusdeal.entity.CreditRecord;
import com.campusdeal.entity.CreditRule;
import com.campusdeal.entity.User;
import com.campusdeal.entity.UserCredit;
import com.campusdeal.repository.CreditRecordRepository;
import com.campusdeal.repository.CreditRuleRepository;
import com.campusdeal.repository.UserCreditRepository;
import com.campusdeal.repository.UserRepository;
import com.campusdeal.service.CreditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CreditServiceImpl implements CreditService {
    private final UserCreditRepository userCreditRepository;
    private final CreditRecordRepository creditRecordRepository;
    private final CreditRuleRepository creditRuleRepository;
    private final UserRepository userRepository;

    public CreditServiceImpl(UserCreditRepository userCreditRepository,
                             CreditRecordRepository creditRecordRepository,
                             CreditRuleRepository creditRuleRepository,
                             UserRepository userRepository) {
        this.userCreditRepository = userCreditRepository;
        this.creditRecordRepository = creditRecordRepository;
        this.creditRuleRepository = creditRuleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserCredit getUserCredit(Long userId) {
        return userCreditRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserCredit uc = new UserCredit();
                    uc.setUserId(userId);
                    uc.setCurrentScore(100);
                    return userCreditRepository.save(uc);
                });
    }

    @Override
    public PageResult<CreditRecord> getCreditRecords(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CreditRecord> recordPage = creditRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PageResult.of(recordPage.getContent(), recordPage.getTotalElements(), page, size);
    }

    @Override
    @Transactional
    public void changeCredit(Long userId, Integer amount, String reason, Long operatorId) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在");
        }

        UserCredit uc = getUserCredit(userId);
        int newScore = uc.getCurrentScore() + amount;
        if (newScore < 0) {
            throw new BusinessException("信誉分不能为负数，当前分数：" + uc.getCurrentScore());
        }
        uc.setCurrentScore(newScore);
        userCreditRepository.save(uc);

        // 同步更新 users 表的 credit_score 字段
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setCreditScore(newScore);
            userRepository.save(user);
        }

        CreditRecord record = new CreditRecord();
        record.setUserId(userId);
        record.setChangeAmount(amount);
        record.setReason(reason);
        record.setOperatorId(operatorId);
        creditRecordRepository.save(record);
    }

    @Override
    public PageResult<UserCredit> getAllUserCredits(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "currentScore"));
        Page<UserCredit> ucPage = userCreditRepository.findAll(pageable);
        return PageResult.of(ucPage.getContent(), ucPage.getTotalElements(), page, size);
    }

    @Override
    public PageResult<Map<String, Object>> getAllUserCreditsWithName(int page, int size) {
        // 先确保所有用户都有 user_credits 记录（懒初始化）
        ensureAllUsersHaveCredit();

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "currentScore"));
        Page<UserCredit> ucPage = userCreditRepository.findAll(pageable);

        List<Long> userIds = ucPage.getContent().stream()
                .map(UserCredit::getUserId)
                .collect(Collectors.toList());
        List<User> users = userRepository.findAllById(userIds);
        Map<Long, String> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        List<Map<String, Object>> enriched = ucPage.getContent().stream().map(uc -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", uc.getId());
            m.put("userId", uc.getUserId());
            m.put("username", userMap.getOrDefault(uc.getUserId(), "未知用户"));
            m.put("currentScore", uc.getCurrentScore());
            m.put("updatedAt", uc.getUpdatedAt());
            return m;
        }).collect(Collectors.toList());

        return PageResult.of(enriched, ucPage.getTotalElements(), page, size);
    }

    /**
     * 确保所有普通用户都有 user_credits 记录，
     * 解决管理员页面查询 user_credits 表为空的问题。
     */
    @Transactional
    public void ensureAllUsersHaveCredit() {
        List<User> allUsers = userRepository.findAll();
        List<Long> existingUserIds = userCreditRepository.findAll().stream()
                .map(UserCredit::getUserId)
                .toList();

        List<UserCredit> toCreate = new ArrayList<>();
        for (User user : allUsers) {
            if (!existingUserIds.contains(user.getId())) {
                UserCredit uc = new UserCredit();
                uc.setUserId(user.getId());
                // 从 users 表的 credit_score 初始化
                uc.setCurrentScore(user.getCreditScore() != null ? user.getCreditScore() : 100);
                toCreate.add(uc);
            }
        }
        if (!toCreate.isEmpty()) {
            userCreditRepository.saveAll(toCreate);
        }
    }

    @Override
    public List<CreditRule> getRules() {
        return creditRuleRepository.findAllByOrderBySortOrderAsc();
    }

    @Override
    @Transactional
    public CreditRule createRule(CreditRule rule) {
        Integer maxSort = creditRuleRepository.findAll().stream()
                .mapToInt(r -> r.getSortOrder() != null ? r.getSortOrder() : 0)
                .max().orElse(0);
        rule.setSortOrder(maxSort + 1);
        return creditRuleRepository.save(rule);
    }

    @Override
    @Transactional
    public CreditRule updateRule(Long id, CreditRule rule) {
        CreditRule existing = creditRuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("规则不存在"));
        existing.setDescription(rule.getDescription());
        existing.setCategory(rule.getCategory());
        existing.setScore(rule.getScore());
        return creditRuleRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        creditRuleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void applyRule(Long id) {
        CreditRule rule = creditRuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("规则不存在"));
        String reason = rule.getDescription();
        int score = rule.getScore();
        if ("penalty".equals(rule.getCategory())) {
            score = -Math.abs(score);
        } else {
            score = Math.abs(score);
        }

        List<UserCredit> allCredits = userCreditRepository.findAll();
        List<CreditRecord> records = new ArrayList<>();
        List<User> usersToUpdate = new ArrayList<>();
        List<Long> userIds = allCredits.stream().map(UserCredit::getUserId).toList();
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        for (UserCredit uc : allCredits) {
            int newScore = uc.getCurrentScore() + score;
            if (newScore < 0) newScore = 0;
            uc.setCurrentScore(newScore);

            // 同步更新 users 表的 credit_score
            User user = userMap.get(uc.getUserId());
            if (user != null) {
                user.setCreditScore(newScore);
                usersToUpdate.add(user);
            }

            CreditRecord record = new CreditRecord();
            record.setUserId(uc.getUserId());
            record.setChangeAmount(score);
            record.setReason(reason);
            record.setOperatorId(null);
            records.add(record);
        }
        userCreditRepository.saveAll(allCredits);
        if (!usersToUpdate.isEmpty()) {
            userRepository.saveAll(usersToUpdate);
        }
        creditRecordRepository.saveAll(records);

        creditRuleRepository.setApplied(id, true);
    }

    @Override
    @Transactional
    public void unapplyRule(Long id) {
        creditRuleRepository.setApplied(id, false);
    }
}
