package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("聊天成员PO")
@TableName(value = "tb_chat_member", autoResultMap = true)
public class ChatMemberPO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	 * 聊天id
	 */
	@ApiModelProperty("聊天ID")
	private Long chatId;

	/**
	 * 类型：0-后续拓展
	 */
	@ApiModelProperty("类型：0-后续拓展")
	private Integer type;

	/**
	 * 用户id
	 */
	@ApiModelProperty("用户ID")
	private Long userId;
}
