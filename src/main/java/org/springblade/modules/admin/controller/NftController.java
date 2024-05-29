package org.springblade.modules.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.HttpGlobalConfig;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.redis.cache.BladeRedis;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.modules.admin.dao.MemberMapper;
import org.springblade.modules.admin.dao.PFPHistoryMapper;
import org.springblade.modules.admin.dao.PFPTokenMapper;
import org.springblade.modules.admin.pojo.enums.*;
import org.springblade.modules.admin.pojo.po.MemberPO;
import org.springblade.modules.admin.pojo.po.PFPHistoryPO;
import org.springblade.modules.admin.pojo.po.PFPTokenPO;
import org.springblade.modules.admin.pojo.vo.AfterMintNftVo;
import org.springblade.modules.admin.pojo.vo.EnumVo;
import org.springblade.modules.admin.pojo.vo.MintNftVo;
import org.springblade.modules.admin.pojo.vo.MintPictureVo;
import org.springblade.modules.admin.service.NftService;
import org.springblade.modules.system.entity.Dict;
import org.springblade.modules.system.mapper.DictMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@Slf4j
@RequestMapping("/api/admin/nft")
@Api(value = "铸造NFT",tags = "铸造NFT")
public class NftController {


	@Autowired
	MemberMapper memberMapper;

	@Autowired
	NftService nftService;

	@Autowired
	PFPTokenMapper pfpTokenMapper;

	@Autowired
	PFPHistoryMapper pfpHistoryMapper;

	@Autowired
	DictMapper dictMapper;

	@Autowired
	private BladeRedis bladeRedis;

	private static final String AIGC_TOKEN = "r8_PmZJMtv2hpOF2AbCFtFRskJFDWm1y7C2QYEH5";

	//测试VERSION
//	private static final String AIGC_VSERSION = "d7486b47257d76291290a91f9825fcf2b8bd6e4591ef4fce60efc61924551e1b";

	//正式VERSION
	private static final String AIGC_VSERSION = "014ff6820ab7847c918105fdc4e4ed25cb22ecde803e911bf012009eb0b4f091";




//	@PostMapping("/mintPFP")
//	@ApiOperation(value = "铸造PFP")
//	public R addCollect(@RequestParam("toAddress") String toAddress) {
////		Long userId = StpUtil.getLoginIdAsLong();
//
//		bnbService.mintPFP(toAddress);
//
//		return R.success("铸造成功");
//	}

//	@PostMapping("/createAdminAddress")
//	@ApiOperation(value = "创建平台钱包地址")
//	public R createAdminAddress() {
//		String adminWallet = bnbService.createAdminWallet();
//		return R.data(adminWallet);
//	}


	@GetMapping("/getNFTPersonality")
	@ApiOperation(value = "获取NFT角色下拉框（personality）")
	public R<List<EnumVo>> getNFTPersonality() {
		List<EnumVo> result = new ArrayList<>();
		for (NFTPersonalityEnum value : NFTPersonalityEnum.values()) {
			EnumVo enumVo = new EnumVo();
			BeanUtil.copyProperties(value,enumVo);
			result.add(enumVo);
		}
		return R.data(result);
	}

	@GetMapping("/getNFTLevel")
	@ApiOperation(value = "获取NFT等级下拉框（level）")
	public R<List<EnumVo>> getNFTLevel() {
		List<EnumVo> result = new ArrayList<>();
		for (NFTLevelEnum value : NFTLevelEnum.values()) {
			EnumVo enumVo = new EnumVo();
			BeanUtil.copyProperties(value,enumVo);
			result.add(enumVo);
		}
		return R.data(result);
	}

	@GetMapping("/getNFTMood")
	@ApiOperation(value = "获取NFTmood下拉框（mood）")
	public R<List<EnumVo>> getNFTMood() {
		List<EnumVo> result = new ArrayList<>();
		for (NFTMoodEnum value : NFTMoodEnum.values()) {
			EnumVo enumVo = new EnumVo();
			BeanUtil.copyProperties(value,enumVo);
			result.add(enumVo);
		}
		return R.data(result);
	}

