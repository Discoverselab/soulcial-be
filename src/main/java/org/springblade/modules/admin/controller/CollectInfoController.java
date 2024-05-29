package org.springblade.modules.admin.controller;

import cn.hutool.http.HttpGlobalConfig;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.assist.ISqlRunner;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.api.R;
import org.springblade.modules.admin.dao.PFPTokenMapper;
import org.springblade.modules.admin.pojo.enums.NFTLevelEnum;
import org.springblade.modules.admin.pojo.po.BasePO;
import org.springblade.modules.admin.pojo.po.MemberPO;
import org.springblade.modules.admin.pojo.po.PFPTokenPO;
import org.springblade.modules.admin.pojo.vo.SoulVo;
import org.springblade.modules.admin.service.NftService;
import org.springblade.modules.admin.service.UserScoreService;
import org.springblade.modules.admin.service.impl.UserScoreServiceImpl;
import org.springblade.modules.admin.util.ScoreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

@RestController
@RequestMapping("/api/admin/collect")
@Api(value = "自用测试接口",tags = "自用测试接口")
@Slf4j
public class CollectInfoController {

	private final static int timeout = HttpGlobalConfig.getTimeout();
	private final static String knn3AuthKey = "a97c41edaf7bbcde96dfc31ab15226a2e09c1d15913551f85d56847d3ee10ef8";

	private final static String NFTGOAPIKEY = "18a5b214-3815-4dd8-b280-413b8844431e";

	private final static String LensFollowingCountUrl = "https://knn3-gateway.knn3.xyz/data-api/api/addresses/lensFollowingCount";

	private final static String LensFollowersCountUrl = "https://knn3-gateway.knn3.xyz/data-api/api/lens/followers/:profileId/count";

	private final static String LensProfileIdsByAddressUrl = "https://knn3-gateway.knn3.xyz/data-api/api/addresses/boundLens";

	private final static String PoapsCountUrl = "https://knn3-gateway.knn3.xyz/data-api/api/addresses/poaps/count";

	private final static String SnapshotCountUrl = "https://knn3-gateway.knn3.xyz/data-api/api/addresses/calVotes/";

	private final static String NFTCountUrlKnn3 = "https://knn3-gateway.knn3.xyz/data-api/api/addresses/holdNfts";

	private final static String NFTCountUrlNFTGO = "https://data-api.nftgo.io/eth/v2/address/metrics?address=";

	private final static String CCMainnetUrl = "https://api.cyberconnect.dev/playground";

	private final static String CCTestUrl = "https://api.cyberconnect.dev/testnet/playground";

	@Autowired
	private UserScoreService userScoreService;

	@Autowired
	private NftService nftService;

	@Autowired
	private PFPTokenMapper pfpTokenMapper;


	@GetMapping("/batchCalcUserSoul")
	@ApiOperation(value = "批量计算现有用户的soul")
	public R<?> batchCalcUserSoul() {
		userScoreService.batchCalcUserSoul();
	    return R.success("操作成功");
	}

	@GetMapping("/batchCalcNftSoul")
	@ApiOperation(value = "批量计算现有NFT的soul")
	public R<?> batchCalcNftSoul() {
		nftService.batchCalcNftSoul();
		return R.success("操作成功");
	}

	@GetMapping("/getLensFollowing")
	@ApiOperation(value = "getLensFollowing")
	public R<Integer> getLensFollowing(@ApiParam(value = "查lensFollowing需要的address",required = true)@RequestParam(value = "address")String address) {
		int lensFollowing = UserScoreServiceImpl.getLensFollowing(address);
		return R.data(lensFollowing);
	}

	@GetMapping("/getLensFollowsByProfileId")
	@ApiOperation(value = "根据profileId查询LensFollows")
	public R<Integer> getLensFollowsByProfileId(@ApiParam(value = "profileId",required = true)@RequestParam(value = "profileId",required = true)String profileId) {
		int lensFollowing = UserScoreServiceImpl.getLensFollows(profileId);
		return R.data(lensFollowing);
	}

