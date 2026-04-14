package com.example.foodshop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.URI;

@Configuration
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${REDIS_URL:${REDIS_PUBLIC_URL:${spring.data.redis.url:}}}")
    private String redisUrl;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.username:}")
    private String redisUsername;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean redisSslEnabled;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        
        // If REDIS_URL is provided (Railway public URL), parse it
        if (redisUrl != null && !redisUrl.isEmpty()) {
            try {
                logger.info("Parsing REDIS_URL for connection...");
                URI uri = new URI(redisUrl);
                
                String host = uri.getHost();
                int port = uri.getPort();
                
                if (host == null || port == -1) {
                    throw new IllegalArgumentException("Invalid REDIS_URL format. Expected: redis://default:password@host:port");
                }
                
                config.setHostName(host);
                config.setPort(port);
                
                // Extract password from URL (format: redis://default:password@host:port)
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    if (parts.length == 2) {
                        String password = parts[1];
                        config.setPassword(password);
                        logger.info("Redis connection configured from URL: {}:{}", host, port);
                    }
                } else {
                    logger.warn("No password found in REDIS_URL");
                }
                
            } catch (Exception e) {
                logger.error("Failed to parse REDIS_URL: {}. Falling back to individual properties.", e.getMessage());
                // Fallback to individual properties
                config.setHostName(redisHost);
                config.setPort(redisPort);
                if (redisPassword != null && !redisPassword.isEmpty()) {
                    config.setPassword(redisPassword);
                }
                logger.info("Redis connection configured from properties: {}:{}", redisHost, redisPort);
            }
        } else {
            // Use individual properties
            logger.info("Using individual Redis properties for connection");
            config.setHostName(redisHost);
            config.setPort(redisPort);
            if (redisUsername != null && !redisUsername.isEmpty()) {
                config.setUsername(redisUsername);
            }
            if (redisPassword != null && !redisPassword.isEmpty()) {
                config.setPassword(redisPassword);
            }
            logger.info("Redis connection configured: {}:{} (SSL: {})", redisHost, redisPort, redisSslEnabled);
        }
        
        JedisConnectionFactory factory = new JedisConnectionFactory(config);
        
        // Enable SSL if configured
        if (redisSslEnabled) {
            factory.setUseSsl(true);
            logger.info("✅ Redis SSL enabled");
        }
        
        // Test connection on startup
        try {
            factory.afterPropertiesSet();
            logger.info("✅ Redis connection factory initialized successfully");
        } catch (Exception e) {
            logger.error("❌ Failed to initialize Redis connection factory: {}", e.getMessage());
            logger.warn("⚠️ Application will continue but JWT blacklist will not work!");
        }
        
        return factory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for both keys and values
        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        
        logger.info("✅ RedisTemplate configured successfully");
        
        return template;
    }
}
