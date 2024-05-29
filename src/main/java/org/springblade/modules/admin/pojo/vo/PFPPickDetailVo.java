package org.springblade.modules.admin.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel("PFPPick详情VO")
@AllArgsConstructor
@NoArgsConstructor
public class PFPPickDetailVo implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 交易状态：0-拼团中 1-待开奖 2-已开奖 3-已取消
	 */
	@ApiModelProperty("交易状态：0-拼团中 1-待开奖 2-已开奖 3-已取消")
	private Integer status;

	/**
	 * 当前pick人数：0-4
	 */
	@ApiModelProperty("当前pick人数：0-4")
	private Integer nowPickCount;

	/**
	 * 开奖时间
	 */
	@ApiModelProperty("开奖时间")
	private Date rewardTime;

	/**
	 * 开奖倒计时
	 */
	@ApiModelProperty("开奖倒计时：单位：分钟")
	private String rewardTimeStr;

	/**
	 * 签号0用户地址
	 */
	@ApiModelProperty("签号0用户地址")
	private String indexAddress0;

	/**
	 * 签号0用户id
	 */
	@ApiModelProperty("签号0用户id")
	private Long indexUserId0;

	/**
	 * 签号0用户昵称
	 */
	@ApiModelProperty("签号0用户昵称")
	private String indexUserName0;

	/**
	 * 签号0用户头像
	 */
	@ApiModelProperty("签号0用户头像")
	private String indexAvatar0;

	/**
	 * 签号1用户地址
	 */
	@ApiModelProperty("签号1用户地址")
	private String indexAddress1;

	/**
	 * 签号1用户id
	 */
	@ApiModelProperty("签号1用户id")
	private Long indexUserId1;

	/**
	 * 签号1用户昵称
	 */
	@ApiModelProperty("签号1用户昵称")
	private String indexUserName1;

	/**
	 * 签号1用户头像
	 */
	@ApiModelProperty("签号1用户头像")
	private String indexAvatar1;

	/**
	 * 签号2用户地址
	 */
	@ApiModelProperty("签号2用户地址")
	private String indexAddress2;

	/**
	 * 签号2用户id
	 */
	@ApiModelProperty("签号2用户id")
	private Long indexUserId2;

	/**
	 * 签号2用户昵称
	 */
	@ApiModelProperty("签号2用户昵称")
	private String indexUserName2;

	/**
	 * 签号2用户头像
	 */
	@ApiModelProperty("签号2用户头像")
	private String indexAvatar2;

	/**
	 * 签号3用户地址
	 */
	@ApiModelProperty("签号3用户地址")
	private String indexAddress3;

	/**
	 * 签号3用户id
	 */
	@ApiModelProperty("签号3用户id")
	private Long indexUserId3;

	/**
	 * 签号3用户昵称
	 */
	@ApiModelProperty("签号3用户昵称")
	private String indexUserName3;

	/**
	 * 签号3用户头像
	 */
	@ApiModelProperty("签号3用户头像")
	private String indexAvatar3;

	/**
	 * 开奖的行高
	 */
	@ApiModelProperty("开奖的行高")
	private Long rewardBlockHeight;

	/**
	 * 是否可以退款 0可以退款，1不可退款
	 * @author FengZi
	 * @date 16:55 2023/12/19
	 * @param null
	 * @return null
	 **/
	@ApiModelProperty("是否可以退款 0不可退款，1可以退款")
	private String isRefundPick = "0";

}
