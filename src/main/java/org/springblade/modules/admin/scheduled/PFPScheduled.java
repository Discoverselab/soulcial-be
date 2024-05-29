package org.springblade.modules.admin.scheduled;


import cn.hutool.http.HttpGlobalConfig;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.redis.cache.BladeRedis;
import org.springblade.modules.admin.dao.PFPPickMapper;
import org.springblade.modules.admin.dao.PFPTokenMapper;
import org.springblade.modules.admin.dao.PFPTransactionMapper;
import org.springblade.modules.admin.pojo.enums.NFTColorEnum;
import org.springblade.modules.admin.pojo.enums.NFTMoodEnum;
import org.springblade.modules.admin.pojo.po.PFPPickPO;
import org.springblade.modules.admin.pojo.po.PFPTokenPO;
import org.springblade.modules.admin.pojo.vo.MintPictureVo;
import org.springblade.modules.admin.service.ETHService;
import org.springblade.modules.admin.service.NftService;
import org.springblade.modules.admin.util.PickUtil;
import org.springblade.modules.system.entity.Dict;
import org.springblade.modules.system.mapper.DictMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 定时任务
 */
@Component
@Slf4j
public class PFPScheduled {

	@Autowired
	private PFPTransactionMapper pfpTransactionMapper;

	@Autowired
	private PFPTokenMapper pfpTokenMapper;

	@Autowired
	private ETHService ethService;

	@Autowired
	private NftService nftService;

	@Autowired
	private BladeRedis bladeRedis;

	@Autowired
	private PFPPickMapper pfpPickMapper;

	@Autowired
	private DictMapper dictMapper;

	private static final String AIGC_TOKEN = "r8_PmZJMtv2hpOF2AbCFtFRskJFDWm1y7C2QYEH5";

	//正式VERSION
	private static final String AIGC_VSERSION = "014ff6820ab7847c918105fdc4e4ed25cb22ecde803e911bf012009eb0b4f091";


