package org.springblade.modules.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import lombok.RequiredArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.modules.admin.dao.ChatDetailMapper;
import org.springblade.modules.admin.dao.ChatMemberMapper;
import org.springblade.modules.admin.dao.ChatOverviewMapper;
import org.springblade.modules.admin.dao.MemberConnectMapper;
import org.springblade.modules.admin.pojo.po.ChatDetailPO;
import org.springblade.modules.admin.pojo.po.ChatMemberPO;
import org.springblade.modules.admin.pojo.po.ChatOverviewPO;
import org.springblade.modules.admin.pojo.po.MemberConnectPO;
import org.springblade.modules.admin.pojo.vo.ConnectListVo;
import org.springblade.modules.admin.pojo.vo.ConnectVo;
import org.springblade.modules.admin.service.MemberConnectService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberConnectServiceImpl implements MemberConnectService {

	private final MemberConnectMapper memberConnectMapper;

	@Resource
	private ChatMemberMapper chatMemberMapper;

	@Resource
	private ChatOverviewMapper chatOverviewMapper;

	@Resource
	private ChatDetailMapper chatDetailMapper;

	/**
	 * 发起好友连接请求
	 *
	 * @param toUserId 对方用户id
	 */
	@Override
	public void subConnect(String toUserId) {
		//判断是否已经发起请求
		MemberConnectPO one = new LambdaQueryChainWrapper<>(memberConnectMapper).eq(MemberConnectPO::getUserId, StpUtil.getLoginIdAsLong())
			.eq(MemberConnectPO::getToUserId, Long.valueOf(toUserId))
			.eq(MemberConnectPO::getStatus, 0).one();
		if (ObjectUtil.isNotEmpty(one)) {
			throw new ServiceException("已发送连接请求");
		}
		MemberConnectPO memberConnectPO = new MemberConnectPO();
		memberConnectPO.setUserId(StpUtil.getLoginIdAsLong());
		memberConnectPO.setToUserId(Long.valueOf(toUserId));
		memberConnectPO.setStatus(0);
		memberConnectPO.initForInsert();
		memberConnectMapper.insert(memberConnectPO);
	}

	/**
	 * 确认加好友
	 *
	 * @param connectId 请求id
	 */
	@Override
	public void confirm(String connectId) {
		MemberConnectPO memberConnectPO = memberConnectMapper.selectById(connectId);
		if (ObjectUtil.isEmpty(memberConnectPO)) {
			throw new ServiceException("请求id错误");
		}
		if (memberConnectPO.getToUserId() != StpUtil.getLoginIdAsLong()) {
			throw new ServiceException("请确认自己的好友申请");
		}
		memberConnectPO.setStatus(1);
		memberConnectPO.initForUpdate();
		memberConnectMapper.updateById(memberConnectPO);

		//20240222 新功能 互为好友开启单聊
		//判断两个用户是否建立过单聊
		//获取两个用户的所有单聊 判断是否有重复的，有就说明已经创建过单聊
		List<Long> chats1 = chatMemberMapper.getUserChatType1(memberConnectPO.getUserId());
		List<Long> chats2 = chatMemberMapper.getUserChatType1(memberConnectPO.getToUserId());
		//判断两个list是否存在交集
		Set<Long> set1 = new HashSet<>(chats1);
		Set<Long> set2 = new HashSet<>(chats2);

		// 会将重复的值存放在set1中
		set1.retainAll(set2);

		if (set1.size() > 0) {
			return;
		}

		//创建单聊
		ChatOverviewPO chatOverviewPO = new ChatOverviewPO();
		chatOverviewPO.setType(0);
		chatOverviewPO.setTitle("Chat#Friends");
		chatOverviewPO.setCreateTime(DateUtil.date());
		chatOverviewMapper.insert(chatOverviewPO);
		//拉入单聊
		ChatMemberPO chatMemberPO = new ChatMemberPO();
		chatMemberPO.setUserId(memberConnectPO.getUserId());
		chatMemberPO.setChatId(chatOverviewPO.getId());
		chatMemberPO.initForInsert();
		chatMemberMapper.insert(chatMemberPO);
		ChatMemberPO chatMemberPO2 = new ChatMemberPO();
		chatMemberPO2.setUserId(memberConnectPO.getToUserId());
		chatMemberPO2.setChatId(chatOverviewPO.getId());
		chatMemberPO2.initForInsert();
		chatMemberMapper.insert(chatMemberPO2);

		//拉入单聊消息
		ChatDetailPO chatDetailPO = new ChatDetailPO();
		chatDetailPO.setType(99);
		chatDetailPO.setChatId(chatOverviewPO.getId());
		chatDetailPO.setUserId(memberConnectPO.getToUserId());
		chatDetailPO.setContent("joined the chat");
		chatDetailPO.initForInsert();
		chatDetailMapper.insert(chatDetailPO);
		ChatDetailPO chatDetailPO2 = new ChatDetailPO();
		chatDetailPO2.setType(99);
		chatDetailPO2.setChatId(chatOverviewPO.getId());
		chatDetailPO2.setUserId(memberConnectPO.getUserId());
		chatDetailPO2.setContent("joined the chat");
		chatDetailPO2.initForInsert();
		chatDetailMapper.insert(chatDetailPO2);

	}

	/**
	 * 获取好友列表
	 */
	@Override
	public ConnectListVo list() {
		Long loginUserId = StpUtil.getLoginIdAsLong();
		// 查询代接受的好友列表
		List<ConnectVo> newList = memberConnectMapper.getNewList(loginUserId);
		if (CollUtil.isNotEmpty(newList)) {
//			newList = newList.stream().sorted(Comparator.comparing(s->s.getCreateTime().getTime())).collect(Collectors.toList());
//			Collections.reverse(newList);
			for (ConnectVo connectVo : newList) {
				connectVo.setLinkUserId(connectVo.getUserId());
			}
		}
		//查询Star的好友列表
		List<ConnectVo> starList = memberConnectMapper.getStarList(loginUserId);
		if (CollUtil.isNotEmpty(starList)) {
			starList = starList.stream().sorted(Comparator.comparing(s -> s.getCreateTime().getTime())).collect(Collectors.toList());
			Collections.reverse(starList);
		}
		//查询All的好友列表
		List<ConnectVo> allList = memberConnectMapper.getAllList(loginUserId);
//		if (CollUtil.isNotEmpty(allList)) {
//			// 去重操作
//			List<ConnectVo> newAllList = new ArrayList<>();
//			Map<String, ConnectVo> cacheMap = new HashMap<>();
//			for (ConnectVo item : allList) {
//				String cacheKey = null;
//				if (item.getUserId() > item.getToUserId()) {
//					cacheKey = item.getUserId() + "-" + item.getToUserId();
//				} else {
//					cacheKey = item.getToUserId() + "-" + item.getUserId();
//				}
//				// 判断当前status若为1时是否存在2的数据，若存在则跳过
//				if (item.getStatus().equals(1) && cacheMap.containsKey(cacheKey)) {
//					continue;
//				}
//				cacheMap.put(cacheKey, item);
//			}
//
//			for (String key : cacheMap.keySet()) {
//				// 双向关联不确定是在userId还是toUserId上，进行判断
//				ConnectVo newItem = cacheMap.get(key);
//				if (newItem.getUserId().equals(loginUserId)) {
//					newItem.setLinkUserId(newItem.getToUserId());
//				} else {
//					newItem.setLinkUserId(newItem.getUserId());
//				}
//				newAllList.add(cacheMap.get(key));
//			}
//
//			allList = newAllList.stream().sorted(Comparator.comparing(s->s.getCreateTime().getTime())).collect(Collectors.toList());
//			Collections.reverse(allList);
//		}
		return ConnectListVo.builder()
			.newList(newList)
			.startList(starList)
			.allList(allList)
			.build();
	}

	/**
	 * 取消好友连接
	 *
	 * @param connectId 连接id
	 */
	@Override
	public void cancel(String connectId) {
		MemberConnectPO memberConnectPO = memberConnectMapper.selectById(connectId);
		if (ObjectUtil.isEmpty(memberConnectPO)) {
			throw new ServiceException("连接id错误");
		}
		if (memberConnectPO.getToUserId() != StpUtil.getLoginIdAsLong() && memberConnectPO.getUserId() != StpUtil.getLoginIdAsLong()) {
			throw new ServiceException("请取消自己的连接好友");
		}
		if (memberConnectPO.getStatus() == 0) {
			throw new ServiceException("不能取消待确认的好友");
		}
		if (memberConnectPO.getStatus() == 2) {
			throw new ServiceException("不能取消star连接");
		}
		memberConnectMapper.deleteById(connectId);
	}

	/**
	 * 获取两个用户之间的连接状态
	 *
	 * @param formUserId
	 * @param toUserId
	 */
	@Override
	public Integer getConnectStatus(long formUserId, Long toUserId) {
		MemberConnectPO memberConnectPO = new LambdaQueryChainWrapper<>(memberConnectMapper)
			.eq(MemberConnectPO::getUserId, formUserId)
			.eq(MemberConnectPO::getToUserId, toUserId)
			.last(" limit 1").one();
		if (ObjectUtil.isEmpty(memberConnectPO)) {
			MemberConnectPO one = new LambdaQueryChainWrapper<>(memberConnectMapper)
				.eq(MemberConnectPO::getUserId, toUserId)
				.eq(MemberConnectPO::getToUserId, formUserId)
				.last(" limit 1").one();
			if (ObjectUtil.isEmpty(one)) {
				return null;
			}
			return one.getStatus();
		}
		return memberConnectPO.getStatus();
	}

	/**
	 * 获取已连接数量
	 *
	 * @param userId 用户id
	 */
	@Override
	public Integer getConnectNum(Long userId) {
		return memberConnectMapper.getConnectNum(userId);
	}

	/**
	 * 添加Star连接
	 *
	 * @param fromUserId
	 * @param toUserId
	 */
	@Override
	public void addStarConnected(Long fromUserId, Long toUserId) {
		// 判断是否已经链接 判断 fromUserId 是否等于 数据库的UserId 或者 toUserId 是否等于 数据库的toUserId
		MemberConnectPO one = new LambdaQueryChainWrapper<>(memberConnectMapper)
			.eq(MemberConnectPO::getUserId, fromUserId)
			.eq(MemberConnectPO::getToUserId, toUserId)
			.last(" limit 1").one();

		// 如果一开始的查询没有找到结果，进行第二个查询
		if (one == null) {
			one = new LambdaQueryChainWrapper<>(memberConnectMapper)
				.eq(MemberConnectPO::getUserId, toUserId)
				.eq(MemberConnectPO::getToUserId, fromUserId)
				.last(" limit 1").one();
		}
		if (ObjectUtil.isNotEmpty(one)) {
			one.setStatus(2);
			one.initForUpdateNoAuth(1L);
			memberConnectMapper.updateById(one);
			return;
		}
		MemberConnectPO memberConnectPO = new MemberConnectPO();
		memberConnectPO.setUserId(fromUserId);
		memberConnectPO.setToUserId(toUserId);
		memberConnectPO.setStatus(2);
		memberConnectPO.initForInsertNoAuth();
		memberConnectMapper.insert(memberConnectPO);
	}

	/**
	 * 添加普通连接
	 *
	 * @param fromUserId
	 * @param toUserId
	 */
	@Override
	public void addConnected(Long fromUserId, Long toUserId) {
		//判断是否已经发起请求
		MemberConnectPO one = new LambdaQueryChainWrapper<>(memberConnectMapper)
			.eq(MemberConnectPO::getUserId, fromUserId)
			.eq(MemberConnectPO::getToUserId, Long.valueOf(toUserId))
			.one();
		if (ObjectUtil.isNotEmpty(one)) {
			return;
		}
		MemberConnectPO memberConnectPO = new MemberConnectPO();
		memberConnectPO.setUserId(fromUserId);
		memberConnectPO.setToUserId(toUserId);
		memberConnectPO.setStatus(1);
		memberConnectPO.initForInsert();
		memberConnectMapper.insert(memberConnectPO);

		//20240301 用户建立连接 创建私聊
		//创建单聊
		ChatOverviewPO chatOverviewPO = new ChatOverviewPO();
		chatOverviewPO.setType(0);
		chatOverviewPO.setTitle("Chat#Friends");
		chatOverviewPO.setCreateTime(DateUtil.date());
		chatOverviewPO.initForInsertNoAuth();
		chatOverviewMapper.insert(chatOverviewPO);
		//拉入单聊
		ChatMemberPO chatMemberPO = new ChatMemberPO();
		chatMemberPO.setUserId(memberConnectPO.getUserId());
		chatMemberPO.setChatId(chatOverviewPO.getId());
		chatMemberPO.initForInsertNoAuth();
		chatMemberMapper.insert(chatMemberPO);
		ChatMemberPO chatMemberPO2 = new ChatMemberPO();
		chatMemberPO2.setUserId(memberConnectPO.getToUserId());
		chatMemberPO2.setChatId(chatOverviewPO.getId());
		chatMemberPO2.initForInsertNoAuth();
		chatMemberMapper.insert(chatMemberPO2);

		//拉入单聊消息
		ChatDetailPO chatDetailPO = new ChatDetailPO();
		chatDetailPO.setType(99);
		chatDetailPO.setChatId(chatOverviewPO.getId());
		chatDetailPO.setUserId(memberConnectPO.getToUserId());
		chatDetailPO.setContent("joined the chat");
		chatDetailPO.initForInsertNoAuth();
		chatDetailMapper.insert(chatDetailPO);
		ChatDetailPO chatDetailPO2 = new ChatDetailPO();
		chatDetailPO2.setType(99);
		chatDetailPO2.setChatId(chatOverviewPO.getId());
		chatDetailPO2.setUserId(memberConnectPO.getUserId());
		chatDetailPO2.setContent("joined the chat");
		chatDetailPO2.initForInsertNoAuth();
		chatDetailMapper.insert(chatDetailPO2);
	}

	@Override
	public Boolean getHasConfirm(Long userId) {
		Long count = new LambdaQueryChainWrapper<>(memberConnectMapper)
			.eq(MemberConnectPO::getToUserId, userId)
			.eq(MemberConnectPO::getStatus, 0).count();
		return count > 0;
	}
}
