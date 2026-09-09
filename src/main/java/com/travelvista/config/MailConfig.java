package com.travelvista.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;

/**
 * Dev-only fake mail sender.
 *
 * IMPORTANT (root cause of missing emails on Railway):
 * Previously this bean was registered UNCONDITIONALLY. Because user-defined
 * @Bean methods are processed before Spring Boot's auto-configuration, this
 * fake "log only" sender hijacked the real JavaMailSender in production too.
 * mailSender.send() then always "succeeded" while nothing was ever delivered.
 *
 * It is now active ONLY when app.mail.dev-mode=true (default: local dev when
 * MAIL_USERNAME / MAIL_PASSWORD are not configured). Railway must NOT set
 * MAIL_DEV_MODE, or must set it to false.
 */
@Configuration
public class MailConfig {

    private final Environment env;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.host:}")
    private String mailHost;

    public MailConfig(Environment env) {
        this.env = env;
    }

    @Bean
    @ConditionalOnProperty(name = "app.mail.dev-mode", havingValue = "true", matchIfMissing = false)
    public JavaMailSender devMailSender() {

        System.out.println("=============================================");
        System.out.println("  [MAIL] DEV MODE: emails will be LOGGED ONLY");
        System.out.println("  [MAIL] Not sent to any real SMTP server!");
        System.out.println("  [MAIL] Disable app.mail.dev-mode to send for real");
        System.out.println("=============================================");

        return new JavaMailSenderImpl() {

            @Override
            public MimeMessage createMimeMessage(InputStream contentStream) {
                return super.createMimeMessage(contentStream);
            }

            @Override
            public void send(SimpleMailMessage simpleMessage) {
                // NOTE: body may contain an OTP — print it in dev mode only.
                System.out.println("\n═══════════════════════════════════════");
                System.out.println("  DEV EMAIL (not sent — dev mode)");
                System.out.println("  To:      " + (simpleMessage.getTo() != null ? String.join(", ", simpleMessage.getTo()) : "N/A"));
                System.out.println("  Subject: " + simpleMessage.getSubject());
                System.out.println("  Body:\n" + simpleMessage.getText());
                System.out.println("═══════════════════════════════════════\n");
            }

            @Override
            public void send(SimpleMailMessage... simpleMessages) {
                for (SimpleMailMessage msg : simpleMessages) send(msg);
            }

            @Override
            public void send(MimeMessage mimeMessage) {
                System.out.println("[DEV MAIL] MimeMessage sent (logged only)");
            }

            @Override
            public void send(MimeMessage... mimeMessages) {
                for (MimeMessage msg : mimeMessages) send(msg);
            }

            @Override
            public void send(MimeMessagePreparator mimeMessagePreparator) {
                System.out.println("[DEV MAIL] MimeMessagePreparator sent (logged only)");
            }

            @Override
            public void send(MimeMessagePreparator... mimeMessagePreparators) {
                for (MimeMessagePreparator p : mimeMessagePreparators) send(p);
            }
        };
    }

    /**
     * Runs at startup so you can verify in Railway logs which mail mode is active.
     * Reads the SAME property source that @ConditionalOnProperty uses, so the
     * printed mode can never drift from the bean that was actually created.
     * Never prints the username or password.
     */
    @Bean
    public Object mailStartupCheck() {
        boolean devMode = "true".equalsIgnoreCase(env.getProperty("app.mail.dev-mode", "false"));

        System.out.println("=============================================");
        if (devMode) {
            System.out.println("  [MAIL] MODE: DEV (emails logged, NOT sent)");
        } else if (mailUsername == null || mailUsername.isBlank() || mailHost == null || mailHost.isBlank()) {
            System.out.println("  [MAIL] WARNING: app.mail.dev-mode=false but spring.mail.username/host is EMPTY.");
            System.out.println("  [MAIL] OTP emails will FAIL. Set MAIL_USERNAME / MAIL_PASSWORD / MAIL_HOST env vars.");
        } else {
            System.out.println("  [MAIL] MODE: PRODUCTION SMTP");
            System.out.println("  [MAIL] host=" + mailHost + ", username configured: true");
        }
        System.out.println("=============================================");
        return new Object();
    }
}
