package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * tb_member实体类
 *
 * @author yuanxx
 *
 */
@Data
@ApiModel("粉丝PO")
@TableName("tb_fans")
@NoArgsConstructor
@AllArgsConstructor
public class FansPO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	 * 类型：0-pick 1-collect
	 */
	private Integer type;

	/**
	 * 购买人id
	 */
	private Long pickUserId;

	/**
	 * 铸造人id
	 */
	private Long minterUserId;

	/**
	 * token_id
	 */
	private Long tokenId;

	/**
	 * pick_id
	 */
	private Long pickId;

	//group count
	@TableField(exist = false)
	private Long count;

	public FansPO(Integer type, Long pickUserId, Long minterUserId, Long tokenId, Long pickId) {
		this.type = type;
		this.pickUserId = pickUserId;
		this.minterUserId = minterUserId;
		this.tokenId = tokenId;
		this.pickId = pickId;
	}
}
