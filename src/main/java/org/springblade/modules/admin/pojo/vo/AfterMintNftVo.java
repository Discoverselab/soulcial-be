package org.springblade.modules.admin.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("铸造VO")
@AllArgsConstructor
@NoArgsConstructor
public class AfterMintNftVo implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull
	@ApiModelProperty("token_id")
	private Long tokenId;

	@NotNull
	@ApiModelProperty("是否成功：0-否 1-是")
	private Integer isSuccess;

	@ApiModelProperty("交易hash")
	private String txn;

}
