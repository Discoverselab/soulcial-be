package org.springblade.modules.admin.service;

import java.math.BigDecimal;

public interface UserVSoulService {

	/**
	 * 新增用户vSoul积分
	 * @param userId  用户id
	 * @param vSoul 积分值
	 */
	void addUserVSoul(Long userId, BigDecimal vSoul, Integer type);

	/**
	 * 根据用户id获取用户Boost  如果用户没有nft返回1
	 * @author FengZi
	 * @date 17:39 2024/1/22
	 * @param userId
	 * @return java.math.BigDecimal
	 **/
	BigDecimal getBoostByUserId(Long userId);

	/**
	 * 给用户邀请人新增积分
	 * @author FengZi
	 * @date 18:07 2024/1/22
	 * @param userId	用户id
	 * @param vsoulprie  积分值
	 * @return boolean
	 **/
	boolean setxInviteUserVSoulPriceByUserId(Long userId,BigDecimal vsoulprie);
}
