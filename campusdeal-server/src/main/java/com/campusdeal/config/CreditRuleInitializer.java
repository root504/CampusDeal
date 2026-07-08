package com.campusdeal.config;

import com.campusdeal.entity.CreditRule;
import com.campusdeal.repository.CreditRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 项目启动时自动初始化5条信誉分规则，不存在则创建并设为已应用。
 */
@Component
public class CreditRuleInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CreditRuleInitializer.class);

    private final CreditRuleRepository creditRuleRepository;

    public CreditRuleInitializer(CreditRuleRepository creditRuleRepository) {
        this.creditRuleRepository = creditRuleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始检查信誉分规则初始化...");

        List<CreditRule> existing = creditRuleRepository.findAll();
        if (existing.size() >= 5) {
            // Check if descriptions match - if the first 5 already exist with correct desc, skip
            boolean allMatch = checkExistingRules(existing);
            if (allMatch) {
                log.info("5条信誉分规则已存在，跳过初始化");
                return;
            }
        }

        initRule("初始信誉分：100 分", "bonus", 100, 1);
        initRule("成功交易一笔 +1 分", "bonus", 1, 2);
        initRule("被投诉且核实 -5 分", "penalty", 5, 3);
        initRule("违规发布商品 -10 分", "penalty", 10, 4);
        initRule("信誉分低于 60 分将限制发布商品", "penalty", 0, 5);

        log.info("信誉分规则初始化完成");
    }

    private void initRule(String description, String category, int score, int sortOrder) {
        boolean exists = creditRuleRepository.findAll()
                .stream().anyMatch(r -> description.equals(r.getDescription()));
        if (!exists) {
            CreditRule rule = new CreditRule();
            rule.setDescription(description);
            rule.setCategory(category);
            rule.setScore(score);
            rule.setSortOrder(sortOrder);
            rule.setApplied(true);
            creditRuleRepository.save(rule);
            log.info("已创建规则: {}", description);
        }
    }

    private boolean checkExistingRules(List<CreditRule> rules) {
        String[] expected = {
            "初始信誉分：100 分",
            "成功交易一笔 +1 分",
            "被投诉且核实 -5 分",
            "违规发布商品 -10 分",
            "信誉分低于 60 分将限制发布商品"
        };
        for (String desc : expected) {
            boolean found = rules.stream().anyMatch(r -> desc.equals(r.getDescription()));
            if (!found) return false;
        }
        return true;
    }
}
