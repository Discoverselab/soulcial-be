package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Auther: FengZi
 * @Date: 2024/3/6 18:31
 * @Description:
 */
@TableName("tb_active_users")

@Data
public class ActiveUserPO {

	private String date;

	private String ip;
}
