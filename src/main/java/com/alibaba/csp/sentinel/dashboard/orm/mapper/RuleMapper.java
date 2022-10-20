package com.alibaba.csp.sentinel.dashboard.orm.mapper;

import com.alibaba.csp.sentinel.dashboard.orm.entity.Rule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Zhengxianyi
 * @since 2022-10-11
 */
public interface RuleMapper extends BaseMapper<Rule> {

    public List<Rule> findAllByMachine(Map<String, Object> param);

}
