package com.alibaba.csp.sentinel.dashboard.orm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sentinel_metric")
public class Metric implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 修改时间
     */
    private Date gmtModified;

    /**
     * 应用名称
     */
    private String app;

    /**
     * 监控信息时间戳
     */
    private Date timestamp;

    /**
     * 资源名称
     */
    private String resource;

    /**
     * 通过QPS
     */
    private Long passQps;

    /**
     * 成功QPS
     */
    private Long successQps;

    /**
     * 限流QPS
     */
    private Long blockQps;

    /**
     * 异常QPS
     */
    private Long exceptionQps;

    /**
     * 资源平均响应时间
     */
    private Double rt;

    /**
     * 本次聚合的总条数
     */
    private Integer count;

    /**
     * 资源hashcode
     */
    private Integer resourceCode;

    // setter 、getter
}