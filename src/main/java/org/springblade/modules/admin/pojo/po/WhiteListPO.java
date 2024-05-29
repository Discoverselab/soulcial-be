package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * tb_member实体类
 *
 * @author yuanxx
 *
 */
@Data
@ApiModel("白名单PO")
@TableName("tb_white_list")
public class WhiteListPO extends BasePO {

	private static final long serialVersionUID = 1L;
	/**
	 * 钱包地址
	 */
	private String address;

	/**
	 * 是否可以mint两次
	 */
	private int canMintTwice;
}
