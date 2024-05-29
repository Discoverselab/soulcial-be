package org.springblade.modules.admin.service;

import org.springblade.modules.admin.pojo.po.MemberInvitePO;

import java.util.List;

public interface MemberInviteService {

	/**
	 * 获取用户的邀请码列表
	 * @param userId 用户id
	 */
	List<MemberInvitePO> getInviteCodes(long userId);

	/**
	 * 使用邀请码
	 * @param inviteCode 邀请码
	 */
	void useInviteCode(String inviteCode);
}
