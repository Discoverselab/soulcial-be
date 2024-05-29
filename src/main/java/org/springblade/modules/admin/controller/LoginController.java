package org.springblade.modules.admin.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth10aService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.redis.cache.BladeRedis;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.api.ResultCode;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.modules.admin.cache.IUserCache;
import org.springblade.modules.admin.dao.ActiveUsersMapper;
import org.springblade.modules.admin.dao.MemberMapper;
import org.springblade.modules.admin.dao.PFPTokenMapper;
import org.springblade.modules.admin.dao.WhiteListMapper;
import org.springblade.modules.admin.pojo.dto.TwitterUserInfoDto;
import org.springblade.modules.admin.pojo.po.*;
import org.springblade.modules.admin.pojo.vo.MemberVo;
import org.springblade.modules.admin.pojo.vo.SoulVo;
import org.springblade.modules.admin.pojo.vo.UserScoreInfoVo;
import org.springblade.modules.admin.service.*;
import org.springblade.modules.admin.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.utils.Numeric;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/admin/login")
@Api(value = "登录", tags = "登录")
public class LoginController {

	@Autowired
	MemberMapper memberMapper;

	@Autowired
	UserScoreService userScoreService;

	@Autowired
	WhiteListMapper whiteListMapper;

	@Autowired
	MemberInviteService memberInviteService;

	@Autowired
	PFPTokenMapper pfpTokenMapper;

	// 初始化web3j
	@Autowired
	@Qualifier("ethWeb3j")
	private Web3j web3j;

	// 读取配置文件
	@Value("${twitter.callbackUrl}")
	private String callbackUrl;

	@GetMapping("/checkSteamId")
	@ApiOperation(value = "调用于login接口之前：查询是否已生成steam_id")
	public R<UserScoreInfoVo> checkSteamId(@RequestParam("address") String address,
										   @ApiParam(value = "是否刷新分数：0-否 1-是（不传默认为1）") @RequestParam(value = "refreshScore", required = false) Integer refreshScore,
										   @ApiParam(value = "登录类型：0-钱包 1-particle", required = true) @RequestParam("loginType") Integer loginType,
										   @ApiParam(value = "particleType类型：传数字每个数字分别代表一种类型", required = false) @RequestParam(value = "particleType", required = false) Integer particleType) {
		//白名单校验
//		WhiteListPO whiteListPO = whiteListMapper.selectOne(new LambdaQueryWrapper<WhiteListPO>()
//			.eq(BasePO::getIsDeleted, 0)
//			.eq(WhiteListPO::getAddress, address));
//		if(whiteListPO == null){
//			return R.fail("address is not in white list");
//		}

		if (loginType == 1 && particleType == null) {
			return R.fail(ResultCode.INTERNAL_SERVER_ERROR, "particleType must not be null!");
		}

		address = address.toLowerCase();
		MemberPO memberPO = memberMapper.selectOne(new LambdaQueryWrapper<MemberPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(MemberPO::getAddress, address.toLowerCase()));

		UserScoreInfoVo userScoreInfoVo = new UserScoreInfoVo();
		userScoreInfoVo.setIsRegister(0);
		if (memberPO == null) {
			//创建用户
			memberPO = createUser(address, loginType, particleType);
			// 生成邀请码 - 永久有效
			String superCode = InviteCodeGenUtil.genInviteCode(address);
			memberPO.setSuperInviteCode(superCode);
			//是注册
			userScoreInfoVo.setIsRegister(1);

			// 查询pfpToken中是否存在address为该用户的数据，若存在，则更新pfpToken表中的userId
			List<PFPTokenPO> pfpTokenPOList = pfpTokenMapper.selectList(new LambdaQueryWrapper<PFPTokenPO>()
				.eq(BasePO::getIsDeleted, 0)
				.eq(PFPTokenPO::getOwnerAddress, address));
			for (PFPTokenPO pfpTokenPO : pfpTokenPOList) {
				pfpTokenPO.setOwnerUserId(memberPO.getId());
				pfpTokenMapper.updateById(pfpTokenPO);
			}
		}

		Long userId = memberPO.getId();

		//如果refreshScore为空，默认刷分
		if (refreshScore == null) {
			refreshScore = 1;
		}

		//是否刷新分数
		if (refreshScore == 1) {
//			if(memberPO.getCharisma() == null){
			//刷新分数
			userScoreService.updateUserScore(userId);
//			}
		}

		memberPO = memberMapper.selectById(userId);


		BeanUtil.copyProperties(memberPO, userScoreInfoVo);

		return R.data(userScoreInfoVo);
	}

	/**
	 * 对签名消息，原始消息，账号地址三项信息进行认证，判断签名是否有效
	 *
	 * @param signature
	 * @param message
	 * @param address
	 * @return
	 */
	public static final String PERSONAL_MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";

