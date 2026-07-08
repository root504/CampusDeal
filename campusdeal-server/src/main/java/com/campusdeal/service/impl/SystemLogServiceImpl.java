package com.campusdeal.service.impl;

import com.campusdeal.common.PageResult;
import com.campusdeal.entity.SystemLog;
import com.campusdeal.repository.SystemLogRepository;
import com.campusdeal.service.SystemLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class SystemLogServiceImpl implements SystemLogService {
    private final SystemLogRepository systemLogRepository;

    public SystemLogServiceImpl(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    @Override
    public void saveLog(String action, String description, String operator, String ip) {
        SystemLog log = new SystemLog();
        log.setAction(action);
        log.setDescription(description);
        log.setOperator(operator);
        log.setIp(ip);
        systemLogRepository.save(log);
    }

    @Override
    public PageResult<SystemLog> getLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<SystemLog> logPage = systemLogRepository.findAll(pageable);
        return PageResult.of(logPage.getContent(), logPage.getTotalElements(), page, size);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteAllLogs() {
        systemLogRepository.deleteAll();
    }
}
