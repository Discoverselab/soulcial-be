package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * tb_member_connect实体类
 *
 * @author yuanxx
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("用户连接PO")
@TableName("tb_member_connect")
public class MemberConnectPO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	 * 发起连接的用户id
	 */
	private Long userId;

	/**
	 * 被连接的用户id
	 */
	private Long toUserId;

	/**
	 * 0-待确认，1-已连接，2-star连接
	 */
	private Integer status;
}
