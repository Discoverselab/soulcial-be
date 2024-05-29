package org.springblade.modules.admin.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@ApiModel("VO")
@AllArgsConstructor
@NoArgsConstructor
public class FansVo implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("用户ID")
	private Long userId;

	@ApiModelProperty("用户头像")
	private String avatar;

	@ApiModelProperty("用户钱包地址")
	private String address;

	@ApiModelProperty("用户昵称")
	private String userName;

	@ApiModelProperty("pick/collect数量")
	private Long count;

}
