package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * tb_member_invite实体类
 *
 * @author yuanxx
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("用户邀请码PO")
@TableName("tb_member_invite")
public class MemberInvitePO extends BasePO{

	/**
	 * 用户id
	 */
	private Long userId;

	/**
	 * 邀请码
	 */
	@ApiModelProperty("邀请码")
	private String inviteCode;

	/**
	 * 0-未使用，1-已使用
	 */
	@ApiModelProperty(" 0-未使用，1-已使用")
	private Integer used;
}
