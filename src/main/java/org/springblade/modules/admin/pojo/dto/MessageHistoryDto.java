package org.springblade.modules.admin.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MessageHistoryDto {

	@ApiModelProperty("群聊/单聊 id")
	private Long chatId;


	@ApiModelProperty("群聊/单聊 未读数")
	private Long unreadNum;
}
