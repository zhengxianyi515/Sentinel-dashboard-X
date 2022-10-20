package com.alibaba.csp.sentinel.dashboard.orm.mapper;

import com.alibaba.csp.sentinel.dashboard.orm.entity.Metric;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MetricMapper extends BaseMapper<Metric> {

    int batchInsert(List<Metric> metricList);

}