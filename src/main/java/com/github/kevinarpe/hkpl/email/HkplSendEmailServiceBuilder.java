package com.github.kevinarpe.hkpl.email;

import com.googlecode.kevinarpe.papaya.java_mail.AlwaysTrustSSL;
import com.googlecode.kevinarpe.papaya.java_mail.EmailMessageAddress;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public interface HkplSendEmailServiceBuilder {

    HkplSendEmailServiceBuilder
    smtpHost(String host, int port, AlwaysTrustSSL alwaysTrustSSL);

    HkplSendEmailServiceBuilder
    smtpUsernameAndPassword(String username, String password);

    HkplSendEmailServiceBuilder
    fromEmailAddress(EmailMessageAddress fromEmailAddress);

    HkplSendEmailServiceBuilder
    toEmailAddress(EmailMessageAddress toEmailAddress);

    HkplSendEmailService
    build();
}
