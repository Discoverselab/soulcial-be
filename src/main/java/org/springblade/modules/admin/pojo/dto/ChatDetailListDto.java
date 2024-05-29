package org.springblade.modules.admin.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatDetailListDto {

	@ApiModelProperty("群聊/单聊 id")
	private Long chatId;

	@ApiModelProperty("消息ID")
	private Long messageId;

	@ApiModelProperty("发送者id")
	private Long sendUserId;

	@ApiModelProperty("消息内容")
	private String content;

	@ApiModelProperty("消息类型")
	private String type;

	@ApiModelProperty("消息发送时间")
	private String time;



}
