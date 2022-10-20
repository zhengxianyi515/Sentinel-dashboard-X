package com.alibaba.csp.sentinel.dashboard.util;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class MakeDDL {

    public static void printTableSql(Class claz) {
        Field[] fields = claz.getDeclaredFields();
        System.out.println("create table if not exists " + claz.getSimpleName() + " (");
        for (Field field : fields) {
            String fname = field.getName();
            String cname = field.getType().getSimpleName();
//            System.out.println(fname+","+ cname);
            if ("id".equals(cname)) {
                System.out.println(fname + " bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '" + fname + "' ,");
            } else if ("String".equalsIgnoreCase(cname)) {
                System.out.println(fname + " varchar(50) DEFAULT NULL COMMENT '" + fname + "' ,");
            } else if ("Long".equalsIgnoreCase(cname)) {
                System.out.println(fname + " bigint(20) DEFAULT NULL COMMENT '" + fname + "' ,");
            } else if ("Integer".equalsIgnoreCase(cname)||"int".equalsIgnoreCase(cname)) {
                System.out.println(fname + " int(10) DEFAULT NULL COMMENT '" + fname + "' ,");
            } else if ("Float".equalsIgnoreCase(cname)) {
                System.out.println(fname + " decimal(10, 2) DEFAULT NULL COMMENT '" + fname + "' ,");
            } else if ("Double".equalsIgnoreCase(cname)) {
                System.out.println(fname + " decimal(20, 2) DEFAULT NULL COMMENT '" + fname + "' ,");
            } else if ("Date".equalsIgnoreCase(cname)) {
                System.out.println(fname + " datetime DEFAULT NULL COMMENT '" + fname + "' ,");
            }
        }
        System.out.println(") primary key (id) comment '" + claz.getSimpleName() + "';");
        System.out.println();
        System.out.println();
    }

    public static void mainn(String[] args) {
//        List<Class> list = Arrays.asList(AbstractRuleEntity.class, AuthorityRule.class , DegradeRuleEntity.class, FlowRuleEntity.class,
//                ParamFlowRuleEntity.class,AbstractRuleEntity.class, ParamFlowRule.class, SystemRuleEntity.class, ClusterFlowConfig.class);
        List<Class> list = Arrays.asList(FlowRuleEntity.class,DegradeRuleEntity.class);
        for (Class c : list) {
            printTableSql(c);
        }
    }
}