	@GetMapping("/getNFTWeather")
	@ApiOperation(value = "获取NFTWeather下拉框（Weather）")
	public R<List<EnumVo>> getNFTWeather() {
		List<EnumVo> result = new ArrayList<>();
		for (NFTWeatherEnum value : NFTWeatherEnum.values()) {
			EnumVo enumVo = new EnumVo();
			BeanUtil.copyProperties(value,enumVo);
			result.add(enumVo);
		}
		return R.data(result);
	}

	@GetMapping("/getNFTColor")
	@ApiOperation(value = "获取NFTColor下拉框（Color）")
	public R<List<EnumVo>> getNFTColor() {
		List<EnumVo> result = new ArrayList<>();
		for (NFTColorEnum value : NFTColorEnum.values()) {
			EnumVo enumVo = new EnumVo();
			BeanUtil.copyProperties(value,enumVo);
			result.add(enumVo);
		}
		return R.data(result);
	}

	@GetMapping("/getLastMintPictur")
	@ApiOperation(value = "查询是否有已生成的AIGC图片")
	public R<List<MintPictureVo>> getLastMintPictur(){

		List<MintPictureVo> result = new ArrayList<>();

		Long userId = StpUtil.getLoginIdAsLong();
		//从redis中获取已生成的AIGC图片URL
		String resultUrl = bladeRedis.get("AIGC_" + userId);

		//没有缓存
		if(StringUtil.isBlank(resultUrl)){
			return R.fail("There is no picture has been created");
		}

		try{
			int i = 0;
			//3秒调一次,300秒后超时
			while(i < 100) {
				//访问生成图片的接口
				String pictureResultBody = HttpRequest.post(resultUrl).header("Authorization", "Token " + AIGC_TOKEN)
					.timeout(HttpGlobalConfig.getTimeout()).execute().body();

				log.info("访问AIGC生成图片接口返回:{}" + pictureResultBody);

				try {
					JSONObject resultObj = JSONObject.parseObject(pictureResultBody);
					Object error = resultObj.get("error");
					if (error != null) {
						log.info("获取AIGC图片失败：" + error.toString());
						//终止循环
						i = 100;
					} else {
						String status = resultObj.getString("status");
						if ("succeeded".equals(status)) {
							log.info("AIGC图片生成成功");
							//终止循环
							i = 100;
							//获取图片链接
							JSONArray output = resultObj.getJSONArray("output");
							//获取color和mood
							String logs = resultObj.getString("logs");
							// ------------------ 2023-09-17 17:00:00 ------------------
							// 正则匹配
							String colorReg = Objects.requireNonNull(NFTColorEnum.getAigcNameByName("Random")).replace("{", "(").replace("}", ")");
							String moodReg = Objects.requireNonNull(NFTMoodEnum.getAigcNameByName("Random")).replace("{", "(").replace("}", ")");
							String regex = "base=BatchDataBase\\(step=.{20,60}" + colorReg + "_background, " + moodReg + ", expressions";
							// 正则匹配logs遍历所有的color和mood
							Pattern pattern = Pattern.compile(regex);
							Matcher matcher = pattern.matcher(logs);
							List<String> colorList = new ArrayList<>();
							List<String> moodList = new ArrayList<>();
							while (matcher.find()) {
								String group1 = matcher.group(1);
								String group2 = matcher.group(2);
								colorList.add(group1);
								moodList.add(group2);
							}
							log.info("colorList:{}", colorList);
							log.info("moodList:{}", moodList);
							for (int s = 0; s < output.size(); s++) {
								String picUrl = output.getString(s);
								MintPictureVo mintPictureVo = new MintPictureVo();
								mintPictureVo.setSquarePictureUrl(picUrl);
	//								mintPictureVo.setPictureUrl(picUrl);
	//								mintPictureVo.setColorAttribute(20);
//
//								int background = logs.indexOf("_background");
//								int colorBegin = logs.lastIndexOf(",", background);
//								int colorEnd = logs.indexOf(",", background);
//
//								String colorAigc = logs.substring(colorBegin + 1, background).trim();
//								System.out.println(colorAigc);
//
//								int expressions = logs.indexOf("expressions");
//
//								int moodBegin = colorEnd + 1;
//								int moodEnd = logs.lastIndexOf(",", expressions);
//
//								String moodAigc = logs.substring(moodBegin, moodEnd).trim();
//								System.out.println(moodAigc);
//
//								logs = logs.substring(moodEnd + 11);

								//设置color
//								mintPictureVo.setColor(NFTColorEnum.getCodeByAigcName(colorAigc));
								mintPictureVo.setColor(NFTColorEnum.getCodeByAigcName(colorList.get(s)));
								//设置mood
//								mintPictureVo.setMood(NFTMoodEnum.getCodeByAigcName(moodAigc));
								mintPictureVo.setMood(NFTMoodEnum.getCodeByAigcName(moodList.get(s)));
								result.add(mintPictureVo);
							}
						} else {
							if ("starting".equals(status) && i == 0) {
								log.info("AIGC启动中");
							}
							if ("processing".equals(status)) {
								log.info("AIGC图片生成中");
							}
							if ("failed".equals(status)) {
								log.info("AIGC图片生成失败");
								return R.fail("AIGC failed");
							}
							if ("canceled".equals(status)) {
								log.info("AIGC图片生成取消");
								return R.fail("AIGC canceled");
							}
							//计数+1
							i++;
							//间隔3秒
							try {
								Thread.sleep(3000);
							} catch (Exception e) {
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					return R.fail("Get NFT resource failed");
				}
				if(result.size() == 0){
					return R.fail("Generate NFT faild");
				}
			}
		}catch (Exception e){
			e.printStackTrace();
			return R.fail("Request for NFT resource failed");
		}
		return R.data(result);
	}

	@GetMapping("/getMintPicture")
	@ApiOperation(value = "获取铸造NFT的图片（返回6张图片） P0")
	public R<List<MintPictureVo>> getMintPicture(//@ApiParam(value = "personality:传1到16",required = true) @RequestParam("personality") Integer personality,
												 @ApiParam(value = "mood",required = true,example = "0") @RequestParam("mood") Integer mood,
												 //@ApiParam(value = "weather:不知道有哪些，先传个1吧",required = true) @RequestParam("weather") Integer weather,
												 @ApiParam(value = "color",required = true,example = "0") @RequestParam("color") Integer color) {
		//获取mood
		String moodAigcName = NFTMoodEnum.getAigcNameByCode(mood);
		if(moodAigcName == null){
			return R.fail("mood is not exist");
		}

		//获取color
		String colorAigcName = NFTColorEnum.getAigcNameByCode(color);
		if(colorAigcName == null){
			return R.fail("color is not exist");
		}

		List<MintPictureVo> result = new ArrayList<>();

		//获取DICT开关
		Dict aicg_switch = dictMapper.selectOne(new LambdaQueryWrapper<Dict>()
			.eq(Dict::getCode, "aicg_switch")
			.eq(Dict::getDictKey, "0")
			.eq(Dict::getIsDeleted, 0));

		if("0".equals(aicg_switch.getDictValue())){
			//不启用aicg
			for (int i=0;i<6;i++){
				MintPictureVo mintPictureVo = new MintPictureVo();


				if(i == 0){
					mintPictureVo.setSquarePictureUrl("https://sfhmaster-1313464417.cos.ap-nanjing.myqcloud.com/2023/06/12/1667935016998465536.png");
					mintPictureVo.setPictureUrl("https://sfhmaster-1313464417.cos.ap-nanjing.myqcloud.com/2023/06/12/1667935749877592064.png");
					mintPictureVo.setColorAttribute(166);
				} else if (i == 1) {
					mintPictureVo.setSquarePictureUrl("https://sfhmaster-1313464417.cos.ap-nanjing.myqcloud.com/2023/06/13/1668603439399104512.png");
					mintPictureVo.setPictureUrl("https://sfhmaster-1313464417.cos.ap-nanjing.myqcloud.com/2023/06/13/1668603329093103616.png");
					mintPictureVo.setColorAttribute(-42);
				} else if(i == 2){
					mintPictureVo.setSquarePictureUrl("https://sfhmaster-1313464417.cos.ap-nanjing.myqcloud.com/2023/06/13/1668603730555105280.png");
					mintPictureVo.setPictureUrl("https://sfhmaster-1313464417.cos.ap-nanjing.myqcloud.com/2023/06/13/1668603551709982720.png");
					mintPictureVo.setColorAttribute(20);
				} else if (i == 3) {
					mintPictureVo.setSquarePictureUrl("https://sfhmaster-1313464417.cos.ap-nanjing.myqcloud.com/2023/06/13/1668603875980013568.png");
					mintPictureVo.setPictureUrl("https://sfhmaster-1313464417.cos.ap-nanjing.myqcloud.com/2023/06/13/1668603822204841984.png");
					mintPictureVo.setColorAttribute(-26);
				} else if(i == 4){
					mintPictureVo.setSquarePictureUrl("https://sfhmaster-1313464417.cos.ap-nanjing.myqcloud.com/2023/06/13/1668604008863952896.png");
					mintPictureVo.setPictureUrl("https://sfhmaster-1313464417.cos.ap-nanjing.myqcloud.com/2023/06/13/1668603945051811840.png");
					mintPictureVo.setColorAttribute(41);
				} else if (i == 5) {
					mintPictureVo.setSquarePictureUrl("https://sfhmaster-1313464417.cos.ap-nanjing.myqcloud.com/2023/06/13/1668604170780864512.png");
					mintPictureVo.setPictureUrl("https://sfhmaster-1313464417.cos.ap-nanjing.myqcloud.com/2023/06/13/1668604086018174976.png");
					mintPictureVo.setColorAttribute(-15);
				}

				result.add(mintPictureVo);
			}
		}else {
			//启用aicg
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("version",AIGC_VSERSION);
			JSONObject input = new JSONObject();
			// 获取pfp_token总数量
//			Long tokenCount = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
//				.eq(PFPTokenPO::getIsDeleted, 0));
//			System.out.printf("maxId:{}" + tokenCount);
			// 获取member
			MemberPO memberPO = memberMapper.selectById(StpUtil.getLoginIdAsLong());
			input.put("prompt","soulcial_avatar, person, 1{boy|girl}, " +
				//设置color
				colorAigcName +"_background, " +
				//设置mood
				moodAigcName + ", " +
				"expressions, (random complementary colored clothes), {striped|leopard_print|bubble|flower|styled|}_clothes, (muted_color:0.8)," +
				" flat_color, {curly|medium|short|long|straight}_hair, detailed_hair, {blonde|dark|silver|pink|red|brown}_hair, " +
				"{earrings|hat|tiara|fake_animal_ears|jewelry|}, (Whimsical:0.2)" +
				// 判断level是否>=3（雪花效果）
				(memberPO.getLevel() < 3 ? "" : ",(sparkle_background,starry_background,Confetti_background)")
				);
			input.put("negative_prompt","multiple_person:1.5, crossed_arms, bad_hands, oil_painting, watercolor_pencil,traditional_media,monochrome," +
				"lineart,greyscale,graphite(medium), watercolor,water_pastel_color, photo_realistic, anime, blue_background:0.6, pale_skin:0.5");
			input.put("width",512);
			input.put("height",512);
			input.put("images_per_prompt",6);
			input.put("n_iter",1);
			input.put("num_inference_steps",22);
			jsonObject.put("input",input);

			//获取图片的URL
			String resultUrl = null;
			try {
				//发起请求，生成图片
				String body = HttpRequest.post("https://api.replicate.com/v1/predictions").body(jsonObject.toJSONString())
					.header("Authorization", "Token " + AIGC_TOKEN)
					.header("Content-Type","application/json")
					.timeout(HttpGlobalConfig.getTimeout()).execute().body();
				log.info("生成AIGC图片接口返回:{}"+body);

				JSONObject resultObj = JSONObject.parseObject(body);
				resultUrl = resultObj.getJSONObject("urls").getString("get");
			}catch (Exception e){
				e.printStackTrace();
				return R.fail("Request AIGC failed");
			}

			Long userId = StpUtil.getLoginIdAsLong();

			//间隔20秒
			try {Thread.sleep(20000);}catch (Exception e){}
			int i = 0;
			//3秒调一次,300秒后超时
			while(i < 100){

				try {
					//访问生成图片的接口
//					String pictureResultBody = HttpRequest.post(resultUrl).header("Authorization", "Token " + AIGC_TOKEN)
					String pictureResultBody = HttpRequest.get(resultUrl).header("Authorization", "Token " + AIGC_TOKEN)
						.timeout(HttpGlobalConfig.getTimeout()).execute().body();

					log.info("访问AIGC生成图片接口返回:{}"+pictureResultBody);

					JSONObject resultObj = JSONObject.parseObject(pictureResultBody);
					Object error = resultObj.get("error");
					if(error != null){
						log.info("获取AIGC图片失败："+error.toString());
						//终止循环
						i = 100;
					}else {
						String status = resultObj.getString("status");
						if("succeeded".equals(status)){
							log.info("AIGC图片生成成功");
							//终止循环
							i = 100;
							//获取图片链接
							JSONArray output = resultObj.getJSONArray("output");
							//获取color和mood
							String logs = resultObj.getString("logs");
							// ------------------ 2023-09-17 17:00:00 ------------------
							// 正则匹配
							String colorReg = Objects.requireNonNull(NFTColorEnum.getAigcNameByName("Random")).replace("{", "(").replace("}", ")");
							String moodReg = Objects.requireNonNull(NFTMoodEnum.getAigcNameByName("Random")).replace("{", "(").replace("}", ")");
							String regex = "base=BatchDataBase\\(step=.{20,60}" + colorReg + "_background, " + moodReg + ", expressions";
							// 正则匹配logs遍历所有的color和mood
							Pattern pattern = Pattern.compile(regex);
							Matcher matcher = pattern.matcher(logs);
							List<String> colorList = new ArrayList<>();
							List<String> moodList = new ArrayList<>();
							while (matcher.find()) {
								String group1 = matcher.group(1);
								String group2 = matcher.group(2);
								colorList.add(group1);
								moodList.add(group2);
							}
							log.info("colorList:{}", colorList);
							log.info("moodList:{}", moodList);

							for (int s=0;s<output.size();s++){
								String picUrl = output.getString(s);
								MintPictureVo mintPictureVo = new MintPictureVo();
								mintPictureVo.setSquarePictureUrl(picUrl);
//								mintPictureVo.setPictureUrl(picUrl);
//								mintPictureVo.setColorAttribute(20);
//								int background = logs.indexOf("_background");
//								int colorBegin = logs.lastIndexOf(",", background);
//								int colorEnd = logs.indexOf(",", background);
//
//								String colorAigc = logs.substring(colorBegin + 1, background).trim();
//								System.out.println(colorAigc);
//
//								int expressions = logs.indexOf("expressions");
//
//								int moodBegin = colorEnd + 1;
//								int moodEnd = logs.lastIndexOf(",", expressions);
//
//								String moodAigc = logs.substring(moodBegin, moodEnd).trim();
//								System.out.println(moodAigc);
//
//								logs = logs.substring(moodEnd + 11);

								//设置color
//								mintPictureVo.setColor(NFTColorEnum.getCodeByAigcName(colorAigc));
								mintPictureVo.setColor(NFTColorEnum.getCodeByAigcName(colorList.get(s)));
								//设置mood
//								mintPictureVo.setMood(NFTMoodEnum.getCodeByAigcName(moodAigc));
								mintPictureVo.setMood(NFTMoodEnum.getCodeByAigcName(moodList.get(s)));
								result.add(mintPictureVo);
							}
							//将获取图片的URL存入redis中,过期时间8分钟
							bladeRedis.setEx("AIGC_" + userId,resultUrl,8 * 60l);
						}else {
							if("starting".equals(status)){
								log.info("AIGC启动中");

								//间隔30秒
								try {Thread.sleep(30 * 1000);}catch (Exception e){}
							}
							if("processing".equals(status)){
								log.info("AIGC图片生成中");

								//间隔5秒
								try {Thread.sleep(5 * 1000);}catch (Exception e){}
							}
							if("failed".equals(status)){
								log.info("AIGC图片生成失败");
								return R.fail("AIGC failed");
							}
							if("canceled".equals(status)){
								log.info("AIGC图片生成取消");
								return R.fail("AIGC canceled");
							}
							//计数+1
							i++;
						}
					}
				}catch (Exception e){
					e.printStackTrace();
					return R.fail("Get NFT resource failed");
				}

			}

			if(result.size() == 0){
				return R.fail("Generate NFT faild");
			}
		}
		return R.data(result);
	}

	@PostMapping("/mintFreeNft")
	@ApiOperation(value = "铸造免费NFT P0")
	public R<Long> mintFreeNft(@Valid @RequestBody MintNftVo mintNftVo) {
		R result = nftService.mintFreeNft(mintNftVo);
		return result;
	}

	@PostMapping("/frontMintFreeNft")
	@ApiOperation(value = "zksync前端铸造免费NFT")
	public R<Long> frontMintFreeNft(@Valid @RequestBody MintNftVo mintNftVo) {
		R result = nftService.frontMintFreeNft(mintNftVo);
		return result;
	}

	@PostMapping("/afterMintFreeNft")
	public R<Long> afterMintFreeNft(@Valid @RequestBody AfterMintNftVo afterMintNftVo) {
		Long tokenId = afterMintNftVo.getTokenId();
//		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectById(tokenId);
		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(tokenId);
		MemberPO memberPO = memberMapper.selectById(StpUtil.getLoginIdAsLong());

		if(afterMintNftVo.getIsSuccess() == 1){
			String txnHash = afterMintNftVo.getTxn();

			//已铸造
			pfpTokenPO.setMintStatus(1);
			pfpTokenPO.setMintTime(new Date());
			pfpTokenPO.setMintTxnHash(txnHash);
			pfpTokenPO.initForUpdate();

			pfpTokenMapper.updateById(pfpTokenPO);

			//添加history
			PFPHistoryPO pfpHistoryPO = new PFPHistoryPO();
			pfpHistoryPO.setTokenId(pfpTokenPO.getId());
			//铸造
			pfpHistoryPO.setType(0);
//			pfpHistoryPO.setTransactionId();
			pfpHistoryPO.setAdminAddress(pfpTokenPO.getAdminAddress());
			pfpHistoryPO.setLinkType(pfpTokenPO.getLinkType());
			pfpHistoryPO.setNetwork(pfpTokenPO.getNetwork());
			pfpHistoryPO.setContractAddress(pfpTokenPO.getContractAddress());
			pfpHistoryPO.setContractName(pfpTokenPO.getContractName());
			pfpHistoryPO.setToAddress(pfpTokenPO.getMintUserAddress());
//			pfpHistoryPO.setToAddress();
			pfpHistoryPO.setToUserId(pfpTokenPO.getMintUserId());
//			pfpHistoryPO.setToUserId();
			pfpHistoryPO.setTxnHash(txnHash);
//			pfpHistoryPO.setPrice();

			pfpHistoryPO.initForInsert();

			pfpHistoryMapper.insert(pfpHistoryPO);

			return R.data(pfpTokenPO.getId(),"mint success");

		}else {
			//铸造失败，退还免费次数
			//用户免费mint变更为未使用
			memberPO.setFreeMint(0);
			memberPO.initForUpdate();

			memberMapper.updateById(memberPO);

			//删除token
			pfpTokenPO.initForUpdate();
			pfpTokenPO.setIsDeleted(1);

			pfpTokenMapper.updateById(pfpTokenPO);

			return R.data(pfpTokenPO.getId(),"mint failed");
		}
	}

}
