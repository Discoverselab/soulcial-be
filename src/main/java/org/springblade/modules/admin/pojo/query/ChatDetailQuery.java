package org.springblade.modules.admin.pojo.query;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("聊天消息详情Query")
@AllArgsConstructor
@NoArgsConstructor
public class ChatDetailQuery implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull
	@ApiModelProperty("群聊/单聊 ID")
	private Long chatId;

	@ApiModelProperty("messageId 用户已经展示的最上面的一条消息messageId")
	private Long messageId;

	@ApiModelProperty("查询多少条。默认20")
	private Long size = 20L;

	@ApiModelProperty("查询多少条。默认20")
	private Long userId;


}