	/**
	 * 定时调用AICG服务，以便增加AIGC的访问速度
	 * 每8分钟执行一次
	 */
	@Scheduled(fixedDelay = 8 * 60 * 1000)
	public void runAICGService (){
		//获取该定时任务开关
		Dict aicg_scheduled_switch = dictMapper.selectOne(new LambdaQueryWrapper<Dict>()
			.eq(Dict::getCode, "aicg_scheduled_switch")
			.eq(Dict::getDictKey, "0")
			.eq(Dict::getIsDeleted, 0));
		if ("0".equals(aicg_scheduled_switch.getDictValue())) {
			return;
		}
		log.info(">>> 定时调用AICG服务开启");
		//获取mood
		String moodAigcName = NFTMoodEnum.getAigcNameByCode(0);
		//获取color
		String colorAigcName = NFTColorEnum.getAigcNameByCode(0);
		List<MintPictureVo> result = new ArrayList<>();
		//获取DICT开关
		Dict aicg_switch = dictMapper.selectOne(new LambdaQueryWrapper<Dict>()
			.eq(Dict::getCode, "aicg_switch")
			.eq(Dict::getDictKey, "0")
			.eq(Dict::getIsDeleted, 0));
		if("1".equals(aicg_switch.getDictValue())){
			//启用aicg
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("version",AIGC_VSERSION);
			JSONObject input = new JSONObject();
			input.put("prompt","soulcial_avatar, person, 1{boy|girl}, " +
				//设置color
				colorAigcName +"_background, " +
				//设置mood
				moodAigcName + ", " +
				"expressions, (random complementary colored clothes), {striped|leopard_print|bubble|flower|styled|}_clothes, (muted_color:0.8)," +
				" flat_color, {curly|medium|short|long|straight}_hair, detailed_hair, {blonde|dark|silver|pink|red|brown}_hair, " +
				"{earrings|hat|tiara|fake_animal_ears|jewelry|}, (Whimsical:0.2)");
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
				log.info(">>> 定时调用AICG服务开启--生成AIGC图片接口返回:{}",body);
				JSONObject resultObj = JSONObject.parseObject(body);
				resultUrl = resultObj.getJSONObject("urls").getString("get");
			}catch (Exception e){
				e.printStackTrace();
				log.error(">>> 定时调用AICG服务, 请求失败，url:https://api.replicate.com/v1/predictions");
			}
			//间隔5秒
			try {Thread.sleep(5000);}catch (Exception e){}
			int i = 0;
			//3秒调一次,300秒后超时
			while(i < 100){
				try {
					//访问生成图片的接口
//					String pictureResultBody = HttpRequest.post(resultUrl).header("Authorization", "Token " + AIGC_TOKEN)
					String pictureResultBody = HttpRequest.get(resultUrl).header("Authorization", "Token " + AIGC_TOKEN)
						.timeout(HttpGlobalConfig.getTimeout()).execute().body();
					log.info(">>>定时调用AICG服务,访问AIGC生成图片接口返回:{}",pictureResultBody);
					JSONObject resultObj = JSONObject.parseObject(pictureResultBody);
					Object error = resultObj.get("error");
					if(error != null){
						log.error(">>>定时调用AICG服务,获取AIGC图片失败:{}",error.toString());
						//终止循环
						i = 100;
					}else {
						String status = resultObj.getString("status");
						if("succeeded".equals(status)){
							log.info(">>>定时调用AICG服务,AIGC图片生成成功");
							//终止循环
							i = 100;
							//获取图片链接
							JSONArray output = resultObj.getJSONArray("output");
							//获取color和mood
							String logs = resultObj.getString("logs");
							for (int s=0;s<output.size();s++){
								String picUrl = output.getString(s);
								MintPictureVo mintPictureVo = new MintPictureVo();
								mintPictureVo.setSquarePictureUrl(picUrl);
								int background = logs.indexOf("_background");
								int colorBegin = logs.lastIndexOf(",", background);
								int colorEnd = logs.indexOf(",", background);
								String colorAigc = logs.substring(colorBegin + 1, background).trim();
								int expressions = logs.indexOf("expressions");
								int moodBegin = colorEnd + 1;
								int moodEnd = logs.lastIndexOf(",", expressions);
								String moodAigc = logs.substring(moodBegin, moodEnd).trim();
								logs = logs.substring(moodEnd + 11);
								//设置color
								mintPictureVo.setColor(NFTColorEnum.getCodeByAigcName(colorAigc));
								//设置mood
								mintPictureVo.setMood(NFTMoodEnum.getCodeByAigcName(moodAigc));
								result.add(mintPictureVo);
							}
						}else {
							if("starting".equals(status)){
								log.info(">>>定时调用AICG服务,AIGC启动中");
								//间隔2秒
								try {Thread.sleep(5 * 1000);}catch (Exception e){}
							}
							if("processing".equals(status)){
								log.info(">>>定时调用AICG服务,AIGC图片生成中");
								//间隔2秒
								try {Thread.sleep(5 * 1000);}catch (Exception e){}
							}
							if("failed".equals(status)){
								log.info(">>>定时调用AICG服务,AIGC图片生成失败");
								return;
							}
							if("canceled".equals(status)){
								log.info(">>>定时调用AICG服务,AIGC图片生成取消");
								return;
							}
							//计数+1
							i++;
						}
					}
				}catch (Exception e){
					e.printStackTrace();
					return;
				}
			}
			if(result.isEmpty()){
				log.error(">>>定时调用AICG服务,图片生成失败");
				return;
			}
			log.info(">>>定时调用AICG服务成功！！！");
		}
	}


	/**
	 * 开奖
	 * 每12秒执行一次
	 */
//	@Scheduled(fixedDelay = 12 * 1000)
	public void rewardNFT() throws Exception{
		//查询开奖队列数量
		Long size = bladeRedis.lLen(PickUtil.REWARD_LIST);
		//查询开奖时间
		Long rewardTime = bladeRedis.get(PickUtil.REWARD_TIME);
		log.info("==============待开奖队列数量{}，开奖时间{}",size, rewardTime == null? "暂无开奖时间" :new Date(rewardTime));
		//当前时间
		Long nowTime = System.currentTimeMillis();
		if(rewardTime != null && rewardTime < nowTime){
			//已到达开奖时间

			//变更状态为开奖中
			for (int i=0;i<size;i++){
				Long pickId = bladeRedis.lIndex(PickUtil.REWARD_LIST, i);
				PFPPickPO pfpPickPO = pfpPickMapper.selectById(pickId);
				Long tokenId = pfpPickPO.getTokenId();
//				PFPTokenPO pfpTokenPO = pfpTokenMapper.selectById(tokenId);
				PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(tokenId);
				//开奖中，不可交易
				pfpTokenPO.setPickStatus(3);
				pfpTokenMapper.updateById(pfpTokenPO);
			}

			//全部开奖
			while(bladeRedis.lLen(PickUtil.REWARD_LIST) > 0){
				//拿第一个
				Long pickId = bladeRedis.lIndex(PickUtil.REWARD_LIST, 0);
				ethService.rewardNFT(pickId);
				//删除第一个
				bladeRedis.lPop(PickUtil.REWARD_LIST);
				//间隔12秒
				Thread.sleep(12 * 1000);
			}

			//开奖完成
			//清除开奖时间
			bladeRedis.del(PickUtil.REWARD_TIME);
			//清除最后一个区块
			bladeRedis.del(PickUtil.LAST_REWARD_BLOCK_HEIGHT);
		}


	}


	/**
	 * 关闭超时订单
	 * 每分钟执行一次
	 */
