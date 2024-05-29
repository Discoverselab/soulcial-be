package org.springblade.modules.admin.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatDetailDto {


	@ApiModelProperty("tokenid")
	private Long tokenId;

	@ApiModelProperty("address")
	private String address;

	@ApiModelProperty("群聊/单聊 展示图标url")
	private String avatar;

	@ApiModelProperty("群聊人数")
	private Long memberNumber;

	@ApiModelProperty("单聊or群聊类型 0 单聊 1 群聊")
	private Long type;

	@ApiModelProperty("用户id")
	private Long userId;

	@ApiModelProperty("title")
	private String  title;

	@ApiModelProperty("status 状态 0 不可用 1 可用")
	private String  status;

}
