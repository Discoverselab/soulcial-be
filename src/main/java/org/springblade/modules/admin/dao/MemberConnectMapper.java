package org.springblade.modules.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springblade.modules.admin.pojo.po.MemberConnectPO;
import org.springblade.modules.admin.pojo.vo.ConnectVo;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 用户连接表 DAO
 */
@Mapper
public interface MemberConnectMapper extends BaseMapper<MemberConnectPO> {

	/**
	 * 获取代接受的好友请求列表
	 * @param loginUserId 当前登录用户id
	 */
    List<ConnectVo> getNewList(@Param("loginUserId") Long loginUserId);

	/**
	 * 获取Star好友列表
	 * @param loginUserId 当前登录用户id
	 */
	List<ConnectVo> getStarList(@Param("loginUserId") Long loginUserId);

	/**
	 * 获取全部好友列表
	 * @param loginUserId 当前登录用户id
	 */
	List<ConnectVo> getAllList(@Param("loginUserId") Long loginUserId);

	/**
	 * 获取用户连接数量
	 * @param userId 用户id
	 */
	Integer getConnectNum(@Param("userId") Long userId);
}
