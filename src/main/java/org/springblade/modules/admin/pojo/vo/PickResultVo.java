package org.springblade.modules.admin.pojo.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springblade.modules.admin.config.BigDecimalHandler;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("VO")
@AllArgsConstructor
@NoArgsConstructor
public class PickResultVo implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long pickId;


	/**
	 * 开奖的行高
	 */
	@ApiModelProperty("开奖的行高")
	private Long rewardBlockHeight;

	/**
	 * 开奖的区块哈希
	 */
	@ApiModelProperty("开奖的区块哈希")
	private String rewardBlockHash;

	/**
	 * 中签签号：0/1/2/3
	 */
	@ApiModelProperty("中签签号：0/1/2/3")
	private Integer rewardIndex;

	/**
	 * 用户昵称
	 */
	@ApiModelProperty("用户昵称")
	private String userName;

	/**
	 * NFT_ID
	 */
	@ApiModelProperty("tokenId")
	private Long tokenId;

	/**
	 * 获得的积分
	 */
	@ApiModelProperty("获得的积分")
	private BigDecimal vSoulPrice;

	/**
	 * 得到的wBNB金额
	 */
	@ApiModelProperty("得到的wBNB金额")
	private BigDecimal rewardPirce;

	/**
	 * NFT信息
	 */
	@ApiModelProperty("NFT信息")
	private PFPTokenDetailVo pfpTokenDetailVo;
}
