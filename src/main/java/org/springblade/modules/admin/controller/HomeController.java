package org.springblade.modules.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.modules.admin.config.Web3jConfig;
import org.springblade.modules.admin.dao.*;
import org.springblade.modules.admin.pojo.enums.UserTagsEnum;
import org.springblade.modules.admin.pojo.po.*;
import org.springblade.modules.admin.pojo.query.FollowUserQuery;
import org.springblade.modules.admin.pojo.query.PickByInviteCodeQuery;
import org.springblade.modules.admin.pojo.vo.*;
import org.springblade.modules.admin.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;

@RestController
@Slf4j
@RequestMapping("/api/admin/home")
@Api(value = "我的信息相关接口(home)", tags = "我的信息相关接口(home)")
public class HomeController {

	@Autowired
	ETHService ethService;

	@Autowired
	MemberMapper memberMapper;

	@Autowired
	NftService nftService;

	@Autowired
	@Qualifier("marketService")
	MarketService marketService;

	@Autowired
	@Qualifier("newMarketService")
	MarketService newMarketService;

	@Autowired
	@Qualifier("newMarketService2")
	MarketService newMarketService2;

	// 交易所合约地址
	@Value("${contract.marketAddress}")
	private String marketAddress;

	@Value("${contract.newmarketAddress}")
	private String marketAddress2;

	@Autowired
	ActiveMapper activeMapper;

	@Autowired
	PFPTokenMapper pfpTokenMapper;

	@Autowired
	MemberFollowMapper memberFollowMapper;

	@Autowired
	UserScoreService userScoreService;

	@Autowired
	PFPPickMapper pfpPickMapper;

	@Autowired
	WhiteListMapper whiteListMapper;

	@Autowired
	FansMapper fansMapper;

	@Autowired
	MemberConnectService memberConnectService;

	@Autowired
	MemberInviteService memberInviteService;

	@Autowired
	VSoulHistoryMapper vSoulHistoryMapper;

	// 添加userVSoulMapper
	@Autowired
	UserVSoulMapper userVSoulMapper;

	@Resource
	private ChatMemberMapper chatMemberMapper;


	@PostMapping("/followUser")
	@ApiOperation(value = "关注/取消关注")
	public R<UserInfoVo> followUser(@Valid @RequestBody FollowUserQuery followUserQuery) {

		Long userId = StpUtil.getLoginIdAsLong();
		Integer followType = followUserQuery.getFollowType();
		Long subscribeUserId = followUserQuery.getSubscribeUserId();
		if (followType == 0) {
			//取消关注
			MemberFollowPO memberFollowPO = memberFollowMapper.selectOne(new LambdaQueryWrapper<MemberFollowPO>()
				.eq(BasePO::getIsDeleted, 0)
				.eq(MemberFollowPO::getUserId, userId)
				.eq(MemberFollowPO::getSubscribeUserId, subscribeUserId));

			//存在，删除关注
			if (memberFollowPO != null) {
				memberFollowMapper.deleteById(memberFollowPO.getId());
			}
		} else {
			if (userId.equals(subscribeUserId)) {
				//自己不能关注自己
				return R.fail("can not follow yourself");
			}

			//关注
			MemberFollowPO memberFollowPO = memberFollowMapper.selectOne(new LambdaQueryWrapper<MemberFollowPO>()
				.eq(BasePO::getIsDeleted, 0)
				.eq(MemberFollowPO::getUserId, userId)
				.eq(MemberFollowPO::getSubscribeUserId, subscribeUserId));

			//不存在，添加关注
			if (memberFollowPO == null) {
				memberFollowPO = new MemberFollowPO();
				memberFollowPO.setUserId(userId);
				memberFollowPO.setSubscribeUserId(subscribeUserId);
				memberFollowPO.initForInsert();
				memberFollowMapper.insert(memberFollowPO);
			}
		}
		return R.success("成功");
	}

