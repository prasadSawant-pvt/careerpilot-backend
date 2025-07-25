package com.pathprep.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Configuration class for MongoDB connection settings with SSL support.
 * Handles connection pooling, timeouts, and SSL configuration.
 */
@Configuration
@Slf4j
public class MongoDBConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Value("${spring.data.mongodb.connection-timeout-ms:30000}")
    private int connectionTimeoutMs;

    @Value("${spring.data.mongodb.socket-timeout-ms:60000}")
    private int socketTimeoutMs;

    @Value("${spring.data.mongodb.ssl.enabled:true}")
    private boolean sslEnabled;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Bean
    @Override
    public MongoClient mongoClient() {
        log.info("Configuring MongoDB client with URI: {}", maskSensitiveInfo(mongoUri));
        
        ConnectionString connectionString = new ConnectionString(mongoUri);
        
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToSslSettings(builder -> {
                    builder.enabled(sslEnabled);
                    builder.invalidHostNameAllowed(true); // Only for development/testing
                })
                .applyToConnectionPoolSettings(builder -> 
                    builder.maxConnectionIdleTime(60000, TimeUnit.MILLISECONDS)
                           .minSize(5)
                           .maxSize(100)
                )
                .applyToSocketSettings(builder -> 
                    builder.connectTimeout(connectionTimeoutMs, TimeUnit.MILLISECONDS)
                           .readTimeout(socketTimeoutMs, TimeUnit.MILLISECONDS)
                )
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }

    private String maskSensitiveInfo(String connectionString) {
        if (connectionString == null) {
            return "null";
        }
        // Mask password in connection string for logging
        return connectionString.replaceAll(":[^@/]+@", ":****@");
    }
}
