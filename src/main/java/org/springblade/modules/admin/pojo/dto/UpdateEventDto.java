package org.springblade.modules.admin.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Auther: FengZi
 * @Date: 2024/2/19 16:28
 * @Description:
 */
@Data
public class UpdateEventDto {

	/**
	 * 活动id
	 **/
	@ApiModelProperty("活动id")
	private Long eventId;

	/**
	 * 活动名称
	 **/
	@ApiModelProperty("活动名称")
	private String eventName;

	/**
	 * 海报banner
	 **/
	@ApiModelProperty("海报bannerUrl")
	private String bannerUrl;
//	private MultipartFile bannerFile;


	/**
	 * 主办方名称
	 **/
	@ApiModelProperty("主办方名称")
	private String eventNameProviderl;

	/**
	 * 活动时间（列表页展示用）
	 **/
	@ApiModelProperty("活动时间（列表页展示用）")
	private String eventDateList;

	/**
	 * 活动时间（详情页展示用）
	 **/
	@ApiModelProperty("活动时间（详情页展示用）")
	private String eventDateDetail;

	/**
	 * 开始时间（实际计算用）
	 **/
	@ApiModelProperty("开始时间（实际计算用）")
	private String startTime;

	/**
	 * 结束时间（实际计算用）
	 **/
	@ApiModelProperty("结束时间（实际计算用）")
	private String endTime;

	/**
	 * 活动地址
	 **/
	@ApiModelProperty("活动地址")
	private String eventAddress;

	/**
	 * 活动城市
	 **/
	@ApiModelProperty("活动城市")
	private String eventCity;

	/**
	 * 打卡地经度
	 **/
	@ApiModelProperty("打卡地经度")
	private BigDecimal longitude;

	/**
	 * 打卡地纬度
	 **/
	@ApiModelProperty("打卡地纬度")
	private BigDecimal latitude;

	/**
	 * 打卡半径，单位米m
	 **/
	@ApiModelProperty("打卡半径，单位米m")
	private Integer distance;

	/**
	 * 活动描述
	 **/
	@ApiModelProperty("活动描述")
	private String intro;

	/**
	 * 积分奖励，默认500
	 **/
	@ApiModelProperty("积分奖励，默认500")
	private BigDecimal reward;

	/**
	 * 第三方链接
	 **/
	@ApiModelProperty("第三方链接")
	private String link;

	/**
	 * 优先级
	 **/
	@ApiModelProperty("优先级")
	private Integer priority;

}
