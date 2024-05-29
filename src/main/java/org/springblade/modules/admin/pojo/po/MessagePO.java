package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * tb_member实体类
 *
 * @author yuanxx
 *
 */
@Data
@ApiModel("聊天表")
@TableName("tb_message")
@NoArgsConstructor
@AllArgsConstructor
public class MessagePO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	 * 类型：0-系统消息 1-用户聊天
	 */
	private Integer type;

	/**
	 * 发送方用户id
	 */
	private Long fromUserId;

	/**
	 * 接收方用户id
	 */
	private Long toUserId;

	/**
	 * 消息内容
	 */
	@ApiModelProperty("消息标识")
	private String message;

	/**
	 * 标题
	 */
	@ApiModelProperty("消息内容")
	private String title;

	/**
	 * 内容
	 */
	@ApiModelProperty("内容")
	private String content;

	/**
	 * 已读：0-否 1-是
	 */
	private Integer isRead;

	/**
	 * 业务ID
	 */
	private Long businessId;

	/**
	 * pickId
	 */
	private Long pickId;

}
