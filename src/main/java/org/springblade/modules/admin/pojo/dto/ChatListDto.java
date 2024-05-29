package org.springblade.modules.admin.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatListDto {

	@ApiModelProperty("群聊/单聊 id")
	private Long id;

	@ApiModelProperty("群聊/单聊 标题")
	private String title;

	@ApiModelProperty("群聊/单聊 类型")
	private String type;

	@ApiModelProperty("群聊/单聊 状态")
	private String status;

	@ApiModelProperty("ftp token")
	private Long tokenId;

	@ApiModelProperty("群聊/单聊 最后一条消息发送者")
	private Long sendUserId;

	@ApiModelProperty("群聊/单聊 最后一条消息")
	private String content;

	@ApiModelProperty("群聊/单聊 最后一条消息类型")
	private String messageType;

	@ApiModelProperty("群聊/单聊 最后一条消息发送时间")
	private String time;

	@ApiModelProperty("群聊/单聊 未读数")
	private String unreadNum;

	@ApiModelProperty("该聊天中所有用户信息")
	private List<Long> userIds;

	@ApiModelProperty("活动群聊 bannerurl")
	private String eventBannerUrl;

}
