package org.jeecg.modules.leave.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 请假信息
 * @Author: lym
 * @Date:   2020-09-02
 * @Version: V1.0
 */
@Data
@TableName("zt_leave")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="zt_leave对象", description="请假信息")
public class ZtLeave implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
	/**创建人*/
    @ApiModelProperty(value = "创建人")
    private String createBy;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private Date createTime;
	/**请假类型*/
	@Excel(name = "请假类型", width = 15)
    @ApiModelProperty(value = "请假类型")
    private Integer leaveType;
	/**请假时长*/
	@Excel(name = "请假时长", width = 15)
    @ApiModelProperty(value = "请假时长")
    private Double leaveLength;
	/**请假日期*/
	@Excel(name = "请假日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "请假日期")
    private Date leaveDate;
	/**姓名*/
	@Excel(name = "姓名", width = 15)
    @ApiModelProperty(value = "姓名")
    private String name;
}
