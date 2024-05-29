package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("聊天详情PO")
@TableName(value = "tb_chat_detail", autoResultMap = true)
public class ChatDetailPO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	 * 聊天id
	 */
	@ApiModelProperty("聊天ID")
	private Long chatId;

	/**
	 * 类型：0-文本 1-图片 99-系统消息
	 */
	@ApiModelProperty("类型：0-文本 1-图片 99-系统消息")
	private Integer type;

	/**
	 * 消息用户id
	 */
	@ApiModelProperty("消息用户ID")
	private Long userId;

	/**
	 * 内容（字符串或url地址）
	 */
	@ApiModelProperty("内容（字符串或url地址）")
	private String content;
}
