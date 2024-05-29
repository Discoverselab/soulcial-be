package org.springblade.modules.admin.pojo.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class UserChatRoomsDto {
    private Long userId;
    private String chatRooms; // 因为GROUP_CONCAT返回的是字符串

    public List<Long> getChatRoomIdList() {
        if (chatRooms != null && !chatRooms.isEmpty()) {
            String[] ids = chatRooms.split(",");
            return Arrays.stream(ids).map(Long::parseLong).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
