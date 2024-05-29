package org.springblade.modules.admin.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class WallectHistoryVo {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户id
	 */
	@ApiModelProperty("用户id")
	private Long userId;

	/**
	 * 类型：0-Deposit 1-Withdraw 2-Earn 3-Collect 4-Refund 5-Sell 6-Creartor Earnings
	 */
	@ApiModelProperty("类型：0-Deposit 1-Withdraw 2-Earn 3-Collect 4-Refund 5-Sell 6-Creartor Earnings")
	private Integer type;

	/**
	 * 付款方地址
	 */
	@ApiModelProperty("付款方地址")
	private Long fromAddress;

	/**
	 * 收款方地址
	 */
	@ApiModelProperty("收款方地址")
	private Long toAddress;

	/**
	 * 链上交易哈希
	 */
	@ApiModelProperty("链上交易哈希")
	private String txnHash;

	/**
	 * 金额
	 */
	@ApiModelProperty("金额")
	private BigDecimal price;

	/**
	 * 时间
	 */
	@ApiModelProperty("时间")
	private Date createTime;

}
