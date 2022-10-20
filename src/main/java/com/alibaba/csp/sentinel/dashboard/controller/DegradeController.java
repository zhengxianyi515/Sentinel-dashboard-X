package com.alibaba.csp.sentinel.dashboard.controller;

import com.alibaba.csp.sentinel.dashboard.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService.PrivilegeType;
import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.DatabaseDegradeRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemDegradeRuleStore;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/degrade")
public class DegradeController {

    private final Logger logger = LoggerFactory.getLogger(DegradeController.class);

    @Autowired
    private DatabaseDegradeRuleStore repository;

    @Autowired
    private SentinelApiClient sentinelApiClient;

    @GetMapping("/rules.json")
    @AuthAction(PrivilegeType.READ_RULE)
    public Result<List<DegradeRuleEntity>> queryMachineRules(@RequestParam String app,
                                                             @RequestParam String ip,
                                                             @RequestParam Integer port) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        try {
            List<DegradeRuleEntity> rules = repository.findAllByMachine(MachineInfo.of(app, ip, port));
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("queryApps error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    @PostMapping("/rule")
    @AuthAction(PrivilegeType.WRITE_RULE)
    public Result<DegradeRuleEntity> add(@RequestBody DegradeRuleEntity entity) {
        if (StringUtil.isBlank(entity.getApp())) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (StringUtil.isBlank(entity.getLimitApp())) {
            return Result.ofFail(-1, "limitApp can't be null or empty");
        }
        if (StringUtil.isBlank(entity.getResource())) {
            return Result.ofFail(-1, "resource can't be null or empty");
        }
        if (entity.getCount() == null) {
            return Result.ofFail(-1, "count can't be null");
        }
        if (entity.getTimeWindow() == null) {
            return Result.ofFail(-1, "timeWindow can't be null");
        }
        if (entity.getGrade() == null) {
            return Result.ofFail(-1, "grade can't be null");
        }
        if (entity.getGrade() < RuleConstant.DEGRADE_GRADE_RT || entity.getGrade() > RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT) {
            return Result.ofFail(-1, "Invalid grade: " + entity.getGrade());
        }
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        try {
            entity = repository.save(entity);
        } catch (Throwable throwable) {
            logger.error("add error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        if (!publishRules(entity.getApp(), entity.getIp(), entity.getPort())) {
            logger.info("publish degrade rules fail after rule add");
        }
        return Result.ofSuccess(entity);
    }

    @PutMapping("/rule/{id}")
    @AuthAction(PrivilegeType.WRITE_RULE)
    public Result<DegradeRuleEntity> updateIfNotNull(@PathVariable Long id, @RequestBody DegradeRuleEntity en) {
        String app = en.getApp();
        String limitApp = en.getLimitApp();
        String resource = en.getResource();
        String ip = en.getIp();
        Integer port = en.getPort();
        Double count = en.getCount();
        Integer timeWindow = en.getTimeWindow();
        Integer grade = en.getGrade();
        Double slowRatioThreshold = en.getSlowRatioThreshold();
        Integer statIntervalMs = en.getStatIntervalMs();
        Integer minRequestAmount = en.getMinRequestAmount();

        if (id == null) {
            return Result.ofFail(-1, "id can't  be null");
        }
        if (grade != null) {
            if (grade < RuleConstant.DEGRADE_GRADE_RT || grade > RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT) {
                return Result.ofFail(-1, "Invalid grade: " + grade);
            }
        }
        DegradeRuleEntity entity = repository.findById(id);
        if (entity == null) {
            return Result.ofFail(-1, "id " + id + " dose not exist");
        }

        if (StringUtil.isNotBlank(app)) {
            entity.setApp(app.trim());
        }
        if (ip != null) {
            entity.setIp(ip.trim());
        } else {
            entity.setIp(ip);
        }
        entity.setPort(port);
        if (StringUtil.isNotBlank(limitApp)) {
            entity.setLimitApp(limitApp.trim());
        }
        if (StringUtil.isNotBlank(resource)) {
            entity.setResource(resource.trim());
        }
        if (count != null) {
            entity.setCount(count);
        }
        if (timeWindow != null) {
            entity.setTimeWindow(timeWindow);
        }
        if (grade != null) {
            entity.setGrade(grade);
        }
        if (minRequestAmount != null) {
            entity.setMinRequestAmount(minRequestAmount);
        }
        if (slowRatioThreshold != null) {
            entity.setSlowRatioThreshold(slowRatioThreshold);
        }
        if (statIntervalMs != null) {
            entity.setStatIntervalMs(statIntervalMs);
        }
        Date date = new Date();
        entity.setGmtModified(date);
        entity.setId(id);
        try {
            entity = repository.save(entity);
        } catch (Throwable throwable) {
            logger.error("save error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        if (!publishRules(entity.getApp(), entity.getIp(), entity.getPort())) {
            logger.info("publish degrade rules fail after rule update");
        }
        return Result.ofSuccess(entity);
    }

    @DeleteMapping("/rule/{id}")
    @AuthAction(PrivilegeType.DELETE_RULE)
    public Result<Long> delete(@PathVariable("id") Long id) {
        if (id == null) {
            return Result.ofFail(-1, "id can't be null");
        }

        DegradeRuleEntity entity = repository.findById(id);
        if (entity == null) {
            return Result.ofSuccess(null);
        }

        try {
            repository.delete(id);
        } catch (Throwable throwable) {
            logger.error("delete error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        if (!publishRules(entity.getApp(), entity.getIp(), entity.getPort())) {
            logger.info("publish degrade rules fail after rule delete");
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
            List<DegradeRuleEntity> rules = repository.findAllByMachine(MachineInfo.of(app, ip, port));
            return sentinelApiClient.setDegradeRuleOfMachine(app, ip, port, rules);
        } catch (Exception e){
            logger.error("Publish authority rules failed after rule delete", e);
            return false;
        }
    }

}