	@GetMapping("/getFollowers")
	@ApiOperation(value = "被关注列表（FOLLOWERS）")
	public R<List<SubscribeFollowUserVo>> getFollowers(@ApiParam("用户id：不传的话默认查自己，不登录时userId必传") @RequestParam(value = "userId", required = false) Long userId) {

		List<SubscribeFollowUserVo> result = new ArrayList<>();
		//是否是其他人
		boolean isOther = true;

		if (userId == null) {
			//是本人
			isOther = false;
			userId = StpUtil.getLoginIdAsLong();
		}
		//关注
		List<MemberFollowPO> list = memberFollowMapper.selectList(new LambdaQueryWrapper<MemberFollowPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(MemberFollowPO::getSubscribeUserId, userId));

		for (MemberFollowPO x : list) {
			Long subscribeUserId = x.getUserId();
			MemberPO memberPO = memberMapper.selectById(subscribeUserId);

			SubscribeFollowUserVo subscribeFollowUserVo = new SubscribeFollowUserVo();
			BeanUtil.copyProperties(memberPO, subscribeFollowUserVo);

			//是本人的时候，查询是否互关
			if (!isOther) {
				//查询是否互关
				MemberFollowPO memberFollowPO = memberFollowMapper.selectOne(new LambdaQueryWrapper<MemberFollowPO>()
					.eq(BasePO::getIsDeleted, 0)
					.eq(MemberFollowPO::getUserId, userId)
					.eq(MemberFollowPO::getSubscribeUserId, x.getUserId()));
				if (memberFollowPO != null) {
					//互关
					subscribeFollowUserVo.setIsFollow(1);
				} else {
					//未互关
					subscribeFollowUserVo.setIsFollow(0);
				}
			}

			result.add(subscribeFollowUserVo);
		}

		return R.data(result);
	}

	@GetMapping("/getFollowing")
	@ApiOperation(value = "关注列表（FOLLOWING）")
	public R<List<FollowUserVo>> getFollowing(@ApiParam("用户id：不传的话默认查自己，不登录时userId必传") @RequestParam(value = "userId", required = false) Long userId) {

		List<FollowUserVo> result = new ArrayList<>();
		if (userId == null) {
			userId = StpUtil.getLoginIdAsLong();
		}
		//关注
		List<MemberFollowPO> list = memberFollowMapper.selectList(new LambdaQueryWrapper<MemberFollowPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(MemberFollowPO::getUserId, userId));

		list.forEach(x -> {
			Long subscribeUserId = x.getSubscribeUserId();
			MemberPO memberPO = memberMapper.selectById(subscribeUserId);

			FollowUserVo followUserVo = new FollowUserVo();
			BeanUtil.copyProperties(memberPO, followUserVo);

			result.add(followUserVo);
		});

		return R.data(result);
	}

	@GetMapping("/getTagsList")
	@ApiOperation(value = "获取用户标签选项list（Tags）")
	public R<List<EnumVo>> getTagsList() {
		List<EnumVo> result = new ArrayList<>();
		for (UserTagsEnum value : UserTagsEnum.values()) {
			EnumVo enumVo = new EnumVo();
			BeanUtil.copyProperties(value, enumVo);
			result.add(enumVo);
		}
		return R.data(result);
	}

	@GetMapping("/getTwitterHtml")
	@ApiOperation(value = "获取当前用户分享的推的html")
	public R<String> getTwitterHtml(
		@ApiParam("netWork") @RequestParam(value = "netWork", required = true)
		String netWork,
		@ApiParam("userInfoId") @RequestParam(value = "userId", required = true)
		String userInfoId,
		@ApiParam("personality") @RequestParam(value = "personality", required = true)
		String personality,
		@ApiParam("chracter") @RequestParam(value = "chracter", required = true)
		String chracter
	) {
		String html = "<!DOCTYPE html>\n" +
			"        <html lang=\"en\">\n" +
			"        <head>\n" +
			"          <meta charset=\"UTF-8\">\n" +
			"          <meta name=\"viewport\" content=\"width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0\">\n" +
			"          <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\n" +
			"          <meta property=\"twitter:url\" content= " + netWork + "/user?id=" + userInfoId + "/>\n" +
			"          <meta name=\"twitter:title\" content= My SOUL in Web3 is " + personality + " " + chracter + "/>\n" +
			"          <meta name=\"twitter:description\" content=\"This is desc\"/>\n" +
			"          <meta name=\"twitter:site\" content=\"http://gg.chendahai.cn/static/share/index.html\">\n" +
			"          <meta name=\"twitter:card\" content=\"summary_large_image\"/>\n" +
			"          <meta name=\"twitter:image\" content=\"https://img0.baidu.com/it/u=3232582821,3516640051&fm=253&app=138&size=w931&n=0&f=JPEG&fmt=auto?sec=1694624400&t=9e7252c7a5a41ac57e3f55666be42e50\"/>\n" +
			"          \n" +
			"          <title>share test</title>\n" +
			"        </head>\n" +
			"        <body>\n" +
			"        </body>\n" +
			"        </html>`";

		return R.data(html);
	}

