package org.springblade.modules.admin.pojo.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Auther: FengZi
 * @Date: 2023/11/23 19:29
 * @Description:
 */
@Data
public class ActivePageQuery {

//	@ApiParam(value = "当前页", required = true)
	Integer current = 1;
//	@ApiParam(value = "每页的数量", required = true)
	Integer size = 10;
//	/**
//	 * 开奖状态 0 未开奖 1 已开奖
//	 * @author FengZi
//	 * @date 18:50 2023/11/23
//	 * @param null
//	 * @return null
//	 **/
//	@ApiModelProperty("开奖状态 0 未开奖 1 已开奖")
//	private String checkLotteryStatus;


	/**
	 * pick状态：0-不可pick 1-可以pick 2-待开奖 3-开奖中
	 * @author FengZi
	 * @date 18:50 2023/11/23
	 * @param null
	 * @return null
	 **/
	@ApiModelProperty("开奖状态 0 未开奖 1 已开奖")
	private String pickStatus;

}
