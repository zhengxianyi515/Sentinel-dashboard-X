package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class NacosConfigUtil {

    @Resource
    private NacosConfig nacosConfig;

    public static final String GROUP_ID = "DEFAULT_GROUP";

    public static final String FLOW_DATA_ID_POSTFIX = "-flow-rules";
    public static final String DEGRADE_DATA_ID_POSTFIX = "-degrade-rules";
    public static final String SYSTEM_DATA_ID_POSTFIX = "-system-rules";
    public static final String PARAM_FLOW_DATA_ID_POSTFIX = "-param-flow-rules";
    public static final String AUTHORITY_DATA_ID_POSTFIX = "-authority-rules";
    public static final String DASHBOARD_POSTFIX = "-sentinel-dashboard";
    public static final String CLUSTER_MAP_DATA_ID_POSTFIX = "-cluster-map";

    /**
     * cc for `cluster-client`
     */
    public static final String CLIENT_CONFIG_DATA_ID_POSTFIX = "-cc-config";
    /**
     * cs for `cluster-server`
     */
    public static final String SERVER_TRANSPORT_CONFIG_DATA_ID_POSTFIX = "-cs-transport-config";
    public static final String SERVER_FLOW_CONFIG_DATA_ID_POSTFIX = "-cs-flow-config";
    public static final String SERVER_NAMESPACE_SET_DATA_ID_POSTFIX = "-cs-namespace-set";

    private NacosConfigUtil() {}

    /**
     *
     * 将规则序列化成JSON文本，存储到Nacos server中
     *
     * @param configService nacos config service
     * @param app           应用名称
     * @param postfix       规则后缀 eg.NacosConfigUtil.FLOW_DATA_ID_POSTFIX
     * @param rules         规则对象
     * @throws NacosException 异常
     */

    public static <T> void setRuleStringToNacos(ConfigService configService, String app, String postfix, List<T> rules) throws NacosException {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null || rules.isEmpty()) {
//            NacosDataSource
            return;
        }
        System.out.println(JSONUtils.toJSONString(rules.get(0)));
        System.out.println(JSONUtils.toJSONString(((RuleEntity)rules.get(0)).toRule()));
        List<Rule> ruleForApp = rules.stream()
                .map(rule -> {
                    RuleEntity rule1 = (RuleEntity) rule;
                    Rule rule2 = rule1.toRule();
//                    System.out.println(rule1.getClass());
//                    System.out.println(rule2.getClass());
                    return rule2;
                })
                .collect(Collectors.toList());


        String dataId = genDataId(app, postfix);
        /**
         * 俩种存储只是入参不同,为了满足功能的实现,存入nacos后,会有俩个配置,以后继续完善
         */
        // 存储，控制微服务使用,即可以起到拦截作用,但是由于无法显示到控制台
        configService.publishConfig(
                dataId,
                NacosConfigUtil.GROUP_ID,
                JSONUtils.toJSONString(rules),
                "json"
        );

//        // 存储，给控制台显示使用,由于数据太多,会出现转化异常,虽然可以提供控制台显示,但是无法对微服务进行保护
//        configService.publishConfig(
//                dataId + DASHBOARD_POSTFIX,
//                NacosConfigUtil.GROUP_ID,
//                JSONUtils.toJSONString(rules),
//                "json"
//        );
    }

    /**
     *
     * 从Nacos server中查询响应规则，并将其反序列化成对应Rule实体
     *
     * @param configService nacos config service
     * @param appName       应用名称
     * @param postfix       规则后缀 eg.NacosConfigUtil.FLOW_DATA_ID_POSTFIX
     * @param clazz         类
     * @param <T>           泛型
     * @return 规则对象列表
     * @throws NacosException 异常
     */
    public static <T> List<T> getRuleEntitiesFromNacos(ConfigService configService, String appName, String postfix, Class<T> clazz) throws NacosException {
        String rules = configService.getConfig(
//                genDataId(appName, postfix) + DASHBOARD_POSTFIX,
                genDataId(appName, postfix),
                NacosConfigUtil.GROUP_ID,
                3000
        );
        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        return JSONUtils.parseObject(clazz, rules);
    }

    private static String genDataId(String appName, String postfix) {
        return appName + postfix;
    }
}

