/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.controller;

import java.util.Date;
import java.util.List;

import com.alibaba.csp.sentinel.dashboard.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService.PrivilegeType;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.util.StringUtil;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.rule.DatabaseRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.domain.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Flow rule controller.
 *
 * @author leyou
 * @author Eric Zhao
 */
@RestController
@RequestMapping(value = "/v1/flow")
public class FlowControllerV1 {

    private final Logger logger = LoggerFactory.getLogger(FlowControllerV1.class);

    @Autowired
    private DatabaseRuleRepositoryAdapter<FlowRuleEntity> repository;

    @Autowired
    private SentinelApiClient sentinelApiClient;

    @GetMapping("/rules")
    @AuthAction(PrivilegeType.READ_RULE)
    public Result<List<FlowRuleEntity>> apiQueryMachineRules(@RequestParam String app,
                                                             @RequestParam String ip,
                                                             @RequestParam Integer port) {

        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
//        if (StringUtil.isEmpty(ip)) {
//            return Result.ofFail(-1, "ip can't be null or empty");
//        }
//        if (port == null) {
//            return Result.ofFail(-1, "port can't be null");
//        }
        try {
//            List<FlowRuleEntity> rules = ruleProvider.getRules(app);
            List<FlowRuleEntity> rules = repository.findAllByMachine(MachineInfo.of(app, ip, port));
            if (rules != null && !rules.isEmpty()) {
                for (FlowRuleEntity entity : rules) {
                    entity.setApp(app);
                    if (entity.getClusterConfig() != null && entity.getClusterConfig().getFlowId() != null) {
                        entity.setId(entity.getClusterConfig().getFlowId());
                    }
                }
            }
            rules = repository.saveAll(rules);
            for (FlowRuleEntity rule : rules) {
                ClusterFlowConfig cluster = rule.getClusterConfig();
                if (cluster != null) {
                    cluster.setFlowId(rule.getId());
                }
            }
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("Error when querying flow rules", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    private <R> Result<R> checkEntityInternal(FlowRuleEntity entity) {
        if (entity == null) {
            return Result.ofFail(-1, "invalid body");
        }
        if (StringUtil.isBlank(entity.getApp())) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
//        if (StringUtil.isBlank(entity.getIp())) {
//            return Result.ofFail(-1, "ip can't be null or empty");
//        }
//        if (entity.getPort() == null) {
//            return Result.ofFail(-1, "port can't be null");
//        }
        if (StringUtil.isBlank(entity.getLimitApp())) {
            return Result.ofFail(-1, "limitApp can't be null or empty");
        }
        if (StringUtil.isBlank(entity.getResource())) {
            return Result.ofFail(-1, "resource can't be null or empty");
        }
        if (entity.getGrade() == null) {
            return Result.ofFail(-1, "grade can't be null");
        }
        if (entity.getGrade() != 0 && entity.getGrade() != 1) {
            return Result.ofFail(-1, "grade must be 0 or 1, but got " + entity.getGrade());
        }
        if (entity.getCount() == null || entity.getCount() < 0) {
            return Result.ofFail(-1, "count should be at lease zero");
        }
        if (entity.getStrategy() == null) {
            return Result.ofFail(-1, "strategy can't be null");
        }
        if (entity.getStrategy() != 0 && StringUtil.isBlank(entity.getRefResource())) {
            return Result.ofFail(-1, "refResource can't be null or empty when strategy!=0");
        }
        if (entity.getControlBehavior() == null) {
            return Result.ofFail(-1, "controlBehavior can't be null");
        }
        int controlBehavior = entity.getControlBehavior();
        if (controlBehavior == 1 && entity.getWarmUpPeriodSec() == null) {
            return Result.ofFail(-1, "warmUpPeriodSec can't be null when controlBehavior==1");
        }
        if (controlBehavior == 2 && entity.getMaxQueueingTimeMs() == null) {
            return Result.ofFail(-1, "maxQueueingTimeMs can't be null when controlBehavior==2");
        }
        if (entity.isClusterMode() && entity.getClusterConfig() == null) {
            return Result.ofFail(-1, "cluster config should be valid");
        }
        return null;
    }

    @PostMapping("/rule")
    @AuthAction(PrivilegeType.WRITE_RULE)
    public Result<FlowRuleEntity> apiAddFlowRule(@RequestBody FlowRuleEntity entity) {
        Result<FlowRuleEntity> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        entity.setId(null);
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        entity.setLimitApp(entity.getLimitApp().trim());
        entity.setResource(entity.getResource().trim());
        try {
            entity = repository.save(entity);
        } catch (Throwable throwable) {
            logger.error("Failed to add flow rule", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        if (!publishRules(entity.getApp(), entity.getIp(), entity.getPort())) {
            logger.info("publish flow rules fail after rule add");
        }
        return Result.ofSuccess(entity);
    }

    @PutMapping("/rule/{id}")
    @AuthAction(PrivilegeType.WRITE_RULE)
    public Result<FlowRuleEntity> apiUpdateFlowRule(@PathVariable Long id, @RequestBody FlowRuleEntity entity) {
        if (entity.getClusterConfig() != null) {
            entity.getClusterConfig().setFlowId(entity.getId());
        }
        Date date = new Date();
        entity.setGmtModified(date);
        try {
            entity = repository.save(entity);
            if (entity == null) {
                return Result.ofFail(-1, "save entity fail");
            }
        } catch (Throwable throwable) {
            logger.error("Failed to update flow rule", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        if (!publishRules(entity.getApp(), entity.getIp(), entity.getPort())) {
            logger.info("publish flow rules fail after rule update");
        }
        return Result.ofSuccess(entity);
    }

    @DeleteMapping("/delete.json")
    @AuthAction(PrivilegeType.WRITE_RULE)
    public Result<Long> apiDeleteFlowRule(Long id) {

        if (id == null || id <= 0) {
            return Result.ofFail(-1, "id can't be null");
        }
        FlowRuleEntity entity = repository.findById(id);
        if (entity == null) {
            return Result.ofSuccess(null);
        }

        try {
            repository.delete(id);
        } catch (Exception e) {
            return Result.ofFail(-1, e.getMessage());
        }
        if (!publishRules(entity.getApp(), entity.getIp(), entity.getPort())) {
            logger.info("publish flow rules fail after rule delete");
        }
        return Result.ofSuccess(id);
    }

    @GetMapping("/rules/{app}")
    @AuthAction(PrivilegeType.READ_RULE)
    public Result<Boolean> syncRules(@PathVariable String app, String ip, Integer port){
        if (!publishRules(app, null, null)) {
            return Result.ofSuccess(false);
        }
        return Result.ofSuccess(true);
    }
    private boolean publishRules(String app, String ip, Integer port) {
        try {
            List<FlowRuleEntity> rules = repository.findAllByMachine(MachineInfo.of(app, ip, port));
            return sentinelApiClient.setFlowRuleOfMachine(app, ip, port, rules);
        } catch (Exception e){
            logger.error("Publish authority rules failed after rule delete", e);
            return false;
        }
    }
}
