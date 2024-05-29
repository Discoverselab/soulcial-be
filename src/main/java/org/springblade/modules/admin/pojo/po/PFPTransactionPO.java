package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
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
@ApiModel("PFP交易信息PO")
@TableName(value = "tb_pfp_transaction",autoResultMap = true)
public class PFPTransactionPO extends BasePO {

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
	 *pickId
	 */
	private Long pickId;
	/**
	*admin账号地址
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
	private String fromAddress;
	/**
	*买方地址
	*/
	private String toAddress;
	/**
	*卖方用户id
	*/
	private Long fromUserId;
	/**
	*买房用户id
	*/
	private Long toUserId;
	/**
	*交易状态：0-待付款 1-已验证付款未交易PFP 2-交易完成 3-交易取消 4-付款验证失败 5-已付款未验证
	*/
	private Integer transactionStatus;
	/**
	* 买方付款的交易哈希
	*/
	private String buyerMoneyTxnHash;
	/**
	 * 卖方收款的交易哈希
	 */
	private String sellerMoneyTxnHash;
	/**
	 * 铸造者收款的交易哈希
	 */
	private String minterMoneyTxnHash;
	/**
	*PFP的交易哈希
	*/
	private String pfpTxnHash;
	/**
	*代币铸造用户id
	*/
	private Long mintUserId;
	/**
	*代币铸造用户地址
	*/
	private String mintUserAddress;

	/**
	 * 成交价格
	 */
	@TableField(typeHandler = BigDecimalHandler.class)
	private BigDecimal listPrice;

	/**
	 * 卖家获得金额
	 */
	@TableField(typeHandler = BigDecimalHandler.class)
	private BigDecimal sellerEarnPrice;

	/**
	 * 铸造者获得金额
	 */
	@TableField(typeHandler = BigDecimalHandler.class)
	private BigDecimal minterEarnPrice;

	/**
	 * 平台获得金额
	 */
	@TableField(typeHandler = BigDecimalHandler.class)
	private BigDecimal platformEarnPrice;

	/**
	 * 签号：0/1/2/3
	 */
	private Integer payIndex;


//	public void setListPrice(BigDecimal listPrice){
//		if(listPrice != null){
//			listPrice = listPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
//			this.listPrice = listPrice;
//
//			this.sellerEarnPrice = listPrice.multiply(new BigDecimal("0.95"));
//			BigDecimal earnPrice = listPrice.multiply(new BigDecimal("0.025"));
//
//			this.platformEarnPrice = earnPrice;
//			this.minterEarnPrice = earnPrice;
//		}
//	}


}
