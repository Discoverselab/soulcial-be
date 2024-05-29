package org.springblade.modules.admin.service.impl;

//import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springblade.core.tool.api.R;
import org.springblade.modules.admin.config.Web3jConfig;
import org.springblade.modules.admin.dao.*;
import org.springblade.modules.admin.pojo.enums.RewardMessageEnum;
import org.springblade.modules.admin.pojo.po.*;
import org.springblade.modules.admin.service.ETHService;
import org.springblade.modules.admin.service.MemberConnectService;
import org.springblade.modules.admin.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.tx.Contract;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;

@Component
@Slf4j
public class ETHServiceImpl implements ETHService {


	@Autowired
	@Qualifier("ethWeb3j")
	private Web3j web3j;

	@Autowired
	@Qualifier("ethMainWeb3j")
	private Web3j ethMainWeb3j;

	@Autowired
	private PFPPickMapper pfpPickMapper;

	@Autowired
	private PFPTokenMapper pfpTokenMapper;

	@Autowired
	private PFPTransactionMapper pfpTransactionMapper;

	@Autowired
	private PFPHistoryMapper pfpHistoryMapper;

	@Value("${spring.profiles.active}")
	List<String> activeProfiles;

	@Autowired
	MessageMapper messageMapper;

	@Autowired
	WallectHistoryMapper wallectHistoryMapper;

	@Autowired
	MemberMapper memberMapper;

	@Autowired
	VSoulHistoryMapper vSoulHistoryMapper;

	@Autowired
	UserVSoulMapper userVSoulMapper;

	@Autowired
	MemberConnectService memberConnectService;

//	@Autowired
//	RedisTemplate<String,Object> redisTemplate;

	//	private final static BigInteger gasLimit = new BigInteger("5500000");
	private final static BigInteger gasLimit = Contract.GAS_LIMIT;
	//private final static BigInteger gasLimit = new BigInteger("3000000");
	private final static BigInteger gasPrice = Contract.GAS_PRICE;


//    @Autowired
//    private JsonRpcHttpClient jsonrpcClient;

	@Value("${contract.newadminPassword}")
	private String coinCreatePwd;

	@Value("${contract.newadminAddress}")
	private String newadminAddress;

//	private final static String coinCreatePwd = "discoverse2022";

	// 弃用，使用配置文件
	//	private static final String coinKeystorePathWindows = "E:/work/coin";
//	private static final String coinKeystorePathMacOs = "/Users/wangyukai/Desktop/项目/Soulcial/work/coin";
//	private static final String coinKeystorePathLinux = "/opt/peic/keystore/bnb";
//	private static final String coinWalletFile = "UTC--2023-06-05T15-42-39.669000000Z--e52e23326668117034a0ec6a288e5bb117b7f2c6.json";

	@Value("${contract.newkeystorePath}")
	private String coinKeystorePath;
	@Value("${contract.newwalletFile}")
	private String coinWalletFile;
	private static final String coinWalletFile2 = "UTC--2023-06-22T18-49-03.182000000Z--ad028d3bf652ddab9a7f46d73a20ee24c672e656.json";

	//代币合约地址
	private static final String tokenContractAddress = "";

	//admin钱包地址
	private static final String adminAddress = "0xe52e23326668117034a0ec6a288e5bb117b7f2c6";

	//BSC合约地址
//	private static final String contractAddress = "0x37a7860b29ffF81CDE90C9F1cB741186D3290A0D";

	//ETH合约地址
	private static final String contractAddress = "0xa947AF197bD5105d7f7C454139215fc37829cc86";

	//ETH测试链-sepolia  wETH 合约地址
//	private static final String wEthContractAddress = "0x7b79995e5f793A07Bc00c21412e50Ecae098E7f9";
	@Value("${contract.wrappedAddress}")
	private String wEthContractAddress;

	//zksync链合约地址
//	private static final String contractAddress = "0xB9Aa0f4b7Bbf8A172D637d951bed72ce47A53486";

	@Value("${contract.linkRate}")
	private BigDecimal linkRate;

	@Autowired
	private FansMapper fansMapper;

