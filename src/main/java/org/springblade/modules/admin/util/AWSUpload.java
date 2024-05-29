package org.springblade.modules.admin.util;

import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.context.annotation.Configuration;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.web.multipart.MultipartFile;

@Configuration
public class AWSUpload {
	@Value("${4everland.bucket}")
	private String BUCKET_NAME;
	@Value("${4everland.folderName}")
	private String FOLDER_NAME;
	@Value("${4everland.key}")
	private String ACCESS_KEY;
	@Value("${4everland.secKey}")
	private String SECRET_KEY;
	public String uploadFileByFile(File file, String fileName) {
		try {
			Region region = Region.US_EAST_1; // 确认你的region。

			AwsBasicCredentials awsCreds = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);

			S3Client s3 = S3Client.builder()
				.region(region)
				.endpointOverride(new URI("https://endpoint.4everland.co")) // 添加自定义的endpoint。
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.build();

			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(BUCKET_NAME)
				.key(FOLDER_NAME + fileName)
				.build();

			s3.putObject(putObjectRequest, RequestBody.fromFile(file));

			// 手动创建对象的URL
			String objectUrl = "https://" + BUCKET_NAME + ".4everland.store/" + FOLDER_NAME + fileName;
			System.out.println("S3 Object URL: " + objectUrl);
			return objectUrl;
		} catch (AwsServiceException | URISyntaxException e) {
			// 打印错误信息
			System.out.println("Upload failed.");
			e.printStackTrace();
		}
		return null;
	}
	public String uploadFileByMultiPartFile(MultipartFile multiPartFile, String fileName) {
		try {
			Region region = Region.US_EAST_1; // 确认你的region。

			AwsBasicCredentials awsCreds = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);

			S3Client s3 = S3Client.builder()
				.region(region)
				.endpointOverride(new URI("https://endpoint.4everland.co")) // 添加自定义的endpoint。
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.build();

			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(BUCKET_NAME)
				.key(FOLDER_NAME + fileName)
				.build();
			s3.putObject(putObjectRequest, RequestBody.fromBytes(multiPartFile.getBytes()));
			// 手动创建对象的URL
			String objectUrl = "https://" + BUCKET_NAME + ".4everland.store/" + FOLDER_NAME + fileName;
			System.out.println("S3 Object URL: " + objectUrl);
			return objectUrl;
		} catch (AwsServiceException | URISyntaxException | IOException e) {
			System.out.println("Upload failed.");
			e.printStackTrace();
		}
		return null;
	}
}
