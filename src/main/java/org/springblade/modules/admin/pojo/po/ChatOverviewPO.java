package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("聊天概览PO")
@TableName(value = "tb_chat_overview", autoResultMap = true)
public class ChatOverviewPO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	 * 类型：0-单聊 1-群聊
	 */
	@ApiModelProperty("类型：0-单聊 1-群聊")
	private Integer type;

	/**
	 * token_id
	 */
	@ApiModelProperty("Token ID")
	private Long tokenId;

	/**
	 * 状态：0-不可用 1-可用
	 */
	@ApiModelProperty("状态：0-不可用 1-可用")
	private Integer status;

	/**
	 * 标题
	 */
	@ApiModelProperty("标题")
	private String title;
}
