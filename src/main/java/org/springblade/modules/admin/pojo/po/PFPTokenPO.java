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
@ApiModel("PFPtokenPO")
@TableName(value = "tb_pfp_token",autoResultMap = true)
public class PFPTokenPO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	*主键
	*/
	@TableId(type = IdType.AUTO)
	private Long id;
	/**
	 * 链上真实合约id
	 */
	private Long RealTokenId;
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
	 *合约地址
	 */
	private String contractMarketAddress;
	/**
	*合约名称
	*/
	private String contractName;
	/**
	*代币链上所属地址
	*/
	private String ownerAddress;
	/**
	*代币所属用户ID
	*/
	private Long ownerUserId;
	/**
	*铸造状态：0-未铸造 1-已铸造 2-铸造中
	*/
	private Integer mintStatus;
	/**
	*代币铸造用户地址
	*/
	private String mintUserAddress;
	/**
	*代币铸造用户id
	*/
	private Long mintUserId;
	/**
	 *铸造交易哈希
	 */
	private String mintTxnHash;
	/**
	 * 铸造时，用户的标签
	 */
	private String mintUserTags;
	/**
	 *铸造时间
	 */
	private Date mintTime;
	/**
	 *图片url
	 */
	private String pictureUrl;

	@ApiModelProperty("方形图片url")
	private String squarePictureUrl;

	@ApiModelProperty("图片颜色属性")
	private Integer colorAttribute;

	/**
	 *personality
	 */
	private Integer personality;

	/**
	 *mood
	 */
	private Integer mood;

	/**
	 *weather
	 */
	private Integer weather;

	/**
	 *color
	 */
	private Integer color;

	/**
	 * 交易状态：0-可交易 1-交易中 2-不可交易
	 */
	private Integer status;

	/**
	 * pick状态：0-不可pick 1-可以pick 2-待开奖 3-开奖中
	 */
	private Integer pickStatus;

	/**
	 * pick_id
	 */
	@TableField(updateStrategy = FieldStrategy.IGNORED)
	private Long pickId;

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
//	@TableField(updateStrategy = FieldStrategy.IGNORED)
	@TableField(typeHandler = BigDecimalHandler.class)
	private BigDecimal price;

	/**
	 *设置出售价格的时间
	 */
	@TableField(updateStrategy = FieldStrategy.IGNORED)
	private Date priceTime;

	/**
	 *喜欢人数
	 */
	private Integer likes;

	/**
	 * 用于排序
	 */
	@TableField("rank_score")
	private Long rank;

	// pick后rank增加10
	public void pumpAddRank(){
		if (this.rank == null){
			this.rank = 10L;
		} else {
			// 最大为99
			this.rank = Math.min(this.rank + 10, 99);
		}
	}

	// 开奖后为原来二分之一
	public void dealItemRank(){
		if (this.rank == null){
			this.rank = 0L;
		} else {
			// 除不尽的情况下，向下取整
			this.rank = this.rank / 2;
		}
	}

	/**
	 *level
	 */
	private Integer level;

	/**
	 *level
	 */
	private Integer levelScore;

	private Integer charisma;
	private Integer extroversion;
	private Integer energy;
	private Integer wisdom;
	private Integer art;
	private Integer courage;

	/**
	 * 身份：Personality+Character
	 */
	private String soul;

	@TableField(typeHandler = BigDecimalHandler.class)
	private BigDecimal lastSale;

	private Date lastSaleTime;

	public void countLevelScore(){
		int courage = this.courage == null ? 0 : this.courage;
		int charisma = this.charisma == null ? 0 : this.charisma;
		int extroversion = this.extroversion == null ? 0 : this.extroversion;
		int energy = this.energy == null ? 0 : this.energy;
		int wisdom = this.wisdom == null ? 0 : this.wisdom;
		int art = this.art == null ? 0 : this.art;

		this.levelScore = courage + charisma + extroversion + energy + wisdom + art;
	}

	public void countLevel(){
		if(this.levelScore == null || this.levelScore < 200){
			this.level = 1;
		} else if(this.levelScore < 300){
			this.level = 2;
		} else if(this.levelScore < 400){
			this.level = 3;
		} else if(this.levelScore < 500){
			this.level = 4;
		} else {
			this.level = 5;
		}
	}
}