	// 取消推特绑定状态
	@PostMapping("/cancelTwitterBind")
	@ApiOperation(value = "取消推特绑定状态")
	public R<String> cancelTwitterBind() {
		Long userId = StpUtil.getLoginIdAsLong();
		MemberPO memberPO = memberMapper.selectById(userId);
		memberPO.setBindTwitter(0);
		memberPO.initForUpdate();
		memberMapper.updateById(memberPO);
		return R.data("success");
	}


	@GetMapping("/getUserInfo")
	@ApiOperation(value = "获取用户信息 P1")
	public R<UserInfoVo> getUserInfo(@ApiParam("用户code查询：都不传的话默认查自己e") @RequestParam(value = "code", required = false) String code,
									 @ApiParam("用户id查询：都不传的话默认查自己") @RequestParam(value = "userId", required = false) Long userId) {
		boolean isOther = false;
		MemberPO memberPO = null;
		UserInfoVo userInfoVo = new UserInfoVo();
		int mintStatus = 0;
		WhiteListPO whiteListPO = null;
		// 是本人
		if (code == null && userId == null) {
			memberPO = memberMapper.selectById(StpUtil.getLoginIdAsLong());
			log.info("memberPO = {}", memberPO);
			if (memberPO.getWhiteList() == 1) {
				// 平台白名单用户（自用无限mint）
				mintStatus = 2;
			} else {
				// 如果是white_list用户
				whiteListPO = whiteListMapper.selectOne(new LambdaQueryWrapper<WhiteListPO>()
					.eq(BasePO::getIsDeleted, 0)
					.eq(WhiteListPO::getAddress, memberPO.getAddress()));
				// 获取pfp token 本人数量
				Long pfpTokenCount = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
					.eq(BasePO::getIsDeleted, 0)
					.eq(PFPTokenPO::getMintUserId, memberPO.getId()));
				// 获取本人已经pick的数量
				Long pickCount = activeMapper.selectCount(new LambdaQueryWrapper<ActivePO>()
					.eq(BasePO::getIsDeleted, 0)
					.eq(ActivePO::getType, 1)
					.eq(ActivePO::getUserAddress, memberPO.getAddress().toLowerCase()));
				log.info("pfpTokenCount = {}", pfpTokenCount);
				log.info("pickCount = {}", pickCount);
				log.info("WhiteListPO = {}", whiteListPO);
				userInfoVo.setPickCount(pickCount.intValue());
				int mintCount = 1; // mint阈值 1 默认 1 直接mint 普通白名单 2  2次的白名单
				if (whiteListPO != null) {
					// canMintTwice为1，可以mint两次
					if (whiteListPO.getCanMintTwice() == 1) {
						mintCount += 1;
					}
					// 如果pfpTokenCount小于mintCount，可以mint
					if (pfpTokenCount < mintCount) {
						mintStatus = 2;
					}
				} else {
					// 普通用户  若没有mint出阈值  mintStatus为1
					if (pfpTokenCount < mintCount) {
						mintStatus = 1;
					}
				}
			}
		} else if (userId != null) {
			// 根据userId查询用户
			isOther = true;
			memberPO = memberMapper.selectById(userId);
		} else {
			isOther = true;
			// 根据inviteCode查询用户
			memberPO = memberMapper.selectOne(new LambdaQueryWrapper<MemberPO>()
				.eq(BasePO::getIsDeleted, 0)
				.eq(MemberPO::getSuperInviteCode, "soul-" + code));

		}


		BeanUtil.copyProperties(memberPO, userInfoVo);

		userId = memberPO.getId();

		userInfoVo.setIsLoginUser(getIsLoginUser(userId));
		// 设置code
		userInfoVo.setSuperInviteCode(memberPO.getSuperInviteCode().replace("Soul-", ""));

