package org.springblade.modules.admin.pojo.vo;

import cn.hutool.core.date.DateUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
public class MessageVo  {

	private static final long serialVersionUID = 1L;

	/**
	 * message_id
	 */
	@ApiModelProperty("message_id")
	private Long id;

	/**
	 * 类型：0-系统消息 1-用户聊天
	 */
	@ApiModelProperty("类型：0-系统消息 1-用户聊天")
	private Integer type;

	/**
	 * 发送方用户id
	 */
	@ApiModelProperty("发送方用户id")
	private Long fromUserId;

	/**
	 * 发送方用户id
	 */
	@ApiModelProperty("发送方用户名")
	private String fromUserName;

	/**
	 * 发送方用户id
	 */
	@ApiModelProperty("发送方用户头像")
	private String fromUserAvatar;

	/**
	 * 接收方用户id
	 */
	@ApiModelProperty("接收方用户id")
	private Long toUserId;

	/**
	 * 发送方用户id
	 */
	@ApiModelProperty("接收方用户名")
	private String toUserName;

	/**
	 * 发送方用户id
	 */
	@ApiModelProperty("接收方用户头像")
	private String toUserAvatar;

	/**
	 * 消息内容
	 */
	@ApiModelProperty("消息标识")
	private String message;

	/**
	 * 标题
	 */
	@ApiModelProperty("消息内容")
	private String title;

	/**
	 * 内容
	 */
	@ApiModelProperty("内容")
	private String content;

	/**
	 * 已读：0-否 1-是
	 */
	@ApiModelProperty("已读：0-否 1-是")
	private Integer isRead;

	/**
	 * 业务ID
	 */
	@ApiModelProperty("业务ID")
	private Long businessId;

	/**
	 * 业务ID
	 */
	@ApiModelProperty("时间")
	private Date createTime;

	/**
	 * 消息时间
	 */
	@ApiModelProperty("消息时间")
	private String createTimeStr;

	public void setCreateTimeStr(Date createTime){
		if(createTime == null){
			this.createTimeStr = null;
		}else {
			Long diff = System.currentTimeMillis() - createTime.getTime();
			Long sec = diff / 1000;
			if(sec < 60){
				this.createTimeStr = "1min";
			}else {
				Long min = sec / 60;
				if(min < 60){
					this.createTimeStr = min + "min";
				}else {
					Long hour = min / 60;
					if(hour < 24){
						this.createTimeStr = hour + "h";
					}else {
						this.createTimeStr = DateUtil.format(createTime,"yyyy-MM-dd");
					}
				}
			}
		}
	}
}
