package org.springblade.modules.admin.util;

import cn.hutool.core.util.RandomUtil;

public class InviteCodeGenUtil {

	/**
	 * 生产邀请码
	 * @param address 用户钱包地址
	 */
	public static String genInviteCode(String address){
        return "soul-"+ RandomUtil.randomString(address,6);
	}
}
