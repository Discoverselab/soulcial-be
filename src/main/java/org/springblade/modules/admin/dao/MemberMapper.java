package org.springblade.modules.admin.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springblade.modules.admin.pojo.po.MemberPO;

/**
 * ExchangeCodePO的Dao接口
 *
 * @author
 *
 */
@Mapper
public interface MemberMapper extends BaseMapper<MemberPO> {
	// 通过address查询用户
	default MemberPO selectByAddress(String address) {
		return selectOne(new LambdaQueryWrapper<MemberPO>().eq(MemberPO::getAddress, address)
		);
	}

	/**
	 * 获取当前登录用户 平台收入
	 * @author FengZi
	 * @date 19:30 2023/12/13
	 * @param userId
	 * @return java.lang.Double
	 **/
	@Select("SELECT sum(price) FROM tb_wallect_history WHERE user_id = #{userId} \n" +
		"AND \n" +
		"(type = 6 \n" +
		"OR type = 7\n" +
		"OR type = 2)\n" +
		"GROUP BY user_id;")
	String getCurrentUserEarnings(Long userId);

	/**
	 * 根据用户id查询推荐人钱包地址
	 * @author FengZi
	 * @date 14:41 2023/12/14
	 * @param userId
	 * @return org.springblade.modules.admin.pojo.po.MemberPO
	 **/
	@Select("SELECT * FROM  tb_member WHERE id =  (SELECT invite_user_id FROM tb_member WHERE id = #{userId})")
	MemberPO getUserByInviteAddress(Long userId);
}
