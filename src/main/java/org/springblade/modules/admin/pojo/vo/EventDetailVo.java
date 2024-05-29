package org.springblade.modules.admin.pojo.vo;

import lombok.Data;

/**
 * 活动详情返回
 * @Auther: FengZi
 * @Date: 2024/2/20 13:18
 * @Description:
 */
@Data
public class EventDetailVo {

	/**
	 * 活动id
	 **/
	private Long eventId;

	/**
	 * 活动名称
	 **/
	private String eventName;

	/**
	 * 活动banner url
	 **/
	private String eventBannerUrl;

	/**
	 * 活动时间
	 **/
	private String eventDate;

	/**
	 * 活动地址
	 **/
	private String eventAddress;

}
