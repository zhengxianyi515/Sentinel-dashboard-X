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
package com.alibaba.csp.sentinel.dashboard.repository.rule;

import java.util.*;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.orm.entity.Rule;
import com.alibaba.csp.sentinel.dashboard.orm.mapper.RuleMapper;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;

/**
 * Database storage for authority rules.
 *
 * @author ZhengXianyi
 *
 */
public abstract class DatabaseRuleRepositoryAdapter<T extends RuleEntity> implements RuleRepository<T, Long> {

    @Autowired
    private RuleMapper ruleMapper;

    protected Class ruleEntityClass;

    public DatabaseRuleRepositoryAdapter() {
        this.ruleEntityClass = ResolvableType.forClass(this.getClass()).getSuperType().getGeneric(0).resolve();
    }

    @Override
    public T save(T entity) {
        Rule rule = new Rule();
        rule.setId(entity.getId());
        rule.setApp(entity.getApp());
        rule.setIp(entity.getIp());
        rule.setPort(entity.getPort());
        rule.setRuleType(ruleEntityClass.getSimpleName());
        rule.setRuleContent(JSON.toJSONString(entity));

        Date date = new Date();
        if (entity.getId() == null) {
            rule.setCreateTime(date);
            rule.setUpdateTime(date);
            ruleMapper.insert(rule);
            entity.setId(rule.getId());
        } else {
            rule.setUpdateTime(date);
            ruleMapper.updateById(rule);
        }
        T processedEntity = preProcess(entity);
        return processedEntity;
    }

    @Override
    public List<T> saveAll(List<T> rules) {
        // TODO: check here.
        if (rules == null) {
            return null;
        }
        List<T> savedRules = new ArrayList<>(rules.size());
        for (T rule : rules) {
            savedRules.add(save(rule));
        }
        return savedRules;
    }

    @Override
    public T delete(Long id) {
        Rule rule = ruleMapper.selectById(id);
        if (rule == null) {
            return null;
        }
        ruleMapper.deleteById(id);
        return convert(rule);
    }

    @Override
    public T findById(Long id) {
        Rule rule = ruleMapper.selectById(id);
        return convert(rule);
    }

    @Override
    public List<T> findAllByMachine(MachineInfo machineInfo) {
        Map<String, Object> param = new HashMap<>();
        param.put("app",machineInfo.getApp());
        param.put("appType",machineInfo.getAppType());
        param.put("ruleType", ruleEntityClass.getSimpleName());
        param.put("ip",machineInfo.getIp());
        param.put("port",machineInfo.getPort());
        List<Rule> list = ruleMapper.findAllByMachine(param);
        return convert(list);
    }

    @Override
    public List<T> findAllByApp(String appName) {
        QueryWrapper<Rule> query = new QueryWrapper<Rule>();
        query.eq("app", appName);
        query.eq("rule_type", ruleEntityClass.getSimpleName());
        List<Rule> list = ruleMapper.selectList(query);
        return convert(list);
    }

    public <T> List<T> convert(List<Rule> rules) {
        List<T> rs = new ArrayList<T>();
        for (Rule rule : rules) {
            rs.add(convert(rule));
        }
        return rs;
    }

    public <T> T convert(Rule rule) {
        if (rule == null) {
            return null;
        }
        JSONObject json = JSON.parseObject(rule.getRuleContent());
        json.put("id", rule.getId());
        return (T) JSON.parseObject(json.toJSONString(), this.ruleEntityClass);
    }

    public void clearAll() {
    }

    protected T preProcess(T entity) {
        return entity;
    }

}
