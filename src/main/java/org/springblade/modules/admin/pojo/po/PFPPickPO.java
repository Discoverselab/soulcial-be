package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.modules.admin.config.BigDecimalHandler;

import java.math.BigDecimal;
import java.util.Date;

/**
 * tb_pfp_token实体类
 *
 * @author yuanxx
 *
 */
@Data
@ApiModel("PFPPickPO")
@TableName(value = "tb_pfp_pick",autoResultMap = true)
public class PFPPickPO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	*主键
	*/
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 *主键
	 */
	private Long tokenId;

	/**
	* 代币铸造用户地址
	*/
	private String mintUserAddress;

	/**
	* 代币铸造用户id
	*/
	private Long mintUserId;

	/**
	 * 卖家钱包地址
	 */
	private String sellerUserAddress;

	/**
	 * 卖家用户ID
	 */
	private Long sellerUserId;

	/**
	 *level
	 */
	private Integer level;

	/**
	 * 底价
	 */
	@TableField(typeHandler = BigDecimalHandler.class)
	private BigDecimal basePrice;

	/**
	 * 已成交的次数
	 */
	private Integer transactionsCount;

	/**
	 *出售价格
	 */
	@TableField(typeHandler = BigDecimalHandler.class)
	private BigDecimal price;

	/**
	 * 交易状态：0-拼团中 1-待开奖 2-已开奖 3-已取消
	 */
	private Integer status;

	/**
	 * 当前pick人数：0-4
	 */
	private Integer nowPickCount;

	/**
	 * 满员时间
	 */
	private Date fullPickTime;

	/**
	 * 中签用户地址
	 */
	private String rewardUserAddress;

	/**
	 * 中签用户id
	 */
	private Long rewardUserId;

	/**
	 * 开奖的行高
	 */
	private Long rewardBlockHeight;

	/**
	 * 开奖的区块哈希
	 */
	private String rewardBlockHash;

	/**
	 * 中签签号：0/1/2/3
	 */
	private Integer rewardIndex;

	/**
	 * 开奖时间
	 */
	private Date rewardTime;

	/**
	 * 未中奖得到的金额
	 */
	@TableField(typeHandler = BigDecimalHandler.class)
	private BigDecimal rewardPrice;

	/**
	 * NFT交易哈希
	 */
	private String nftTxn;

	/**
	 * 铸造者收益
	 */
	@TableField(typeHandler = BigDecimalHandler.class)
	private BigDecimal minterRewardPrice;

	/**
	 * 铸造者收益哈希
	 */
	private String minterRewardTxn;

	/**
	 * 卖出者收益
	 */
	@TableField(typeHandler = BigDecimalHandler.class)
	private BigDecimal sellerRewardPrice;

	/**
	 * 卖出者收益流水号
	 */
	private String sellerRewardTxn;

	/**
	 * 签号0用户地址
	 */
	private String indexAddress0;

	/**
	 * 签号0用户id
	 */
	private Long indexUserId0;

	/**
	 * 签号0付款哈希
	 */
	private String indexPayTxn0;

	/**
	 * 签号0收款哈希
	 */
	private String indexRewardTxn0;

	/**
	 * 签号1用户地址
	 */
	private String indexAddress1;

	/**
	 * 签号1用户id
	 */
	private Long indexUserId1;

	/**
	 * 签号1付款哈希
	 */
	private String indexPayTxn1;

	/**
	 * 签号1收款哈希
	 */
	private String indexRewardTxn1;

	/**
	 * 签号2用户地址
	 */
	private String indexAddress2;

	/**
	 * 签号2用户id
	 */
	private Long indexUserId2;

	/**
	 * 签号2付款哈希
	 */
	private String indexPayTxn2;

	/**
	 * 签号2收款哈希
	 */
	private String indexRewardTxn2;

	/**
	 * 签号3用户地址
	 */
	private String indexAddress3;

	/**
	 * 签号3用户id
	 */
	private Long indexUserId3;

	/**
	 * 签号3付款哈希
	 */
	private String indexPayTxn3;

	/**
	 * 签号3收款哈希
	 */
	private String indexRewardTxn3;

	/**
	 * 签号0用户pickTime
	 */
	private Date indexUserPickTime0;
	/**
	 * 签号1用户pickTime
	 */
	private Date indexUserPickTime1;
	/**
	 * 签号2用户pickTime
	 */
	private Date indexUserPickTime2;
	/**
	 * 签号3用户pickTime
	 */
	private Date indexUserPickTime3;

}
