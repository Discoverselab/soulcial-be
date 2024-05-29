package org.springblade.modules.admin.service;

import org.springblade.modules.admin.pojo.query.ChatDetailQuery;
import org.springblade.modules.admin.pojo.vo.ChatDetailListVo;
import org.springblade.modules.admin.pojo.vo.ChatDetailVo;
import org.springblade.modules.admin.pojo.vo.ChatListVo;

import java.util.List;

public interface ChatService {

	/**
	 * 获取用户聊天信息列表
	 * @param
	 * @return
	 */
	List<ChatListVo> getChatList(Long userId);

	/**
	 * 获取用户聊天信息详情
	 * @param query		聊天详情查询条件
	 * @return
	 */
	ChatDetailVo getChatDetailList(ChatDetailQuery query);

}
