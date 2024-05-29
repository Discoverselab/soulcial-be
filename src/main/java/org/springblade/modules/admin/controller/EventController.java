package org.springblade.modules.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.api.R;
import org.springblade.modules.admin.pojo.dto.AddEventDto;
import org.springblade.modules.admin.pojo.dto.UpdateEventDto;
import org.springblade.modules.admin.pojo.po.EventPO;
import org.springblade.modules.admin.pojo.vo.EventListVo;
import org.springblade.modules.admin.service.EventService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @Auther: FengZi
 * @Date: 2024/2/19 16:18
 * @Description:
 */
@RestController
@Slf4j
@RequestMapping("/api/admin/event")
@Api(value = "活动", tags = "活动")
public class EventController {

	@Resource
	private EventService eventService;

	@PostMapping("/addEvent")
	@ApiOperation(value = "添加活动")
	public R<?> addEvent(
		@RequestBody AddEventDto dto) {
		boolean b = eventService.addEvent(dto);
		return b ? R.success("活动添加成功。") : R.fail("活动添加失败。");
	}

	@PostMapping("/updateEvent")
	@ApiOperation(value = "修改活动")
	public R<?> updateEvent(
		@RequestBody UpdateEventDto dto) {
		boolean b = eventService.updateEvent(dto);
		return b ? R.success("活动修改成功。") : R.fail("活动修改失败。");
	}

	@GetMapping("/getEventList")
	@ApiOperation(value = "获取活动列表")
	public R<?> getEventList(
		@ApiParam(value = "搜索类型 0 All， 1 Star， 2 Joined", required = true) @RequestParam("type") Integer type,
		@ApiParam(value = "当前页", required = true) @RequestParam("current") Integer current,
		@ApiParam(value = "每页的数量", required = true) @RequestParam("size") Integer size
	) {
		Page<EventListVo> eventList = eventService.getEventList(type, current, size);
		return R.data(eventList);
	}

	@GetMapping("/getEventDetail")
	@ApiOperation(value = "获取活动详情")
	public R<?> getEventDetail(
		@ApiParam(value = "活动id", required = true) @RequestParam("eventId") Long eventId
	) {
		EventPO event = eventService.getEventById(eventId);
		return R.data(event);
	}

	@GetMapping("/eventCheckIn")
	@ApiOperation(value = "活动签到")
	public R<?> eventCheckIn(
		@ApiParam(value = "活动id", required = true) @RequestParam("eventId") Long eventId,
		@ApiParam(value = "签到经度", required = true) @RequestParam("longitude") BigDecimal longitude,
		@ApiParam(value = "签到纬度", required = true) @RequestParam("latitude") BigDecimal latitude

	) {
		return eventService.eventCheckIn(eventId, longitude, latitude);
	}

	@GetMapping("/eventStar")
	@ApiOperation(value = "活动收藏")
	public R<?> eventStar(
		@ApiParam(value = "活动id", required = true) @RequestParam("eventId") Long eventId
	) {
		boolean b = eventService.eventStar(eventId);
		return b ? R.success("收藏成功。") : R.fail("收藏失败。");
	}

	@GetMapping("/eventUnStar")
	@ApiOperation(value = "活动取消收藏")
	public R<?> eventUnStar(
		@ApiParam(value = "活动id", required = true) @RequestParam("eventId") Long eventId
	) {
		boolean b = eventService.eventUnStar(eventId);
		return b ? R.success("取消收藏成功。") : R.fail("取消收藏失败。");
	}

	@GetMapping("/getGoogleMapsKey")
	@ApiOperation("获取谷歌地图key")
	public R<?> getGoogleMapsKey(){
		return R.data("AIzaSyDoWV63W0HicSWb4crnabPS492I5PjgIXE");
	}

}
