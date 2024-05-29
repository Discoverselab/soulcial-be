package org.springblade.modules.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springblade.core.tool.api.R;
import org.springblade.modules.admin.pojo.dto.AddEventDto;
import org.springblade.modules.admin.pojo.dto.UpdateEventDto;
import org.springblade.modules.admin.pojo.po.EventPO;
import org.springblade.modules.admin.pojo.vo.EventListVo;

import java.math.BigDecimal;

public interface EventService {

	/**
	 * 添加活动
	 *
	 * @param dto
	 * @return boolean
	 * @author FengZi
	 * @date 14:07 2024/2/20
	 **/
	boolean addEvent(AddEventDto dto);

	/**
	 * 修改活动
	 * @author FengZi
	 * @date 13:53 2024/2/29
	 * @param dto
	 * @return boolean
	 **/
	boolean updateEvent(UpdateEventDto dto);

	/**
	 * 获取活动列表
	 *
	 * @param type
	 * @param current
	 * @param size
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<org.springblade.modules.admin.pojo.vo.EventListVo>
	 * @author FengZi
	 * @date 14:08 2024/2/20
	 **/
	Page<EventListVo> getEventList(Integer type, Integer current, Integer size);

	/**
	 * 获取活动id
	 *
	 * @param eventId
	 * @return org.springblade.modules.admin.pojo.po.EventPO
	 * @author FengZi
	 * @date 14:11 2024/2/20
	 **/
	EventPO getEventById(Long eventId);

	/**
	 * 活动签到
	 * @author FengZi
	 * @date 19:29 2024/2/21
	 * @param eventId
	 * @param longitude
	 * @param latitude
	 * @return boolean
	 **/
	R<?> eventCheckIn(Long eventId, BigDecimal longitude, BigDecimal latitude);

	/**
	 * 活动收藏
	 * @author FengZi
	 * @date 10:45 2024/2/22
	 * @param eventId
	 * @return boolean
	 **/
	boolean eventStar(Long eventId);

	/**
	 * 活动取消收藏
	 * @author FengZi
	 * @date 10:45 2024/2/22
	 * @param eventId
	 * @return boolean
	 **/
	boolean eventUnStar(Long eventId);

}
