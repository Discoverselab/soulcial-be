package org.springblade.modules.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.oss.props.OssProperties;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.modules.admin.config.ContractProperties;
import org.springblade.modules.admin.dao.*;
import org.springblade.modules.admin.pojo.enums.RewardMessageEnum;
import org.springblade.modules.admin.pojo.po.*;
import org.springblade.modules.admin.pojo.query.ChatDetailQuery;
import org.springblade.modules.admin.pojo.vo.*;
import org.springblade.modules.admin.service.ChatService;
import org.springblade.modules.admin.service.NftService;
import org.springblade.modules.admin.util.PickUtil;
import org.springblade.modules.admin.util.ScoreUtil;
import org.springblade.modules.system.service.IDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/api/admin/message")
@Api(value = "聊天模块",tags = "聊天模块")
public class MessageController {


	@Autowired
	MemberMapper memberMapper;

	@Autowired
	NftService nftService;

	@Autowired
	PFPTokenMapper pfpTokenMapper;

	@Autowired
	PFPPickMapper pfpPickMapper;

	@Autowired
	MessageMapper messageMapper;

	@Autowired
	IDictService dictService;

	@Autowired
	VSoulHistoryMapper vSoulHistoryMapper;

	@Resource
	private ChatService chatService;

	@Value("${contract.linkRate}")
	private BigDecimal linkRate;

	@Autowired
	private OSS ossClient;

	@Autowired
	private OssProperties ossProperties;


	// 统一aliyun oss上传
	@PostMapping("/chat/upload")
	@CrossOrigin
	@ApiOperation(value = "上传聊天图片，返回文件路径，压缩预览格式为${url}?x-oss-process=image/resize,w_180 （180为宽度，自行调整）")
	public R<String> upload(@Validated @RequestParam("file") MultipartFile file) {
		// 时间戳文件名 + 原始文件名后缀
		String fileName = System.currentTimeMillis() + file.getOriginalFilename();
		String bucketName = ossProperties.getBucketName();

		try {
			// Get the file input stream
			InputStream inputStream = file.getInputStream();
			// Upload the file to OSS
			PutObjectResult result = ossClient.putObject(bucketName, fileName, inputStream);
//			ossClient.shutdown();
			return R.data("https://" + bucketName + "." + ossProperties.getEndpoint() + "/" + fileName);
		} catch (Exception e) {
			e.printStackTrace();
			return R.fail("Error while uploading: " + e.getMessage());
		}
	}

	@GetMapping("/getUserMessage")
	@ApiOperation(value = "获取用户聊天信息")
	public R<List<MessageVo>> getUserMessage() {
		List<MessageVo> list = new ArrayList<>();

		Long userId = StpUtil.getLoginIdAsLong();
		MemberPO memberPO = memberMapper.selectById(userId);

		List<MessagePO> messagePOS = messageMapper.selectList(new LambdaQueryWrapper<MessagePO>()
			.eq(MessagePO::getToUserId, userId)
			.eq(BasePO::getIsDeleted, 0)
			.orderByDesc(BasePO::getCreateTime));

		for (MessagePO messagePO : messagePOS) {
			Long fromUserId = messagePO.getFromUserId();
			MemberPO fromUser = memberMapper.selectById(fromUserId);

			MessageVo messageVo = new MessageVo();
			BeanUtil.copyProperties(messagePO,messageVo);

			if(messagePO.getType() == 0){
				//系统消息：设置默认用户名和头像
				String systemName = dictService.getValue("system_name", "0");
				String sysytemAvatar = dictService.getValue("sysytem_avatar", "0");

				messageVo.setFromUserName(systemName);
				messageVo.setFromUserAvatar(sysytemAvatar);
			}else if(messagePO.getType() == 1){
				//用户消息
				messageVo.setFromUserName(fromUser.getUserName());
				messageVo.setFromUserAvatar(fromUser.getAvatar());
			}

			messageVo.setToUserId(userId);
			messageVo.setToUserName(memberPO.getUserName());
			messageVo.setToUserAvatar(memberPO.getAvatar());

			messageVo.setCreateTimeStr(messageVo.getCreateTime());

			list.add(messageVo);

		}
		return R.data(list);
	}

