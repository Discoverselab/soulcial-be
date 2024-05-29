package org.springblade.modules.admin.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SourceVo {

	/**
	 * 属性名称
	 */
	private String filedName;

	/**
	 * 分数
	 */
	private Integer source;

}
