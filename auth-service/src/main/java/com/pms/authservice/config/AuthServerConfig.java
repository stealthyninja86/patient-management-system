package com.pms.authservice.config;

import com.pms.authservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.user.UserDestinationResolver;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Configuration
public class AuthServerConfig {

    private static final Logger log = LoggerFactory.getLogger(AuthServerConfig.class);


    @Bean
    public RegisteredClientRepository registeredClientRepository(DataSource dataSource) {
        return new JdbcRegisteredClientRepository(
                new JdbcTemplate(dataSource)
        );
    }

    @Bean
    public CommandLineRunner seedOAuth2Clients(RegisteredClientRepository clientRepository,
                                               PasswordEncoder passwordEncoder) {
        return args -> {
            Instant now = Instant.now();

            if(clientRepository.findByClientId("gateway-client") == null) {
                RegisteredClient gatewayClient = RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("gateway-client")
                        .clientIdIssuedAt(now)
                        .clientSecret(passwordEncoder.encode("gateway-secret"))
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                        .redirectUri("http://localhost:4005/authorized")
                        .scope("user:read")
                        .scope("user:write")
                        .scope("openid")
                        .clientSettings(
                                ClientSettings.builder()
                                .requireProofKey(true)
                                .build())
                        .tokenSettings(
                                TokenSettings.builder()
                                .accessTokenTimeToLive(Duration.ofHours(1))
                                .build())
                        .build();

                clientRepository.save(gatewayClient);
                log.info("seeded OAuth2 client: gateway-client with id {}", gatewayClient.getId());
            }

            if(clientRepository.findByClientId("admin-client") == null) {
                RegisteredClient adminClient = RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("admin-client")
                        .clientIdIssuedAt(now)
                        .clientSecret(passwordEncoder.encode("admin-secret"))
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .scope("role:admin")
                        .tokenSettings(TokenSettings.builder()
                                .accessTokenTimeToLive(Duration.ofHours(1))
                                .build())
                        .build();

                clientRepository.save(adminClient);
                log.info("seeded OAuth2 client: admin-client");
            }

            if(clientRepository.findByClientId("doctor-client") == null) {
                RegisteredClient doctorClient = RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("doctor-client")
                        .clientIdIssuedAt(now)
                        .clientSecret(passwordEncoder.encode("doctor-secret"))
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .scope("role:doctor")
                        .tokenSettings(TokenSettings.builder()
                                .accessTokenTimeToLive(Duration.ofHours(1))
                                .build())
                        .build();

                clientRepository.save(doctorClient);
                log.info("seeded OAuth2 client: doctor-client");
            }

            if(clientRepository.findByClientId("patient-client") == null) {
                RegisteredClient patientClient = RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("patient-client")
                        .clientIdIssuedAt(now)
                        .clientSecret(passwordEncoder.encode("patient-secret"))
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .scope("role:patient")
                        .tokenSettings(TokenSettings.builder()
                                .accessTokenTimeToLive(Duration.ofHours(1))
                                .build())
                        .build();

                clientRepository.save(patientClient);
                log.info("seeded OAuth2 client: patient-client");
            }

            if(clientRepository.findByClientId("internal-service") == null) {
                RegisteredClient serviceClient = RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("internal-service")
                        .clientIdIssuedAt(now)
                        .clientSecret(passwordEncoder.encode("service-secret"))
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .scope("service:internal")
                        .tokenSettings(
                                TokenSettings.builder()
                                .accessTokenTimeToLive(Duration.ofHours(2))
                                .build())
                        .build();

                clientRepository.save(serviceClient);
                log.info("seeded OAuth2 client: internal-service with id {}", serviceClient.getId());
            }
        };
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(UserRepository userRepository) {

        return (context) ->{
            if(AuthorizationGrantType.CLIENT_CREDENTIALS.equals(context.getAuthorizationGrantType())) {
                String clientId = context.getPrincipal().getName();
                switch (clientId) {
                    case "admin-client" -> {
                        context.getClaims().claim("role", "ADMIN");
                        log.debug("Token customized for client: {} - role=ADMIN", clientId);
                    }
                    case "doctor-client" -> {
                        context.getClaims().claim("role", "DOCTOR");
                        log.debug("Token customized for client: {} - role=DOCTOR", clientId);
                    }
                    case "patient-client" -> {
                        context.getClaims().claim("role", "PATIENT");
                        log.debug("Token customized for client: {} - role=PATIENT", clientId);
                    }
                    default -> {
                        context.getClaims().claim("clientId", clientId);
                    }
                }
                return;
            }

            String username = context.getPrincipal().getName();
            userRepository.findByEmail(username).ifPresentOrElse(
                    user -> {
                        context.getClaims()
                                .claim("role", user.getRole().name())
                                .claim("userId", user.getId().toString());

                        if(user.getDoctorId() != null) {
                            context.getClaims().claim("doctorId", user.getDoctorId());
                        }
                        if(user.getPatientId() != null) {
                            context.getClaims().claim("patientId", user.getPatientId());
                        }
                        log.debug("Token customized for user: {} - role={}", username,  user.getRole());
                    }, () -> log.warn("Token requested for unkown user: {}", username)
            );
        };
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://auth-service:4005")
                .build();
    }
}