	public static boolean validate(String signature, String message, String address) {
		//参考 eth_sign in https://github.com/ethereum/wiki/wiki/JSON-RPC
		// eth_sign
		// The sign method calculates an Ethereum specific signature with:
		//    sign(keccak256("\x19Ethereum Signed Message:\n" + len(message) + message))).
		//
		// By adding a prefix to the message makes the calculated signature recognisable as an Ethereum specific signature.
		// This prevents misuse where a malicious DApp can sign arbitrary data (e.g. transaction) and use the signature to
		// impersonate the victim.
		try {
			String prefix = PERSONAL_MESSAGE_PREFIX + message.length();
			byte[] msgHash = Hash.sha3((prefix + message).getBytes());

			byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
			byte v = signatureBytes[64];
			if (v < 27) {
				v += 27;
			}
			Sign.SignatureData sd = new Sign.SignatureData(
				v,
				Arrays.copyOfRange(signatureBytes, 0, 32),
				Arrays.copyOfRange(signatureBytes, 32, 64));

			String addressRecovered = null;
			boolean match = false;

			// Iterate for each possible key to recover
			for (int i = 0; i < 4; i++) {
				BigInteger publicKey = Sign.recoverFromSignature(
					(byte) i,
					new ECDSASignature(new BigInteger(1, sd.getR()), new BigInteger(1, sd.getS())),
					msgHash);

				if (publicKey != null) {
					addressRecovered = "0x" + Keys.getAddress(publicKey);
					System.out.println("addressRecovered:" + addressRecovered);
					if (addressRecovered.equals(address)) {
						match = true;
						break;
					}
				}
			}
			return match;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}

	}

	// 获取nonce
	@GetMapping("/getNonce")
	@ApiOperation(value = "获取nonce")
	public R<String> getNonce(@RequestParam("address") String address) throws ExecutionException, InterruptedException {
		EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
			address, DefaultBlockParameterName.LATEST).sendAsync().get();

