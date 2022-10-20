package com.alibaba.csp.sentinel.dashboard.orm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.excel.poi.annotation.ExportField;

/**
 * <p>
 *
 * </p>
 *
 * @author Zhengxianyi
 * @since 2022-10-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sentinel_rule")
@ApiModel(value="Order对象", description="")
public class Rule implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "id", dataType = "Long", value = "规则ID")
    @TableId(value = "id", type = IdType.AUTO)
    @ExportField(columnName = "规则ID")
    private Long id;

    @ApiModelProperty(name = "app", dataType = "String", value = "应用名称")
    @ExportField(columnName = "应用名称")
    private String app;

    @ApiModelProperty(name = "appType", dataType = "Integer", value = "应用类型")
    @ExportField(columnName = "应用类型")
    private Integer appType;

    @ApiModelProperty(name = "ip", dataType = "String", value = "主机IP")
    @ExportField(columnName = "主机IP")
    private String ip;

    @ApiModelProperty(name = "port", dataType = "Integer", value = "应用端口")
    @ExportField(columnName = "应用端口")
    private Integer port;

    @ApiModelProperty(name = "ruleType", dataType = "String", value = "应用类型")
    @ExportField(columnName = "应用类型")
    private String ruleType;

    @ApiModelProperty(name = "ruleContent", dataType = "String", value = "规则内容")
    @ExportField(columnName = "规则内容")
    private String ruleContent;

    @ApiModelProperty(name = "createTime", dataType = "Date", value = "创建时间")
    @ExportField(columnName = "创建时间")
    private Date createTime;

    @ApiModelProperty(name = "updateTime", dataType = "Date", value = "更新时间")
    @ExportField(columnName = "更新时间")
    private Date updateTime;


}