	@GetMapping("/getRewardInfo")
	@ApiOperation(value = "获取开奖信息详情")
	public R<PickResultVo> getRewardInfo(@RequestParam("messageId") Long messageId) {

		MessagePO messagePO = messageMapper.selectById(messageId);
		//消息已读
		messagePO.setIsRead(1);
		messageMapper.updateById(messagePO);

		Long pickId = messagePO.getPickId();

		PFPPickPO pfpPickPO = pfpPickMapper.selectById(pickId);

		PickResultVo resultVo = new PickResultVo();
		resultVo.setPickId(pickId);
		resultVo.setRewardBlockHeight(pfpPickPO.getRewardBlockHeight());
		resultVo.setRewardBlockHash(pfpPickPO.getRewardBlockHash());
		resultVo.setRewardIndex(pfpPickPO.getRewardIndex());
		resultVo.setTokenId(pfpPickPO.getTokenId());

		//当前登录用户
		Long userId = StpUtil.getLoginIdAsLong();

		//中奖、未中奖消息
		if(RewardMessageEnum.REWARD_SUCCESS.getName().equals(messagePO.getMessage()) || RewardMessageEnum.REWARD_FAILED.getName().equals(messagePO.getMessage())){
			//获取用户的积分
			List<VSoulHistoryPO> vSoulHistoryPO = vSoulHistoryMapper.selectList(new LambdaQueryWrapper<VSoulHistoryPO>()
				.eq(BasePO::getIsDeleted, 0)
				.eq(VSoulHistoryPO::getUserId, userId)
				.eq(VSoulHistoryPO::getPickId, pickId));
			if (vSoulHistoryPO != null && !vSoulHistoryPO.isEmpty()) {
				resultVo.setVSoulPrice(vSoulHistoryPO.get(0).getVSoulPrice());
			}

			//当前用户不是中奖用户
			if(!userId.equals(pfpPickPO.getRewardUserId())){
				//设置未中奖收益 跟mint一样
				resultVo.setRewardPirce(pfpPickPO.getMinterRewardPrice());
			}
		}
		//卖出消息
		else if (RewardMessageEnum.SELL_SUCCESS.getName().equals(messagePO.getMessage())) {
			//卖出收益
			BigDecimal sellerRewardPrice = pfpPickPO.getSellerRewardPrice();
			resultVo.setRewardPirce(sellerRewardPrice);
		}
		//铸造者收益消息
		else if (RewardMessageEnum.MINTER_REWARD.getName().equals(messagePO.getMessage())) {
			//铸造者收益
			BigDecimal minterRewardPrice = pfpPickPO.getMinterRewardPrice();
			resultVo.setRewardPirce(minterRewardPrice);
		}
		//卖出 + 铸造者收益消息
		else if (RewardMessageEnum.SELLER_IS_MINTER.getName().equals(messagePO.getMessage())) {
			//卖出 + 铸造者收益
			BigDecimal minterRewardPrice = pfpPickPO.getMinterRewardPrice();
			BigDecimal sellerRewardPrice = pfpPickPO.getSellerRewardPrice();
			BigDecimal rewardPrice = minterRewardPrice.add(sellerRewardPrice);
			//去除末尾的0
			String bigDecimalStr = rewardPrice.stripTrailingZeros().toPlainString();
			resultVo.setRewardPirce(new BigDecimal(bigDecimalStr));
		}

		//获取NFT信息
		Long tokenId = pfpPickPO.getTokenId();

//		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectById(tokenId);
		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(tokenId);
		PFPTokenDetailVo result = new PFPTokenDetailVo();
		BeanUtil.copyProperties(pfpTokenPO, result);

		//获取持有人信息
		MemberPO mintUser = memberMapper.selectById(pfpTokenPO.getMintUserId());
		MemberPO ownerUser = memberMapper.selectById(pfpTokenPO.getOwnerUserId());

		result.setMintUserName(mintUser.getUserName());
		result.setMintUserAvatar(mintUser.getAvatar());
		result.setOwnerUserName(ownerUser.getUserName());
		result.setOwnerUserAvatar(ownerUser.getAvatar());

		result.setBestPick(null);

		result.setIsMineMint(0);
		result.setIsMineOwner(0);

		MemberPO memberPO = memberMapper.selectById(userId);

		//计算相似度
		result.setMatch(ScoreUtil.getMatch(memberPO.getUserTags(),
			memberPO.getCharisma(),memberPO.getExtroversion(),memberPO.getEnergy(),
			memberPO.getWisdom(),memberPO.getArt(),memberPO.getCourage(),
			result.getMintUserTags(),
			result.getCharisma(),result.getExtroversion(),result.getEnergy(),
			result.getWisdom(),result.getArt(),result.getCourage()));

		//是否为本人铸造
		if(pfpTokenPO.getMintUserId().equals(userId)){
			result.setIsMineMint(1);
		}

		//是否为本人持有
		if(pfpTokenPO.getOwnerUserId().equals(userId)){
			result.setIsMineOwner(1);
		}

		//获取中奖人名称
		resultVo.setUserName(memberMapper.selectById(pfpPickPO.getRewardUserId()).getUserName());

		//出售价格改为本次成交价格
		result.setPrice(pfpPickPO.getPrice());
		//下次出售价格
		result.setNextListPrice(PickUtil.getSalePrice(pfpPickPO.getLevel(),pfpPickPO.getTransactionsCount() + 1,linkRate));
		resultVo.setPfpTokenDetailVo(result);

		return R.data(resultVo);
	}