	@GetMapping("/getLensFollowsByAddress")
	@ApiOperation(value = "根据address查询LensFollows")
	public R<Integer> getLensFollowsByAddress(@ApiParam(value = "address",required = true)@RequestParam(value = "address",required = true)String address) {
		String lensProfile = UserScoreServiceImpl.getLensProfileIdByAddress(address);
		int lensFollowing = UserScoreServiceImpl.getLensFollows(lensProfile);
		return R.data(lensFollowing);
	}

	@GetMapping("/getCCFollowing")
	@ApiOperation(value = "getCCFollowing")
	public R<Integer> getCCFollowing(@ApiParam(value = "查CCFollowing需要的address",required = true)@RequestParam(value = "address",required = true)String address) {
		int lensFollowing = UserScoreServiceImpl.getCCFollowing(address);
		return R.data(lensFollowing);
	}

	@GetMapping("/getCCFollows")
	@ApiOperation(value = "getCCFollows")
	public R<Integer> getCCFollows(@ApiParam(value = "查CCFollowers需要的address",required = true)@RequestParam(value = "address",required = true)String address) {
		int lensFollowing = UserScoreServiceImpl.getCCFollowers(address);
		return R.data(lensFollowing);
	}

	@GetMapping("/getPoapsCount")
	@ApiOperation(value = "getPoapsCount")
	public R<Integer> getPoapsCount(@ApiParam(value = "查PoapsCount需要的address",required = true)@RequestParam(value = "address",required = true)String address) {
		int lensFollowing = UserScoreServiceImpl.getPoapsCount(address);
		return R.data(lensFollowing);
	}

	@GetMapping("/getSnapshotCount")
	@ApiOperation(value = "getSnapshotCount")
	public R<Integer> getSnapshotCount(@ApiParam(value = "查SnapshotCount需要的address",required = true)@RequestParam(value = "address",required = true)String address) {
		int lensFollowing = UserScoreServiceImpl.getSnapshotCount(address);
		return R.data(lensFollowing);
	}

	@GetMapping("/getW3STCount")
	@ApiOperation(value = "getW3STCount")
	public R<Integer> getW3STCount(@ApiParam(value = "查W3STCount需要的address",required = true)@RequestParam(value = "address",required = true)String address) {
		int lensFollowing = UserScoreServiceImpl.getW3STCount(address);
		return R.data(lensFollowing);
	}

	@GetMapping("/getNFTCount")
	@ApiOperation(value = "getNFTCount")
	public R<Integer> getNFTCount(@ApiParam(value = "查NFTCount需要的address",required = true)@RequestParam(value = "address",required = true)String address) {
		int lensFollowing = UserScoreServiceImpl.getNFTCount(address);
		return R.data(lensFollowing);
	}

	@GetMapping("/getGasTotal")
	@ApiOperation(value = "getGasTotal(仅集成了ETH)")
	public R<String> getGasTotal(@ApiParam(value = "查手续费需要的address",required = true)@RequestParam(value = "address",required = true)String address) {
		BigDecimal ethGasPrice = UserScoreServiceImpl.getETHGasTotal(address);
		return R.data(ethGasPrice.toString());
	}

	// 查询 getPersonalityCharacter 根据分数查人格特征
	@GetMapping("/getPersonalityCharacter")
	@ApiOperation(value = "根据分数查人格特征")
	public R<SoulVo> getPersonalityCharacter(
		@ApiParam(value = "影响力", required = true) @RequestParam(value = "影响力",required = true) int influence,
		@ApiParam(value = "连接度", required = true) @RequestParam(value = "连接度",required = true) int connection,
		@ApiParam(value = "精力", required = true) @RequestParam(value = "精力",required = true) int energy,
		@ApiParam(value = "感知", required = true) @RequestParam(value = "感知",required = true) int wisdom,
		@ApiParam(value = "品味", required = true) @RequestParam(value = "品味",required = true) int art,
		@ApiParam(value = "勇气", required = true) @RequestParam(value = "勇气",required = true)int courage) {
		SoulVo vo = ScoreUtil.getPersonalityCharacter(influence, connection, energy, wisdom, art, courage);
		return R.data(vo);
	}

