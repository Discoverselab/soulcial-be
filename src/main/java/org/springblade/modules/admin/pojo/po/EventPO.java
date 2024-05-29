package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * active实体类
 *
 * @author yuanxx
 *
 */
@Data
@ApiModel("活动表")
@TableName(value = "tb_event")
public class EventPO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	*主键
	*/
	private Long id;

	/**
	 * 活动名称
	 **/
	private String eventName;

	/**
	 * 海报banner
	 **/
	private String eventBanner;

	/**
	 * 主办方名称
	 **/
	private String eventNameProviderl;

	/**
	 * 活动时间（列表页展示用）
	 **/
	private String eventDateList;

	/**
	 * 活动时间（详情页展示用）
	 **/
	private String eventDateDetail;

	/**
	 * 开始时间（实际计算用）
	 **/
	private Date startTime;

	/**
	 * 结束时间（实际计算用）
	 **/
	private Date endTime;

	/**
	 * 活动地址
	 **/
	private String eventAddress;

	/**
	 * 活动城市
	 **/
	private String eventCity;

	/**
	 * 打卡地经度
	 **/
	private BigDecimal longitude;

	/**
	 * 打卡地纬度
	 **/
	private BigDecimal latitude;

	/**
	 * 打卡半径，单位米m
	 **/
	private Integer distance;

	/**
	 * 活动描述
	 **/
	private String intro;

	/**
	 * 积分奖励，默认500
	 **/
	private BigDecimal reward;

	/**
	 * 第三方链接
	 **/
	private String link;

	/**
	 * 优先级
	 **/
	private Integer priority;

	/**
	 * 聊天室id
	 **/
	private Long chatOverviewId;


	@ApiModelProperty("当前登录用户是否签到,当前未登录用户返回null")
	@TableField(exist = false)
	private Boolean isCheckIn;

	@ApiModelProperty("当前登录用户是否收藏,当前未登录用户返回null")
	@TableField(exist = false)
	private Boolean isStar;


}