//	@Scheduled(cron = "0 0/1 * * * ?")
//	@Scheduled(fixedDelay = 60 * 1000)
//	public void closeOvertimeOrder(){
//		List<PFPTransactionPO> pfpTransactionPOS = pfpTransactionMapper.selectList(new LambdaQueryWrapper<PFPTransactionPO>()
//			.eq(BasePO::getIsDeleted, 0)
//			//已下单未交易数据
//			.eq(PFPTransactionPO::getTransactionStatus, 0));
//
//		log.info("===============当前进行中的订单数："+pfpTransactionPOS.size());
//		int overTimeConnt = 0;
//
//		for (PFPTransactionPO x : pfpTransactionPOS) {
//			//关闭超时订单
//			boolean isOverTime = closeOverTimeOrder(x);
//			if (isOverTime){
//				overTimeConnt++;
//			}
//		}
//
//		log.info("===============关闭超时未支付订单数："+overTimeConnt);
//
//	}


	/**
	 * 校验已支付订单
	 * 每分钟执行一次
	 */
//	@Scheduled(cron = "0 0/1 * * * ?")
//	@Scheduled(fixedDelay = 60 * 1000)
//	public void checkPayedOrder(){
//		List<PFPTransactionPO> pfpTransactionPOS = pfpTransactionMapper.selectList(new LambdaQueryWrapper<PFPTransactionPO>()
//			.eq(BasePO::getIsDeleted, 0)
//			//5-已付款未验证
//			.eq(PFPTransactionPO::getTransactionStatus, 5));
//
//		log.info("===============当前已付款未确认订单数："+pfpTransactionPOS.size());
//		int overTimeConnt = 0;
//
//		for (PFPTransactionPO x : pfpTransactionPOS) {
//
//			R<Boolean> checkBNBTransResult = ethService.checkBNBTransacation(x.getBuyerMoneyTxnHash(),x.getListPrice(),x.getFromAddress(),x.getToAddress());
//
//			if(checkBNBTransResult.getCode() == 200){
//				Boolean data = checkBNBTransResult.getData();
//				if(data){
//					//校验成功
//
//					//状态变更为：已付款未交易PFP
//					x.setTransactionStatus(1);
//					pfpTransactionMapper.updateById(x);
//
//					//转NFT
//					R result = nftService.transferNFT(x);
//					log.info("======定时任务转NFT结果："+result.getCode() + "============" + result.getMsg());
//				}else {
//					//付款验证失败
//					x.setTransactionStatus(4);
//					pfpTransactionMapper.updateById(x);
//
//					//变更NFT状态为允许交易
//					updateTokenStatus(x.getTokenId());
//				}
//			}else {
//				//确认中，不做处理
//			}
//
//			//关闭超时订单
//			boolean isOverTime = closeOverTimeOrder(x);
//			if (isOverTime){
//				overTimeConnt++;
//			}
//		}
//
//		log.info("===============关闭超时已付款未确认订单数："+overTimeConnt);
//
//	}

	/**
	 * 变更NFT状态为允许交易
	 */
//	private void updateTokenStatus(Long tokenId){
//		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectById(tokenId);
//		//交易状态：0-可交易
//		pfpTokenPO.setStatus(0);
//		pfpTokenMapper.updateById(pfpTokenPO);
//	}

	/**
	 * 关闭超时订单
	 * 已超时返回true
	 * 未超时返回false
	 */
//	private boolean closeOverTimeOrder(PFPTransactionPO x){
//		//创建时间
//		Date createTime = x.getCreateTime();
//		//当前时间
//		long now = System.currentTimeMillis();
//
//		//订单超过10分钟 未验证通过
//		if(now - createTime.getTime() > 10 * 60 * 1000){
//			//交易取消
//			x.setTransactionStatus(3);
//			pfpTransactionMapper.updateById(x);
//
//			//变更NFT状态为允许交易
//			updateTokenStatus(x.getTokenId());
//
//			return true;
//		}
//
//		return false;
//	}

}
