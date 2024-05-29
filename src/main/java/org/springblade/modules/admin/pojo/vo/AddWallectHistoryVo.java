package org.springblade.modules.admin.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.wildfly.common.annotation.NotNull;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class AddWallectHistoryVo {

	private static final long serialVersionUID = 1L;

	/**
	 * 类型：0-Deposit 1-Withdraw 2-Earn 3-Collect 4-Refund 5-Sell 6-Creartor Earnings
	 */
	@NotNull
	@ApiModelProperty("类型：0-Deposit 1-Withdraw 2-Earn 3-Collect 4-Refund 5-Sell 6-Creartor Earnings")
	private Integer type;

	/**
	 * 链上交易哈希
	 */
	@NotBlank
	@ApiModelProperty("链上交易哈希")
	private String txnHash;

	/**
	 * 金额
	 */
	@NotNull
	@ApiModelProperty("金额")
	private BigDecimal price;

}
