package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Auther: FengZi
 * @Date: 2024/1/5 17:45
 * @Description:
 */
@Data
@ApiModel("统计指标记录表")
@TableName(value = "tb_statistics_data",autoResultMap = true)
public class StatisticsDataPO{

	@ApiModelProperty("主键")
	@TableId
	private Integer id; // id

	@ApiModelProperty("统计数据的日期，主键")
	private Date statDate; // 统计数据的日期，主键

	@ApiModelProperty("当日新注册的用户数（通过用户ID去重）")
	private Integer newUsersIdUnique; // 当日新注册的用户数（通过用户ID去重）

	@ApiModelProperty("当日新注册的用户数（通过用户地址去重）")
	private Integer newUsersAddressUnique; // 当日新注册的用户数（通过用户地址去重）

	@ApiModelProperty("当日mint出的NFT数量")
	private Integer mintedNftCount; // 当日mint出的NFT数量

	@ApiModelProperty("参与pump活动的用户数")
	private Integer usersParticipatingPump; // 参与pump活动的用户数

	@ApiModelProperty("当日进行pump活动的次数")
	private Integer pumpOccurrences; // 当日进行pump活动的次数

	@ApiModelProperty("当日进行开奖的次数")
	private Integer raffleOccurrences; // 当日进行开奖的次数

	@ApiModelProperty("当日通过pump获得的总金额")
	private BigDecimal pumpTotalAmount; // 当日通过pump获得的总金额

	@ApiModelProperty("至该日期的累计注册用户数")
	private Integer cumulativeUsers; // 至该日期的累计注册用户数

	@ApiModelProperty("至该日期的累计mint出的NFT数量")
	private Integer cumulativeMintedNft; // 至该日期的累计mint出的NFT数量

	@ApiModelProperty("至该日期的累计pump的总金额")
	private BigDecimal cumulativePumpAmount; // 至该日期的累计pump的总金额


}
