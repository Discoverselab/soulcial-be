package org.springblade.modules.admin.service;

import org.springblade.modules.admin.pojo.vo.ConnectListVo;

public interface MemberConnectService {

	/**
	 * 发起好友连接请求
	 * @param toUserId 对方用户id
	 */
	void subConnect(String toUserId);

	/**
	 * 确认加好友
	 * @param connectId 请求id
	 */
	void confirm(String connectId);

	/**
	 * 获取好友列表
	 */
	ConnectListVo list();

	/**
	 * 取消好友连接
	 * @param connectId 连接id
	 */
	void cancel(String connectId);


	/**
	 * 获取两个用户之间的连接状态
	 */
	Integer getConnectStatus(long formUserId, Long toUserId);

	/**
	 * 获取已连接数量
	 * @param userId 用户id
	 */
	Integer getConnectNum(Long userId);

	/**
	 * 添加Star连接
	 */
	void addStarConnected(Long fromUserId, Long toUserId);

	/**
	 * 添加普通连接
	 */
	void addConnected(Long fromUserId, Long toUserId);

	Boolean getHasConfirm(Long userId);
}