		// 设置是否使用邀请码，根据memberPO的是invite_user_id是否为0 或者 whitelist 存在
		userInfoVo.setIsUseInviteCode(memberPO.getInviteUserId() != 0 || whiteListPO != null);
		// 获取pfpToken数据总数量
//		Long pfpTokenCount = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
//			.eq(BasePO::getIsDeleted, 0));
		// 读取配置
		Integer mintMaxCount = Web3jConfig.MINT_MAX_COUNT;

		userInfoVo.setMintStatus(mintStatus);

		// 设置推特相关信息
		userInfoVo.setTwitterStatus(memberPO.getBindTwitter());
		userInfoVo.setTwitterName(memberPO.getTwitterName());
		userInfoVo.setTwitterUserName(memberPO.getTwitterUsername());
		userInfoVo.setTwitterAvatar(memberPO.getTwitterImageUrl());
		//获取被关注人数
		Long followers = memberFollowMapper.selectCount(new LambdaQueryWrapper<MemberFollowPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(MemberFollowPO::getSubscribeUserId, userId));
		userInfoVo.setFollowers(followers);

		//获取关注人数
		Long following = memberFollowMapper.selectCount(new LambdaQueryWrapper<MemberFollowPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(MemberFollowPO::getUserId, userId));
		userInfoVo.setFollowing(following);

		//是其他人，并且已登录时，查询是否关注
		if (isOther && StpUtil.isLogin()) {
			//查询是否互关
			MemberFollowPO memberFollowPO = memberFollowMapper.selectOne(new LambdaQueryWrapper<MemberFollowPO>()
				.eq(BasePO::getIsDeleted, 0)
				.eq(MemberFollowPO::getUserId, StpUtil.getLoginIdAsLong())
				.eq(MemberFollowPO::getSubscribeUserId, userId));
			if (memberFollowPO != null) {
				//已经关注
				userInfoVo.setIsFollow(1);
			} else {
				//未关注
				userInfoVo.setIsFollow(0);
			}
			//获取两个用户之间的connect状态
			Integer connectStatus = memberConnectService.getConnectStatus(StpUtil.getLoginIdAsLong(), userId);
			userInfoVo.setConnectStatus(connectStatus);
		}
		//是本人,并且已登录,查询是否有待申请的请求
		if (!isOther && StpUtil.isLogin()) {
			Boolean redPoint = memberConnectService.getHasConfirm(userId);
			userInfoVo.setRedPoint(redPoint);
		}
		//获取该用户的已连接数量
		Integer connectedNum = memberConnectService.getConnectNum(userId);
		userInfoVo.setConnectedNum(connectedNum);


		//20240301 判断是否具有私聊 getConnectStatus 1 or 2 并返回与该用户私聊id
		if (userInfoVo.getConnectStatus() != null) {
			if (1== userInfoVo.getIsFollow() || 1 == userInfoVo.getConnectStatus() || 2 == userInfoVo.getConnectStatus()) {
				List<Long> chats1 = chatMemberMapper.getUserChatType1(userId);
				List<Long> chats2 = chatMemberMapper.getUserChatType1(StpUtil.getLoginIdAsLong());
				//判断两个list是否存在交集
				Set<Long> set1 = new HashSet<>(chats1);
				Set<Long> set2 = new HashSet<>(chats2);
				boolean b = set1.retainAll(set2);
				if (b) {
					userInfoVo.setSingleChatId((Long) set1.toArray()[0]);
				}
			}
		}
		//20240528 设置pick次数3次
		userInfoVo.setPickCount(3);

