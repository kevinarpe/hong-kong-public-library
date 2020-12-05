package com.github.kevinarpe.hkpl.email;

import com.googlecode.kevinarpe.papaya.argument.ObjectArgs;
import com.googlecode.kevinarpe.papaya.java_mail.EmailMessageAddress;
import com.googlecode.kevinarpe.papaya.java_mail.EmailMessageAddressListType;
import com.googlecode.kevinarpe.papaya.java_mail.EmailMessageAddressType;
import com.googlecode.kevinarpe.papaya.java_mail.EmailMessageBuilder;
import com.googlecode.kevinarpe.papaya.java_mail.JavaMailSession;
import com.googlecode.kevinarpe.papaya.java_mail.TextMimeSubType;

import javax.mail.internet.MimeMessage;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
// Scope: Global singleton
public final class HkplSendEmailServiceImp
implements HkplSendEmailService {

    private final JavaMailSession javaMailSession;
    private final EmailMessageAddress fromEmailAddress;
    private final EmailMessageAddress toEmailAddress;

    public HkplSendEmailServiceImp(JavaMailSession javaMailSession,
                                   EmailMessageAddress fromEmailAddress,
                                   EmailMessageAddress toEmailAddress) {

        this.javaMailSession = ObjectArgs.checkNotNull(javaMailSession, "javaMailSession");
        this.fromEmailAddress = ObjectArgs.checkNotNull(fromEmailAddress, "fromEmailAddress");
        this.toEmailAddress = ObjectArgs.checkNotNull(toEmailAddress, "toEmailAddress");
    }

    @Override
    public void
    sendMessage(String subjectSuffix,
                TextMimeSubType bodyTextMimeSubType,
                String bodyText)
    throws Exception {

        final EmailMessageBuilder b = javaMailSession.emailMessageBuilder();
        b.address(EmailMessageAddressType.FROM, fromEmailAddress);
        b.addressSet(EmailMessageAddressListType.TO).add(toEmailAddress);
        b.subject("Hong Kong Public Library: " + subjectSuffix);
        b.body(bodyTextMimeSubType, bodyText);

        final MimeMessage m = b.build();
        javaMailSession.sendMessage(m);
    }
}