		BigInteger nonce = ethGetTransactionCount.getTransactionCount();
		return R.data(nonce.toString());
	}

	@PostMapping("/login")
	@ApiOperation(value = "登录")
	public R<MemberVo> login(@RequestParam("address") String address,
							 @ApiParam(value = "lens账号lensProfile，多个用逗号隔开") @RequestParam(value = "lensProfile", required = false) String lensProfile,
							 @ApiParam(value = "用户名") @RequestParam(value = "userName", required = false) String userName,
							 @ApiParam(value = "dataverse-streamId") @RequestParam(value = "dataverse-streamId", required = false) String streamId,
							 @ApiParam(value = "登录类型：0-钱包 1-particle", required = true) @RequestParam("loginType") Integer loginType,
							 @ApiParam(value = "签名内容", required = true) @RequestParam(value = "message", required = true) String message,
							 @ApiParam(value = "签名后的内容", required = true) @RequestParam(value = "signature", required = true) String signature,
							 @ApiParam(value = "particleType类型：传数字每个数字分别代表一种类型", required = false) @RequestParam(value = "particleType", required = false) Integer particleType) {
		// 验签
		System.out.println("address1:" + address);
		System.out.println("message1:" + message);
		System.out.println("signature1:" + signature);
		// 如果登录类型为0，需要验证签名
		if (loginType == 0) {
			boolean validate = validate(signature, message, address);
			if (!validate) {
				return R.fail("Signature verification failed");
			}
		}
		MemberVo memberVo = new MemberVo();
		//白名单校验
		WhiteListPO whiteListPO = whiteListMapper.selectOne(new LambdaQueryWrapper<WhiteListPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(WhiteListPO::getAddress, address));
		memberVo.setWhiteUser(ObjectUtil.isNotEmpty(whiteListPO));

		if (loginType == 1 && particleType == null) {
			return R.fail(ResultCode.INTERNAL_SERVER_ERROR, "particleType must not be null!");
		}

		address = address.toLowerCase();
		MemberPO memberPO = memberMapper.selectOne(new LambdaQueryWrapper<MemberPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(MemberPO::getAddress, address.toLowerCase()));

		if (memberPO == null) {
			memberPO = createUser(address, loginType, particleType);
		}
		// 判断注册后不存在永久邀请码的情况
		if (StringUtil.isBlank(memberPO.getSuperInviteCode())) {
			// 生成邀请码 - 永久有效
			String superCode = InviteCodeGenUtil.genInviteCode(address);
			memberPO.setSuperInviteCode(superCode);
			memberMapper.updateById(memberPO);
		}

		//判断是否使用过邀请码
		memberVo.setUsedInviteCode(memberPO.getInviteUserId() != 0);

		//TODO 目前暂定由前端传入lensProfile
		if (StringUtil.isNotBlank(lensProfile)) {
			memberPO.setLensProfile(lensProfile);
			memberMapper.updateById(memberPO);
		}

		//TODO 目前暂定由前端传入lensHandle
		if (StringUtil.isNotBlank(userName)) {
			memberPO.setUserName(userName);
			memberMapper.updateById(memberPO);
		}

		Long userId = memberPO.getId();

//		if(memberPO.getCharisma() == null){
//			//刷新分数
//			userScoreService.updateUserScore(userId);
//		}

		//dataverse steam_Id
//		if(StringUtil.isBlank(memberPO.getStreamId()) && StringUtil.isNotBlank(streamId)){
//			memberPO.setStreamId(streamId);
//			memberMapper.updateById(memberPO);
//		}

		//20240312 添加记录用户登录时间逻辑
		memberPO.initForUpdateNoAuth();
		memberMapper.updateById(memberPO);

		StpUtil.login(userId);
		SaTokenInfo tokenInfo = StpUtil.getTokenInfo();


		memberVo.setTokenName(tokenInfo.getTokenName());
		memberVo.setTokenValue(tokenInfo.getTokenValue());

		memberVo.setAddress(memberPO.getAddress());
		memberVo.setFreeMint(memberPO.getFreeMint());

		memberVo.setLoginType(memberPO.getLoginType());
		memberVo.setParticleType(memberPO.getParticleType());

		//添加userid返回给前端
		memberVo.setUserId(userId);

		return R.data(memberVo);
	}

	private OAuth10aService service;
	private OAuth1RequestToken requestToken;
	private static final String PROTECTED_RESOURCE_URL = "https://api.twitter.com/2/users/me?user.fields=public_metrics,id,name,username,profile_image_url";

	//	private static final String API_KEY = "QZ7qhmXXmy5p8Px9hDcw9Q26W";
	private static final String API_KEY = "o1JHqZAxqIq7UWy7fDoWrpPhY";

	//	private static final String API_SECRET = "G5yod67UJdjkX12a4j8djPXdNRmDtadQ4ECZLBigKdlaK9DGRK";
	private static final String API_SECRET = "57FtMTV7LOiYxq6unRdOC2YDVWwfBSDaQ9uGZw3DY42y3DdL0y";
	@Autowired
	private BladeRedis bladeRedis;

	public String getRedirectUrl(String callbackUrl) throws IOException, InterruptedException, ExecutionException {
		service = new ServiceBuilder(API_KEY)
			.apiSecret(API_SECRET)
			.callback(callbackUrl)
			.build(TwitterApi.instance());
		requestToken = service.getRequestToken();
		// 放入redis中，设置过期时间为5分钟，绑定id和requestToken
		long userId = StpUtil.getLoginIdAsLong();
		bladeRedis.setEx("twitterRequestToken_" + userId, requestToken, 300L);
//		bladeRedis.setEx("twitterService_" + userId, service, 300L);
		return service.getAuthorizationUrl(requestToken);
	}

	public String setTwitterUserInfoUtil(String oauthVerifier) throws IOException, ExecutionException, InterruptedException {
		// 判断service是否初始化，若未初始化，则初始化
//		if (service == null) {
//			service = new ServiceBuilder(API_KEY)
//				.apiSecret(API_SECRET)
//				.build(TwitterApi.instance());
//			requestToken = service.getRequestToken();
//		}
		// 从redis中取出requestToken
		long userId = StpUtil.getLoginIdAsLong();
		requestToken = bladeRedis.get("twitterRequestToken_" + userId);
//		OAuth10aService service = bladeRedis.get("twitterService_" + userId);
		if (requestToken == null) {
			return "fail, requestToken is null!";
		}
		final OAuth1AccessToken accessToken = service.getAccessToken(requestToken, oauthVerifier);
		System.out.println("Got the Access Token!");
		System.out.println("(The raw response looks like this: " + accessToken.getRawResponse() + "')");
		System.out.println();

		// Now let's go and ask for a protected resource!
		System.out.println("Now we're going to access a protected resource...");
		final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
		service.signRequest(accessToken, request);
		try (Response response = service.execute(request)) {
			System.out.println("Got it! Lets see what we found...");
			System.out.println();
			System.out.println(response.getBody());
			return response.getBody();
		} catch (Exception e) {
			e.printStackTrace();
			return "fail";
		}
	}

	@PostMapping("/twitterRedirect")
	@ApiOperation(value = "推特重定向登录")
	public R<?> twitterRedirect(
		@ApiParam(value = "0 = 首次；1 = 我的", required = true) @RequestParam(value = "source", required = true) Integer source
	) throws IOException, ExecutionException, InterruptedException {

		String redirectUrl = getRedirectUrl(callbackUrl);
//		if (source == 0) {
//			redirectUrl = getRedirectUrl("https://testbsc.soulcial.network/#/twitterAuth");
//		} else if (source == 1) {
//			redirectUrl = getRedirectUrl("https://test2bsc.soulcial.network/#/twitterAuth");
//		}
//		if (source == 0) {
//			redirectUrl = getRedirectUrl("https://app.soulcial.network/#/twitterAuth");
//		} else if (source == 1) {
//			redirectUrl = getRedirectUrl("https://app.soulcial.network/#/twitterAuth");
//		}
		// 检查redirectUrl是否有效，如果有效，重定向到这个Url
		if (redirectUrl != null && !redirectUrl.isEmpty()) {
			return R.data(redirectUrl);
		} else {
			return R.fail("Redirect fail"); // 如果无效或为空，导向错误页面。
		}
	}

	// 手动设置member得twitter信息
	@PostMapping("/setTwitterUserInfo2")
	@ApiOperation(value = "手动设置twitter信息")
	public R<?> setTwitterUserInfo2(
		// address参数
		@ApiParam(value = "address", required = false) @RequestParam(value = "address", required = false) String address,
		// twitterUserName 参数
		@ApiParam(value = "twitterUserName", required = false) @RequestParam(value = "twitterUserName", required = false) String twitterUserName,
		// twitterName 参数
		@ApiParam(value = "twitterName", required = false) @RequestParam(value = "twitterName", required = false) String twitterName,
		// twitterFollowers 参数
		@ApiParam(value = "twitterFollowers", required = false) @RequestParam(value = "twitterFollowers", required = false) Integer twitterFollowers,
		// twitterImageUrl 参数
		@ApiParam(value = "twitterImageUrl", required = false) @RequestParam(value = "twitterImageUrl", required = false) String twitterImageUrl,
		// twitterId 参数
		@ApiParam(value = "twitterId", required = false) @RequestParam(value = "twitterId", required = false) String twitterId
	) throws IOException, ExecutionException, InterruptedException {
		// 若address存在则根据address查询member表，查询出一条，若没有则返回错误
		MemberPO memberPO = new MemberPO();
		if (address != null && !address.isEmpty()) {
			memberPO = memberMapper.selectOne(new LambdaQueryWrapper<MemberPO>()
				.eq(BasePO::getIsDeleted, 0)
				.eq(MemberPO::getAddress, address));
		} else {
			return R.fail("Address or twitterUserName must not be null!");
		}

		// 设置绑定状态
		memberPO.setBindTwitter(1);
		memberPO.setTwitterUsername(twitterUserName);
		memberPO.setTwitterName(twitterName);
		memberPO.setUserName(twitterName);
		memberPO.setTwitterFollowers(twitterFollowers);
		memberPO.setTwitterImageUrl(twitterImageUrl);
		memberPO.setTwitterId(twitterId);
		memberMapper.updateById(memberPO);

		return R.success("Set Twitter user info success!");
	}

	//批量设置twitter信息
	@PostMapping("/setTwitterUserInfo3")
	@ApiOperation(value = "批量设置twitter信息")
	public R<?> setTwitterUserInfo3(@RequestParam("file") MultipartFile file) {
		// 检查文件是否为空
		if (file.isEmpty()) {
			return R.fail("上传失败，文件不能为空");
		}

		// 检查文件类型是否为Excel
		String fileName = file.getOriginalFilename();
		if (!(fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
			return R.fail("上传失败，只支持Excel文件");
		}

		try {
			// 使用Hutool创建ExcelReader
			ExcelReader reader = ExcelUtil.getReader(file.getInputStream());

			// 读取表头（列名）
			List<Object> objects = reader.readRow(0);
			if (objects != null && objects.size() > 0) {
				List<String> collect = objects.stream().map(s -> {
					return s.toString();
				}).collect(Collectors.toList());
				ArrayList<String> list = new ArrayList<>();
				list.add("address");
				list.add("twitterUserName");
				list.add("twitterName");
				list.add("twitterFollowers");
				list.add("twitterImageUrl");
				list.add("twitterId");
				if (!collect.containsAll(list)) {
					return R.fail("Excel列名不符合要求");
				}
				List<TwitterUserInfoDto> datas = reader.readAll(TwitterUserInfoDto.class);

				Integer successCount = 0;
				ArrayList<String> errorAddress = new ArrayList<>();

				for (int i = 0; i < datas.size(); i++) {
					TwitterUserInfoDto s = datas.get(i);
					// 若address存在则根据address查询member表，查询出一条，若没有则返回错误
					MemberPO memberPO = null;
					if (s.getAddress() != null && !s.getAddress().isEmpty()) {
						memberPO = memberMapper.selectOne(new LambdaQueryWrapper<MemberPO>()
							.eq(BasePO::getIsDeleted, 0)
							.eq(MemberPO::getAddress, s.getAddress()));
					} else {
						continue;
					}

					if (memberPO == null) {
						errorAddress.add(s.getAddress());
						continue;
					}

					// 设置绑定状态
					memberPO.setBindTwitter(1);
					memberPO.setTwitterUsername(s.getTwitterUserName());
					memberPO.setTwitterName(s.getTwitterName());
					memberPO.setUserName(s.getTwitterName());
					if (s.getTwitterFollowers() != null) {
						memberPO.setTwitterFollowers(Integer.parseInt(s.getTwitterFollowers()));
					}
					memberPO.setTwitterImageUrl(s.getTwitterImageUrl());
					memberPO.setTwitterId(s.getTwitterId());
					int i1 = memberMapper.updateById(memberPO);
					if (i1 > 0) {
						successCount++;
					}
				}
				if (successCount == datas.size()) {
					return R.success("Set Twitter user info success!");
				}
				return R.success("解析 " + datas.size() + " 条数据，共绑定 " + successCount + " 条数据。绑定失败地址：" + errorAddress.toString());
			} else {
				return R.fail("Excel列名不符合要求");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return R.fail("文件解析失败：" + e.getMessage());
		}
	}


	// 临时 resetTwitterUserInfo
	@PostMapping("/resetTwitterUserInfo")
	@ApiOperation(value = "重置twitter信息")
	public R<?> resetTwitterUserInfo(
		// address参数
		@ApiParam(value = "address", required = false) @RequestParam(value = "address", required = false) String address,
		// twitterUserName 参数
		@ApiParam(value = "twitterUserName", required = false) @RequestParam(value = "twitterUserName", required = false) String twitterUserName
	) throws IOException, ExecutionException, InterruptedException {
		// 若address存在则根据address查询member表，若不存在，则根据twitterUsername查询，查出列表循环重置
		List<MemberPO> memberPOList = new ArrayList<>();
		if (address != null && !address.isEmpty()) {
			MemberPO memberPO = memberMapper.selectOne(new LambdaQueryWrapper<MemberPO>()
				.eq(BasePO::getIsDeleted, 0)
				.eq(MemberPO::getAddress, address));
			memberPOList.add(memberPO);
		} else if (twitterUserName != null && !twitterUserName.isEmpty()) {
			memberPOList = memberMapper.selectList(new LambdaQueryWrapper<MemberPO>()
				.eq(BasePO::getIsDeleted, 0)
				.eq(MemberPO::getTwitterUsername, twitterUserName));
		} else {
			return R.fail("Address or twitterUserName must not be null!");
		}

		for (MemberPO memberPO : memberPOList) {
			// 设置绑定状态
			memberPO.setBindTwitter(0);
			memberPO.setTwitterUsername("");
			memberPO.setTwitterName("");
			memberPO.setTwitterFollowers(0);
			memberPO.setTwitterImageUrl("");
			memberPO.setTwitterId("");
			memberMapper.updateById(memberPO);
		}

		return R.success("Reset Twitter user info success!");
	}

	@PostMapping("/setTwitterUserInfo")
	@ApiOperation(value = "获取回调用回传")
	public R<?> setTwitterUserInfo(
		@ApiParam(value = "回传参数，字段：oauthVerifier", required = true) @RequestParam(value = "oauthVerifier", required = true) String oauthVerifier) throws IOException, ExecutionException, InterruptedException {
		TwitterOAuth TwitterOAuth = new TwitterOAuth();
		String jsonBody = setTwitterUserInfoUtil(oauthVerifier);
		try {
			// 解析 JSON 字符串
			JSONObject jsonObject = JSONObject.parseObject(jsonBody);

			// 提取相关参数
			JSONObject data = jsonObject.getJSONObject("data");
			String username = data.getString("username");
			String name = data.getString("name");
			String id = data.getString("id");
			// 获取该id中数据库查询出的数量
			Long count = memberMapper.selectCount(new LambdaQueryWrapper<MemberPO>()
				.eq(BasePO::getIsDeleted, 0)
				.eq(MemberPO::getBindTwitter, 1)
				.eq(MemberPO::getTwitterId, id));
			if (count > 0) {
				return R.fail("Failed. Twitter already used.");
			}
			// 获取头像
			String profileImageUrl = data.getString("profile_image_url");

			int followersCount = data.getJSONObject("public_metrics").getInteger("followers_count");

			// 更新到当前登录用户的member表中
			Long userId = StpUtil.getLoginIdAsLong();
			MemberPO memberPO = memberMapper.selectById(userId);
			// 设置绑定状态
			memberPO.setBindTwitter(1);
			memberPO.setTwitterUsername(username);
			memberPO.setTwitterName(name);
			memberPO.setTwitterFollowers(followersCount);
			memberPO.setTwitterImageUrl(profileImageUrl);
			memberPO.setTwitterId(id);
			// 将来twitter头像以及name更新为平台的头像和name
			memberPO.setUserName(name);
			// 将profilerImageUrl的后缀.前面的normal改为400x400
			profileImageUrl = profileImageUrl.replace("normal", "400x400");
			// 上传头像到oss
			String twitterAvatar = OssUtil.getTwitterAvatar(profileImageUrl);
			memberPO.setAvatar(twitterAvatar);
			memberMapper.updateById(memberPO);


		} catch (Exception e) {
			System.err.println("Unable to parse JSON string.");
			e.printStackTrace();
			return R.fail("Something wrong!");
		}
		return R.success("Get Twitter user info success!");
	}

	@PutMapping("/useInviteCode/{inviteCode}")
	@ApiOperation(value = "使用邀请码")
	public R<?> useInviteCode(@ApiParam(value = "邀请码", required = true) @PathVariable String inviteCode) {
		memberInviteService.useInviteCode(inviteCode);
		return R.success("使用邀请码成功");
	}

	private MemberPO createUser(String address, Integer loginType, Integer particleType) {
		MemberPO memberPO = new MemberPO();

		memberPO.setAddress(address);
		memberPO.setFreeMint(0);
		Date date = new Date();
		memberPO.setCreateTime(date);
		memberPO.setUpdateTime(date);
		memberPO.setIsDeleted(0);
		// 生成永久邀请码
		String superCode = InviteCodeGenUtil.genInviteCode(address);
		memberPO.setSuperInviteCode(superCode);

		String userName = address.substring(0, 6);
		// 用户注册，获取lens账号
//		String lensName = userScoreService.getLensNameByAddress(address);
//		if(StringUtil.isNotBlank(lensName)){
//			userName = lensName;
//		}
		memberPO.setUserName(userName);

		List<String> avatarList = new ArrayList<>();
//		avatarList.add("https://sfhmaster-1313464417.cos.ap-nanjing.myqcloud.com/2023/06/25/1672961118976385024.png");
//		avatarList.add("https://sfhmaster-1313464417.cos.ap-nanjing.myqcloud.com/2023/06/25/1672961478398877696.png");
//		avatarList.add("https://sfhmaster-1313464417.cos.ap-nanjing.myqcloud.com/2023/06/25/1672961557138546688.png");
		avatarList.add("https://arseed.web3infra.dev/6nbyPGvXW-MTcnNXB60MScm4Y9AKTFQcIBcZt0_gzg0");
		avatarList.add("https://arseed.web3infra.dev/DUrJ0SsqWWdk2Ga2cmmzP8QMx14XVVi38XUqZ538r4s");
		avatarList.add("https://arseed.web3infra.dev/T79pPTaT_ZSXyWTqcPbp4GYlgAKDaE8qgicyTdUw3ho");
		memberPO.setAvatar(avatarList.get(RandomUtil.randomInt(0, 3)));

		memberPO.setLoginType(loginType);
		memberPO.setParticleType(particleType);

		// 查询当前pfpToken中，拥有者为该用户并且id为0，若存在，则更新pfpToken表中的userId
		List<PFPTokenPO> pfpTokenPOList = pfpTokenMapper.selectList(new LambdaQueryWrapper<PFPTokenPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(PFPTokenPO::getOwnerAddress, address)
			.eq(PFPTokenPO::getOwnerUserId, 0L));
		for (PFPTokenPO pfpTokenPO : pfpTokenPOList) {
			pfpTokenPO.setOwnerUserId(memberPO.getId());
			pfpTokenMapper.updateById(pfpTokenPO);
		}

		int i = memberMapper.insert(memberPO);
		if (i > 0) {
			//用户注册成功更新缓存信息
			IUserCache.updateUserCache();
			IUserCache.updateUserRoomsCache();
			IUserCache.updateRoomUsersCache();
		}

		return memberPO;
	}

	@ApiOperation("退出登录")
	@PostMapping("/logout")
	public R logout() {
		try {
			StpUtil.logout();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return R.success("退出成功");
	}


	@ApiOperation("文件url上传")
	@GetMapping("/uploadFileByUrl")
	public R uploadFileByUrl(@RequestParam("url") String url) {
		try {
			String twitterAvatar = OssUtil.getTwitterAvatar(url);
			if ("".equals(twitterAvatar)) {
				return R.data("uploadFileByUrl error!");
			}
			return R.data(twitterAvatar);
		} catch (Exception e) {
			e.printStackTrace();
			return R.fail("上传失败");
		}
	}


	@ApiOperation("推特头像批量修改-OSS")
	@GetMapping("/updateTwitterAvatarSendOss")
	public R updateTwitterAvatarSendOss() {
		try {
			LambdaQueryWrapper<MemberPO> wp = new LambdaQueryWrapper<>();
			wp.like(MemberPO::getAvatar, "twimg.com");
			List<MemberPO> memberPOS = memberMapper.selectList(wp);
			Integer index = 0;
			for (MemberPO memberPO : memberPOS) {
				String avatar = memberPO.getAvatar();
				String twitterAvatar = OssUtil.getTwitterAvatar(avatar);
				if ("".equals(twitterAvatar)) {
					continue;
				}
				memberPO.setAvatar(twitterAvatar);
				int i = memberMapper.updateById(memberPO);
				index = index + i;
			}
			return R.data("头像修改数量：" + index);
		} catch (Exception e) {
			e.printStackTrace();
			return R.fail("上传失败");
		}
	}


	@Resource
	private ActiveUsersMapper activeUsersMapper;


	//20240306 记录日活用户信息
	@ApiOperation("记录日活用户")
	@GetMapping("/recordDailyActiveUsers")
	public R recordDailyActiveUsers(HttpServletRequest request) {
		// 获取当前日期的LocalDate实例
		LocalDate today = LocalDate.now();

		// 创建一个DateTimeFormatter格式化器，设置为YYYY-MM-DD的格式
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		// 使用格式化器将LocalDate格式化为字符串
		String formattedDate = today.format(formatter);

		String ipAddr = IPUtils.getIpAddr(request);

		LambdaQueryWrapper<ActiveUserPO> wp = new LambdaQueryWrapper<>();
		wp.eq(ActiveUserPO::getDate, formattedDate);
		wp.eq(ActiveUserPO::getIp, ipAddr);
		List<ActiveUserPO> list = activeUsersMapper.selectList(wp);
		if (list != null && list.size() > 0) {
			return R.data("已记录过该用户的日活信息");
		}
		ActiveUserPO activeUserPO = new ActiveUserPO();
		activeUserPO.setDate(formattedDate);
		activeUserPO.setIp(ipAddr);
		activeUsersMapper.insert(activeUserPO);
		return R.success("记录日活用户成功");

	}

	@Resource
	private MyMemberService myMemberService;

	//20240325 批量注册用户登录
	@ApiOperation("批量注册用户登录")
	@PostMapping("/logins")
	public R<?> logins(@RequestParam("file") MultipartFile file) {
		// 检查文件是否为空
		if (file.isEmpty()) {
			return R.fail("上传失败，文件不能为空");
		}

		// 检查文件类型是否为Excel
		String fileName = file.getOriginalFilename();
		if (!(fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
			return R.fail("上传失败，只支持Excel文件");
		}

		try {
			// 使用Hutool创建ExcelReader
			ExcelReader reader = ExcelUtil.getReader(file.getInputStream());

			// 读取表头（列名）
			List<Object> objects = reader.readRow(0);
			if (objects != null && objects.size() > 0) {
				List<String> collect = objects.stream().map(s -> {
					return s.toString();
				}).collect(Collectors.toList());
				ArrayList<String> list = new ArrayList<>();
				list.add("address");
				list.add("twitterUserName");
				list.add("twitterName");
				list.add("twitterFollowers");
				list.add("twitterImageUrl");
				list.add("twitterId");
				if (!collect.containsAll(list)) {
					return R.fail("Excel列名不符合要求");
				}
				List<TwitterUserInfoDto> datas = reader.readAll(TwitterUserInfoDto.class);

				Integer successCount = 0;
				ArrayList<String> errorAddress = new ArrayList<>();

				ArrayList insertMemberList = new ArrayList<MemberPO>();
				ArrayList inviteList = new ArrayList<MemberPO>();
				for (int i = 0; i < datas.size(); i++) {
					TwitterUserInfoDto s = datas.get(i);
					// 1.注册用户
					MemberPO memberPO = new MemberPO();

					memberPO.setAddress(s.getAddress());
					memberPO.setFreeMint(0);
					Date date = new Date();
					memberPO.setCreateTime(date);
					memberPO.setUpdateTime(date);
					memberPO.setIsDeleted(0);
					// 生成永久邀请码
					String superCode = InviteCodeGenUtil.genInviteCode(s.getAddress());
					memberPO.setSuperInviteCode(superCode);

					String userName = s.getAddress().substring(0, 6);
					memberPO.setUserName(userName);

					List<String> avatarList = new ArrayList<>();
					avatarList.add("https://arseed.web3infra.dev/6nbyPGvXW-MTcnNXB60MScm4Y9AKTFQcIBcZt0_gzg0");
					avatarList.add("https://arseed.web3infra.dev/DUrJ0SsqWWdk2Ga2cmmzP8QMx14XVVi38XUqZ538r4s");
					avatarList.add("https://arseed.web3infra.dev/T79pPTaT_ZSXyWTqcPbp4GYlgAKDaE8qgicyTdUw3ho");
					memberPO.setAvatar(avatarList.get(RandomUtil.randomInt(0, 3)));

					memberPO.setLoginType(0);
					memberPO.setParticleType(1);


					//2.填写邀请码
					memberPO.setInviteUserId(0L);

					if (StringUtil.isNotBlank(s.getTwitterName())) {
						//3.绑定推特
						memberPO.setBindTwitter(1);
						memberPO.setTwitterUsername(s.getTwitterUserName());
						memberPO.setTwitterName(s.getTwitterName());
						memberPO.setUserName(s.getTwitterName());
						if (s.getTwitterFollowers() != null) {
							memberPO.setTwitterFollowers(Integer.parseInt(s.getTwitterFollowers()));
						}
						memberPO.setTwitterImageUrl(s.getTwitterImageUrl());
						memberPO.setTwitterId(s.getTwitterId());
					}


					//4.刷新分数
					memberPO.setCourage(RandomUtil.randomInt(10, 20));
					memberPO.setArt(RandomUtil.randomInt(10, 20));
					memberPO.setWisdom(RandomUtil.randomInt(10, 20));
					memberPO.setEnergy(RandomUtil.randomInt(10, 20));
					memberPO.setExtroversion(RandomUtil.randomInt(10, 20));
					memberPO.setCharisma(RandomUtil.randomInt(10, 25));

					//更新soul字段
					if (ObjectUtil.isNotEmpty(memberPO.getCharisma())) {
						SoulVo soulVo = ScoreUtil.getPersonalityCharacter(
							memberPO.getCharisma(),
							memberPO.getExtroversion(),
							memberPO.getEnergy(),
							memberPO.getWisdom(),
							memberPO.getArt(),
							memberPO.getCourage()
						);
						memberPO.setPersonality(soulVo.getPersonality());
						memberPO.setChracter(soulVo.getCharacter());
						memberPO.setSoul(soulVo.getPersonality() + " " + soulVo.getCharacter());
					}

					//计算总分
					memberPO.countLevelScore();
					//计算level
					memberPO.countLevel();

					memberPO.setUpdateTime(new Date());

					insertMemberList.add(memberPO);

					if (insertMemberList.size() % 1000 == 0) {
						myMemberService.saveBatch(insertMemberList);
						successCount = successCount + insertMemberList.size();
						inviteList.addAll(insertMemberList);
						insertMemberList.clear();
					}
				}
				if (insertMemberList.size() > 0) {
					myMemberService.saveBatch(insertMemberList);
					inviteList.addAll(insertMemberList);
					successCount = successCount + insertMemberList.size();
				}

				//填写邀请码逻辑
				//区分A类与B类用户
				Integer subIndex = (int) (inviteList.size() * 0.25);
				List<MemberPO> aClassList = new ArrayList<>(inviteList.subList(0, subIndex));
				List<MemberPO> bClassList = new ArrayList<>(inviteList.subList(subIndex, inviteList.size()));


				//A类用户填写官方邀请码
				for (MemberPO memberPO : aClassList) {
					//1711680711437004802
					useInviteCode(memberPO.getId(), "soul-c011d1");
					//正式
//					useInviteCode(memberPO.getId(), "soul-befb9e");
				}

				//B类用户从A类用户中获取邀请码
				// 基于索引的分配逻辑
				int aClassIndex = 0; // A类用户列表中当前用户的索引
				// 确保至少每个A类用户可以邀请3个B类用户
				int bUsersPerAClassUser = 3;

				for (int i = 0; i < bClassList.size(); i++) {
					// 找到对应的A类用户，将其ID作为邀请码分配给B类用户
					MemberPO currentAClassUser = aClassList.get(aClassIndex);

					// 使用当前A类用户的ID为B类用户设置邀请码
					useInviteCode(bClassList.get(i).getId(), currentAClassUser.getSuperInviteCode());

					// 检查是否需要移动到下一个A类用户
					// 如果当前索引i+1是bUsersPerAClassUser的倍数
					// 并且aClassIndex还没有到最后一个A类用户，就移动到下一个A类用户
					if ((i + 1) % bUsersPerAClassUser == 0 && aClassIndex < aClassList.size() - 1) {
						aClassIndex++;
					}
					if (aClassIndex >= aClassList.size() - 1){
						aClassIndex = 0;
					}
				}


				//用户注册成功更新缓存信息
				IUserCache.updateUserCache();
				IUserCache.updateUserRoomsCache();
				IUserCache.updateRoomUsersCache();

				if (successCount == datas.size()) {
					return R.success("insert user info success!");
				}
				return R.success("解析 " + datas.size() + " 条数据，共注册 " + successCount + " 条数据。注册失败地址：" + errorAddress.toString());
			} else {
				return R.fail("Excel列名不符合要求");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return R.fail("文件解析失败：" + e.getMessage());
		}
	}


	@Resource
	private MemberConnectService memberConnectService;

	@Resource
	private UserVSoulService userVSoulService;

	public void useInviteCode(Long userId, String inviteCode) {
		Long inviteUserId = null;
		// 改为始终使用超级邀请码
		MemberPO memberPO = memberMapper.selectOne(new LambdaQueryWrapper<MemberPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(MemberPO::getSuperInviteCode, inviteCode));
		if (ObjectUtil.isEmpty(memberPO)) {
			throw new ServiceException("The invite code is incorrect");
		}
		inviteUserId = memberPO.getId();

		//新增connect连接
		memberConnectService.addConnected(userId, inviteUserId);
		//更新当前新用户的被邀请用户id字段
		MemberPO curNewUser = memberMapper.selectById(userId);
		if (ObjectUtil.isEmpty(curNewUser)) {
			throw new ServiceException("New user does not exist");
		}
		curNewUser.setInviteUserId(inviteUserId);
		memberMapper.updateById(curNewUser);

//		20240122邀请人积分新逻辑
//		1. 如果没有NFT，上下两级都是基础分：2分
//		2. 如果有NFT，上下两级各得:2*booster（按照邀请人计算）
		// 获取邀请用户booster积分
		BigDecimal booster = userVSoulService.getBoostByUserId(inviteUserId);

		Integer newVSoul = 2;

		if (booster.toString().equals("1")) {
			//没有NFT，上下两级都是基础分：2分
			userVSoulService.addUserVSoul(inviteUserId,new BigDecimal(newVSoul), 3); //邀请人 用户id
			userVSoulService.addUserVSoul(curNewUser.getId(),new BigDecimal(newVSoul), 4);
		}else {
			//有NFT，上下两级各得:2*booster（按照邀请人计算）
			userVSoulService.addUserVSoul(inviteUserId,new BigDecimal(newVSoul).multiply(booster), 3); //邀请人 用户id
			userVSoulService.addUserVSoul(curNewUser.getId(),new BigDecimal(newVSoul).multiply(booster), 4);
		}
	}

}
