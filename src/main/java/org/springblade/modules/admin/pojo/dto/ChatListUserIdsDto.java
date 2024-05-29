package org.springblade.modules.admin.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatListUserIdsDto {

	//聊天id
	private Long chatId;

	//用户ids
	private List<Long> userIds;

}
