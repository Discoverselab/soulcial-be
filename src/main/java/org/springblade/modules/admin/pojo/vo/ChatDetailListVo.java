package org.springblade.modules.admin.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@ApiModel("聊天消息详情VO")
@AllArgsConstructor
@NoArgsConstructor
public class ChatDetailListVo implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("用户id")
	private Long userId;

	@ApiModelProperty("用户头像url")
	private String userAvatar;

	@ApiModelProperty("用户名")
	private String userName;

	@ApiModelProperty("用户类型")
	private String type;

	@ApiModelProperty("信息ID")
	private Long messageId;

	@ApiModelProperty("内容")
	private String content;

	@ApiModelProperty("消息时间")
	private String time;

}