		return R.data(userInfoVo);
	}

	private Integer getIsLoginUser(Long userId) {
		Integer isLoginUser = 0;
		boolean login = StpUtil.isLogin();
		if (login) {
			Long loginUserId = StpUtil.getLoginIdAsLong();
			if (userId.equals(loginUserId)) {
				isLoginUser = 1;
			}
		}
		return isLoginUser;
	}

	@GetMapping("/updateUserScore")
	@ApiOperation(value = "更新用户分数：较为耗时推荐异步 P0")
	public R<UserInfoVo> updateUserScore() {
		Long userId = StpUtil.getLoginIdAsLong();

		userScoreService.updateUserScore(userId);

		return getUserInfo(null, null);
	}

	@PostMapping("/setUserTags")
	@ApiOperation(value = "设置用户标签(1到12,多个用逗号隔开)")
	public R<UserInfoVo> setUserTags(@Valid @RequestBody UserTagsVo userTagsVo) {
		Long userId = StpUtil.getLoginIdAsLong();

		MemberPO memberPO = memberMapper.selectById(userId);
		memberPO.setUserTags(userTagsVo.getUserTags());

		memberPO.initForUpdate();

		memberMapper.updateById(memberPO);

		return R.success("修改成功");
	}

	@PostMapping("/setUserInfo")
	@ApiOperation(value = "设置用户昵称、头像、bio")
	public R<UserInfoVo> setUserInfo(@Valid @RequestBody UserNameAvatarVo userNameAvatarVo) {

		String avatar = userNameAvatarVo.getAvatar();
		String userName = userNameAvatarVo.getUserName();
		String bio = userNameAvatarVo.getBio();

		if (StringUtil.isNotBlank(avatar) || StringUtil.isNotBlank(userName) || StringUtil.isNotBlank(bio)) {
			Long userId = StpUtil.getLoginIdAsLong();

			MemberPO memberPO = memberMapper.selectById(userId);
			if (StringUtil.isNotBlank(avatar)) {
				memberPO.setAvatar(avatar);
			}
			if (StringUtil.isNotBlank(userName)) {
				memberPO.setUserName(userName);
			}
			if (StringUtil.isNotBlank(bio)) {
				memberPO.setBio(bio);
			}

			memberPO.initForUpdate();

			memberMapper.updateById(memberPO);
		}
		return R.success("修改成功");
	}


	@GetMapping("/getMintedNFTPage")
	@ApiOperation(value = "获取我铸造的NFT分页 P1")
	public R<Page<PFPTokenMinePageVo>> getNFTPage(@ApiParam("用户id：不传的话默认查自己，不登录时userId必传") @RequestParam(value = "userId", required = false) Long userId,
												  @ApiParam(value = "当前页", required = true) @RequestParam("current") Integer current,
												  @ApiParam(value = "每页的数量", required = true) @RequestParam("size") Integer size) {
		if (userId == null) {
			userId = StpUtil.getLoginIdAsLong();
		}

		Page<PFPTokenPO> page = new Page<>(current, size);
		LambdaQueryWrapper<PFPTokenPO> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(BasePO::getIsDeleted, 0)
			.eq(PFPTokenPO::getMintStatus, 1)
			.eq(PFPTokenPO::getMintUserId, userId)
			.orderByDesc(BasePO::getCreateTime);

		page = pfpTokenMapper.selectPage(page, wrapper);

		Page<PFPTokenMinePageVo> result = new Page<>(current, size);
		BeanUtil.copyProperties(page, result);

		List<PFPTokenMinePageVo> pfpTokenPageVos = BeanUtil.copyProperties(result.getRecords(), PFPTokenMinePageVo.class);
		result.setRecords(pfpTokenPageVos);

		//TODO 设置最高出价
		result.getRecords().forEach(x -> {
			x.setTopPick(null);
		});

		return R.data(result);
	}

	@GetMapping("/getCollectNFTPage")
	@ApiOperation(value = "获取我购买的NFT分页")
	public R<Page<PFPTokenMinePageVo>> getCollectNFTPage(@ApiParam("用户id：不传的话默认查自己，不登录时userId必传") @RequestParam(value = "userId", required = false) Long userId,
														 @ApiParam(value = "当前页", required = true) @RequestParam("current") Integer current,
														 @ApiParam(value = "每页的数量", required = true) @RequestParam("size") Integer size) {
		if (userId == null) {
			userId = StpUtil.getLoginIdAsLong();
		}

		MemberPO memberPO = memberMapper.selectById(userId);

		Page<PFPTokenPO> page = new Page<>(current, size);
		LambdaQueryWrapper<PFPTokenPO> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(BasePO::getIsDeleted, 0)
			.eq(PFPTokenPO::getMintStatus, 1)
			//持有者是本人
//			.eq(PFPTokenPO::getOwnerUserId,userId)
			.eq(PFPTokenPO::getOwnerAddress, memberPO.getAddress().toLowerCase())
			//不是本人铸造
			.ne(PFPTokenPO::getMintUserAddress, memberPO.getAddress().toLowerCase())
//			.ne(PFPTokenPO::getMintUserId,userId)
			.orderByDesc(BasePO::getUpdateTime);

		page = pfpTokenMapper.selectPage(page, wrapper);

		Page<PFPTokenMinePageVo> result = new Page<>(current, size);
		BeanUtil.copyProperties(page, result);

		List<PFPTokenMinePageVo> pfpTokenPageVos = BeanUtil.copyProperties(result.getRecords(), PFPTokenMinePageVo.class);
		result.setRecords(pfpTokenPageVos);

		//TODO 设置最高出价
		result.getRecords().forEach(x -> {
			x.setTopPick(null);

			//设置购买费用
			PFPTransactionPO lastTransaction = nftService.getLastTransaction(x.getId());
			x.setCostPrice(lastTransaction.getListPrice());

			//设置当前pick人数
			Long pickId = x.getPickId();
			if (pickId != null) {
				PFPPickPO pfpPickPO = pfpPickMapper.selectById(pickId);
				x.setNowPickCount(pfpPickPO.getNowPickCount());
			}

		});

		return R.data(result);
	}

	@GetMapping("/getPicksNFTPage")
	@ApiOperation(value = "获取我PICK的NFT分页")
	public R<Page<PFPTokenMinePageVo>> getPicksNFTPage(
		@ApiParam(value = "当前页", required = true) @RequestParam("current") Integer current,
		@ApiParam(value = "每页的数量", required = true) @RequestParam("size") Integer size) {
		Long userId = StpUtil.getLoginIdAsLong();

		Page<PFPPickPO> page = new Page<>(current, size);
		LambdaQueryWrapper<PFPPickPO> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(BasePO::getIsDeleted, 0)
			//拼团中、待开奖
			.in(PFPPickPO::getStatus, 0, 1)
			.orderByDesc(BasePO::getUpdateTime);
		wrapper.and(tmp -> {
			tmp.eq(PFPPickPO::getIndexUserId0, userId)
				.or().eq(PFPPickPO::getIndexUserId1, userId)
				.or().eq(PFPPickPO::getIndexUserId2, userId)
				.or().eq(PFPPickPO::getIndexUserId3, userId);
		});

		page = pfpPickMapper.selectPage(page, wrapper);

		List<PFPTokenMinePageVo> pfpTokenPageVos = new ArrayList<>();

		page.getRecords().forEach(x -> {
			Long tokenId = x.getTokenId();
//			PFPTokenPO pfpTokenPO = pfpTokenMapper.selectById(tokenId);
			PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(tokenId);

			PFPTokenMinePageVo pfpTokenMinePageVo = BeanUtil.copyProperties(pfpTokenPO, PFPTokenMinePageVo.class);
			pfpTokenMinePageVo.setNowPickCount(x.getNowPickCount());

			//设置最高出价
			pfpTokenMinePageVo.setTopPick(null);

			//设置购买费用
			PFPTransactionPO lastTransaction = nftService.getLastTransaction(x.getId());
			pfpTokenMinePageVo.setCostPrice(lastTransaction.getListPrice());

			pfpTokenPageVos.add(pfpTokenMinePageVo);
		});

		Page<PFPTokenMinePageVo> result = new Page<>(current, size);
		BeanUtil.copyProperties(page, result);

		result.setRecords(pfpTokenPageVos);
		return R.data(result);
	}

	// 使用邀请码pick
	@PostMapping("/pickByInviteCode")
	@ApiOperation(value = "使用邀请码pick")
	@Transactional(rollbackFor = Exception.class)
	public R pickByInviteCode(
		// body接收一个code参数
		@RequestBody PickByInviteCodeQuery PickByInviteCodeQuery) {
		try {
			Long userId = StpUtil.getLoginIdAsLong();
		} catch (Exception e) {
			log.error("please login " + e.getMessage());
			return R.fail("please login");
		}
		String code = PickByInviteCodeQuery.getInviteCode();
		log.info("code = {}", code);
		// 根据inviteCode查询用户
		MemberPO memberPO = memberMapper.selectOne(new LambdaQueryWrapper<MemberPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(MemberPO::getSuperInviteCode, "soul-" + code));
		if (memberPO == null) {
			return R.fail("invite code is not exist");
		} else {
			// 更新分数历史及用户分数
			// 邀请码用户拥有nft才能得分
			Long pfpTokenCount = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
				.eq(BasePO::getIsDeleted, 0)
				.eq(PFPTokenPO::getOwnerUserId, memberPO.getId()));
			if (pfpTokenCount > 0) {
				BigDecimal vSoulPrice0 = new BigDecimal(100);
				VSoulHistoryPO vSoulHistoryPO = new VSoulHistoryPO(memberPO.getId(), 5, null, vSoulPrice0);
				vSoulHistoryPO.initForInsertNoAuth();
				vSoulHistoryMapper.insert(vSoulHistoryPO);
				//更新积分余额
				updateUserVSoul(memberPO.getId(), vSoulPrice0);
			}
			return R.success("success, pfp count：" + pfpTokenCount);
		}
	}

	private void updateUserVSoul(Long userId, BigDecimal vSoulPrice) {


		UserVSoulPO userVSoulPO = userVSoulMapper.selectOne(new LambdaQueryWrapper<UserVSoulPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(UserVSoulPO::getUserId, userId));

		if (userVSoulPO == null) {
			userVSoulPO = new UserVSoulPO();
			userVSoulPO.setUserId(userId);
			userVSoulPO.setVSoulPrice(BigDecimal.ZERO);
			userVSoulPO.initForInsertNoAuth();

			userVSoulMapper.insert(userVSoulPO);
		}

		//更新余额
		userVSoulPO.setVSoulPrice(userVSoulPO.getVSoulPrice().add(vSoulPrice));

		userVSoulMapper.updateById(userVSoulPO);
	}


	@PostMapping("/cancelListNFT")
	@ApiOperation(value = "取消出售NFT")
	public R cancelListNFT(@Valid @RequestBody RequestIdVo requestIdVo) {
		Long tokenId = requestIdVo.getId();
		Long userId = StpUtil.getLoginIdAsLong();

//		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectById(tokenId);
		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(tokenId);
		if (pfpTokenPO != null && pfpTokenPO.getMintStatus() == 1 && pfpTokenPO.getOwnerUserId().equals(userId) && pfpTokenPO.getIsDeleted() == 0) {

			//判断是否正在交易
			//交易中
//			if(pfpTokenPO.getStatus() == 1){
//				return R.fail("This PFP is currently being traded");
//			}

			//不是可以pick的状态（上架中）
			if (pfpTokenPO.getPickStatus() != 1) {
				return R.fail("cancel list is forbiden");
			}

			//查看pick人数
			if (pfpTokenPO.getPickId() != null) {
				PFPPickPO pfpPickPO = pfpPickMapper.selectById(pfpTokenPO.getPickId());
				if (pfpPickPO.getNowPickCount() != 0) {
					return R.fail("There is one or more users pick the NFT");
				}

				//合约1
				if (marketAddress.equals(pfpTokenPO.getContractMarketAddress())) {
					// 调取合约下架接口
					R result = marketService.cancelList(pfpTokenPO.getRealTokenId());
					if (result.getCode() != 200) {
						return result;
					}
					//合约2
				} else if (marketAddress2.equals(pfpTokenPO.getContractMarketAddress())) {
					// 调取合约下架接口
					R result = newMarketService.cancelList(pfpTokenPO.getRealTokenId());
					if (result.getCode() != 200) {
						return result;
					}
					//合约3
				} else {
					// 调取合约下架接口
					R result = newMarketService2.cancelList(pfpTokenPO.getRealTokenId());
					if (result.getCode() != 200) {
						return result;
					}
				}

				//下架:删除pick记录
				pfpPickPO.setIsDeleted(1);
				pfpPickMapper.updateById(pfpPickPO);

				// list信息存储active
				ActivePO activePO = new ActivePO();
				activePO.initForInsert();
				activePO.setTokenId(tokenId);
				activePO.setType(2);
				activePO.setTokenImg(pfpTokenPO.getSquarePictureUrl());
//				activePO.setUsername(memberPO.getUserName());
				activePO.setUserAddress(pfpTokenPO.getOwnerAddress());
//				activePO.setUserImg(memberPO.getAvatar());
				activePO.setPrice(pfpTokenPO.getPrice());
				log.info("cancel list 取消挂单数据添加: " + activePO);
				activeMapper.insert(activePO);
			}

			//下架
//			pfpTokenPO.setPrice(null);
			pfpTokenPO.setPriceTime(null);
			pfpTokenPO.setPickId(null);
			//pick状态：0-不可pick 下架中
			pfpTokenPO.setPickStatus(0);
			pfpTokenPO.initForUpdate();

			pfpTokenMapper.updateById(pfpTokenPO);

			//TODO 取消所有出价

			return R.success("cancel success");
		} else {
			return R.fail("please refresh and try again");
		}
	}

	// listNFT 调用合约listItem接口


	@PostMapping("/listNFTApprove")
	@ApiOperation(value = "上架：设置NFT出售价格：授权(approve)")
	@Transactional(rollbackFor = Exception.class)
	public R listNFTApprove(@Valid @RequestBody ListNFTVo listNFTVo) {
		Long tokenId = listNFTVo.getId();
		Long userId = StpUtil.getLoginIdAsLong();

//		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectById(tokenId);
		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(tokenId);
		if (pfpTokenPO != null && pfpTokenPO.getMintStatus() == 1 &&
			pfpTokenPO.getOwnerUserId().equals(userId) &&
			pfpTokenPO.getIsDeleted() == 0) {
			//是当前用户的资产

			//pick状态： 0-未出价(不可pick)
			if (pfpTokenPO.getPickStatus() != 0) {
				return R.fail("This PFP has list");
			}

			//创建pick记录
			PFPPickPO pfpPickPO = new PFPPickPO();
			pfpPickPO.setTokenId(pfpTokenPO.getId());
			pfpPickPO.setMintUserAddress(pfpTokenPO.getMintUserAddress());
			pfpPickPO.setMintUserId(pfpTokenPO.getMintUserId());
			pfpPickPO.setLevel(pfpTokenPO.getLevel());
			pfpPickPO.setBasePrice(pfpTokenPO.getBasePrice());
			pfpPickPO.setTransactionsCount(pfpTokenPO.getTransactionsCount());
			pfpPickPO.setPrice(pfpTokenPO.getPrice());
			//交易状态：0-拼团中
			pfpPickPO.setStatus(0);
			pfpPickPO.setNowPickCount(0);

			pfpPickPO.initForInsert();
			pfpPickMapper.insert(pfpPickPO);


			pfpTokenPO.setPriceTime(new Date());
			//pick状态:1- launching (可以pick)
			pfpTokenPO.setPickStatus(1);
			//关联的pick信息
			pfpTokenPO.setPickId(pfpPickPO.getId());
			pfpTokenPO.initForUpdate();

			pfpTokenMapper.updateById(pfpTokenPO);

			return R.success("list success");
		} else {
			return R.fail("please refresh and try again");
		}
	}

	@GetMapping("/getFansRank")
	@ApiOperation(value = "获取粉丝排行榜")
	public R<List<FansVo>> getFansRank(
		@ApiParam(value = "类型：0-pick 1-collect", required = true) @RequestParam("type") Integer type) {
		Long userId = StpUtil.getLoginIdAsLong();

		List<FansVo> result = new ArrayList<>();

		List<FansPO> fansPOS = fansMapper.selectList(new QueryWrapper<FansPO>()
			.eq("is_deleted", 0)
			.eq("type", type)
			.eq("minter_user_id", userId)
			.groupBy("pick_user_id")
			.orderByDesc("count(1)")
			.select("count(1) as count", "pick_user_id as pickUserId")
			.last("limit 20"));

		fansPOS.forEach(x -> {
			Long pickUserId = x.getPickUserId();
			MemberPO memberPO = memberMapper.selectById(pickUserId);

			FansVo fansVo = new FansVo();
			fansVo.setUserId(pickUserId);
			fansVo.setAvatar(memberPO.getAvatar());
			fansVo.setAddress(memberPO.getAddress());
			fansVo.setUserName(memberPO.getUserName());
			fansVo.setCount(x.getCount());

			result.add(fansVo);
		});

		return R.data(result);
	}


	@GetMapping("/getInviteCodes")
	@ApiOperation(value = "获取用户的邀请码列表")
	public R<List<MemberInvitePO>> getInviteCodes() {
		List<MemberInvitePO> inviteCodes = memberInviteService.getInviteCodes(StpUtil.getLoginIdAsLong());
		return R.data(inviteCodes);
	}


}
