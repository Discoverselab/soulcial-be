package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springblade.modules.admin.config.BigDecimalHandler;

import java.math.BigDecimal;

/**
 * tb_pfp_transaction实体类
 *
 * @author yuanxx
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("用户积分表PO")
@TableName(value = "tb_user_vSoul",autoResultMap = true)
public class UserVSoulPO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	* 用户id
	*/
	private Long userId;

	/**
	 * 积分余额
	 */
	@ApiModelProperty("积分余额")
	@TableField(typeHandler = BigDecimalHandler.class)
	private BigDecimal vSoulPrice;

	/*
	 * 不作为数据库字段使用
	 * @author FengZi
	 * @date 18:36 2024/1/22
	 * @param null
	 * @return null
	 **/
	@ApiModelProperty("当前登录用户Booster值")
	@TableField(exist = false)
	private BigDecimal Booster;

}
