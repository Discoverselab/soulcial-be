package org.springblade.modules.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnr.ffi.annotations.In;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.python.antlr.ast.Str;
import org.springblade.modules.admin.pojo.po.ActivePO;
import org.springframework.data.repository.query.Param;

/**
 * ExchangeCodePO的Dao接口
 *
 * @author
 */
@Mapper
public interface ActiveMapper extends BaseMapper<ActivePO> {
	Page<ActivePO> selectActiveWithUser(Page<ActivePO> page);


//	@Select(" <script>" +
//		"SELECT b.owner_user_id as awardedUserId,a.check_lottery_status, a.* FROM tb_active a " +
//		"JOIN tb_pfp_token b ON a.token_id = b.real_token_id " +
////		"<if test='pickStatus != null and !pickStatus.isEmpty()'>" +
////		" AND b.pick_status = #{pickStatus} " +
////		"</if>" +
//		"<if test='checkLotteryStatus != null and !checkLotteryStatus.isEmpty()'>" +
//		" AND a.check_lottery_status = #{checkLotteryStatus} " +
//		"</if>" +
//		"WHERE a.is_deleted = 0 " +
//		"AND b.is_deleted = 0 " +
//		"ORDER BY a.create_time DESC" +
//		"</script> ")

	@Select(" <script>" +
		"SELECT b.owner_user_id as awardedUserId,b.pick_status, a.* FROM tb_active a " +
//		"JOIN tb_pfp_token b ON a.token_id = b.real_token_id " +
		"LEFT JOIN tb_pfp_token b ON a.token_id = b.real_token_id AND b.is_deleted = 0  " +
		"<if test='pickStatus != null and !pickStatus.isEmpty()'>" +
		" AND b.pick_status = #{pickStatus} " +
		"</if>" +
//		"<if test='checkLotteryStatus != null and !checkLotteryStatus.isEmpty()'>" +
//		" AND a.check_lottery_status = #{checkLotteryStatus} " +
//		"</if>" +
		"WHERE a.is_deleted = 0 " +
//		"AND b.is_deleted = 0 " +
		"ORDER BY a.create_time DESC" +
		"</script> ")
	IPage<ActivePO> selectPageVo(Page<?> page, @Param("pickStatus") String pickStatus);


	@Select("SELECT COUNT(id) FROM tb_active WHERE type = '1' AND user_address = #{address} AND token_id is NOT NULL AND is_deleted = '0';")
	Integer selectPumpCount(@Param("address") String address);

	@Select("SELECT COUNT(id) FROM tb_member WHERE address = #{address} AND level_score IS NOT NULL AND is_deleted = '0';")
	Integer userRegistrationStatus(@Param("address") String address);

	@Select("SELECT COUNT(DISTINCT address) FROM tb_member WHERE invite_user_id IN " +
		"(SELECT a.id FROM ( SELECT id FROM tb_member WHERE address = #{address} LIMIT 1)a)")
	Integer hasInvitedOthers(@Param("address") String address);

//	@Select("SELECT count(address) FROM tb_member WHERE address = #{address} AND (level_score != NULL OR level_score > 0);")
	@Select("SELECT count(address) FROM tb_member WHERE address = #{address} ;")
	Integer isRegister(@Param("address")String address);

//	@Select("SELECT COUNT( DISTINCT address ) FROM tb_member WHERE `level` > 0 AND is_deleted = 0 AND address = #{address} AND update_time >= NOW() - INTERVAL 24 HOUR;")
	@Select("SELECT COUNT( DISTINCT address ) FROM tb_member WHERE  is_deleted = 0 AND address = #{address} AND update_time >= NOW() - INTERVAL 24 HOUR;")
	Integer isLogin24h(@Param("address")String address);

	@Select("SELECT COUNT(id) FROM tb_active WHERE type = '1' AND user_address = #{address} AND token_id is NOT NULL AND is_deleted = '0' AND update_time >= NOW() - INTERVAL 24 HOUR;")
	Integer selectPumpCount24h(@Param("address") String address);

}
