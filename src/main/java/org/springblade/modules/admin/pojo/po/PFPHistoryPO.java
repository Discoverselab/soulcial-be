package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.modules.admin.config.BigDecimalHandler;

import java.math.BigDecimal;

/**
 * tb_pfp_transaction实体类
 *
 * @author yuanxx
 *
 */
@Data
@ApiModel("PFP代币历史记录PO")
@TableName(value = "tb_pfp_history",autoResultMap = true)
public class PFPHistoryPO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	*主键
	*/
	private Long id;

	/**
	*代币id
	*/
	private Long tokenId;

	/**
	 *类型：0-铸造(Mint) 1-被购买(Collect) 2-接收出价(Pick)
	 */
	@ApiModelProperty("类型：0-铸造(Mint) 1-被购买(Collect) 2-接收出价(Pick)")
	private Integer type;


	/**
	 *关联的交易ID
	 */
	private Long transactionId;

	/**
	* admin账号地址
	*/
	private String adminAddress;
	/**
	*合约所在链：0-BNB Chain 1-Ethereum 2-Polygon
	*/
	private Integer linkType;
	/**
	*链名：BNB Chain/Ethereum/Polygon
	*/
	private String network;
	/**
	*合约地址
	*/
	private String contractAddress;
	/**
	*合约名称
	*/
	private String contractName;
	/**
	*卖方地址
	*/
	@ApiModelProperty("from地址")
	private String fromAddress;
	/**
	*买方地址
	*/
	@ApiModelProperty("to地址")
	private String toAddress;
	/**
	*卖方用户id
	*/
	@ApiModelProperty("from用户ID")
	private Long fromUserId;
	/**
	*买房用户id
	*/
	@ApiModelProperty("to用户ID")
	private Long toUserId;

	/**
	 * 链上交易哈希
	 */
	@ApiModelProperty("NFT的链上交易哈希")
	private String txnHash;

	/**
	 * 成交价格
	 */
	@ApiModelProperty("成交价格")
	@TableField(typeHandler = BigDecimalHandler.class)
	private BigDecimal price;

	@TableField(exist = false)
	@ApiModelProperty("交易时间")
	private String transTimeStr;

}
