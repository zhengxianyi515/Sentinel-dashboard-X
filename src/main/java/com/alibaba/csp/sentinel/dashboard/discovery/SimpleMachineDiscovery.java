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
package com.alibaba.csp.sentinel.dashboard.discovery;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
import com.alibaba.csp.sentinel.dashboard.repository.rule.*;
import com.alibaba.csp.sentinel.util.AssertUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author leyou
 */
@Component
public class SimpleMachineDiscovery implements MachineDiscovery {

    private final ConcurrentMap<String, AppInfo> apps = new ConcurrentHashMap<>();

    @Autowired
    private DatabaseFlowRuleStore flowRuleStore;

    @Autowired
    private DatabaseDegradeRuleStore degradeRuleStore;

    @Autowired
    private DatabaseParamFlowRuleStore paramFlowRuleStore;

    @Autowired
    private DatabaseSystemRuleStore systemRuleStore;

    @Autowired
    private DatabaseAuthorityRuleStore authorityRuleStore;

    @Autowired
    private SentinelApiClient sentinelApiClient;

    @Override
    public long addMachine(MachineInfo machineInfo) {
        AssertUtil.notNull(machineInfo, "machineInfo cannot be null");
        AppInfo oldAppInfo = apps.get(machineInfo.getApp());
        MachineInfo oldMachineInfo=null;
        if(oldAppInfo!=null && machineInfo.getPort()!=null){
            Optional<MachineInfo> optionalMachineInfo = oldAppInfo.getMachine(machineInfo.getIp(), machineInfo.getPort().intValue());
            if(optionalMachineInfo.isPresent()){
                oldMachineInfo = optionalMachineInfo.get();
            }
        }
        AppInfo appInfo = apps.computeIfAbsent(machineInfo.getApp(), o -> new AppInfo(machineInfo.getApp(), machineInfo.getAppType()));
        appInfo.addMachine(machineInfo);

        if(oldAppInfo == null || oldMachineInfo==null || !oldMachineInfo.isHealthy()) {
            //应用注册即推送规则
            List<FlowRuleEntity> flowRuleEntityList = flowRuleStore.findAllByMachine(machineInfo);
            sentinelApiClient.setFlowRuleOfMachine(machineInfo.getApp(), machineInfo.getIp(), machineInfo.getPort(), flowRuleEntityList);

            List<ParamFlowRuleEntity> paramFlowRuleEntityList = paramFlowRuleStore.findAllByMachine(machineInfo);
            sentinelApiClient.setParamFlowRuleOfMachineSync(machineInfo.getApp(), machineInfo.getIp(), machineInfo.getPort(), paramFlowRuleEntityList);

            List<DegradeRuleEntity> degradeRuleEntityList = degradeRuleStore.findAllByMachine(machineInfo);
            sentinelApiClient.setDegradeRuleOfMachine(machineInfo.getApp(), machineInfo.getIp(), machineInfo.getPort(), degradeRuleEntityList);

            List<SystemRuleEntity> systemRuleEntityList = systemRuleStore.findAllByMachine(machineInfo);
            sentinelApiClient.setSystemRuleOfMachine(machineInfo.getApp(), machineInfo.getIp(), machineInfo.getPort(), systemRuleEntityList);

            List<AuthorityRuleEntity> authorityRuleEntityList = authorityRuleStore.findAllByMachine(machineInfo);
            sentinelApiClient.setAuthorityRuleOfMachine(machineInfo.getApp(), machineInfo.getIp(), machineInfo.getPort(), authorityRuleEntityList);
        }
        return 1;
    }

    @Override
    public boolean removeMachine(String app, String ip, int port) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        AppInfo appInfo = apps.get(app);
        if (appInfo != null) {
            return appInfo.removeMachine(ip, port);
        }
        return false;
    }

    @Override
    public List<String> getAppNames() {
        return new ArrayList<>(apps.keySet());
    }

    @Override
    public AppInfo getDetailApp(String app) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        return apps.get(app);
    }

    @Override
    public Set<AppInfo> getBriefApps() {
        return new HashSet<>(apps.values());
    }

    @Override
    public void removeApp(String app) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        apps.remove(app);
    }

}
