package org.springblade.modules.admin.socket;

import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaFoxUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.admin.cache.IUserCache;
import org.springblade.modules.admin.dao.ChatDetailMapper;
import org.springblade.modules.admin.dao.ChatOverviewMapper;
import org.springblade.modules.admin.dao.ChatSessionHistoryMapper;
import org.springblade.modules.admin.pojo.dto.IUserCacheDto;
import org.springblade.modules.admin.pojo.dto.WebsocketMessageDto;
import org.springblade.modules.admin.pojo.po.ChatDetailPO;
import org.springblade.modules.admin.pojo.po.ChatSessionHistoryPO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@ServerEndpoint(value = "/websocket/{satoken}")  // 接口路径 ws://localhost:9005/pfp/webSocket/{satoken};
public class WebSocket {


	private static ChatDetailMapper chatDetailMapper;

	@Autowired
	public void setChatDetailMapper(ChatDetailMapper chatDetailMapper) {
		WebSocket.chatDetailMapper = chatDetailMapper;
	}

	private static ChatSessionHistoryMapper chatSessionHistoryMapper;

	@Autowired
	public void setChatSessionHistoryMapper(ChatSessionHistoryMapper chatSessionHistoryMapper) {
		WebSocket.chatSessionHistoryMapper = chatSessionHistoryMapper;
	}

	private static ChatOverviewMapper chatOverviewMapper;

	@Autowired
	public void setChatOverviewMapper(ChatOverviewMapper chatOverviewMapper) {
		WebSocket.chatOverviewMapper = chatOverviewMapper;
	}

	/**
	 * 用来记录session_id与用户id之间的关系
	 */
	private static Map<Long, Session> userIdMap = new ConcurrentHashMap(8);

	/**
	 * 链接成功调用的方法
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("satoken") String satoken) throws IOException {
		// 根据 token 获取对应的 userId
		Long userId = getUserIdByToken(session, satoken);
		userIdMap.put(userId, session);
		log.info("用户：{}加入聊天室", userId);
	}

	@OnClose
	public void close(Session session, @PathParam("satoken") String satoken) {
		Long userId = getUserIdByToken(session, satoken);
		userIdMap.remove(userId);
		//根据用户最近一次进入群聊时间，推断出用户最近一次进入的群聊，并赋予该群聊的推出时间。
		ChatSessionHistoryPO historyPO = new ChatSessionHistoryPO();
		historyPO.setIsDeleted(0);
		Long chatId = chatOverviewMapper.getChatHistoryChatId(userId);
		historyPO.setChatId(chatId);
		historyPO.setUserId(userId);
		historyPO.setEndTime(DateUtil.now());
		int insert = chatSessionHistoryMapper.insert(historyPO);
		log.info("message history insert : {}", insert > 0);
		log.info("用户：{}离开了聊天室：{}", userId, chatId);
	}

	@OnMessage
	public void reveiveMessage(@PathParam("satoken") String satoken
		, Session session, String message) throws IOException {
		//解析数据
		ChatDetailPO bean = JSONUtil.toBean(message, ChatDetailPO.class);
		Long userIdByToken = getUserIdByToken(session, satoken);
		if ("999".equals(bean.getType() + "")) {
			//心跳
			sendMessagePing(userIdByToken);
			log.info("接受到用户：{} 的心跳",userIdByToken);
			return;
		}

		//判断用户是否具有该聊天权限
		Long userId = getUserIdByToken(session, satoken, bean.getChatId());
		//用户不是该群聊的用户
		if (userId == null) {
			response(userId, new WebsocketMessageDto());
			return;
		}
		log.info("接受到用户{}的数据:{},tokenuserId{}", session.getId(), message, userId);
		if ("888".equals(bean.getType() + "")) {
			//记录用户进入群聊，退出群聊时间
			ChatSessionHistoryPO historyPO = JSONUtil.toBean(message, ChatSessionHistoryPO.class);
			historyPO.setIsDeleted(0);
			int insert = chatSessionHistoryMapper.insert(historyPO);
			log.info("message history insert : {}", insert > 0);
		} else {
			//判断消息是否存在内容
			if (StringUtils.isBlank(bean.getContent())) {
				return;
			}
			//转发消息 消息入库
			bean.setChatId(bean.getChatId());
			bean.setCreateTime(DateTime.now());
			bean.setIsDeleted(0);
			bean.setUpdateTime(DateTime.now());
			bean.setVersion(1L);
			bean.setUpdateUser(userId);
			bean.setCreateUser(userId);
			bean.setUserId(userId);
			int insert = chatDetailMapper.insert(bean);
			//封装消息
			WebsocketMessageDto vo = new WebsocketMessageDto();
			BeanUtils.copyProperties(bean, vo);
			IUserCacheDto iUserCacheDto = IUserCache.getUserCache(vo.getUserId());
			if (iUserCacheDto != null) {
				vo.setMessageId(bean.getId());
				vo.setUserAvatar(iUserCacheDto.getUserAvatar());
				vo.setUserName(iUserCacheDto.getUserName());
				vo.setChatId(bean.getChatId());
			}
			vo.setTime(DateUtil.now());
			log.info("用户和：{}，消息发送结果：{}，消息内容：{}", userId, insert > 0, vo);

			//通知消息是否发送成功
			response(userId, vo);

			//发送消息
			sendMessage(bean.getChatId(), userId, vo);
		}
	}

	/**
	 * 通知消息是否发送成功
	 *
	 * @param userId
	 * @param vo
	 */
	private static void response(Long userId, WebsocketMessageDto vo) {
		//---------给前端回复是否发送成功---------
		Session ss = userIdMap.get(userId);
		try {
			ss.getBasicRemote().sendText(JSONUtil.toJsonStr(vo));
		} catch (IOException e) {
			log.info("消息发送异常：{}", e.getMessage());
			throw new RuntimeException(e);
		}
		//-----------------------------------
	}

