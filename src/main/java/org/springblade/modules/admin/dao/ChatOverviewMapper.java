package org.springblade.modules.admin.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springblade.modules.admin.pojo.dto.*;
import org.springblade.modules.admin.pojo.po.ChatOverviewPO;
import org.springblade.modules.admin.pojo.po.MemberPO;

import java.util.List;

@Mapper
public interface ChatOverviewMapper extends BaseMapper<ChatOverviewPO> {

	List<ChatListDto> getChatList(Long userId);

	List<ChatDetailListDto> getChatDetailList(Long chatId,Long messageId,Long size);

	ChatDetailDto getChatDetail(Long chatId,Long userId);


	/**
	 * 根据用户id查询所在群聊人数
	 * @param userId 用户id
	 * @return
	 */
	List<ChatListUserIdsDto> getChatUserIds(Long userId);


	/**
	 * 查询该用户下所有群聊的消息未读数
	 * @param userId
	 * @return
	 */
	List<MessageHistoryDto> getUnreadNumByUserId(Long userId);


	/**
	 * 查询用户所具有的聊天室权限
	 * @return
	 */
	List<UserChatRoomsDto> getUserChatRooms();

	/**
	 * 查询聊天室中用户id
	 * @return
	 */
	List<ChatRoomUsersDto> getChatRoomUsers();

	/**
	 * 根据chatid查询该chat里面的所有用户
	 * @param chatId
	 * @return
	 */
	ChatRoomUsersDto getChatRoomUsers2(Long chatId);

	/**
	 * 查询该用户最近一次进入的聊天
	 * @param userId
	 * @return
	 */
	Long getChatHistoryChatId(Long userId);

}
