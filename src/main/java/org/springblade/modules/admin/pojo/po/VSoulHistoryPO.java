package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springblade.modules.admin.config.BigDecimalHandler;

import java.math.BigDecimal;

/**
 * tb_pfp_transaction实体类
 *
 * @author yuanxx
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("积分历史记录表PO")
@TableName(value = "tb_vSoul_history",autoResultMap = true)
public class VSoulHistoryPO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	* 用户id
	*/
	private Long userId;

	/**
	 * 类型：1-pick 2-promotion 3-Invite
	 */
	@ApiModelProperty("类型: 1-pick 2-promotion 3-Invite 4-New Soul  5-Invite Pick  7-double VSoul")
	private Integer type;

	/**
	 * pickId
	 */
	private Long pickId;

	/**
	 * 积分金额
	 */
	@ApiModelProperty("积分金额")
	@TableField(typeHandler = BigDecimalHandler.class)
	private BigDecimal vSoulPrice;

}
