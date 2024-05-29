package org.springblade.modules.admin.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("PICK_NFT_VO")
@AllArgsConstructor
@NoArgsConstructor
public class PickNftVo implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull
	@ApiModelProperty("token id")
	private Long tokenId;

	@NotNull
	@ApiModelProperty("签号：0、1、2、3")
	private Integer pickIndex;

	@NotNull
	@ApiModelProperty("交易hash")
	private String txn;

}
