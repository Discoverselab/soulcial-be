package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.modules.admin.config.BigDecimalHandler;

import java.math.BigDecimal;

/**
 * active实体类
 *
 * @author yuanxx
 *
 */
@Data
@ApiModel("交易历史记录表")
@TableName(value = "tb_txn_history",autoResultMap = true)
public class TxnHistoryPO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	*主键
	*/
	@ApiModelProperty("主键")
	private Long id;

	/**
	*代币id
	*/
	@ApiModelProperty("tokenid")
	private Long tokenId;

	/**
	 * '交易哈希'
	 */
	@ApiModelProperty("交易哈希")
	private String txnHash;

	/**
	 * '交易方法'
	 */
	@ApiModelProperty("交易方法")
	private String method;

	/**
	 * 'block'
	 */
	@ApiModelProperty("block")
	private String block;

	/**
	 * 'fromuser'
	 */
	@ApiModelProperty("from")
	private String fromAddress;

	/**
	 * 'touser'
	 */
	@ApiModelProperty("to")
	private String toAddress;

	/**
	 * 'tradeValue'
	 */
	@ApiModelProperty("tradeValue")
	private String tradeValue;

	/**
	 * 'txnFee'
	 */
	@ApiModelProperty("txnFee")
	private String txnFee;

	/**
	 * '交易时间'
	 */
//	@ApiModelProperty("time")
//	private String time;



}
