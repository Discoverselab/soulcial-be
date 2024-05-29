package org.springblade.modules.admin.pojo.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springblade.modules.admin.pojo.dto.ChatDetailDto;

import java.util.List;

@Data
@ApiModel("聊天消息详情VO")
@AllArgsConstructor
@NoArgsConstructor
public class ChatDetailVo {

	@ApiModelProperty("聊天消息记录")
	private List<ChatDetailListVo> detaillist;

	@ApiModelProperty("群聊/单聊相关信息")
	private ChatDetailDto chatDetailDto;

}
