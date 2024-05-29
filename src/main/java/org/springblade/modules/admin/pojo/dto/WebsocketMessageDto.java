package org.springblade.modules.admin.pojo.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebsocketMessageDto {

	/**
	 * 聊天id
	 */
	@ApiModelProperty("消息id")
	private Long messageId;

	/**
	 * 用户名
	 */
	@ApiModelProperty("用户名")
	private String userName;

	/**
	 * 头像
	 */
	@ApiModelProperty("头像")
	private String userAvatar;

	/**
	 * 类型：0-文本 1-图片
	 */
	@ApiModelProperty("消息类型：0-文本 1-图片 99-系统消息")
	private Integer type;

	/**
	 * 消息用户id
	 */
	@ApiModelProperty("消息发送者id")
	private Long userId;

	/**
	 * chatId
	 */
	@ApiModelProperty("chatId")
	private Long chatId;

	/**
	 * 内容（字符串或url地址）
	 */
	@ApiModelProperty("消息内容（字符串或url地址）")
	private String content;

	/**
	 *创建时间
	 */
	@ApiModelProperty("创建时间")
	private String time;


}