	@Override
	public R checkApprove(Long tokenId, String address) {
		try {

			//加載NFT
//			LeaveMsg contract = loadAdminContract();

			Credentials credentials = loadCredentials();
			//獲取gasprice
//			BigInteger gasPrice = getGasPrice();
//			log.info("獲取gasPrice成功：" + gasPrice);

			BigInteger gasPrice = Contract.GAS_PRICE;
			ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);
			//加載NFT
			LeaveMsg contract = LeaveMsg.load(contractAddress, web3j, credentials, gasProvider);
			log.info("加載NFT成功：" + contract);

			BigInteger token_id = new BigInteger(tokenId.toString());

			//admin地址
			String admin_address = contract.admin().send();

			//链上持有者地址
			String owner_address = contract.ownerOf(token_id).send();
			if (!address.equalsIgnoreCase(owner_address)) {
				return R.fail("check owner failed:This nft's owner is not you!");
			}

			//授权地址
			String approved_address = contract.getApproved(token_id).send();
			if (!admin_address.equalsIgnoreCase(approved_address)) {
				return R.fail("check approved failed:This nft has not approved!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return R.success("check success");
	}

	@Override
	public R<Boolean> checkBNBTransacation(String txn, BigDecimal price, String fromAddress, String toAddress) {
		try {
			EthTransaction transaction = web3j.ethGetTransactionByHash(txn).send();
			if (transaction != null && transaction.getTransaction() != null && transaction.getTransaction().get() != null) {
				Transaction tx = transaction.getTransaction().get();
				if (!tx.getBlockHash().equalsIgnoreCase("0x0000000000000000000000000000000000000000000000000000000000000000")) {
					EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(txn).send();
					if (receipt != null && receipt.getTransactionReceipt() != null
						&& receipt.getTransactionReceipt().get() != null
						&& ("0x1").equalsIgnoreCase(receipt.getTransactionReceipt().get().getStatus())) {
						//校验付款方、收款方
						if (!tx.getFrom().equalsIgnoreCase(fromAddress) || !tx.getTo().equalsIgnoreCase(toAddress)) {
							log.info("付款方、收款方 不正确：from=" + tx.getFrom() + ";to=" + tx.getTo());
							return R.data(false);
						}

						BigInteger value = tx.getValue();

						if (value != null) {
							//转账金额
							System.out.println("value:" + value);
							BigDecimal transValue = Convert.fromWei(value.toString(), Convert.Unit.ETHER);
							//金额相等
							if (transValue.compareTo(price) == 0) {
								return R.data(true);
							}
						}
						return R.data(false);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return R.fail("Exception");
		}
		return R.fail("not success");
	}

	@Override
	public R<Boolean> checkWBNBTransacation(String txn, BigDecimal price, String fromAddress, String toAddress) {
		try {
			EthTransaction transaction = web3j.ethGetTransactionByHash(txn).send();
			if (transaction != null && transaction.getTransaction() != null && transaction.getTransaction().get() != null) {
				Transaction tx = transaction.getTransaction().get();
				if (!tx.getBlockHash().equalsIgnoreCase("0x0000000000000000000000000000000000000000000000000000000000000000")) {
					EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(txn).send();
					if (receipt != null && receipt.getTransactionReceipt() != null
						&& receipt.getTransactionReceipt().get() != null
						&& ("0x1").equalsIgnoreCase(receipt.getTransactionReceipt().get().getStatus())) {

						String input = tx.getInput();
						String data = input.substring(10);
						Function function = new Function(
							"transfer",
							Arrays.asList(),
							Arrays.asList(new TypeReference<Address>() {
										  },
								new TypeReference<Uint256>() {
								})
						);
						//付款地址
						String from = tx.getFrom();

						List<Type> params = FunctionReturnDecoder.decode(data, function.getOutputParameters());
						// 收款地址
						String to = params.get(0).getValue().toString().toLowerCase();
						// NFTID
						String amount = params.get(1).getValue().toString();

						//校验付款方、收款方
						if (!from.equalsIgnoreCase(fromAddress) || !to.equalsIgnoreCase(toAddress)) {
							log.info("付款方、收款方 不正确：from=" + tx.getFrom() + ";to=" + tx.getTo());
							return R.data(false);
						}


						BigInteger value = new BigInteger(amount);

						if (value != null) {
							//转账金额
							System.out.println("value:" + value);
							BigDecimal transValue = Convert.fromWei(value.toString(), Convert.Unit.ETHER);
							//金额相等
							if (transValue.compareTo(price) == 0) {
								return R.data(true);
							}
						}
						return R.data(false);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return R.fail("Exception");
		}
		return R.fail("not success");
	}

	@Override
	public R<String> approveTransferNFT(String fromAddress, String toAddress, Long tokenId) {
		try {
			System.out.println("fromAddress:" + fromAddress);
			System.out.println("toAddress:" + toAddress);
			System.out.println("tokenId:" + tokenId);

//			LeaveMsg leaveMsg = loadAdminContract();
			Credentials credentials = loadCredentials();
			//獲取gasprice
			BigInteger gasPrice = getGasPrice();
			log.info("獲取gasPrice成功：" + gasPrice);
			ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);
			//加載NFT
			LeaveMsg contract = LeaveMsg.load(contractAddress, web3j, credentials, gasProvider);
			log.info("加載NFT成功：" + contract);

			BigInteger token_id = new BigInteger(tokenId.toString());

			//轉賬
			TransactionReceipt receipt = contract.transferFrom(fromAddress, toAddress, token_id).send();
			//獲取交易hash
			String transactionHash = receipt.getTransactionHash();
			log.info("approveTransferNFT:transactionHash：" + transactionHash);
			if (transactionHash == null) {
				return R.fail("transfer nft failed");
			} else {
				return R.data(transactionHash);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return R.fail("exception");
		}
	}

	@Override
	public Boolean checkNFTOwner(String toAddress, Long tokenId) {
		try {
//			LeaveMsg leaveMsg = loadAdminContract();

			Credentials credentials = loadCredentials();
			//獲取gasprice
//			BigInteger gasPrice = getGasPrice();
//			log.info("獲取gasPrice成功：" + gasPrice);

			BigInteger gasPrice = Contract.GAS_PRICE;
			ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);
			//加載NFT
			LeaveMsg contract = LeaveMsg.load(contractAddress, web3j, credentials, gasProvider);
			log.info("加載NFT成功：" + contract);

			BigInteger token_id = new BigInteger(tokenId.toString());

			String owner = contract.ownerOf(token_id).send();
			if (toAddress.equalsIgnoreCase(owner)) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public String createNewWallet(String password) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException, CipherException {
		log.info("====>  Generate new wallet file for BNB.");
		String fileName = WalletUtils.generateNewWalletFile(password, new File(coinKeystorePath), true);
		Credentials credentials = WalletUtils.loadCredentials(password, coinKeystorePath + "/" + fileName);
		String address = credentials.getAddress();
		return address;
	}

	/**
	 * 同步余额
	 */
	public void syncAddressBalance(String address) throws IOException {
		BigDecimal balance = getBalance(address);
	}


//    public R transferFromWithdrawWallet(String toAddress, BigDecimal amount, boolean sync, String withdrawId) {
//        Account account = accountService.findByName("admin");
//        Optional.ofNullable(account).orElseThrow(() -> new RuntimeException("賬戶信息異常，請聯繫管理員[C001]"));
//        return transfer(coin.getKeystorePath() + "/" + account.getWalletFile(), coin.getWithdrawWalletPassword(), toAddress, amount, sync, withdrawId);
//    }

	public R transfer(String walletFile, String password, String toAddress, BigDecimal amount, boolean sync, String withdrawId) {
		Credentials credentials;
		try {
			credentials = WalletUtils.loadCredentials(password, walletFile);
		} catch (IOException e) {
			log.error("transfer{}", e);
			// 密钥文件异常
			return R.fail(500, "賬戶信息異常，請聯繫管理員[C002]");
		} catch (CipherException e) {
			log.error("transfer{}", e);
			// 解密失败
			return R.fail(500, "賬戶信息異常，請聯繫管理員[C003]");
		}
//        if (sync) {
//            return paymentHandler.transferBNB(credentials, toAddress, amount);
//        } else {
//            paymentHandler.transferBNBAsync(credentials, toAddress, amount, withdrawId);
//            return new MessageResult(0, "提交成功");
//        }
		return R.success("成功");
	}

	public BigDecimal getBalance(String address) throws IOException {
		EthGetBalance getBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
		return Convert.fromWei(getBalance.getBalance().toString(), Convert.Unit.ETHER);
	}

	public BigInteger getGasPrice() throws IOException {
		EthGasPrice gasPrice = web3j.ethGasPrice().send();
		BigInteger baseGasPrice = gasPrice.getGasPrice();

//		BigInteger baseGasPrice = Contract.GAS_PRICE;

//        return new BigDecimal(baseGasPrice).multiply(coin.getGasSpeedUp()).toBigInteger();
		return baseGasPrice;
	}

	public R transferToken(String fromAddress, String toAddress, BigDecimal amount, boolean sync) {
//        BNBAccountDto account = bnbAccountService.findByAddress(fromAddress);\
		//TODO
		String walletFile = "";

		Credentials credentials;
		try {
			credentials = WalletUtils.loadCredentials(coinCreatePwd, coinKeystorePath + "/" + walletFile);
		} catch (IOException e) {
			log.error("transferToken{}", e);
			// 密钥文件异常
			return R.fail(500, "賬戶信息異常，請聯繫管理員[C004]");
		} catch (CipherException e) {
			log.error("transferToken{}", e);
			// 解密失败
			return R.fail(500, "賬戶信息異常，請聯繫管理員[C005]");
		}
//        if (sync) {
//            return paymentHandler.transferToken(credentials, toAddress, amount);
//        } else {
//            paymentHandler.transferTokenAsync(credentials, toAddress, amount, "");
//            return new MessageResult(0, "提交成功");
//        }
		return R.success("成功");
	}

//    public R transferTokenFromWithdrawWallet(String toAddress, BigDecimal amount, boolean sync, String withdrawId) {
//        Credentials credentials;
//		//TODO
//		String WithdrawWalletPassword = "";
//
//        try {
//            //解锁提币钱包
//            credentials = WalletUtils.loadCredentials(WithdrawWalletPassword, coinKeystorePath + "/" + coinWithdrawWallet);
//        } catch (IOException e) {
//            log.error("transferTokenFromWithdrawWallet{}", e);
//            // 密钥文件异常
//            return R.fail(500, "賬戶信息異常，請聯繫管理員[C006]");
//        } catch (CipherException e) {
//            log.error("transferTokenFromWithdrawWallet{}", e);
//            // 解密失败
//            return R.fail(500, "賬戶信息異常，請聯繫管理員[C007]");
//        }
////        if (sync) {
////            return paymentHandler.transferToken(credentials, toAddress, amount);
////        } else {
////            paymentHandler.transferTokenAsync(credentials, toAddress, amount, withdrawId);
////            return new MessageResult(0, "提交成功");
////        }
//		return R.success("成功");
//    }


//    public BigDecimal getTokenBalance(String address) throws IOException {
//        BigInteger balance = BigInteger.ZERO;
//        Function fn = new Function("balanceOf", Arrays.asList(new org.web3j.abi.datatypes.Address(address)), Collections.emptyList());
//        String data = FunctionEncoder.encode(fn);
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("to", tokenContractAddress);
//        map.put("data", data);
//        try {
//            String methodName = "eth_call";
//            Object[] params = new Object[]{map, "latest"};
//            String result = jsonrpcClient.invoke(methodName, params, Object.class).toString();
//            if (StringUtils.isNotEmpty(result)) {
//                if ("0x".equalsIgnoreCase(result) || result.length() == 2) {
//                    result = "0x0";
//                }
//                balance = Numeric.decodeQuantity(result);
//            }
//        } catch (Throwable e) {
//            e.printStackTrace();
//            log.info("查询接口ERROR");
//        }
//        return EthConvert.fromWei(new BigDecimal(balance), contract.getUnit());
//    }

	public Boolean isTransactionSuccess(String txid) throws IOException {
		EthTransaction transaction = web3j.ethGetTransactionByHash(txid).send();
		try {
			if (transaction != null && transaction.getTransaction().get() != null) {
				Transaction tx = transaction.getTransaction().get();
				if (!tx.getBlockHash().equalsIgnoreCase("0x0000000000000000000000000000000000000000000000000000000000000000")) {
					EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(txid).send();
					if (receipt != null && receipt.getTransactionReceipt().get().getStatus().equalsIgnoreCase("0x1")) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}


	public String restTemplateForHttpUrl(String url, Map params) {
		RestTemplate restTemplate = new RestTemplate();
		//入参及头文件
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
		HttpEntity httpEntity = new HttpEntity(params, headers);
		restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
		return restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class).getBody();
	}

	/**
	 * 获取订单状态
	 *
	 * @param txid
	 * @return status 0:未到账 1：已到账 2：失败
	 */
	public Map<String, Object> getTransactionStatus(String txid) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			EthTransaction transaction = web3j.ethGetTransactionByHash(txid).send();
			if (transaction != null && transaction.getTransaction().get() != null) {
				Transaction tx = transaction.getTransaction().get();
				BigInteger gasPrice = tx.getGasPrice();
				if (!tx.getBlockHash().equalsIgnoreCase("0x0000000000000000000000000000000000000000000000000000000000000000")) {
					EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(txid).send();
					if (receipt != null) {
						TransactionReceipt transactionReceipt = receipt.getTransactionReceipt().get();
						BigInteger gasUsed = transactionReceipt.getGasUsed();

						BigInteger feeInt = gasPrice.multiply(gasUsed);
						BigDecimal realFee = (new BigDecimal(feeInt.toString())).divide(Convert.Unit.ETHER.getWeiFactor());
						map.put("realFee", realFee);

						String status = transactionReceipt.getStatus();
						if (status.equalsIgnoreCase("0x1")) {
							//已到账
							map.put("status", "1");
							return map;
						} else if (status.equalsIgnoreCase("0x0")) {
							//未到账
							map.put("status", "0");
							return map;
						} else {
							//失败
							map.put("status", "2");
							return map;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			map.put("status", "0");
			return map;
		}
		map.put("status", "0");
		return map;
	}


	@Override
	public String createAdminWallet() {
		String walletAddress = "";
		try {
			walletAddress = createNewWallet(coinCreatePwd);
		} catch (Exception e) {
		}
		return walletAddress;
	}

	@Override
	public synchronized R<String> mintNFT(String adminAddress, String contractAddress, String toAddress, Long chainId, Long tokenId) {
		try {
//			LeaveMsg contract = loadAdminContract();

			Credentials credentials = loadCredentials();
			// 打印签名
			String address = credentials.getAddress();
			String ecKeyPair = credentials.getEcKeyPair().toString();
			System.out.println("Credentials address:" + address);
			System.out.println("Credentials ecKeyPair:" + ecKeyPair);
			//獲取gasprice
			BigInteger gasPrice = getGasPrice();
			// op 网
//			BigInteger gasPrice = Convert.toWei("1", Convert.Unit.GWEI).toBigIntegerExact();
			log.info("獲取gasPrice成功：" + gasPrice + "contractAddress:" + contractAddress + "toAddress:" + toAddress + "tokenId:" + tokenId + "chainId:" + chainId);
			ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);


			BigInteger nonce = web3j.ethGetTransactionCount(newadminAddress, DefaultBlockParameterName.LATEST)
				.send()
				.getTransactionCount();
			//加載NFT,使用op网的goerli测试链
			TransactionManager transactionManager = new RawTransactionManager(
				web3j, credentials, chainId);

			Soultest contract = Soultest.load(contractAddress, web3j, transactionManager, gasProvider);
			log.info("加載NFT成功：" + contract);

			//校驗擁有者
//			TransactionReceipt send = contract.admin().send();

			System.out.println("adminAddress:" + adminAddress);
//			System.out.println("fromAddress:" + fromAddress);
			//System.out.println("approvedAddress:"+approvedAddress);
//			if (!adminAddress.equals(fromAddress)) {
//				logger.info("fromAddress不是admin");
//				return new MessageResult(500, "付款地址不是admin");
//			}
			BigInteger productId = new BigInteger(tokenId.toString());
			//鑄造
			log.info("調用safeMint前：toAddress=" + toAddress + ";productId=" + productId);

			TransactionReceipt receipt = contract.safeMint(toAddress, productId).send();
			log.info("調用safeMint成功：" + receipt);
			//獲取交易hash
			String transactionHash = receipt.getTransactionHash();
			log.info("==============NFT鑄造生成交易hash：" + transactionHash);
			if (transactionHash == null) {
				System.out.println("铸造失败");
			} else {
				System.out.println("铸造成功" + transactionHash);
				return R.data(transactionHash);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return R.fail("mint failed please try again later");
	}

	public BigInteger getCurrentNonce(String adminAddress) {
		// 在这里获取和返回你的 nonce，你可以调用 web3j.ethGetTransactionCount(...)
		EthGetTransactionCount ethGetTransactionCount = null;
		try {
			ethGetTransactionCount = web3j.ethGetTransactionCount(adminAddress, DefaultBlockParameterName.PENDING).send();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ethGetTransactionCount.getTransactionCount();
	}


//	private LeaveMsg loadAdminContract() throws Exception{
//
//		//獲取密鑰
//		Credentials credentials = loadCredentials();
//		//獲取gasprice
//		BigInteger gasPrice = getGasPrice();
//		log.info("獲取gasPrice成功：" + gasPrice);
//		ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);
//		//加載NFT
//		LeaveMsg contract = LeaveMsg.load(contractAddress, web3j, credentials, gasProvider);
//		log.info("加載NFT成功：" + contract);
//
//		return contract;
//	}

	private Credentials loadCredentials() throws Exception {
		// 弃用，改用配置文件管理
//		String coinKeystorePath = null;
//		if (activeProfiles != null && activeProfiles.size() > 0) {
//			if (activeProfiles.get(0).equalsIgnoreCase("dev")) {
//				coinKeystorePath = coinKeystorePathMacOs;
//			} else {
//				coinKeystorePath = coinKeystorePathLinux;
//			}
//		}

		//獲取密鑰文件
		String walletFile = coinKeystorePath + "/" + coinWalletFile;
		//獲取密鑰
		Credentials credentials = WalletUtils.loadCredentials(coinCreatePwd, walletFile);

		return credentials;
	}

	@Override
	public R<String> transferBNB(String toAddress, BigDecimal amount) {
		try {
			Credentials credentials = loadCredentials();

			EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.PENDING)
				.sendAsync()
				.get();

			BigInteger nonce = ethGetTransactionCount.getTransactionCount();
			BigInteger gasPrice = getGasPrice();
			BigInteger value = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();

			log.info("value={},gasPrice={},gasLimit={},nonce={},address={}", value, gasPrice, gasLimit, nonce, toAddress);
			RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
				nonce, gasPrice, gasLimit, toAddress, value);

			//TODO ETH测试链
			Long chainId = Web3jConfig.ETH_SEPOLIA_CHAIN_ID;

			byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
			String hexValue = Numeric.toHexString(signedMessage);
			EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
			String transactionHash = ethSendTransaction.getTransactionHash();
			log.info("txid = {}", transactionHash);
			if (StringUtils.isEmpty(transactionHash)) {
				return R.fail("transfer bnb failed");
			} else {
				return R.data(transactionHash);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return R.fail("exception");
		}
	}

	@Override
	public R<String> wEthTransferFrom(String fromAddress, String toAddress, BigDecimal amount) {
		try {

			Credentials credentials = loadCredentials();
			//獲取gasprice
			BigInteger gasPrice = getGasPrice();
			log.info("獲取gasPrice成功：" + gasPrice);
			ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);

			//加載 wETH
			WEthContract contract = WEthContract.load(wEthContractAddress, web3j, credentials, gasProvider);
			log.info("加載wETH成功：" + contract);

//			BigInteger allowance = contract.allowance(fromAddress, credentials.getAddress()).send();
//			System.out.println("allowance:"+allowance);
			BigInteger transferAmount = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
			System.out.println("transferAmount:" + transferAmount);

//			if(allowance.compareTo(transferAmount) < 0){
//				return R.fail("allowance is not enough");
//			}

			log.info("調用transferFrom前：fromAddress=" + fromAddress + ";toAddress=" + toAddress + ";transferAmount=" + transferAmount);
			TransactionReceipt receipt = contract.transferFrom(fromAddress, toAddress, transferAmount).send();
			log.info("調用transferFrom成功：" + receipt);

			//獲取交易hash
			String transactionHash = receipt.getTransactionHash();
			log.info("=============調用transferFrom成功生成交易hash：" + transactionHash);
			if (transactionHash != null) {
				return R.data(transactionHash);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return R.fail("transfer wETH failed");
	}

	public R<String> wEthAllowTransferFrom(String fromAddress, String toAddress, BigDecimal amount) {
		try {

			Credentials credentials = loadCredentials();
			//獲取gasprice
			BigInteger gasPrice = getGasPrice();
			log.info("獲取gasPrice成功：" + gasPrice);
			ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);

			//加載 wETH
			WEthContract contract = WEthContract.load(wEthContractAddress, web3j, credentials, gasProvider);
			log.info("加載wETH成功：" + contract);

			BigInteger allowance = contract.allowance(fromAddress, credentials.getAddress()).send();
			System.out.println("allowance:" + allowance);
			BigInteger transferAmount = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
			System.out.println("transferAmount:" + transferAmount);

			if (allowance.compareTo(transferAmount) < 0) {
				return R.fail("allowance is not enough");
			}

			log.info("調用transferFrom前：fromAddress=" + fromAddress + ";toAddress=" + toAddress + ";transferAmount=" + transferAmount);
			TransactionReceipt receipt = contract.transferFrom(fromAddress, toAddress, transferAmount).send();
			log.info("調用transferFrom成功：" + receipt);

			//獲取交易hash
			String transactionHash = receipt.getTransactionHash();
			log.info("=============調用transferFrom成功生成交易hash：" + transactionHash);
			if (transactionHash != null) {
				return R.data(transactionHash);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return R.fail("transfer wETH failed");
	}

	@Override
	public BigInteger getWEthAllowance(String fromAddress) {
		try {

			Credentials credentials = loadCredentials();
			//獲取gasprice
			BigInteger gasPrice = getGasPrice();
			log.info("獲取gasPrice成功：" + gasPrice);
			ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);

			//加載 wETH
			WEthContract contract = WEthContract.load(wEthContractAddress, web3j, credentials, gasProvider);
			log.info("加載wETH成功：" + contract);

			BigInteger allowance = contract.allowance(fromAddress, credentials.getAddress()).send();
			System.out.println("allowance:" + allowance);

			return allowance;
		} catch (Exception e) {
			log.info("获取allowance失败");
			e.printStackTrace();
		}
		return BigInteger.ZERO;
	}

	@Override
	public Long getLastBlockHeight() {
		Long height = null;

		try {
			EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
			height = blockNumber.getBlockNumber().longValue();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return height;
	}

	@Override
	public Long getLastMainBlockHeight() {
		Long height = null;

		try {
			EthBlockNumber blockNumber = ethMainWeb3j.ethBlockNumber().send();
			height = blockNumber.getBlockNumber().longValue();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return height;
	}

	/**
	 * NFT开奖
	 *
	 * @param pickId
	 * @return
	 */
	@Override
	public R rewardNFT(Long pickId) {
		try {
			PFPPickPO pfpPickPO = pfpPickMapper.selectById(pickId);
			Long tokenId = pfpPickPO.getTokenId();
//			PFPTokenPO pfpTokenPO = pfpTokenMapper.selectById(tokenId);
			PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(tokenId);
			//开奖区块行高
			Long blockHeight = pfpPickPO.getRewardBlockHeight();

			//获取区块信息
			EthBlock block = ethMainWeb3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(blockHeight), true).send();
			String hash = block.getBlock().getHash();
			Integer rewardResult = getRewardResult(hash);
			log.info("开奖结果：行高{}，哈希{},中奖签号{}", blockHeight, hash, rewardResult);

			String ownerAddress = pfpTokenPO.getOwnerAddress();
			Long ownerUserId = pfpTokenPO.getOwnerUserId();
			String toAddress = null;
			Long toUserId = null;
			if (rewardResult == 0) {
				toAddress = pfpPickPO.getIndexAddress0();
				toUserId = pfpPickPO.getIndexUserId0();
			} else if (rewardResult == 1) {
				toAddress = pfpPickPO.getIndexAddress1();
				toUserId = pfpPickPO.getIndexUserId1();
			} else if (rewardResult == 2) {
				toAddress = pfpPickPO.getIndexAddress2();
				toUserId = pfpPickPO.getIndexUserId2();
			} else if (rewardResult == 3) {
				toAddress = pfpPickPO.getIndexAddress3();
				toUserId = pfpPickPO.getIndexUserId3();
			}

			//pfpPick中奖设置
			pfpPickPO.setRewardIndex(rewardResult);
			pfpPickPO.setRewardBlockHash(hash);
			pfpPickPO.setRewardTime(new Date());
			pfpPickPO.setRewardUserId(toUserId);
			pfpPickPO.setRewardUserAddress(toAddress);

			//创建订单
			PFPTransactionPO pfpTransactionPO = new PFPTransactionPO();
			pfpTransactionPO.setTokenId(tokenId);
			pfpTransactionPO.setAdminAddress(pfpTokenPO.getAdminAddress());
			pfpTransactionPO.setLinkType(pfpTokenPO.getLinkType());
			pfpTransactionPO.setNetwork(pfpTokenPO.getNetwork());
			pfpTransactionPO.setContractAddress(pfpTokenPO.getContractAddress());
			pfpTransactionPO.setContractName(pfpTokenPO.getContractName());
			pfpTransactionPO.setFromAddress(ownerAddress);
			pfpTransactionPO.setToAddress(toAddress);
			pfpTransactionPO.setFromUserId(ownerUserId);
			pfpTransactionPO.setToUserId(toUserId);
//		交易状态：0-未交易 1-已付款未交易PFP 2-交易完成 3-交易取消
			pfpTransactionPO.setTransactionStatus(2);
			pfpTransactionPO.setPayIndex(rewardResult);
//		pfpTransactionPO.setPfpTxnHash();
			pfpTransactionPO.setMintUserId(pfpTokenPO.getMintUserId());
			pfpTransactionPO.setMintUserAddress(pfpTokenPO.getMintUserAddress());

			//计算费用
			pfpTransactionPO.setListPrice(pfpTokenPO.getPrice());

			pfpTransactionPO.initForInsertNoAuth();
			pfpTransactionPO.setPickId(pickId);
			pfpTransactionMapper.insert(pfpTransactionPO);

			//添加双方用户为star连接
			memberConnectService.addStarConnected(ownerUserId, toUserId);

			//添加history
			PFPHistoryPO pfpHistoryPO = new PFPHistoryPO();
			pfpHistoryPO.setTokenId(pfpTokenPO.getId());
			//被购买
			pfpHistoryPO.setType(1);
			pfpHistoryPO.setTransactionId(pfpTransactionPO.getId());
			pfpHistoryPO.setAdminAddress(pfpTokenPO.getAdminAddress());
			pfpHistoryPO.setLinkType(pfpTokenPO.getLinkType());
			pfpHistoryPO.setNetwork(pfpTokenPO.getNetwork());
			pfpHistoryPO.setContractAddress(pfpTokenPO.getContractAddress());
			pfpHistoryPO.setContractName(pfpTokenPO.getContractName());

			pfpHistoryPO.setFromAddress(pfpTransactionPO.getFromAddress());
			pfpHistoryPO.setToAddress(pfpTransactionPO.getToAddress());
			pfpHistoryPO.setFromUserId(pfpTransactionPO.getFromUserId());
			pfpHistoryPO.setToUserId(pfpTransactionPO.getToUserId());
//			pfpHistoryPO.setTxnHash(transferNFTTxn);
			pfpHistoryPO.setPrice(pfpTokenPO.getPrice());
			pfpHistoryPO.initForInsertNoAuth();

			pfpHistoryMapper.insert(pfpHistoryPO);

			//wBNB转账：卖NFT收益
			BigDecimal loserReward = PickUtil.getLoserReward(pfpTokenPO.getLevel(), pfpTokenPO.getTransactionsCount(), linkRate);
			BigDecimal minterReward = PickUtil.getMinterReward(pfpTokenPO.getLevel(), pfpTokenPO.getTransactionsCount(), linkRate);
			BigDecimal sellerReward = PickUtil.getSellerReward(pfpTokenPO.getLevel(), pfpTokenPO.getTransactionsCount(), linkRate);
			log.info("售价{}，未中奖收益{},创作者收益{}", pfpTokenPO.getPrice(), loserReward, minterReward);

			//设置未中奖收益
			pfpPickPO.setRewardPrice(loserReward);
			//铸造者收益
			pfpPickPO.setMinterRewardPrice(minterReward);
			//卖家收益
			pfpPickPO.setSellerRewardPrice(sellerReward);

			//消息标题
			String title = "Launching SoulCast #" + tokenId + " Announced";
			//消息内容
			String content = "Check your rewards!";
			//中奖消息
			MessagePO message0 = new MessagePO(0, 1L, pfpPickPO.getIndexUserId0(), RewardMessageEnum.REWARD_FAILED.getName(), title, content, 0, tokenId, pickId);
			MessagePO message1 = new MessagePO(0, 1L, pfpPickPO.getIndexUserId1(), RewardMessageEnum.REWARD_FAILED.getName(), title, content, 0, tokenId, pickId);
			MessagePO message2 = new MessagePO(0, 1L, pfpPickPO.getIndexUserId2(), RewardMessageEnum.REWARD_FAILED.getName(), title, content, 0, tokenId, pickId);
			MessagePO message3 = new MessagePO(0, 1L, pfpPickPO.getIndexUserId3(), RewardMessageEnum.REWARD_FAILED.getName(), title, content, 0, tokenId, pickId);

			//钱包余额记录：退款: 4-refund
			WallectHistoryPO refundWallectHistory0 = new WallectHistoryPO(pfpPickPO.getIndexUserId0(), 4, pfpTransactionPO.getId(), null, pfpPickPO.getPrice());
			WallectHistoryPO refundWallectHistory1 = new WallectHistoryPO(pfpPickPO.getIndexUserId1(), 4, pfpTransactionPO.getId(), null, pfpPickPO.getPrice());
			WallectHistoryPO refundWallectHistory2 = new WallectHistoryPO(pfpPickPO.getIndexUserId2(), 4, pfpTransactionPO.getId(), null, pfpPickPO.getPrice());
			WallectHistoryPO refundWallectHistory3 = new WallectHistoryPO(pfpPickPO.getIndexUserId3(), 4, pfpTransactionPO.getId(), null, pfpPickPO.getPrice());

			//钱包余额记录：收益：2-earn
			WallectHistoryPO wallectHistory0 = new WallectHistoryPO(pfpPickPO.getIndexUserId0(), 2, pfpTransactionPO.getId(), null, loserReward);
			WallectHistoryPO wallectHistory1 = new WallectHistoryPO(pfpPickPO.getIndexUserId1(), 2, pfpTransactionPO.getId(), null, loserReward);
			WallectHistoryPO wallectHistory2 = new WallectHistoryPO(pfpPickPO.getIndexUserId2(), 2, pfpTransactionPO.getId(), null, loserReward);
			WallectHistoryPO wallectHistory3 = new WallectHistoryPO(pfpPickPO.getIndexUserId3(), 2, pfpTransactionPO.getId(), null, loserReward);

			//中奖积分
//			BigDecimal vSoulPrice0 = null;
//			BigDecimal vSoulPrice1 = null;
//			BigDecimal vSoulPrice2 = null;
//			BigDecimal vSoulPrice3 = null;

			//积分余额记录
			VSoulHistoryPO vSoulHistoryPO0 = new VSoulHistoryPO(pfpPickPO.getIndexUserId0(), 1, pickId, null);
			VSoulHistoryPO vSoulHistoryPO1 = new VSoulHistoryPO(pfpPickPO.getIndexUserId1(), 1, pickId, null);
			VSoulHistoryPO vSoulHistoryPO2 = new VSoulHistoryPO(pfpPickPO.getIndexUserId2(), 1, pickId, null);
			VSoulHistoryPO vSoulHistoryPO3 = new VSoulHistoryPO(pfpPickPO.getIndexUserId3(), 1, pickId, null);

			//退款金额
			BigDecimal refund = loserReward.add(pfpPickPO.getPrice());

//			String txn0 = null;txn1 = null,txn2 = null,txn3 = null;
////			String refundTxn0 = null,refundTxn1 = null,refundTxn2 = null,refundTxn3 = null;
			if (rewardResult == 0) {

				//计算中奖积分
				BigDecimal vSoulPrice0 = getVSoulPrice(1, pfpPickPO.getIndexUserId0(), pfpTokenPO);
				vSoulHistoryPO0.setVSoulPrice(vSoulPrice0);
				vSoulHistoryPO0.initForInsertNoAuth();
				vSoulHistoryMapper.insert(vSoulHistoryPO0);
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId0(), vSoulPrice0);

				message0.setMessage(RewardMessageEnum.REWARD_SUCCESS.getName());
				message0.initForInsertNoAuth();
				messageMapper.insert(message0);

				//计算中奖积分
				BigDecimal vSoulPrice1 = getVSoulPrice(0, pfpPickPO.getIndexUserId1(), pfpTokenPO);
				vSoulHistoryPO1.setVSoulPrice(vSoulPrice1);
				vSoulHistoryPO1.initForInsertNoAuth();
				vSoulHistoryMapper.insert(vSoulHistoryPO1);
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId1(), vSoulPrice1);

				message1.initForInsertNoAuth();
				messageMapper.insert(message1);

				//计算中奖积分
				BigDecimal vSoulPrice2 = getVSoulPrice(0, pfpPickPO.getIndexUserId2(), pfpTokenPO);
				vSoulHistoryPO2.setVSoulPrice(vSoulPrice2);
				vSoulHistoryPO2.initForInsertNoAuth();
				vSoulHistoryMapper.insert(vSoulHistoryPO2);
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId2(), vSoulPrice2);

				message2.initForInsertNoAuth();
				messageMapper.insert(message2);

				//计算中奖积分
				BigDecimal vSoulPrice3 = getVSoulPrice(0, pfpPickPO.getIndexUserId3(), pfpTokenPO);
				vSoulHistoryPO3.setVSoulPrice(vSoulPrice3);
				vSoulHistoryPO3.initForInsertNoAuth();
				vSoulHistoryMapper.insert(vSoulHistoryPO3);
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId3(), vSoulPrice3);

				message3.initForInsertNoAuth();
				messageMapper.insert(message3);

				//收益
				ThreadUtil.execAsync(new Runnable() {
					@Override
					public void run() {
						//给中签者转NFT
						R<String> transferNFTResult = approveTransferNFT(ownerAddress, pfpPickPO.getIndexAddress0(), tokenId);
						if (transferNFTResult.getCode() != 200) {
							log.error("=========transferNFTerror===========");
						}
						//NFT交易hash
						String transferNFTTxn = transferNFTResult.getData();
						pfpTransactionPO.setPfpTxnHash(transferNFTTxn);
						pfpPickPO.setNftTxn(transferNFTTxn);
						pfpHistoryPO.setTxnHash(transferNFTTxn);

						pfpHistoryMapper.updateById(pfpHistoryPO);

						String txn1 = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpPickPO.getIndexAddress1(), refund).getData();
						wallectHistory1.setTxnHash(txn1);
						wallectHistory1.initTime();
						wallectHistoryMapper.insert(wallectHistory1);

						refundWallectHistory1.setTxnHash(txn1);
						refundWallectHistory1.initTime();
						wallectHistoryMapper.insert(refundWallectHistory1);

						pfpPickPO.setIndexRewardTxn1(txn1);
						pfpPickMapper.updateById(pfpPickPO);

						String txn2 = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpPickPO.getIndexAddress2(), refund).getData();
						wallectHistory2.setTxnHash(txn2);
						wallectHistory2.initTime();
						wallectHistoryMapper.insert(wallectHistory2);

						refundWallectHistory2.setTxnHash(txn2);
						refundWallectHistory2.initTime();
						wallectHistoryMapper.insert(refundWallectHistory2);

						pfpPickPO.setIndexRewardTxn2(txn2);
						pfpPickMapper.updateById(pfpPickPO);


						String txn3 = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpPickPO.getIndexAddress3(), refund).getData();
						wallectHistory3.setTxnHash(txn3);
						wallectHistory3.initTime();
						wallectHistoryMapper.insert(wallectHistory3);

						refundWallectHistory3.setTxnHash(txn3);
						refundWallectHistory3.initTime();
						wallectHistoryMapper.insert(refundWallectHistory3);

						pfpPickPO.setIndexRewardTxn3(txn3);
						pfpPickMapper.updateById(pfpPickPO);

						//wBNB转账：铸造者收益
						String minterRewardTxn = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpTokenPO.getMintUserAddress(), minterReward).getData();
						//铸造者收益流水号
						pfpTransactionPO.setMinterMoneyTxnHash(minterRewardTxn);
						pfpPickPO.setMinterRewardTxn(minterRewardTxn);

						//铸造者收益记录
						WallectHistoryPO minterWallectHistory = new WallectHistoryPO(pfpTokenPO.getMintUserId(), 6, pfpTransactionPO.getId(), minterRewardTxn, minterReward);
						minterWallectHistory.initForInsertNoAuth();
						wallectHistoryMapper.insert(minterWallectHistory);

						//wBNB转账：卖出者收益
						String sellerRewardTxn = wEthTransferFrom(pfpTokenPO.getAdminAddress(), ownerAddress, sellerReward).getData();
						//卖出者收益流水号
						pfpTransactionPO.setSellerMoneyTxnHash(sellerRewardTxn);
						pfpPickPO.setSellerRewardTxn(sellerRewardTxn);

						pfpTransactionMapper.updateById(pfpTransactionPO);
						pfpPickMapper.updateById(pfpPickPO);

						//卖家收益记录
						WallectHistoryPO sellerWallectHistory = new WallectHistoryPO(ownerUserId, 5, pfpTransactionPO.getId(), sellerRewardTxn, sellerReward);
						sellerWallectHistory.initForInsertNoAuth();
						wallectHistoryMapper.insert(sellerWallectHistory);

						//collect粉丝
						FansPO fansPO = new FansPO(1, pfpPickPO.getIndexUserId0(), pfpTokenPO.getMintUserId(), tokenId, pickId);
						fansPO.initForInsertNoAuth();
						fansMapper.insert(fansPO);
					}
				});
			} else if (rewardResult == 1) {

				//计算中奖积分
				BigDecimal vSoulPrice0 = getVSoulPrice(0, pfpPickPO.getIndexUserId0(), pfpTokenPO);
				vSoulHistoryPO0.setVSoulPrice(vSoulPrice0);
				vSoulHistoryPO0.initForInsertNoAuth();
				vSoulHistoryMapper.insert(vSoulHistoryPO0);
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId0(), vSoulPrice0);

				message0.initForInsertNoAuth();
				messageMapper.insert(message0);

				//计算中奖积分
				BigDecimal vSoulPrice1 = getVSoulPrice(1, pfpPickPO.getIndexUserId1(), pfpTokenPO);
				vSoulHistoryPO1.setVSoulPrice(vSoulPrice1);
				vSoulHistoryPO1.initForInsertNoAuth();
				vSoulHistoryMapper.insert(vSoulHistoryPO1);
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId1(), vSoulPrice1);

				message1.setMessage(RewardMessageEnum.REWARD_SUCCESS.getName());
				message1.initForInsertNoAuth();
				messageMapper.insert(message1);

				//计算中奖积分
				BigDecimal vSoulPrice2 = getVSoulPrice(0, pfpPickPO.getIndexUserId2(), pfpTokenPO);
				vSoulHistoryPO2.setVSoulPrice(vSoulPrice2);
				vSoulHistoryPO2.initForInsertNoAuth();
				vSoulHistoryMapper.insert(vSoulHistoryPO2);
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId2(), vSoulPrice2);

				message2.initForInsertNoAuth();
				messageMapper.insert(message2);

				//计算中奖积分
				BigDecimal vSoulPrice3 = getVSoulPrice(0, pfpPickPO.getIndexUserId3(), pfpTokenPO);
				vSoulHistoryPO3.setVSoulPrice(vSoulPrice3);
				vSoulHistoryPO3.initForInsertNoAuth();
				vSoulHistoryMapper.insert(vSoulHistoryPO3);
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId3(), vSoulPrice3);

				message3.initForInsertNoAuth();
				messageMapper.insert(message3);

				ThreadUtil.execAsync(new Runnable() {
					@Override
					public void run() {
						//给中签者转NFT
						R<String> transferNFTResult = approveTransferNFT(ownerAddress, pfpPickPO.getIndexAddress1(), tokenId);
						if (transferNFTResult.getCode() != 200) {
							log.error("=========transferNFTerror===========");
						}
						//NFT交易hash
						String transferNFTTxn = transferNFTResult.getData();
						pfpTransactionPO.setPfpTxnHash(transferNFTTxn);
						pfpPickPO.setNftTxn(transferNFTTxn);
						pfpHistoryPO.setTxnHash(transferNFTTxn);

						pfpHistoryMapper.updateById(pfpHistoryPO);

						String txn0 = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpPickPO.getIndexAddress0(), refund).getData();
						wallectHistory0.setTxnHash(txn0);
						wallectHistory0.initTime();
						wallectHistoryMapper.insert(wallectHistory0);

						refundWallectHistory0.setTxnHash(txn0);
						refundWallectHistory0.initTime();
						wallectHistoryMapper.insert(refundWallectHistory0);

						pfpPickPO.setIndexRewardTxn0(txn0);
						pfpPickMapper.updateById(pfpPickPO);

						String txn2 = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpPickPO.getIndexAddress2(), refund).getData();
						wallectHistory2.setTxnHash(txn2);
						wallectHistory2.initTime();
						wallectHistoryMapper.insert(wallectHistory2);

						refundWallectHistory2.setTxnHash(txn2);
						refundWallectHistory2.initTime();
						wallectHistoryMapper.insert(refundWallectHistory2);

						pfpPickPO.setIndexRewardTxn2(txn2);
						pfpPickMapper.updateById(pfpPickPO);

						String txn3 = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpPickPO.getIndexAddress3(), refund).getData();
						wallectHistory3.setTxnHash(txn3);
						wallectHistory3.initTime();
						wallectHistoryMapper.insert(wallectHistory3);

						refundWallectHistory3.setTxnHash(txn3);
						refundWallectHistory3.initTime();
						wallectHistoryMapper.insert(refundWallectHistory3);

						pfpPickPO.setIndexRewardTxn3(txn3);
						pfpPickMapper.updateById(pfpPickPO);

						//wBNB转账：铸造者收益
						String minterRewardTxn = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpTokenPO.getMintUserAddress(), minterReward).getData();
						//铸造者收益流水号
						pfpTransactionPO.setMinterMoneyTxnHash(minterRewardTxn);
						pfpPickPO.setMinterRewardTxn(minterRewardTxn);

						//铸造者收益记录
						WallectHistoryPO minterWallectHistory = new WallectHistoryPO(pfpTokenPO.getMintUserId(), 6, pfpTransactionPO.getId(), minterRewardTxn, minterReward);
						minterWallectHistory.initForInsertNoAuth();
						wallectHistoryMapper.insert(minterWallectHistory);

						//wBNB转账：卖出者收益
						String sellerRewardTxn = wEthTransferFrom(pfpTokenPO.getAdminAddress(), ownerAddress, sellerReward).getData();
						//卖出者收益流水号
						pfpTransactionPO.setSellerMoneyTxnHash(sellerRewardTxn);
						pfpPickPO.setSellerRewardTxn(sellerRewardTxn);

						pfpTransactionMapper.updateById(pfpTransactionPO);
						pfpPickMapper.updateById(pfpPickPO);

						//卖家收益记录
						WallectHistoryPO sellerWallectHistory = new WallectHistoryPO(ownerUserId, 5, pfpTransactionPO.getId(), sellerRewardTxn, sellerReward);
						sellerWallectHistory.initForInsertNoAuth();
						wallectHistoryMapper.insert(sellerWallectHistory);

						//collect粉丝
						FansPO fansPO = new FansPO(1, pfpPickPO.getIndexUserId1(), pfpTokenPO.getMintUserId(), tokenId, pickId);
						fansPO.initForInsertNoAuth();
						fansMapper.insert(fansPO);
					}
				});
			} else if (rewardResult == 2) {

				//计算中奖积分
				BigDecimal vSoulPrice0 = getVSoulPrice(0, pfpPickPO.getIndexUserId0(), pfpTokenPO);
				vSoulHistoryPO0.setVSoulPrice(vSoulPrice0);
				vSoulHistoryPO0.initForInsertNoAuth();
				vSoulHistoryMapper.insert(vSoulHistoryPO0);
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId0(), vSoulPrice0);

				message0.initForInsertNoAuth();
				messageMapper.insert(message0);

				//计算中奖积分
				BigDecimal vSoulPrice1 = getVSoulPrice(0, pfpPickPO.getIndexUserId1(), pfpTokenPO);
				vSoulHistoryPO1.setVSoulPrice(vSoulPrice1);
				vSoulHistoryPO1.initForInsertNoAuth();
				vSoulHistoryMapper.insert(vSoulHistoryPO1);
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId1(), vSoulPrice1);

				message1.initForInsertNoAuth();
				messageMapper.insert(message1);

				//计算中奖积分
				BigDecimal vSoulPrice2 = getVSoulPrice(1, pfpPickPO.getIndexUserId2(), pfpTokenPO);
				vSoulHistoryPO2.setVSoulPrice(vSoulPrice2);
				vSoulHistoryPO2.initForInsertNoAuth();
				vSoulHistoryMapper.insert(vSoulHistoryPO2);
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId2(), vSoulPrice2);

