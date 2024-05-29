package org.springblade.modules.admin.cache;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.admin.dao.ChatOverviewMapper;
import org.springblade.modules.admin.dao.MemberMapper;
import org.springblade.modules.admin.pojo.dto.ChatRoomUsersDto;
import org.springblade.modules.admin.pojo.dto.IUserCacheDto;
import org.springblade.modules.admin.pojo.dto.UserChatRoomsDto;
import org.springblade.modules.admin.pojo.po.MemberPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class IUserCache {

	private static ChatOverviewMapper chatOverviewMapper;

	@Autowired
	public void setChatOverviewMapper(ChatOverviewMapper chatOverviewMapper) {
		IUserCache.chatOverviewMapper = chatOverviewMapper;
	}

	private static MemberMapper memberMapper;

	@Autowired
	public void setMemberMapper(MemberMapper memberMapper) {
		IUserCache.memberMapper = memberMapper;
	}

	/**
	 * 存放用户信息缓存 用户id为key
	 */
	private static Map<Long, IUserCacheDto> userDataMap = new ConcurrentHashMap<>(8);

	/**
	 * 存放用户信息缓存 用户address为key
	 */
	private static Map<String, IUserCacheDto> userDataMap2 = new ConcurrentHashMap<>(8);

	/**
	 * 用户id  用户所具有的聊天id
	 */
	private static Map<Long, List<Long>> userRooms = new ConcurrentHashMap<>(8);

	/**
	 * 聊天id 聊天室中的用户id信息
	 */
	private static Map<Long, List<Long>> chatUsers = new ConcurrentHashMap<>(8);


	/**
	 * 初始化缓存信息
	 */
	@PostConstruct
	public static void initCache() {
		initUserData();
		initUserRoomIds();
		initRoomUserids();
	}

	/**
	 * 更新缓存信息
	 * @author FengZi
	 * @date 11:41 2023/12/9
	 **/
	public static void refresh(){
		IUserCache.updateUserCache();
		IUserCache.updateUserRoomsCache();
		IUserCache.updateRoomUsersCache();
	}

	/**
	 * 更新用户缓存数据
	 */
	public static void updateUserCache() {
		initUserData();
	}

	/**
	 * 更新用户聊天室缓存数据
	 */
	public static void updateUserRoomsCache() {
		initUserRoomIds();
	}

	/**
	 * 更新聊天室用户缓存数据
	 */
	public static void updateRoomUsersCache() {
		initRoomUserids();
	}

	/**
	 * 根据用户id查询用户基本信息
	 * @author FengZi
	 * @date 18:44 2023/11/23
	 * @param userId
	 * @return org.springblade.modules.admin.pojo.dto.IUserCacheDto
	 **/
	public static IUserCacheDto getUserCache(Long userId) {
		IUserCacheDto iUserCacheDto = userDataMap.get(userId);
		return iUserCacheDto;
	}

	/**
	 * 根据用户address查询用户基本信息
	 * @author FengZi
	 * @date 18:44 2023/11/23
	 * @param address
	 * @return org.springblade.modules.admin.pojo.dto.IUserCacheDto
	 **/
	public static IUserCacheDto getUserCacheByAddress(String address) {
		IUserCacheDto iUserCacheDto = userDataMap2.get(address);
		return iUserCacheDto;
	}

	/**
	 * 根据用户id查询该用户所具有的聊天室id集合
	 * @author FengZi
	 * @date 18:44 2023/11/23
	 * @param userId
	 * @return java.util.List<java.lang.Long>
	 **/

	public static List<Long> getUserRooms(Long userId) {
		List<Long> longs = userRooms.get(userId);
		return longs;
	}

	/**
	 * 根据聊天id查询聊天室内用户信息
	 * @author FengZi
	 * @date 18:44 2023/11/23
	 * @param chatId
	 * @return java.util.List<java.lang.Long>
	 **/
	public static List<Long> getChatUsers(Long chatId) {
		List<Long> longs = chatUsers.get(chatId);
		return longs;
	}


	/**
	 * 用户信息
	 */
	private static void initUserData() {
		userDataMap.clear();
		//初始化用户信息
		List<MemberPO> users = memberMapper.selectList(new LambdaQueryWrapper<MemberPO>()
			.select(MemberPO::getId, MemberPO::getUserName, MemberPO::getAvatar, MemberPO::getAddress)
			.eq(MemberPO::getIsDeleted, 0));
		//用户id为key
		userDataMap = users
			.stream().map(s -> {
				IUserCacheDto IUserCacheDto = new IUserCacheDto();
				IUserCacheDto.setUserName(s.getUserName());
				IUserCacheDto.setUserId(s.getId());
				IUserCacheDto.setUserAvatar(s.getAvatar());
				IUserCacheDto.setAddress(s.getAddress());
				return IUserCacheDto;
			}).collect(Collectors.toMap(
				IUserCacheDto::getUserId
				, IUserCacheDto -> IUserCacheDto
			));
		//用户address为key
		userDataMap2 = users
			.stream().map(s -> {
				IUserCacheDto IUserCacheDto = new IUserCacheDto();
				IUserCacheDto.setUserName(s.getUserName());
				IUserCacheDto.setUserId(s.getId());
				IUserCacheDto.setUserAvatar(s.getAvatar());
				IUserCacheDto.setAddress(s.getAddress());
				return IUserCacheDto;
			}).collect(Collectors.toMap(
				IUserCacheDto::getAddress
				, IUserCacheDto -> IUserCacheDto
			));
		log.info("初始化用户信息：{}", userDataMap.size());
	}

	/**
	 * 用户所在的聊天室缓存
	 */
	private static void initUserRoomIds() {
		List<UserChatRoomsDto> userChatRooms = chatOverviewMapper.getUserChatRooms();
		if (userChatRooms == null || userChatRooms.size() == 0) {
			return;
		}
		userRooms.clear();
		userRooms = userChatRooms.stream().collect(Collectors.toMap(
			UserChatRoomsDto::getUserId,
			UserChatRoomsDto::getChatRoomIdList
		));
		log.info("初始化用户所在聊天室缓存：{}", userRooms.size());
	}

	/**
	 * 聊天室内所有用户信息
	 */
	private static void initRoomUserids() {
		List<ChatRoomUsersDto> chatRoomUsers = chatOverviewMapper.getChatRoomUsers();
		if (chatRoomUsers == null || chatRoomUsers.size() == 0) {
			return;
		}
		chatUsers.clear();
		chatUsers = chatRoomUsers.stream().collect(Collectors.toMap(
			ChatRoomUsersDto::getChatId,
			ChatRoomUsersDto::getUserIdList
		));
		log.info("初始化聊天室内所有用户信息缓存：{}", chatUsers.size());
	}


}
