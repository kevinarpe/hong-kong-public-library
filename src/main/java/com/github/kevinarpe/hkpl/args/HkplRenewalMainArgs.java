package com.github.kevinarpe.hkpl.args;

import com.beust.jcommander.Parameter;
import com.googlecode.kevinarpe.papaya.argument.IntArgs;
import com.googlecode.kevinarpe.papaya.argument.StringArgs;
import com.googlecode.kevinarpe.papaya.java_mail.AlwaysTrustSSL;
import com.googlecode.kevinarpe.papaya.java_mail.EmailMessageAddress;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.IsHeadless;

import javax.annotation.Nullable;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public final class HkplRenewalMainArgs
implements Args {

    private static final String CHROME_HEADLESS = "--chrome-headless";
    private static final String HKPL_WEB_USERNAME = "--hkpl-web-username";
    private static final String HKPL_WEB_PASSWORD = "--hkpl-web-password";
    private static final String SMTP_HOST = "--smtp-host";
    private static final String SMTP_PORT = "--smtp-port";
    private static final String EMAIL_ADDRESS = "--email-address";
    private static final String SMTP_USERNAME = "--smtp-username";
    private static final String SMTP_PASSWORD = "--smtp-password";

    public HkplRenewalMainArgs() {
        // Empty
    }

    @Parameter(
        names = {"-h", "--help"},
        required = false,
        help = true,
        description = "Display help"
    )
    private boolean isHelpRequested;

    @Override
    public boolean isHelpRequested() {
        return isHelpRequested;
    }

    @Parameter(
        names = CHROME_HEADLESS,
        required = false,
        description = "Display help"
    )
    private boolean isChromeHeadless = false;

    public IsHeadless isChromeHeadless() {
        return isChromeHeadless ? IsHeadless.YES : IsHeadless.NO;
    }

    @Parameter(
        names = HKPL_WEB_USERNAME,
        required = true,
        description = "Example: 23838017432123"
    )
    private String hkplWebUsername;

    public String hkplWebUsername() {
        return hkplWebUsername;
    }

    @Parameter(
        names = HKPL_WEB_PASSWORD,
        required = true,
        description = "Example: password123"
    )
    private String hkplWebPassword;

    public String hkplWebPassword() {
        return hkplWebPassword;
    }

    @Parameter(
        names = SMTP_HOST,
        required = true,
        description = "Example: smtp.gmail.com"
    )
    private String smtpHost;

    public String smtpHost() {
        return smtpHost;
    }

    @Parameter(
        names = SMTP_PORT,
        required = true,
        description = "Example: 587 (modern secure)"
    )
    private int smtpPort;

    public int smtpPort() {
        return smtpPort;
    }

    @Parameter(
        names = "--smtp-host-always-trust-ssl",
        required = false
    )
    private boolean smtpAlwaysTrustSsl = false;

    public AlwaysTrustSSL smtpAlwaysTrustSsl() {
        return smtpAlwaysTrustSsl ? AlwaysTrustSSL.YES : AlwaysTrustSSL.NO;
    }

    @Parameter(
        names = EMAIL_ADDRESS,
        required = true,
        description = "Example: kevinarpe@gmail.com"
    )
    private String emailAddress;

    public EmailMessageAddress emailAddress() {
        final EmailMessageAddress x = EmailMessageAddress.fromEmailAddressOnly(emailAddress);
        return x;
    }

    @Nullable
    @Parameter(
        names = SMTP_USERNAME,
        required = false,
        description = "Optional: SMTP username for authentication; Example: kevinarpe@gmail.com"
    )
    private String nullableSmtpUsername = null;

    @Nullable
    public String nullableSmtpUsername() {
        return nullableSmtpUsername;
    }

    @Nullable
    @Parameter(
        names = SMTP_PASSWORD,
        required = false,
        description = "Optional: SMTP password for authentication; Example: password123"
    )
    private String nullableSmtpPassword = null;

    @Nullable
    public String nullableSmtpPassword() {
        return nullableSmtpPassword;
    }

    @Override
    public void
    validate() {

        StringArgs.checkNotEmptyOrWhitespace(hkplWebUsername, HKPL_WEB_USERNAME);
        StringArgs.checkNotEmptyOrWhitespace(hkplWebPassword, HKPL_WEB_PASSWORD);
        StringArgs.checkNotEmptyOrWhitespace(smtpHost, SMTP_HOST);
        IntArgs.checkMinValue(smtpPort, 1, SMTP_PORT);
        if ((null == nullableSmtpUsername) != (null == nullableSmtpPassword)) {

            throw new IllegalArgumentException(SMTP_USERNAME + " must be paired with " + SMTP_PASSWORD);
        }
        if (null != nullableSmtpUsername && null != nullableSmtpPassword) {

            StringArgs.checkNotEmptyOrWhitespace(nullableSmtpUsername, SMTP_USERNAME);
            StringArgs.checkNotEmptyOrWhitespace(nullableSmtpPassword, SMTP_PASSWORD);
        }
    }
}
