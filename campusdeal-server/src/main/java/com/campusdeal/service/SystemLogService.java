package com.campusdeal.service;

import com.campusdeal.common.PageResult;
import com.campusdeal.entity.SystemLog;

public interface SystemLogService {
    void saveLog(String action, String description, String operator, String ip);
    PageResult<SystemLog> getLogs(int page, int size);
    void deleteAllLogs();
}
