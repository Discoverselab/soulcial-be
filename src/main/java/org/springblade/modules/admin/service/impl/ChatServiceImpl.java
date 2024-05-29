package org.springblade.modules.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.admin.dao.*;
import org.springblade.modules.admin.pojo.dto.*;
import org.springblade.modules.admin.pojo.po.MemberPO;
import org.springblade.modules.admin.pojo.po.PFPTokenPO;
import org.springblade.modules.admin.pojo.query.ChatDetailQuery;
import org.springblade.modules.admin.pojo.vo.ChatDetailListVo;
import org.springblade.modules.admin.pojo.vo.ChatDetailVo;
import org.springblade.modules.admin.pojo.vo.ChatListVo;
import org.springblade.modules.admin.service.ChatService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ChatServiceImpl implements ChatService {

	@Resource
	private ChatDetailMapper chatDetailMapper;

	@Resource
	private ChatMemberMapper chatMemberMapper;

	@Resource
	private ChatOverviewMapper chatOverviewMapper;

	@Resource
	private PFPTokenMapper pfpTokenMapper;

	@Resource
	private MemberMapper memberMapper;

	public static final String STRING = "=-=";
	public static final String MESSAGE_IMAGE = "[image]";

	@Override
	public List<ChatListVo> getChatList(Long userId) {
//		Long userId = StpUtil.getLoginIdAsLong();
		//1.查询消息列表
		List<ChatListDto> chatList = chatOverviewMapper.getChatList(userId);

		//消息未读数
		List<MessageHistoryDto> unreadNumByUserId = chatOverviewMapper.getUnreadNumByUserId(userId);
		Map<Long, Long> unreadNumMaps =
			unreadNumByUserId.stream().collect(Collectors.toMap(MessageHistoryDto::getChatId, MessageHistoryDto::getUnreadNum));

		//查询chat对应的用户id
		List<ChatListUserIdsDto> chatUserIds = chatOverviewMapper.getChatUserIds(userId);
		Map<Long, List<Long>> chatIdBYUserids =
			chatUserIds.stream().collect(Collectors.toMap(ChatListUserIdsDto::getChatId, ChatListUserIdsDto::getUserIds));

		List<ChatListDto> chatList2 = chatList.stream().map(s -> {
			List<Long> longs = chatIdBYUserids.get(s.getId());
			if (longs.size() > 0) {
				s.setUserIds(longs);
			}
			return s;
		}).collect(Collectors.toList());

		// 若为空，返回空result列表
		if (chatList == null || chatList.isEmpty()) {
			return new ArrayList<>();
		}

		//1.1消息列表中所有群聊tokendis
		List<Long> tokenIds = chatList2.stream()
			.filter(s -> "1".equals(s.getType()))
			.map(ChatListDto::getTokenId)
			.distinct()
			.collect(Collectors.toList());
		//1.2消息列表中所有单聊用户数ids
		List<Long> userids = new ArrayList<>();
		chatList2.stream()
			.filter(s -> "0".equals(s.getType()))
			.forEach(s -> {
				List<Long> userIds = s.getUserIds();
				userids.addAll(userIds);
			});
		//添加群聊消息发送者的用户id，用于展示群聊列表用户发送者name
		List<Long> dlIds = chatList.stream().map(ChatListDto::getSendUserId).collect(Collectors.toList());
		userids.addAll(dlIds);


		//2.查询消息列表展示图标
		Map<Long, String> userAvatarMaps;
		if (userids.size() > 0) {
			userAvatarMaps =
				memberMapper.selectList(new QueryWrapper<MemberPO>().select("id,avatar,user_name,address").in("id", userids))
					.stream().collect(Collectors.toMap(MemberPO::getId, s -> s.getAvatar() + STRING + s.getUserName() + STRING + s.getAddress()));
		} else {
			userAvatarMaps = new HashMap<>();
		}
		Map<Long, String> pfpUrlMaps;
		if (tokenIds.size() > 0) {
			pfpUrlMaps = pfpTokenMapper
				.selectList(new QueryWrapper<PFPTokenPO>().select("real_token_id,square_picture_url").in("real_token_id", tokenIds))
				.stream().collect(Collectors.toMap(PFPTokenPO::getRealTokenId, PFPTokenPO::getSquarePictureUrl));
		} else {
			pfpUrlMaps = new HashMap<>();
		}
		//20240227 活动群聊展示图标

		//3.封装信息
		List<ChatListVo> result = chatList2.stream().map(s -> {
			ChatListVo chatListVo = new ChatListVo();
			chatListVo.setId(s.getId());
			chatListVo.setTime(s.getTime());
			chatListVo.setTitle(s.getTitle());
			chatListVo.setUnreadNum(0L);

			if ("1".equals(s.getMessageType())) {
				chatListVo.setRelatedContent(MESSAGE_IMAGE);
			} else {
				chatListVo.setRelatedContent(s.getContent());
			}
			chatListVo.setMemberNum((long) s.getUserIds().size());
			String avatar = "";
			//3.1判断群聊or单聊
			if ("0".equals(s.getType())) {
				//3.2单聊查询另一位用户头像url
				s.getUserIds().remove(userId);
				if (s.getUserIds().size() != 0) {
					String str = userAvatarMaps.get(s.getUserIds().get(0));
					if (str != null) {
						avatar = str.split(STRING)[0];
						chatListVo.setUsername(str.split(STRING)[1]);
						chatListVo.setAddress(str.split(STRING)[2]);
					}
				}
			} else {
				Long tokenId = s.getTokenId();
				if (tokenId != null && tokenId > 0){
					//3.3群聊根据tokenid查询ftp图标
					chatListVo.setTokenId(tokenId);
					avatar = pfpUrlMaps.get(tokenId);

				}else {
					//20240227 事件群聊头像
					avatar = s.getEventBannerUrl();
				}
				//获取最后一条消息发送者的用户姓名
				Long sendUserId = s.getSendUserId();
				String s1 = userAvatarMaps.get(sendUserId);
				if (s1 != null) {
					chatListVo.setUsername(s1.split(STRING)[1]);
					chatListVo.setAddress(userAvatarMaps.get(sendUserId).split(STRING)[2]);
				}

			}
			chatListVo.setAvator(avatar);
			chatListVo.setType(s.getType());
			Long unreadNum = unreadNumMaps.get(s.getId());
			chatListVo.setUnreadNum(unreadNum == null ? 0 : unreadNum);
			return chatListVo;
		}).collect(Collectors.toList());
		return result;
	}

	@Override
	public ChatDetailVo getChatDetailList(ChatDetailQuery query) {
		Long userId = StpUtil.getLoginIdAsLong();
		List<ChatDetailListDto> chatDetailList = new ArrayList<>(20);
		if (query.getMessageId() == null) {
			chatDetailList = chatOverviewMapper.getChatDetailList(
				query.getChatId(),
				null,
				query.getSize());
		} else {
			chatDetailList = chatOverviewMapper.getChatDetailList(
				query.getChatId(),
				query.getMessageId(),
				query.getSize());
		}
		//初始群聊没有消息
		if (chatDetailList.size() == 0) {
			ChatDetailVo chatDetailVo = new ChatDetailVo();
			List<ChatDetailListVo> list = new ArrayList<>();
			//查询chatId其他信息
			ChatDetailDto chatDetail = chatOverviewMapper.getChatDetail(query.getChatId(), userId);
			chatDetailVo.setChatDetailDto(chatDetail);
			chatDetailVo.setDetaillist(list);
			return chatDetailVo;
		}
		List<Long> userIds = chatDetailList.stream().map(ChatDetailListDto::getSendUserId).collect(Collectors.toList());
		Map<Long, String> userAvatarMaps =
			memberMapper.selectList(new QueryWrapper<MemberPO>().select("id,avatar,user_name").in("id", userIds))
				.stream().collect(Collectors.toMap(MemberPO::getId, s -> s.getUserName() + STRING + s.getAvatar()));

		List<ChatDetailListVo> results = chatDetailList.stream().map(s -> {
			ChatDetailListVo vo = new ChatDetailListVo();
			vo.setUserId(s.getSendUserId());
			String userNameAndAvatar = userAvatarMaps.get(s.getSendUserId());
			if (userNameAndAvatar != null) {
				String[] split = userNameAndAvatar.split(STRING);
				if (split.length>0){
					vo.setUserName(userNameAndAvatar.split(STRING)[0]);
				}
				if (split.length>1){
					vo.setUserAvatar(userNameAndAvatar.split(STRING)[1]);
				}
			}
			vo.setType(s.getType());
			vo.setMessageId(s.getMessageId());
			vo.setContent(s.getContent());
			vo.setTime(s.getTime());
			return vo;
		}).collect(Collectors.toList());

		ChatDetailVo chatDetailVo = new ChatDetailVo();
		chatDetailVo.setDetaillist(results);
		//查询chatId其他信息
		ChatDetailDto chatDetail = chatOverviewMapper.getChatDetail(query.getChatId(), userId);
		chatDetailVo.setChatDetailDto(chatDetail);
		return chatDetailVo;
	}
}
