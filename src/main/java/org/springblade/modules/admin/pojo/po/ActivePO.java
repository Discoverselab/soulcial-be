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
@ApiModel("PFP代币历史记录PO")
@TableName(value = "tb_active",autoResultMap = true)
public class ActivePO extends BasePO {

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
	 *类型：0-list 1-pick
	 */
	@ApiModelProperty("类型：0 - list 1 - pick 2 -cancel list 3-开奖通知")
	private Integer type;

	@ApiModelProperty("pickCount")
	private Integer pickCount;

	/**
	* token图片
	*/
	private String tokenImg;
	/**
	 * user
	 */
	private String username;
	/**
	* user地址
	*/
	private String userAddress;
	/**
	* user头像
	*/
	private String userImg;

	/**
	 * 成交价格
	 */
	@ApiModelProperty("成交价格")
	@TableField(typeHandler = BigDecimalHandler.class)
	private BigDecimal price;

	/**
	 * 当前区块高度
	 */
	private Long marketBlockNumber;

	/**
	 * 中奖用户
	 * @author FengZi
	 * @date 18:50 2023/11/23
	 * @param null
	 * @return null
	 **/
	@ApiModelProperty("中奖用户")
	private Long lotteryUserId;

	/**
	 * tokenUserId nft铸造者
	 * @author FengZi
	 * @date 18:50 2023/11/23
	 * @param null
	 * @return null
	 **/
	@ApiModelProperty("nft铸造者")
	private Long tokenUserId;


	/**
	 * nft铸造者用户名
	 * @author FengZi
	 * @date 18:50 2023/11/23
	 * @param null
	 * @return null
	 **/
	@ApiModelProperty("nft铸造者用户名")
	@TableField(exist = false)
	private String tokenUserName;

	/**
	 * pick状态：0-不可pick 1-可以pick 2-待开奖 3-开奖中
	 * @author FengZi
	 * @date 18:50 2023/11/23
	 * @param null
	 * @return null
	 **/
	@ApiModelProperty("pick状态：0-不可pick 1-可以pick 2-待开奖 3-开奖中")
	private Long pickStatus;

	@ApiModelProperty("活动id")
	private Long eventId;

	@ApiModelProperty("活动签到id")
	private Long eventUserId;

	@ApiModelProperty("活动名称")
	@TableField(exist = false)
	private String eventName;

	@ApiModelProperty("活动banner")
	@TableField(exist = false)
	private String eventBannerUrl;

}
