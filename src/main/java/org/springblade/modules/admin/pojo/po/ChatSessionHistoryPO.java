package org.springblade.modules.admin.pojo.po;


import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel("进出群聊记录PO")
@TableName("tb_chat_session_history")
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionHistoryPO extends BasePO{


	@ApiModelProperty(value = "聊天ID", example = "123")
	private Long chatId;

	@ApiModelProperty(value = "用户ID", example = "456")
	private Long userId;

	@ApiModelProperty(value = "进入聊天时间", example = "2023-04-25 10:30:00")
	private String startTime;

	@ApiModelProperty(value = "退出聊天时间", example = "2023-04-25 11:30:00")
	private String endTime;

}
