package org.springblade.modules.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.admin.dao.*;
import org.springblade.modules.admin.pojo.dto.AddEventDto;
import org.springblade.modules.admin.pojo.dto.UpdateEventDto;
import org.springblade.modules.admin.pojo.po.*;
import org.springblade.modules.admin.pojo.vo.EventListVo;
import org.springblade.modules.admin.service.EventService;
import org.springblade.modules.admin.service.UserVSoulService;
import org.springblade.modules.admin.util.PositionUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EventServiceImpl implements EventService {


	@Resource
	private EventMapper eventMapper;

	@Resource
	private EventFavoritesMapper eventFavoritesMapper;

	@Resource
	private EventCheckInMapper eventCheckInMapper;

	@Resource
	private ChatOverviewMapper chatOverviewMapper;

	@Resource
	private ActiveMapper activeMapper;

	@Resource
	private UserVSoulService userVSoulService;

	@Resource
	private ChatMemberMapper chatMemberMapper;

	@Resource
	private ChatDetailMapper chatDetailMapper;

	@Override
	public boolean addEvent(AddEventDto dto) {
		/**
		 * 1.上传banner图片
		 * 2.创建聊天室
		 * 3.创建活动
		 **/
		//上传banner图片
//		MultipartFile bannerFile = dto.getBannerFile();
//		String bannerUrl = OssUtil.getUrlByFile(bannerFile);
		String bannerUrl = dto.getBannerUrl();


		//创建聊天室
		ChatOverviewPO chatOverviewPO = new ChatOverviewPO();
		chatOverviewPO.setType(1);
		chatOverviewPO.setTitle(dto.getEventName());
		chatOverviewPO.setCreateTime(DateUtil.date());
		int insert = chatOverviewMapper.insert(chatOverviewPO);
		if (insert == 0) {
			return false;
		}

		//创建活动
		EventPO eventPO = new EventPO();
		BeanUtil.copyProperties(dto, eventPO);
		eventPO.setEventBanner(bannerUrl);
		eventPO.setChatOverviewId(chatOverviewPO.getId());
		eventPO.setStartTime(org.springblade.modules.admin.util.DateUtil.getStrToDate(dto.getStartTime()));
		eventPO.setEndTime(org.springblade.modules.admin.util.DateUtil.getStrToDate(dto.getEndTime()));
		eventPO.initForInsertNoAuth();
		int insert1 = eventMapper.insert(eventPO);
		if (insert1 == 0) {
			return false;
		}
		return true;
	}

	@Override
	public boolean updateEvent(UpdateEventDto dto) {
		EventPO eventPO = new EventPO();
		eventPO.setId(dto.getEventId());
		BeanUtil.copyProperties(dto, eventPO);
		eventPO.setEventBanner(dto.getBannerUrl());
		eventPO.setStartTime(org.springblade.modules.admin.util.DateUtil.getStrToDate(dto.getStartTime()));
		eventPO.setEndTime(org.springblade.modules.admin.util.DateUtil.getStrToDate(dto.getEndTime()));
		eventPO.initForUpdateNoAuth();

		int i = eventMapper.updateById(eventPO);
		if (i == 0){
			return false;
		}

		return true;
	}

	@Override
	public Page<EventListVo> getEventList(Integer type, Integer current, Integer size) {
		Page<EventListVo> eventListVoPage = new Page<>(current, size);
		switch (type) {
			case 0:
				//全部
				return getAllEventList(eventListVoPage, current, size);
			case 1:
				//收藏
				return getStarEventList(eventListVoPage, current, size);
			case 2:
				//已加入
				return getJoinedEventList(eventListVoPage, current, size);
			default:
				return eventListVoPage;
		}
	}

	@Override
	public EventPO getEventById(Long eventId) {
		EventPO eventPO = eventMapper.selectById(eventId);
		//查看当前是否登录用户，如果有登录用户判断是否签到当前活动
		if (!StpUtil.isLogin()) {
			eventPO.setIsCheckIn(null);
			eventPO.setIsStar(null);
		} else {
			Long userId = StpUtil.getLoginIdAsLong();
			//查询用户是否签到
			LambdaQueryWrapper<EventCheckInPO> wp = new LambdaQueryWrapper<>();
			wp.eq(EventCheckInPO::getEventId, eventPO.getId());
			wp.eq(EventCheckInPO::getUserId, userId);
			List<EventCheckInPO> eventCheckInPOS = eventCheckInMapper.selectList(wp);
			if (eventCheckInPOS.size() > 0) {
				eventPO.setIsCheckIn(true);
			} else {
				eventPO.setIsCheckIn(false);
			}
			//查询用户是否收藏
			LambdaQueryWrapper<EventFavoritesPO> wp2 = new LambdaQueryWrapper<>();
			wp2.eq(EventFavoritesPO::getEventId, eventPO.getId());
			wp2.eq(EventFavoritesPO::getUserId, userId);
			wp2.eq(EventFavoritesPO::getIsDeleted, 0);
			List<EventFavoritesPO> eventFavoritesPOS = eventFavoritesMapper.selectList(wp2);
			if (eventFavoritesPOS.size() > 0) {
				eventPO.setIsStar(true);
			} else {
				eventPO.setIsStar(false);
			}
		}
		return eventPO;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public R<?> eventCheckIn(Long eventId, BigDecimal longitude, BigDecimal latitude) {
		//1.校验用户是否登录
		Long userId = StpUtil.getLoginIdAsLong();

		if (userId == null) {
			return R.fail(403, "未登录");
		}

		EventPO eventPO = eventMapper.selectById(eventId);
		if (eventPO == null) {
			return R.fail("活动id不存在。");
		}

		//判断是否签到过
		LambdaQueryWrapper<EventCheckInPO> check = new LambdaQueryWrapper<>();
		check.eq(EventCheckInPO::getEventId,eventId);
		check.eq(EventCheckInPO::getUserId,userId);
		List<EventCheckInPO> eventCheckInPOS = eventCheckInMapper.selectList(check);
		if (eventCheckInPOS != null && eventCheckInPOS.size() > 0){
			return R.fail(500,"请勿重复签到");
		}

		//2.校验活动是否开始
		boolean timeInPeriod = org.springblade.modules.admin.util.DateUtil.isDateInPeriod(eventPO.getStartTime(), eventPO.getEndTime());
		if (!timeInPeriod) {
			return R.fail(0, "活动未开始。");
		}

		//3.校验是否在签到范围内
		boolean isRange = PositionUtil.checkWithinRange(longitude, latitude, eventPO.getLongitude(), eventPO.getLatitude(), eventPO.getDistance());
		if (!isRange) {
			return R.fail(1, "请到签到地点进行签到。");
		}

		//4.签到表插入数据
		EventCheckInPO eventCheckInPO = new EventCheckInPO();
		eventCheckInPO.setEventId(eventId);
		eventCheckInPO.setUserId(userId);
		eventCheckInPO.initForInsert();
		int insert = eventCheckInMapper.insert(eventCheckInPO);
		if (insert == 0) {
			throw new RuntimeException("签到失败，插入签到表失败。");
//			return R.fail(500, "签到失败。");
		}

		//5.activity通知添加记录
		ActivePO activePO = new ActivePO();
		activePO.setType(4);
		activePO.setEventUserId(userId);
		activePO.setEventId(eventId);
		activePO.initForInsert();
		int insert1 = activeMapper.insert(activePO);
		if (insert1 == 0) {
			throw new RuntimeException("签到失败，activity通知添加记录失败。");
//			return R.fail(500, "签到失败");
		}

		//6.积分添加
		userVSoulService.addUserVSoul(userId, eventPO.getReward(), 8);

		//7.拉入群聊
		ChatMemberPO chatMemberPO = new ChatMemberPO();
		chatMemberPO.setChatId(eventPO.getChatOverviewId());
		chatMemberPO.setUserId(userId);
		chatMemberPO.initForInsert();
		int insert2 = chatMemberMapper.insert(chatMemberPO);
		if (insert2 == 0) {
			throw new RuntimeException("签到失败，拉入群聊失败。");
//			return R.fail(500,"签到失败");
		}

		//8.加入群聊消息
		ChatDetailPO chatDetailPO = new ChatDetailPO();
		chatDetailPO.setType(99);
		chatDetailPO.setChatId(eventPO.getChatOverviewId());
		chatDetailPO.setUserId(userId);
		chatDetailPO.setContent("joined the group chat");
		chatDetailPO.initForInsert();
		log.info("insert chatDetailPO: " + chatDetailPO);
		chatDetailMapper.insert(chatDetailPO);
		return R.success("签到成功。");

	}

	@Override
	public boolean eventStar(Long eventId) {
		Long userId = StpUtil.getLoginIdAsLong();
		EventFavoritesPO po = new EventFavoritesPO();
		po.setEventId(eventId);
		po.setUserId(userId);
		po.initForInsert();
		return eventFavoritesMapper.insert(po) == 0 ? false : true;
	}

	@Override
	public boolean eventUnStar(Long eventId) {
		Long userId = StpUtil.getLoginIdAsLong();
		LambdaQueryWrapper<EventFavoritesPO> wp = new LambdaQueryWrapper<>();
		wp.eq(EventFavoritesPO::getEventId, eventId);
		wp.eq(EventFavoritesPO::getUserId, userId);
		return eventFavoritesMapper.delete(wp) == 0 ? false : true;
	}

	/**
	 * 获取所有的活动
	 *
	 * @param eventListVoPage
	 * @param current
	 * @param size
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<org.springblade.modules.admin.pojo.vo.EventListVo>
	 * @author FengZi
	 * @date 13:35 2024/2/20
	 **/
	private Page<EventListVo> getAllEventList(Page<EventListVo> eventListVoPage, Integer current, Integer size) {
		Page<EventPO> eventPOPage = new Page<>(current, size);
		LambdaQueryWrapper<EventPO> wp = new LambdaQueryWrapper<>();
		wp.eq(EventPO::getIsDeleted, 0);
		wp.orderByAsc(EventPO::getPriority);
		eventPOPage = eventMapper.selectPage(eventPOPage, wp);
		BeanUtil.copyProperties(eventPOPage, eventListVoPage);
		eventListVoPage.setRecords(eventPOPage.getRecords().stream().map(eventPO -> {
			EventListVo eventListVo = new EventListVo();
			eventListVo.setEventId(eventPO.getId());
			eventListVo.setEventName(eventPO.getEventName());
			eventListVo.setEventAddress(eventPO.getEventAddress());
			eventListVo.setEventDate(eventPO.getEventDateList());
			eventListVo.setEventBannerUrl(eventPO.getEventBanner());
			eventListVo.setEventCity(eventPO.getEventCity());
			return eventListVo;
		}).collect(Collectors.toList()));
		return eventListVoPage;
	}

	/**
	 * 获取收藏的活动
	 *
	 * @param eventListVoPage
	 * @param current
	 * @param size
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<org.springblade.modules.admin.pojo.vo.EventListVo>
	 * @author FengZi
	 * @date 14:04 2024/2/20
	 **/
	private Page<EventListVo> getStarEventList(Page<EventListVo> eventListVoPage, Integer current, Integer size) {
		Long userId = StpUtil.getLoginIdAsLong();
		LambdaQueryWrapper<EventFavoritesPO> wp = new LambdaQueryWrapper<EventFavoritesPO>();
		wp.eq(EventFavoritesPO::getIsDeleted, 0);
		wp.eq(EventFavoritesPO::getUserId, userId);
		wp.select(EventFavoritesPO::getEventId);
		List<EventFavoritesPO> eventFavoritesPOS = eventFavoritesMapper.selectList(wp);
		if (eventFavoritesPOS != null && eventFavoritesPOS.size() > 0) {
			List<Long> eventIds = eventFavoritesPOS.stream().map(s -> s.getEventId()).distinct().collect(Collectors.toList());
			Page<EventPO> eventPOPage = new Page<>(current, size);
			LambdaQueryWrapper<EventPO> eventWp = new LambdaQueryWrapper<>();
			eventWp.eq(EventPO::getIsDeleted, 0);
			eventWp.orderByAsc(EventPO::getPriority);
			eventWp.in(EventPO::getId, eventIds);
			eventPOPage = eventMapper.selectPage(eventPOPage, eventWp);
			BeanUtil.copyProperties(eventPOPage, eventListVoPage);
			eventListVoPage.setRecords(eventPOPage.getRecords().stream().map(eventPO -> {
				EventListVo eventListVo = new EventListVo();
				eventListVo.setEventId(eventPO.getId());
				eventListVo.setEventName(eventPO.getEventName());
				eventListVo.setEventAddress(eventPO.getEventAddress());
				eventListVo.setEventDate(eventPO.getEventDateList());
				eventListVo.setEventBannerUrl(eventPO.getEventBanner());
				eventListVo.setEventCity(eventPO.getEventCity());
				return eventListVo;
			}).collect(Collectors.toList()));
			return eventListVoPage;
		} else {
			return eventListVoPage;
		}
	}

	/**
	 * 获取加入的活动
	 *
	 * @param eventListVoPage
	 * @param current
	 * @param size
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<org.springblade.modules.admin.pojo.vo.EventListVo>
	 * @author FengZi
	 * @date 14:04 2024/2/20
	 **/
	private Page<EventListVo> getJoinedEventList(Page<EventListVo> eventListVoPage, Integer current, Integer size) {
		Long userId = StpUtil.getLoginIdAsLong();
		LambdaQueryWrapper<EventCheckInPO> wp = new LambdaQueryWrapper<EventCheckInPO>();
		wp.eq(EventCheckInPO::getIsDeleted, 0);
		wp.eq(EventCheckInPO::getUserId, userId);
		wp.select(EventCheckInPO::getEventId);
		List<EventCheckInPO> eventCheckInPOS = eventCheckInMapper.selectList(wp);
		if (eventCheckInPOS != null && eventCheckInPOS.size() > 0) {
			List<Long> eventIds = eventCheckInPOS.stream().map(s -> s.getEventId()).distinct().collect(Collectors.toList());
			Page<EventPO> eventPOPage = new Page<>(current, size);
			LambdaQueryWrapper<EventPO> eventWp = new LambdaQueryWrapper<>();
			eventWp.eq(EventPO::getIsDeleted, 0);
			eventWp.orderByAsc(EventPO::getPriority);
			eventWp.in(EventPO::getId, eventIds);
			eventPOPage = eventMapper.selectPage(eventPOPage, eventWp);
			BeanUtil.copyProperties(eventPOPage, eventListVoPage);
			eventListVoPage.setRecords(eventPOPage.getRecords().stream().map(eventPO -> {
				EventListVo eventListVo = new EventListVo();
				eventListVo.setEventId(eventPO.getId());
				eventListVo.setEventName(eventPO.getEventName());
				eventListVo.setEventAddress(eventPO.getEventAddress());
				eventListVo.setEventDate(eventPO.getEventDateList());
				eventListVo.setEventBannerUrl(eventPO.getEventBanner());
				eventListVo.setEventCity(eventPO.getEventCity());
				return eventListVo;
			}).collect(Collectors.toList()));
			return eventListVoPage;
		} else {
			return eventListVoPage;
		}
	}

}
