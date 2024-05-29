package org.springblade.modules.admin.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("铸造使用图片VO")
@AllArgsConstructor
@NoArgsConstructor
public class CutPictureVo implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotBlank
	@ApiModelProperty("方形图片url")
	private String squarePictureUrl;


}
