package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 活动收藏表
 * @Auther: FengZi
 * @Date: 2024/2/19 16:08
 * @Description:
 */
@Data
@ApiModel("活动收藏表")
@TableName(value = "tb_event_favorites")
public class EventFavoritesPO extends BasePO {


	/**
	 *主键
	 */
	private Long id;

	/**
	 * 活动id
	 **/
	private Long eventId;

	/**
	 * 用户id
	 **/
	private Long userId;


}