	//=========================================chat=======================================
	@GetMapping("/chat/list")
	@ApiOperation(value = "获取聊天信息列表")
	public R<List<ChatListVo>> getChatList(Long userId){
		if (userId == null) {
			userId = StpUtil.getLoginIdAsLong();
		}
		List<ChatListVo> chatList = chatService.getChatList(userId);
		List<ChatListVo> chatListVos = addSystemMessage(chatList);
		return R.data(chatListVos);
	}

	@GetMapping("/chat/detail")
	@ApiOperation(value = "获取聊天详情数据")
	public R<ChatDetailVo> getChatDetailList(
		ChatDetailQuery query
	){
		ChatDetailVo chatDetailList = chatService.getChatDetailList(query);
		return R.data(chatDetailList);
	}



	/**
	 * 整合系统消息
	 * @param chatList
	 * @return
	 */
	private List<ChatListVo> addSystemMessage(List<ChatListVo> chatList) {
		List<MessageVo> list = new ArrayList<>();

		Long userId = StpUtil.getLoginIdAsLong();
		MemberPO memberPO = memberMapper.selectById(userId);

		List<MessagePO> messagePOS = messageMapper.selectList(new LambdaQueryWrapper<MessagePO>()
			.eq(MessagePO::getToUserId, userId)
			.eq(BasePO::getIsDeleted, 0)
			.orderByDesc(BasePO::getCreateTime));

		for (MessagePO messagePO : messagePOS) {
			Long fromUserId = messagePO.getFromUserId();
			MemberPO fromUser = memberMapper.selectById(fromUserId);

			MessageVo messageVo = new MessageVo();
			BeanUtil.copyProperties(messagePO, messageVo);

			if (messagePO.getType() == 0) {
				//系统消息：设置默认用户名和头像
				String systemName = dictService.getValue("system_name", "0");
				String sysytemAvatar = dictService.getValue("sysytem_avatar", "0");

				messageVo.setFromUserName(systemName);
				messageVo.setFromUserAvatar(sysytemAvatar);
			} else if (messagePO.getType() == 1) {
				//用户消息
				messageVo.setFromUserName(fromUser.getUserName());
				messageVo.setFromUserAvatar(fromUser.getAvatar());
			}

			messageVo.setToUserId(userId);
			messageVo.setToUserName(memberPO.getUserName());
			messageVo.setToUserAvatar(memberPO.getAvatar());
			messageVo.setCreateTimeStr(messageVo.getCreateTime());


			//TODO
			messageVo.setFromUserAvatar("https://soulcial-test.oss-ap-southeast-1.aliyuncs.com/169980007670422222.png");
			list.add(messageVo);

		}
		List<ChatListVo> collect = list.stream().map(s -> {
			ChatListVo vo = new ChatListVo();
			vo.setType("3");
			vo.setAvator(s.getFromUserAvatar());
			vo.setId(s.getId());
			vo.setRelatedContent(s.getContent());
			vo.setTime(dateToStr(s.getCreateTime()));
			vo.setUnreadNum(s.getIsRead() == 1 ? 0L : 1L);
			vo.setTitle(s.getTitle());
			vo.setSysMessage(s.getMessage());
			return vo;
		}).collect(Collectors.toList());
		chatList.addAll(collect);
		//排序
		List<ChatListVo> result = chatList.stream()
			.sorted(Comparator.comparing(ChatListVo::getTime).reversed())
			.collect(Collectors.toList());
		return result;
	}


	/**
	 * 日期格式转换
	 * @param inputDate
	 * @return
	 */
	private String dateToStr(Date inputDate){
		try {
			// 定义日期时间格式
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			// 将日期转换为指定格式的字符串
			String formattedDate = dateFormat.format(inputDate);
			return formattedDate;
		} catch (Exception e) {
			log.info("日期格式类型转换错误：{}",e);
		}
		return DateUtil.now().toString();
	}

	@Resource
	private ContractProperties contractProperties;

	@GetMapping("/getcontract")
	public String getcontract() {
		return JSONUtil.toJsonStr(contractProperties);
	}



}
