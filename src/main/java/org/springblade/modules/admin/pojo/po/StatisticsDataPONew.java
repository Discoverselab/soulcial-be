package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Auther: FengZi
 * @Date: 2024/1/12 14:33
 * @Description:
 */

@TableName("tb_statistics_data_new")
@Data
public class StatisticsDataPONew {

	private String date;

	//-- 1. 每天新增连接钱包用户数（包含有分数的用户）
	private String a1;
	//-- 2. 累积连接钱包用户数
	private String a2;
	//-- 3. 每天新增有分数用户数
	private String a3;
	//-- 4. 累积有分数用户数
	private String a4;
	//-- 5. 每天新mint NFT数量
	private String a5;
	//-- 6. 累积mint NFT数量
	private String a6;
	//-- 7. 目前NFT挂单数量（截止到0112，从0113开始，每天会不一样）
	private String a7;
	//-- 8. 新增NFT挂单
	private String a8;
	//-- 9. 每天参与pump的用户数
	private String a9;
	//-- 10. 累积参与过pump的用户数
	private String a10;
	//-- 11. 每天发生pump的次数
	private String a11;
	//-- 12. 累积发生pump的次数
	private String a12;
	//-- 13. 每天发生开奖次数
	private String a13;
	//-- 14. 累积发生开奖次数
	private String a14;
	//-- 15. 每天发放积分总量
	private String a15;
	//-- 16. 累积发放积分总量
	private String a16;
	//-- 17. 截止到目前有积分的用户数量（截止到0112，从0113开始，每天会不一样）
	private String a17;
	//-- 18. 每天参与pump的总金额
	private String a18;
	//-- 19. 累积pump的总金额（pump就算，取消pump不算）
	private String a19;
	//-- 20. 非官方邀请用户数 (除soul-befb9e，链接钱包就算)
	private String a20;

	//--21. 日活数量
	private String a21;

}