	// 查询总分
	@GetMapping("/getTotalScore")
	@ApiOperation(value = "查询总分")
	public R<Map> getTotalScore(@ApiParam(value = "查总分需要的address",required = true)@RequestParam(value = "address",required = true)String address,
								@ApiParam(value = "nftOwnerCount", required = true) @RequestParam(value = "nftOwnerCount") Long nftOwnerCount,
								@ApiParam(value = "一度连接", required = true) @RequestParam(value = "x1") Long x1,
								@ApiParam(value = "二度连接", required = true) @RequestParam(value = "x2") Long x2,
								@ApiParam(value = "pickCount", required = true) @RequestParam(value = "pickCount") Long pickCount,
								@ApiParam(value = "lv1数量",required = false)@RequestParam(value = "lv1",required = false)Long lv1,
								@ApiParam(value = "lv2数量",required = false)@RequestParam(value = "lv2",required = false)Long lv2,
								@ApiParam(value = "lv3数量",required = false)@RequestParam(value = "lv3",required = false)Long lv3,
								@ApiParam(value = "lv4数量",required = false)@RequestParam(value = "lv4",required = false)Long lv4,
								@ApiParam(value = "lv5数量",required = false)@RequestParam(value = "lv5",required = false)Long lv5) {
		// 影响力 调用本class的scoreINFLUENCE
		int INFLUENCECount = this.scoreINFLUENCE(address, lv1, lv2, lv3, lv4, lv5).getData();
		// 连接度
		int scoreCONNECTIONCount = this.scoreCONNECTION(x1, x2).getData();
		// 精力
		int scoreEnergyCount = this.scoreEnergy(address).getData();
		// 感知
		int scoreWisdomCount = this.scoreWisdom(address).getData();
		// 品味
		int artCount = this.scoreArt(address, nftOwnerCount).getData();
		// 勇气
		int scoreCourageCount = this.scoreCourage(address, pickCount).getData();
		// 返回每个属性的分数及总分
		Map<String, Integer> totalScore = new HashMap<>();
		totalScore.put("INFLUENCE", INFLUENCECount);
		totalScore.put("CONNECTION", scoreCONNECTIONCount);
		totalScore.put("Energy", scoreEnergyCount);
		totalScore.put("Wisdom", scoreWisdomCount);
		totalScore.put("Art", artCount);
		totalScore.put("Courage", scoreCourageCount);
		totalScore.put("Total", INFLUENCECount + scoreCONNECTIONCount + scoreEnergyCount + scoreWisdomCount + artCount + scoreCourageCount);
		return R.data(totalScore);
	}

	@GetMapping("/scoreCourage")
	@ApiOperation(value = "勇气(目前仅计算ETH手续费)")
	public  R<Integer> scoreCourage(@ApiParam(value = "address", required = true) @RequestParam(value = "address") String address,
									@ApiParam(value = "pickCount", required = true) @RequestParam(value = "pickCount") Long pickCount) {

		BigDecimal ethGasPrice = UserScoreServiceImpl.getETHGasTotal(address);
		Integer score = UserScoreServiceImpl.getScoreCourage(ethGasPrice,pickCount);

		return R.data(score);
	}

	@GetMapping("/scoreArt")
	@ApiOperation(value = "品味")
	public  R<Integer> scoreArt(@ApiParam(value = "address", required = true) @RequestParam(value = "address") String address,
								@ApiParam(value = "nftOwnerCount", required = true) @RequestParam(value = "nftOwnerCount") Long nftOwnerCount) {
		int nftCount = UserScoreServiceImpl.getNFTCount(address);
		Integer score = UserScoreServiceImpl.getScoreArt(nftCount,nftOwnerCount);
		return R.data(score);
	}



