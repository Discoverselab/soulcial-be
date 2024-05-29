package org.springblade.modules.resource.config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springblade.core.oss.props.OssProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssConfig {

	@Value("${oss.endpoint}")
	private String endpoint;

	@Value("${oss.access-key}")
	private String accessKey;

	@Value("${oss.secret-key}")
	private String secretKey;

	@Value("${oss.bucket-name}")
	private String bucketName;

	@Bean
	public OSS ossClient() {
		ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
		conf.setMaxConnections(1024);
		conf.setSocketTimeout(50000);
		conf.setConnectionTimeout(50000);
		conf.setConnectionRequestTimeout(1000);
		conf.setIdleConnectionTime(60000);
		conf.setMaxErrorRetry(5);

		return new OSSClientBuilder().build(endpoint, accessKey, secretKey, conf);
	}

	@Bean
	public OssProperties ossProperties() {
		OssProperties ossProperties = new OssProperties();
		ossProperties.setEndpoint(endpoint);
		ossProperties.setAccessKey(accessKey);
		ossProperties.setSecretKey(secretKey);
		ossProperties.setBucketName(bucketName);
		return ossProperties;
	}
}
