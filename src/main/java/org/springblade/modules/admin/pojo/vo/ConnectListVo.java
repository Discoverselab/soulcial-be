package org.springblade.modules.admin.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("好友连接列表")
public class ConnectListVo {

	@ApiModelProperty("全部")
	private List<ConnectVo> allList;

	@ApiModelProperty("star")
	private List<ConnectVo> startList;

	@ApiModelProperty("New")
	private List<ConnectVo> newList;
}
