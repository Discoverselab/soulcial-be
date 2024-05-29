package org.springblade.modules.admin.pojo.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Data
public class ChatRoomUsersDto {
    private Long chatId;
    private String userIds; // 同样是字符串
    public List<Long> getUserIdList() {
        if (userIds != null && !userIds.isEmpty()) {
            String[] ids = userIds.split(",");
            return Arrays.stream(ids).map(Long::parseLong).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
