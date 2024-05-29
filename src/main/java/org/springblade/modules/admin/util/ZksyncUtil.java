//package org.springblade.modules.admin.util;
//
//import io.zksync.ZkSyncWallet;
//import io.zksync.abi.TransactionEncoder;
//import io.zksync.crypto.signer.EthSigner;
//import io.zksync.crypto.signer.PrivateKeyEthSigner;
//import io.zksync.methods.request.Eip712Meta;
//import io.zksync.methods.request.Transaction;
//import io.zksync.protocol.ZkSync;
//import io.zksync.protocol.core.Token;
//import io.zksync.protocol.core.ZkBlockParameterName;
//import io.zksync.transaction.fee.Fee;
//import io.zksync.transaction.type.Transaction712;
//import io.zksync.wrappers.IL2Bridge;
//import org.springframework.beans.factory.annotation.Value;
//import org.web3j.abi.FunctionEncoder;
//import org.web3j.abi.TypeReference;
//import org.web3j.abi.datatypes.Address;
//import org.web3j.abi.datatypes.Function;
//import org.web3j.abi.datatypes.Type;
//import org.web3j.abi.datatypes.generated.Uint256;
//import org.web3j.crypto.Credentials;
//import org.web3j.crypto.WalletUtils;
//import org.web3j.protocol.core.methods.response.TransactionReceipt;
//import org.web3j.protocol.http.HttpService;
//import org.web3j.utils.Numeric;
//
//import java.math.BigInteger;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//public class ZksyncUtil {
//
//	@Value("${spring.profiles.active}")
//	List<String> activeProfiles;
//
//	private static final String coinWalletFile = "UTC--2023-06-05T15-42-39.669000000Z--e52e23326668117034a0ec6a288e5bb117b7f2c6.json";
//
//	private static final String coinKeystorePathWindows = "E:/work/coin";
//	private static final String coinKeystorePathLinux = "/opt/peic/keystore/bnb";
//
//	private final static String coinCreatePwd = "peic8888";
//
//
//	public static void main(String[] args) throws Exception{
//		Long chainId = 280L;
//
//		ZkSync zksync = ZkSync.build(new HttpService("https://testnet.era.zksync.dev"));
//		System.out.println("zksync:"+zksync);
//
//		String walletFile = coinKeystorePathWindows + "/" + coinWalletFile;
//		//獲取密鑰
//		Credentials credentials = WalletUtils.loadCredentials(coinCreatePwd, walletFile);
//		System.out.println("credentials:"+credentials);
//
//		EthSigner signer = new PrivateKeyEthSigner(credentials, chainId);
//		System.out.println("signer:"+signer);
//
//		ZkSyncWallet wallet = new ZkSyncWallet(zksync, signer, Token.ETH);
//		System.out.println("wallet:"+wallet);
//
//
//
////		TransactionReceipt receipt = wallet.deploy(Numeric.hexStringToByteArray("0xa947AF197bD5105d7f7C454139215fc37829cc86")).send();
//
////		System.out.println("receipt:"+receipt);
//
//		String contractAddress = "0xB9Aa0f4b7Bbf8A172D637d951bed72ce47A53486";
//
//		// Example contract function
////		Function contractFunction = new Function(
////			"increment",
////			Collections.singletonList(new Uint256(BigInteger.ONE)),
////			Collections.emptyList());
//
//		Function function = new org.web3j.abi.datatypes.Function(
//			"safeMint",
//			Arrays.<Type>asList(new org.web3j.abi.datatypes.Address("0xE52e23326668117034A0eC6A288E5bB117B7f2C6"),
//				new org.web3j.abi.datatypes.generated.Uint256(31L)),
//			Collections.<TypeReference<?>>emptyList());
//
////		TransactionReceipt receipt = wallet.execute(contractAddress, function).send();
////		System.out.println("receipt"+ receipt);
//
//		String calldata = FunctionEncoder.encode(function);
//
//		Transaction estimate = Transaction.createFunctionCallTransaction(
//			signer.getAddress(),
//			contractAddress,
//			BigInteger.ZERO,
//			BigInteger.ZERO,
//			calldata
//		);
//
//		Fee fee = zksync.zksEstimateFee(estimate).send().getResult();
//		System.out.println("fee:"+fee);
//
////		BigInteger chainId = zksync.ethChainId().send().getChainId();
//
////		BigInteger nonce = zksync
////			.ethGetTransactionCount(signer.getAddress(), ZkBlockParameterName.COMMITTED).send()
////			.getTransactionCount();
////
////		String contractAddress = "0x<contract_address>";
////		String calldata = "0x<calldata>"; // Here is an encoded contract function
////
////		Transaction estimate = Transaction.createFunctionCallTransaction(
////			signer.getAddress(),
////			contractAddress,
////			BigInteger.ZERO,
////			BigInteger.ZERO,
////			calldata
////		);
////
////		Fee fee = zksync.zksEstimateFee(estimate).send().getResult();
////
////		Eip712Meta meta = estimate.getEip712Meta();
////		meta.setErgsPerPubdata(fee.getErgsPerPubdataLimitNumber());
////
////		Transaction712 transaction = new Transaction712(
////			chainId.longValue(),
////			nonce,
////			fee.getErgsLimitNumber(),
////			estimate.getTo(),
////			estimate.getValueNumber(),
////			estimate.getData(),
////			fee.getMaxPriorityFeePerErgNumber(),
////			fee.getErgsPriceLimitNumber(),
////			signer.getAddress(),
////			meta
////		);
////
////		String signature = signer.getDomain().thenCompose(domain -> signer.signTypedData(domain, transaction)).join();
////		byte[] message = TransactionEncoder.encode(transaction, TransactionEncoder.getSignatureData(signature));
////
////		String sentTransactionHash = zksync.ethSendRawTransaction(Numeric.toHexString(message)).send().getTransactionHash();
////
////		// You can check the transaction status
////		Optional<TransactionReceipt> transactionReceipt = zksync.ethGetTransactionReceipt(sentTransactionHash).send().getTransactionReceipt();
//
//	}
//
//
//	private Credentials loadCredentials() throws Exception{
//		String coinKeystorePath = null;
//		if (activeProfiles != null && activeProfiles.size() > 0) {
//			if (activeProfiles.get(0).equalsIgnoreCase("dev")) {
//				coinKeystorePath = coinKeystorePathWindows;
//			} else {
//				coinKeystorePath = coinKeystorePathLinux;
//			}
//		}
//
//		//獲取密鑰文件
//		String walletFile = coinKeystorePath + "/" + coinWalletFile;
//		//獲取密鑰
//		Credentials credentials = WalletUtils.loadCredentials(coinCreatePwd, walletFile);
//
//		return credentials;
//	}
//}
