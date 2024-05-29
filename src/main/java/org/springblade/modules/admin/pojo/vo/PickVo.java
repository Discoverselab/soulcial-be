package org.springblade.modules.admin.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("VO")
@AllArgsConstructor
@NoArgsConstructor
public class PickVo implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull
	private Long messageId;

}
