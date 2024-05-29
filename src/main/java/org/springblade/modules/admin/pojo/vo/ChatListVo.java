package org.springblade.modules.admin.pojo.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@ApiModel("聊天消息列表VO")
@AllArgsConstructor
@NoArgsConstructor
public class ChatListVo implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("群聊/单聊 id")
	private Long id;

	@ApiModelProperty("群聊/单聊 标题")
	private String title;

	@ApiModelProperty("群聊/单聊 展示图标url")
	private String avator;

	@ApiModelProperty("群聊/单聊 最后一条消息")
	private String relatedContent;

	@ApiModelProperty("群聊/单聊 最后一条消息时间")
	private String time;

	@ApiModelProperty("群聊/单聊 未读数")
	private Long unreadNum;

	@ApiModelProperty("群聊人数")
	private Long memberNum;

	@ApiModelProperty("群聊 tokenid")
	private Long tokenId;

	@ApiModelProperty("address")
	private String address;

	@ApiModelProperty("群聊/单聊 最后一条消息发送者")
	private String username;

	@ApiModelProperty("单聊or群聊类型 0 单聊 1 群聊")
	private String type;

	@ApiModelProperty("系统消息")
	private String sysMessage;

//	@ApiModelProperty("活动群聊 bannerurl")
//	private String eventBannerUrl;

}



