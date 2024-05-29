package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springblade.modules.admin.config.BigDecimalHandler;

import java.math.BigDecimal;

/**
 * tb_member实体类
 *
 * @author yuanxx
 *
 */
@Data
@ApiModel("钱包历史记录表")
@TableName(value = "tb_wallect_history",autoResultMap = true)
@NoArgsConstructor
@AllArgsConstructor
public class WallectHistoryPO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户id
	 */
	private Long userId;

	/**
	 * 类型：0-Deposit 1-Withdraw 2-Earn 3-Collect 4-Refund 5-Sell 6-Creartor Earnings
	 */
	private Integer type;

	/**
	 * transaction_id
	 */
	private Long transactionId;

	/**
	 * 链上交易哈希
	 */
	private String txnHash;

	/**
	 * 金额
	 */
	@TableField(typeHandler = BigDecimalHandler.class)
	private BigDecimal price;

}
