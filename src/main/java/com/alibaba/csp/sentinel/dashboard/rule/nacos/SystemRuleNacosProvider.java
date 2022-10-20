package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("systemRuleNacosProvider")
public class SystemRuleNacosProvider implements DynamicRuleProvider<List<SystemRuleEntity>> {
    @Autowired
    private ConfigService configService;

    @Override
    public List<SystemRuleEntity> getRules(String appName) throws Exception {
        return NacosConfigUtil.getRuleEntitiesFromNacos(
                this.configService,
                appName,
                NacosConfigUtil.SYSTEM_DATA_ID_POSTFIX,
                SystemRuleEntity.class
        );
    }
}



