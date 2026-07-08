package com.campusdeal.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate6Module hibernate6Module() {
        Hibernate6Module module = new Hibernate6Module();
        // 禁用 LAZY 字段的强制初始化，由 @JsonIgnoreProperties 处理代理序列化
        module.disable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
        // 禁用 USE_TRANSIENT_ANNOTATION，允许 @Transient 字段（items/buyer/seller）被序列化为 JSON
        // 否则前端无法展示订单商品明细和买卖家昵称
        module.disable(Hibernate6Module.Feature.USE_TRANSIENT_ANNOTATION);
        return module;
    }
}