	/**
	 * 给房间内除了自己所有用户发送消息，包含自己
	 *
	 * @param chatId
	 * @param userId
	 */
	public void sendMessage(Long chatId, Long userId, WebsocketMessageDto vo) {
		try {
			//根据chatid查询出该聊天所有的用户，发送消息
			List<Long> userids = IUserCache.getChatUsers(chatId);
//		List<Long> userids = chatOverviewMapper.getChatRoomUsers2(chatId).getUserIdList();
			userids.stream().forEach(s -> {
				if (!s.equals(userId)) {
					Session session = userIdMap.get(s);
					if (session != null) {
						try {
							session.getBasicRemote().sendText(JSONUtil.toJsonStr(vo));
							log.info("消息发送成功！{}", JSONUtil.toJsonStr(vo));
						} catch (IOException e) {
							log.info("消息发送异常：{}", e.getMessage());
							throw new RuntimeException(e);
						}
					}
				}
			});
		}catch (Exception e){
			log.error("================================");
			log.error("消息发送异常：{}", e.getMessage());
			log.error("消息内容：{}", vo.toString());
			log.error("chatId: {},userId: {}", chatId,userId);
			log.error("================================");
		}

	}

	/**
	 * 发送心跳回应
	 *
	 * @param userId
	 */
	private void sendMessagePing(Long userId) throws IOException {
		//根据chatid查询出该聊天所有的用户，发送消息
		Session session = userIdMap.get(userId);
		HashMap<String, String> map = new HashMap<>();
		map.put("type", "999");
		session.getBasicRemote().sendText(JSONUtil.toJsonStr(map));
		log.info("心跳消息发送成功！{}", JSONUtil.toJsonStr(map));
	}

	/**
	 * 发送错误时的处理
	 *
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		log.error("websocket接口异常：{}", error.getMessage());
	}

	/**
	 * 解析token并返回用户id
	 *
	 * @param session
	 * @param satoken
	 * @return
	 */
	private Long getUserIdByToken(Session session, String satoken, Long chatId) {
		try {
			// 根据 token 获取对应的 userId
			Object loginId = StpUtil.getLoginIdByToken(satoken);
			if (loginId == null) {
				session.close();
				log.error("websocket接口异常：{}", "连接失败，无效Token");
				throw new SaTokenException("连接失败，无效Token：" + satoken);
			}
			Long userId = SaFoxUtil.getValueByType(loginId, long.class);

			if (chatId == null){
				return null;
			}

			//校验用户是否具有该聊天室的权限。
			List<Long> chatUsers = IUserCache.getChatUsers(chatId);
			if (chatUsers != null && chatUsers.size() > 0) {
				boolean contains = chatUsers.contains(userId);
				log.info("用户：{}，是否具有聊天室：{}的权限：{}", userId, chatId, contains);
				return userId;
			} else {
				return null;
			}
		} catch (Exception e) {
			try {
				session.close();
				log.error("websocket接口异常：{}", e);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			log.error("websocket接口异常：{}", e);
			throw new SaTokenException("连接失败，无效Token：" + satoken);
		}
	}

	/**
	 * 解析token并返回用户id
	 *
	 * @param session
	 * @param satoken
	 * @return
	 */
	private Long getUserIdByToken(Session session, String satoken) {
		try {
			// 根据 token 获取对应的 userId
			Object loginId = StpUtil.getLoginIdByToken(satoken);
			if (loginId == null) {
				session.close();
				log.error("websocket接口异常：{}", "连接失败，无效Token");
				throw new SaTokenException("连接失败，无效Token：" + satoken);
			}
			return SaFoxUtil.getValueByType(loginId, long.class);
		} catch (Exception e) {
			try {
				session.close();
				log.error("websocket接口异常：{}", e);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			log.error("websocket接口异常：{}", e);
			throw new SaTokenException("连接失败，无效Token：" + satoken);
		}
	}


}