				message2.setMessage(RewardMessageEnum.REWARD_SUCCESS.getName());
				message2.initForInsertNoAuth();
				messageMapper.insert(message2);

				//计算中奖积分
				BigDecimal vSoulPrice3 = getVSoulPrice(0, pfpPickPO.getIndexUserId3(), pfpTokenPO);
				vSoulHistoryPO3.setVSoulPrice(vSoulPrice3);
				vSoulHistoryPO3.initForInsertNoAuth();
				vSoulHistoryMapper.insert(vSoulHistoryPO3);
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId3(), vSoulPrice3);

				message3.initForInsertNoAuth();
				messageMapper.insert(message3);

				ThreadUtil.execAsync(new Runnable() {
					@Override
					public void run() {
						//给中签者转NFT
						R<String> transferNFTResult = approveTransferNFT(ownerAddress, pfpPickPO.getIndexAddress2(), tokenId);
						if (transferNFTResult.getCode() != 200) {
							log.error("=========transferNFTerror===========");
						}
						//NFT交易hash
						String transferNFTTxn = transferNFTResult.getData();
						pfpTransactionPO.setPfpTxnHash(transferNFTTxn);
						pfpPickPO.setNftTxn(transferNFTTxn);
						pfpHistoryPO.setTxnHash(transferNFTTxn);

						pfpHistoryMapper.updateById(pfpHistoryPO);

						String txn0 = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpPickPO.getIndexAddress0(), refund).getData();
						wallectHistory0.setTxnHash(txn0);
						wallectHistory0.initTime();
						wallectHistoryMapper.insert(wallectHistory0);

						refundWallectHistory0.setTxnHash(txn0);
						refundWallectHistory0.initTime();
						wallectHistoryMapper.insert(refundWallectHistory0);

						pfpPickPO.setIndexRewardTxn0(txn0);
						pfpPickMapper.updateById(pfpPickPO);

						String txn1 = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpPickPO.getIndexAddress1(), refund).getData();
						wallectHistory1.setTxnHash(txn1);
						wallectHistory1.initTime();
						wallectHistoryMapper.insert(wallectHistory1);

						refundWallectHistory1.setTxnHash(txn1);
						refundWallectHistory1.initTime();
						wallectHistoryMapper.insert(refundWallectHistory1);

						pfpPickPO.setIndexRewardTxn1(txn1);
						pfpPickMapper.updateById(pfpPickPO);

						String txn3 = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpPickPO.getIndexAddress3(), refund).getData();
						wallectHistory3.setTxnHash(txn3);
						wallectHistory3.initTime();
						wallectHistoryMapper.insert(wallectHistory3);

						refundWallectHistory3.setTxnHash(txn3);
						refundWallectHistory3.initTime();
						wallectHistoryMapper.insert(refundWallectHistory3);

						pfpPickPO.setIndexRewardTxn3(txn3);
						pfpPickMapper.updateById(pfpPickPO);

						//wBNB转账：铸造者收益
						String minterRewardTxn = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpTokenPO.getMintUserAddress(), minterReward).getData();
						//铸造者收益流水号
						pfpTransactionPO.setMinterMoneyTxnHash(minterRewardTxn);
						pfpPickPO.setMinterRewardTxn(minterRewardTxn);

						//铸造者收益记录
						WallectHistoryPO minterWallectHistory = new WallectHistoryPO(pfpTokenPO.getMintUserId(), 6, pfpTransactionPO.getId(), minterRewardTxn, minterReward);
						minterWallectHistory.initForInsertNoAuth();
						wallectHistoryMapper.insert(minterWallectHistory);

						//wBNB转账：卖出者收益
						String sellerRewardTxn = wEthTransferFrom(pfpTokenPO.getAdminAddress(), ownerAddress, sellerReward).getData();
						//卖出者收益流水号
						pfpTransactionPO.setSellerMoneyTxnHash(sellerRewardTxn);
						pfpPickPO.setSellerRewardTxn(sellerRewardTxn);

						pfpTransactionMapper.updateById(pfpTransactionPO);
						pfpPickMapper.updateById(pfpPickPO);

						//卖家收益记录
						WallectHistoryPO sellerWallectHistory = new WallectHistoryPO(ownerUserId, 5, pfpTransactionPO.getId(), sellerRewardTxn, sellerReward);
						sellerWallectHistory.initForInsertNoAuth();
						wallectHistoryMapper.insert(sellerWallectHistory);

						//collect粉丝
						FansPO fansPO = new FansPO(1, pfpPickPO.getIndexUserId2(), pfpTokenPO.getMintUserId(), tokenId, pickId);
						fansPO.initForInsertNoAuth();
						fansMapper.insert(fansPO);
					}
				});
			} else if (rewardResult == 3) {

				//计算中奖积分
				BigDecimal vSoulPrice0 = getVSoulPrice(0, pfpPickPO.getIndexUserId0(), pfpTokenPO);
				vSoulHistoryPO0.setVSoulPrice(vSoulPrice0);
				vSoulHistoryPO0.initForInsertNoAuth();
				vSoulHistoryMapper.insert(vSoulHistoryPO0);
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId0(), vSoulPrice0);

				message0.initForInsertNoAuth();
				messageMapper.insert(message0);

				//计算中奖积分
				BigDecimal vSoulPrice1 = getVSoulPrice(0, pfpPickPO.getIndexUserId1(), pfpTokenPO);
				vSoulHistoryPO1.setVSoulPrice(vSoulPrice1);
				vSoulHistoryPO1.initForInsertNoAuth();
				vSoulHistoryMapper.insert(vSoulHistoryPO1);
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId1(), vSoulPrice1);

				message1.initForInsertNoAuth();
				messageMapper.insert(message1);

				//计算中奖积分
				BigDecimal vSoulPrice2 = getVSoulPrice(0, pfpPickPO.getIndexUserId2(), pfpTokenPO);
				vSoulHistoryPO2.setVSoulPrice(vSoulPrice2);
				vSoulHistoryPO2.initForInsertNoAuth();
				vSoulHistoryMapper.insert(vSoulHistoryPO2);
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId2(), vSoulPrice2);

				message2.initForInsertNoAuth();
				messageMapper.insert(message2);

				//计算中奖积分
				BigDecimal vSoulPrice3 = getVSoulPrice(1, pfpPickPO.getIndexUserId3(), pfpTokenPO);
				vSoulHistoryPO3.setVSoulPrice(vSoulPrice3);
				vSoulHistoryPO3.initForInsertNoAuth();
				vSoulHistoryMapper.insert(vSoulHistoryPO3);
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId3(), vSoulPrice3);

				message3.setMessage(RewardMessageEnum.REWARD_SUCCESS.getName());
				message3.initForInsertNoAuth();
				messageMapper.insert(message3);

				ThreadUtil.execAsync(new Runnable() {
					@Override
					public void run() {
						//给中签者转NFT
						R<String> transferNFTResult = approveTransferNFT(ownerAddress, pfpPickPO.getIndexAddress3(), tokenId);
						if (transferNFTResult.getCode() != 200) {
							log.error("=========transferNFTerror===========");
						}
						//NFT交易hash
						String transferNFTTxn = transferNFTResult.getData();
						pfpTransactionPO.setPfpTxnHash(transferNFTTxn);
						pfpPickPO.setNftTxn(transferNFTTxn);
						pfpHistoryPO.setTxnHash(transferNFTTxn);

						pfpHistoryMapper.updateById(pfpHistoryPO);

						String txn0 = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpPickPO.getIndexAddress0(), refund).getData();
						wallectHistory0.setTxnHash(txn0);
						wallectHistory0.initTime();
						wallectHistoryMapper.insert(wallectHistory0);

						refundWallectHistory0.setTxnHash(txn0);
						refundWallectHistory0.initTime();
						wallectHistoryMapper.insert(refundWallectHistory0);

						pfpPickPO.setIndexRewardTxn0(txn0);
						pfpPickMapper.updateById(pfpPickPO);

						String txn1 = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpPickPO.getIndexAddress1(), refund).getData();
						wallectHistory1.setTxnHash(txn1);
						wallectHistory1.initTime();
						wallectHistoryMapper.insert(wallectHistory1);

						refundWallectHistory1.setTxnHash(txn1);
						refundWallectHistory1.initTime();
						wallectHistoryMapper.insert(refundWallectHistory1);

						pfpPickPO.setIndexRewardTxn1(txn1);
						pfpPickMapper.updateById(pfpPickPO);

						String txn2 = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpPickPO.getIndexAddress2(), refund).getData();
						wallectHistory2.setTxnHash(txn2);
						wallectHistory2.initTime();
						wallectHistoryMapper.insert(wallectHistory2);

						refundWallectHistory2.setTxnHash(txn2);
						refundWallectHistory2.initTime();
						wallectHistoryMapper.insert(refundWallectHistory2);

						pfpPickPO.setIndexRewardTxn2(txn2);
						pfpPickMapper.updateById(pfpPickPO);

						//wBNB转账：铸造者收益
						String minterRewardTxn = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpTokenPO.getMintUserAddress(), minterReward).getData();
						//铸造者收益流水号
						pfpTransactionPO.setMinterMoneyTxnHash(minterRewardTxn);
						pfpPickPO.setMinterRewardTxn(minterRewardTxn);

						//铸造者收益记录
						WallectHistoryPO minterWallectHistory = new WallectHistoryPO(pfpTokenPO.getMintUserId(), 6, pfpTransactionPO.getId(), minterRewardTxn, minterReward);
						minterWallectHistory.initForInsertNoAuth();
						wallectHistoryMapper.insert(minterWallectHistory);

						//wBNB转账：卖出者收益
						String sellerRewardTxn = wEthTransferFrom(pfpTokenPO.getAdminAddress(), ownerAddress, sellerReward).getData();
						//卖出者收益流水号
						pfpTransactionPO.setSellerMoneyTxnHash(sellerRewardTxn);
						pfpPickPO.setSellerRewardTxn(sellerRewardTxn);

						pfpTransactionMapper.updateById(pfpTransactionPO);
						pfpPickMapper.updateById(pfpPickPO);

						//卖家收益记录
						WallectHistoryPO sellerWallectHistory = new WallectHistoryPO(ownerUserId, 5, pfpTransactionPO.getId(), sellerRewardTxn, sellerReward);
						sellerWallectHistory.initForInsertNoAuth();
						wallectHistoryMapper.insert(sellerWallectHistory);

						//collect粉丝
						FansPO fansPO = new FansPO(1, pfpPickPO.getIndexUserId3(), pfpTokenPO.getMintUserId(), tokenId, pickId);
						fansPO.initForInsertNoAuth();
						fansMapper.insert(fansPO);
					}
				});
			}

			pfpPickPO.setSellerUserId(pfpTokenPO.getOwnerUserId());
			pfpPickPO.setSellerUserAddress(ownerAddress);

			//交易记录：交易完成
			pfpTransactionPO.setTransactionStatus(2);
			pfpTransactionMapper.updateById(pfpTransactionPO);

			//pfpPick修改状态:已开奖
			pfpPickPO.setStatus(2);
			pfpPickMapper.updateById(pfpPickPO);

			//修改持有人
			pfpTokenPO.setOwnerAddress(toAddress);
			pfpTokenPO.setOwnerUserId(toUserId);
			pfpTokenPO.setPrice(null);
			pfpTokenPO.setPriceTime(null);

			pfpTokenPO.initForUpdateNoAuth();
			//设置最新成交价
			pfpTokenPO.setLastSale(pfpTransactionPO.getListPrice());
			pfpTokenPO.setLastSaleTime(new Date());

			//已成交一次，修改价格
			pfpTokenPO.setTransactionsCount(pfpTokenPO.getTransactionsCount() + 1);
			pfpTokenPO.setPrice(PickUtil.getSalePrice(pfpTokenPO.getLevel(), pfpTokenPO.getTransactionsCount(), linkRate));
			//未上架
			pfpTokenPO.setPickStatus(0);
			pfpTokenPO.setPickId(null);

			pfpTokenMapper.updateById(pfpTokenPO);

			//pick粉丝
			FansPO fans0 = new FansPO(0, pfpPickPO.getIndexUserId0(), pfpTokenPO.getMintUserId(), tokenId, pickId);
			FansPO fans1 = new FansPO(0, pfpPickPO.getIndexUserId1(), pfpTokenPO.getMintUserId(), tokenId, pickId);
			FansPO fans2 = new FansPO(0, pfpPickPO.getIndexUserId2(), pfpTokenPO.getMintUserId(), tokenId, pickId);
			FansPO fans3 = new FansPO(0, pfpPickPO.getIndexUserId3(), pfpTokenPO.getMintUserId(), tokenId, pickId);

			fans0.initForInsertNoAuth();
			fans1.initForInsertNoAuth();
			fans2.initForInsertNoAuth();
			fans3.initForInsertNoAuth();

			fansMapper.insert(fans0);
			fansMapper.insert(fans1);
			fansMapper.insert(fans2);
			fansMapper.insert(fans3);

			//铸造者 = 卖出者
			if (ownerUserId.equals(pfpTokenPO.getMintUserId())) {
				//只发送一条消息
				MessagePO messagePO = new MessagePO(0, 1L, ownerUserId, RewardMessageEnum.SELLER_IS_MINTER.getName(), title, content, 0, tokenId, pickId);
				messagePO.initForInsertNoAuth();
				messageMapper.insert(messagePO);
			} else {
				//卖家消息
				MessagePO sellMessage = new MessagePO(0, 1L, ownerUserId, RewardMessageEnum.SELL_SUCCESS.getName(), title, content, 0, tokenId, pickId);
				sellMessage.initForInsertNoAuth();
				messageMapper.insert(sellMessage);

				//铸造者消息
				MessagePO minterMessage = new MessagePO(0, 1L, pfpTokenPO.getMintUserId(), RewardMessageEnum.MINTER_REWARD.getName(), title, content, 0, tokenId, pickId);
				minterMessage.initForInsertNoAuth();
				messageMapper.insert(minterMessage);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return R.success("reward success");

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

	private Integer getRewardResult(String hash) {
		hash = hash.toLowerCase();
		char code = hash.charAt(hash.length() - 1);

		int result;
		if (Character.isDigit(code)) {
			//是数字
			result = Integer.parseInt(String.valueOf(code));
		} else {
			//是字母
			//a=97 需要变成 a=10
			result = (int) code - 87;
		}
		return result % 4;
	}

	/**
	 * 积分计算
	 *
	 * @param rewardType 0-未中奖 1-中奖
	 * @param memberId   用户id
	 * @param x          NFT信息
	 * @return
	 */
	public BigDecimal getVSoulPrice(Integer rewardType, Long memberId, PFPTokenPO x) {
		//NFT等级
		Integer level = x.getLevel();

		//基础津贴
		BigDecimal base = null;
		if (level == 1) {
			base = new BigDecimal("50");
		} else if (level == 2) {
			base = new BigDecimal("100");
		} else if (level == 3) {
			base = new BigDecimal("250");
		} else if (level == 4) {
			base = new BigDecimal("500");
		} else if (level == 5) {
			base = new BigDecimal("1000");
		}

		//如果中奖：基础津贴 * 4
		if (rewardType == 1) {
			base = base.multiply(new BigDecimal("4"));
		}

		MemberPO memberPO = memberMapper.selectById(memberId);

		//计算匹配度
		int match = ScoreUtil.getMatch(memberPO.getUserTags(),
			memberPO.getCharisma(), memberPO.getExtroversion(), memberPO.getEnergy(),
			memberPO.getWisdom(), memberPO.getArt(), memberPO.getCourage(),
			x.getMintUserTags(),
			x.getCharisma(), x.getExtroversion(), x.getEnergy(),
			x.getWisdom(), x.getArt(), x.getCourage());

		//获取当前持有NFT
		List<PFPTokenPO> pfpTokenPOS = pfpTokenMapper.selectList(new LambdaQueryWrapper<PFPTokenPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(PFPTokenPO::getOwnerUserId, memberId)
			.eq(PFPTokenPO::getMintStatus, 1));

		//NFT持有加成
		BigDecimal addition = BigDecimal.ZERO;
		for (PFPTokenPO pfpTokenPO : pfpTokenPOS) {
			Integer pfpLevel = pfpTokenPO.getLevel();
			if (pfpLevel == 1) {
				addition = addition.add(BigDecimal.ZERO);
			} else if (pfpLevel == 2) {
				addition = addition.add(new BigDecimal("0.05"));
			} else if (pfpLevel == 3) {
				addition = addition.add(new BigDecimal("0.01"));
			} else if (pfpLevel == 4) {
				addition = addition.add(new BigDecimal("0.15"));
			} else if (pfpLevel == 5) {
				addition = addition.add(new BigDecimal("0.2"));
			}
		}

		//加成 = 1 + 加成
		addition = BigDecimal.ONE.add(addition);

		//匹配度 = 匹配度 / 100
		BigDecimal matchPercent = new BigDecimal(match).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);

		//计算积分
		BigDecimal vSoul = base.multiply(addition).multiply(matchPercent).setScale(0, BigDecimal.ROUND_UP);

		return vSoul;
	}


	public static void main(String[] args) throws Exception {
//		OkHttpClient.Builder builder = new OkHttpClient.Builder();
//		builder.connectTimeout(30 * 1000, TimeUnit.MILLISECONDS);
//		builder.writeTimeout(30 * 1000, TimeUnit.MILLISECONDS);
//		builder.readTimeout(30 * 1000, TimeUnit.MILLISECONDS);
//		OkHttpClient httpClient = builder.build();
//		Web3j web3j = Web3j.build(new HttpService(Web3jConfig.OPBSC_RPC, httpClient, false));
//		try {
//
//			//獲取密鑰文件
//			String walletFile = coinKeystorePathMacOs + "/" + coinWalletFile;
//			//獲取密鑰
//			Credentials credentials = WalletUtils.loadCredentials(coinCreatePwd, walletFile);
//
//			//獲取gasprice
//			BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
//			log.info("獲取gasPrice成功：" + gasPrice);
//			ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);
//
//			//加載 wETH
//			WEthContract contract = WEthContract.load("0x4200000000000000000000000000000000000006", web3j, credentials, gasProvider);
//			log.info("加載wETH成功：" + contract);
//
//			BigDecimal amount = new BigDecimal("0.001234");
//
//			BigInteger transferAmount = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
//			System.out.println("transferAmount:"+transferAmount);
//
//			TransactionReceipt receipt = contract.withdraw(transferAmount).send();
//
//			//獲取交易hash
//			String transactionHash = receipt.getTransactionHash();
//			log.info("=============調用transferFrom成功生成交易hash：" + transactionHash);
//		}catch (Exception e){
//			e.printStackTrace();
//		}
	}


}