	@GetMapping("/scoreWisdom")
	@ApiOperation(value = "感知")
	public  R<Integer> scoreWisdom(@ApiParam(value = "address", required = true) @RequestParam(value = "address") String address) {

		int snapshotCount = UserScoreServiceImpl.getSnapshotCount(address);
		Integer score = UserScoreServiceImpl.getScoreWisdom(snapshotCount);
		return R.data(score);
	}

	@GetMapping("/scoreEnergy")
	@ApiOperation(value = "精力")
	public  R<Integer> scoreEnergy(@ApiParam(value = "查following需要的address", required = true) @RequestParam(value = "address") String address) {
		int poapsCount = UserScoreServiceImpl.getPoapsCount(address);
		int w3STCount = UserScoreServiceImpl.getW3STCount(address);
		Integer score = UserScoreServiceImpl.getScoreEnergy(poapsCount,w3STCount);
		return R.data(score);
	}

	@GetMapping("/scoreCONNECTION")
	@ApiOperation(value = "连接度")
	public  R<Integer> scoreCONNECTION(@ApiParam(value = "一度连接", required = true) @RequestParam(value = "x1") Long x1,
									   @ApiParam(value = "二度连接", required = true) @RequestParam(value = "x2") Long x2) {
		Integer score = UserScoreServiceImpl.getScoreCONNECTION(x1,x2);
		return R.data(score);
	}

	//即将弃用，由上方的scoreCONNECTION替代
	@GetMapping("/scoreExtroversion")
	@ApiOperation(value = "外向性")
	public  R<Integer> scoreExtroversion(@ApiParam(value = "查following需要的address", required = true) @RequestParam(value = "address") String address) {
		int lensFollowing = UserScoreServiceImpl.getLensFollowing(address);
		int ccFollowing = UserScoreServiceImpl.getCCFollowing(address);
		Integer score = UserScoreServiceImpl.getScoreExtroversion(lensFollowing,ccFollowing);
		return R.data(score);
	}

	@GetMapping("/scoreINFLUENCE")
	@ApiOperation(value = "影响力")
	public  R<Integer> scoreINFLUENCE(@ApiParam(value = "查Followers需要的address", required = true) @RequestParam(value = "address") String address,
									  @ApiParam(value = "lv1数量", required = false) @RequestParam(value = "lv1") Long lv1,
									  @ApiParam(value = "lv2数量", required = false) @RequestParam(value = "lv2") Long lv2,
									  @ApiParam(value = "lv3数量", required = false) @RequestParam(value = "lv3") Long lv3,
									  @ApiParam(value = "lv4数量", required = false) @RequestParam(value = "lv4") Long lv4,
									  @ApiParam(value = "lv5数量", required = false) @RequestParam(value = "lv5") Long lv5) {

		String lensProfile = UserScoreServiceImpl.getLensProfileIdByAddress(address);
		int lensFollows = UserScoreServiceImpl.getLensFollows(lensProfile);

//		int ccFollows = UserScoreServiceImpl.getCCFollowers(address);
		// 获取当前用户绑定的推特数量
		int ttFollows = UserScoreServiceImpl.getTwitterFollowers(address);
		// 查询address是否为本平台用户
		Long userId = UserScoreServiceImpl.getUserIdByAddress(address);
		// 查询持有NFT数量
		if (userId != null) {
			if (lv1 == null) {
				lv1 = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
					.eq(BasePO::getIsDeleted, 0)
					.eq(PFPTokenPO::getMintStatus, 1)
					.eq(PFPTokenPO::getOwnerUserId, userId)
					.eq(PFPTokenPO::getLevel, 1));
			}
			if (lv2 == null) {
				lv2 = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
					.eq(BasePO::getIsDeleted, 0)
					.eq(PFPTokenPO::getMintStatus, 1)
					.eq(PFPTokenPO::getOwnerUserId, userId)
					.eq(PFPTokenPO::getLevel, 2));
			}
			if (lv3 == null) {
				lv3 = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
					.eq(BasePO::getIsDeleted, 0)
					.eq(PFPTokenPO::getMintStatus, 1)
					.eq(PFPTokenPO::getOwnerUserId, userId)
					.eq(PFPTokenPO::getLevel, 3));
			}
			if (lv4 == null) {
				lv4 = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
					.eq(BasePO::getIsDeleted, 0)
					.eq(PFPTokenPO::getMintStatus, 1)
					.eq(PFPTokenPO::getOwnerUserId, userId)
					.eq(PFPTokenPO::getLevel, 4));
			}
			if (lv5 == null) {
				lv5 = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
					.eq(BasePO::getIsDeleted, 0)
					.eq(PFPTokenPO::getMintStatus, 1)
					.eq(PFPTokenPO::getOwnerUserId, userId)
					.eq(PFPTokenPO::getLevel, 5));
			}
		} else {
			// 如果lv1 2 3 4 5 为null，置为0
			if (lv1 == null) {
				lv1 = 0L;
			}
			if (lv2 == null) {
				lv2 = 0L;
			}
			if (lv3 == null) {
				lv3 = 0L;
			}
			if (lv4 == null) {
				lv4 = 0L;
			}
			if (lv5 == null) {
				lv5 = 0L;
			}
		}
		log.info("lv1:{},lv2:{},lv3:{},lv4:{},lv5:{}",lv1,lv2,lv3,lv4,lv5);

		Integer score = UserScoreServiceImpl.getScoreINFLUENCE(lensFollows,ttFollows,lv1,lv2,lv3,lv4,lv5);
		return R.data(score);
	}

	//即将弃用，由上方的scoreINFLUENCE替代
	@GetMapping("/scoreCharisma")
	@ApiOperation(value = "魅力（目前对接了lens + cc）")
	public  R<Integer> scoreCharisma(@ApiParam(value = "查Followers需要的address", required = true) @RequestParam(value = "address") String address) {

		String lensProfile = UserScoreServiceImpl.getLensProfileIdByAddress(address);
		int lensFollows = UserScoreServiceImpl.getLensFollows(lensProfile);

		int ccFollows = UserScoreServiceImpl.getCCFollowers(address);
		Integer score = UserScoreServiceImpl.getScoreCharisma(lensFollows,ccFollows);
		return R.data(score);
	}

