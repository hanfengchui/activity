package com.timesontransfar.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.baomidou.dynamic.datasource.toolkit.CryptoUtils;

@Configuration
public class JdbcConfiguration {
	private static final Logger log = LoggerFactory.getLogger(JdbcConfiguration.class);
	
	@Value("${spring.datasource-mysql.public-key}")
	private String publicKeyMysql;
	
	@Value("${spring.datasource-mysql.enpassword}")
	private String passwordMysql;
	
	@Value("${spring.jscsc-ct-pub.public-key}")
	private String publicKeyPub;
	
	@Value("${spring.jscsc-ct-pub.enpassword}")
	private String passwordPub;
	
	private String getDePassword(String publicKey, String password) {
		String dePassword = "";
		try {
			dePassword = CryptoUtils.decrypt(publicKey, password);
		} catch (Exception e) {
			log.info("publicKey: {} enpassword: {}", publicKey, password);
			log.error("getDePassword error: {}", e.getMessage(), e);
		}
		return dePassword;
	}
	
	@Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource-mysql")
    public DataSource primaryDataSource() {
		String dePassword = this.getDePassword(publicKeyMysql, passwordMysql);
        return DataSourceBuilder.create().password(dePassword).build();
    }
	
	@Bean(name = "dataSourcePub")
    @ConfigurationProperties(prefix = "spring.jscsc-ct-pub")
    public DataSource dataSourcePub() {
		String dePassword = this.getDePassword(publicKeyPub, passwordPub);
        return DataSourceBuilder.create().password(dePassword).build();
    }
	
	@Bean(name = "namedParameterJdbcTemplate")
	@Primary
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
	    return new NamedParameterJdbcTemplate(dataSource);
	}
	
	@Bean(name = "jdbcTemplate")
	@Primary
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
	    return new JdbcTemplate(dataSource);
	}
	
	@Bean(name = "pubJdbcTemplate")
	public JdbcTemplate pubJdbcTemplate(@Qualifier("dataSourcePub") DataSource dataSource) {
	    return new JdbcTemplate(dataSource);
	}
}
