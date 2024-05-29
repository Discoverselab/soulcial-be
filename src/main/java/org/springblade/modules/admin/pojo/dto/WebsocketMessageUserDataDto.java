package org.springblade.modules.admin.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.python.antlr.ast.Str;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebsocketMessageUserDataDto {

	private Long userId;

	private String userAvatar;

	private String userName;

}