//	@GetMapping("/getLevelByAddress")
//	@ApiOperation(value = "根据地址获取等级")
//	public R<Integer> getLevel(@ApiParam(value = "查level需要的address", required = true) @RequestParam(value = "address") String address) {
//
//		Integer charisma = scoreCharisma(address).getData();
//		Integer art = scoreArt(address).getData();
//		Integer courage = scoreCourage(address).getData();
//		Integer energy = scoreEnergy(address).getData();
//		Integer extroversion = scoreExtroversion(address).getData();
//		Integer wisdom = scoreWisdom(address).getData();
//
//		MemberPO memberPO = new MemberPO();
//		memberPO.setCourage(courage);
//		memberPO.setArt(art);
//		memberPO.setWisdom(wisdom);
//		memberPO.setEnergy(energy);
//		memberPO.setExtroversion(extroversion);
//		memberPO.setCharisma(charisma);
//
//		//计算总分
//		memberPO.countLevelScore();
//		//计算level
//		memberPO.countLevel();
//
//		NFTLevelEnum[] values = NFTLevelEnum.values();
//		for (NFTLevelEnum value : values) {
//			if(memberPO.getLevel() == value.getCode()){
//				return R.data(memberPO.getLevelScore(),value.getName());
//			}
//		}
//
//		return R.fail("获取失败");
//	}


	@GetMapping("/getLevel")
	@ApiOperation(value = "获取等级")
	public R<Integer> getLevel(@ApiParam(value = "charisma", required = true) @RequestParam(value = "charisma") Integer charisma,
								 @ApiParam(value = "extroversion", required = true) @RequestParam(value = "extroversion") Integer extroversion,
								 @ApiParam(value = "energy", required = true) @RequestParam(value = "energy") Integer energy,
								@ApiParam(value = "wisdom", required = true) @RequestParam(value = "wisdom") Integer wisdom,
								@ApiParam(value = "art", required = true) @RequestParam(value = "art") Integer art,
								@ApiParam(value = "courage", required = true) @RequestParam(value = "courage") Integer courage) {
//		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
		Proxy proxy = null;

		int levelScore = charisma + extroversion + energy + wisdom + art + courage;

		PFPTokenPO pfpTokenPO = new PFPTokenPO();
		pfpTokenPO.setLevelScore(levelScore);
		pfpTokenPO.countLevel();

		NFTLevelEnum[] values = NFTLevelEnum.values();
		for (NFTLevelEnum value : values) {
			if(pfpTokenPO.getLevel() == value.getCode()){
				return R.data(levelScore,value.getName());
			}
		}

		return R.fail("获取失败");
	}

	@GetMapping("/getMatch")
	@ApiOperation(value = "获取匹配度")
	public  R<Integer> getMatch(@ApiParam(value = "tag1_1", required = false) @RequestParam(value = "tag1_1",required = false) Integer tag1_1,
					   @ApiParam(value = "tag1_2", required = false) @RequestParam(value = "tag1_2",required = false) Integer tag1_2,
					   @ApiParam(value = "tag1_3", required = false) @RequestParam(value = "tag1_3",required = false) Integer tag1_3,
					   @ApiParam(value = "charisma1", required = true) @RequestParam(value = "charisma1") Integer charisma1,
					   @ApiParam(value = "extroversion1", required = true) @RequestParam(value = "extroversion1") Integer extroversion1,
					   @ApiParam(value = "energy1", required = true) @RequestParam(value = "energy1") Integer energy1,
					   @ApiParam(value = "wisdom1", required = true) @RequestParam(value = "wisdom1") Integer wisdom1,
					   @ApiParam(value = "art1", required = true) @RequestParam(value = "art1") Integer art1,
					   @ApiParam(value = "courage1", required = true) @RequestParam(value = "courage1") Integer courage1,
					   @ApiParam(value = "tag2_1", required = false) @RequestParam(value = "tag2_1",required = false) Integer tag2_1,
					   @ApiParam(value = "tag2_2", required = false) @RequestParam(value = "tag2_2",required = false) Integer tag2_2,
					   @ApiParam(value = "tag2_3", required = false) @RequestParam(value = "tag2_3",required = false) Integer tag2_3,
					   @ApiParam(value = "charisma2", required = true) @RequestParam(value = "charisma2") Integer charisma2,
					   @ApiParam(value = "extroversion2", required = true) @RequestParam(value = "extroversion2") Integer extroversion2,
					   @ApiParam(value = "energy2", required = true) @RequestParam(value = "energy2") Integer energy2,
					   @ApiParam(value = "wisdom2", required = true) @RequestParam(value = "wisdom2") Integer wisdom2,
					   @ApiParam(value = "art2", required = true) @RequestParam(value = "art2") Integer art2,
					   @ApiParam(value = "courage2", required = true) @RequestParam(value = "courage2") Integer courage2) {
//		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
		Proxy proxy = null;

		String userTags = "";
		String nftTags = "";

		if(tag1_1 != null){
			userTags = userTags + tag1_1 + ",";
		}
		if(tag1_2 != null){
			userTags = userTags + tag1_2 + ",";
		}
		if(tag1_3 != null){
			userTags = userTags + tag1_3 + ",";
		}
		if(userTags.length() > 0){
			userTags.substring(0,userTags.length()-1);
		}

		if(tag2_1 != null){
			nftTags = nftTags + tag2_1 + ",";
		}
		if(tag2_2 != null){
			nftTags = nftTags + tag2_2 + ",";
		}
		if(tag2_3 != null){
			nftTags = nftTags + tag2_3 + ",";
		}
		if(nftTags.length() > 0){
			nftTags.substring(0,nftTags.length()-1);
		}

		int match = ScoreUtil.getMatch(userTags,
			charisma1,
			extroversion1,
			energy1,
			wisdom1,
			art1,
			courage1,
			nftTags,
			charisma2,
			extroversion2,
			energy2,
			wisdom2,
			art2,
			courage2);

		return R.data(match);
	}

}
