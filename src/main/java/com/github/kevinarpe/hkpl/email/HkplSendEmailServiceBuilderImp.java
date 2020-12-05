package com.github.kevinarpe.hkpl.email;

import com.googlecode.kevinarpe.papaya.argument.ObjectArgs;
import com.googlecode.kevinarpe.papaya.java_mail.AlwaysTrustSSL;
import com.googlecode.kevinarpe.papaya.java_mail.EmailMessageAddress;
import com.googlecode.kevinarpe.papaya.java_mail.EmailMessageBuilderFactory;
import com.googlecode.kevinarpe.papaya.java_mail.JavaMailSession;
import com.googlecode.kevinarpe.papaya.java_mail.JavaMailSessionBuilder;
import com.googlecode.kevinarpe.papaya.java_mail.JavaMailSessionBuilderFactory;

import javax.annotation.Nullable;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public final class HkplSendEmailServiceBuilderImp
implements HkplSendEmailServiceBuilder {

    private final JavaMailSessionBuilder javaMailSessionBuilder;
    @Nullable
    private EmailMessageAddress nullableFromEmailAddress;
    @Nullable
    private EmailMessageAddress nullableToEmailAddress;
    private final EmailMessageBuilderFactory emailMessageBuilderFactory;

    public HkplSendEmailServiceBuilderImp() {
        this(JavaMailSessionBuilderFactory.INSTANCE, EmailMessageBuilderFactory.INSTANCE);
    }

    HkplSendEmailServiceBuilderImp(JavaMailSessionBuilderFactory javaMailSessionBuilderFactory,
                                   EmailMessageBuilderFactory emailMessageBuilderFactory) {

        ObjectArgs.checkNotNull(javaMailSessionBuilderFactory, "javaMailSessionBuilderFactory");
        this.javaMailSessionBuilder = javaMailSessionBuilderFactory.newInstance();

        this.emailMessageBuilderFactory =
            ObjectArgs.checkNotNull(emailMessageBuilderFactory, "emailMessageBuilderFactory");
    }

    @Override
    public HkplSendEmailServiceBuilder
    smtpHost(String host,
             final int port,
             AlwaysTrustSSL alwaysTrustSSL) {

        javaMailSessionBuilder.host(host, alwaysTrustSSL).customPort(port);
        return this;
    }

    @Override
    public HkplSendEmailServiceBuilder
    smtpUsernameAndPassword(String username,
                            String password) {

        javaMailSessionBuilder.usernameAndPassword(username, password);
        return this;
    }

    @Override
    public HkplSendEmailServiceBuilder
    fromEmailAddress(EmailMessageAddress fromEmailAddress) {

        this.nullableFromEmailAddress = ObjectArgs.checkNotNull(fromEmailAddress, "fromEmailAddress");
        return this;
    }

    @Override
    public HkplSendEmailServiceBuilder
    toEmailAddress(EmailMessageAddress toEmailAddress) {

        this.nullableToEmailAddress = ObjectArgs.checkNotNull(toEmailAddress, "toEmailAddress");
        return this;
    }

    @Override
    public HkplSendEmailService
    build() {
        _assertAllSet();

        final JavaMailSession javaMailSession = javaMailSessionBuilder.build();

        final HkplSendEmailServiceImp x =
            new HkplSendEmailServiceImp(javaMailSession, nullableFromEmailAddress, nullableToEmailAddress);
        return x;
    }

    private void
    _assertAllSet() {

        if (null == nullableFromEmailAddress) {
            throw new IllegalStateException("Missing From: email address");
        }
        if (null == nullableToEmailAddress) {
            throw new IllegalStateException("Missing To: email address");
        }
    }
}
