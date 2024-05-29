package org.springblade.modules.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springblade.modules.admin.pojo.po.ChatMemberPO;

import java.util.List;

@Mapper
public interface ChatMemberMapper extends BaseMapper<ChatMemberPO> {

	/**
	 * 查询用户所有的单聊信息
	 * @author FengZi
	 * @date 16:43 2024/2/22
	 * @param userId
	 * @return java.util.List<java.lang.Long>
	 **/
	@Select("SELECT DISTINCT chat_id FROM " +
		"tb_chat_member a JOIN tb_chat_overview b ON a.chat_id = b.id AND b.type = 0 WHERE user_id = #{userId}")
	List<Long> getUserChatType1(Long userId);

}
