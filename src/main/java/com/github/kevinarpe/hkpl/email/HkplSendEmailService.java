package com.github.kevinarpe.hkpl.email;

import com.googlecode.kevinarpe.papaya.java_mail.TextMimeSubType;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public interface HkplSendEmailService {

    void sendMessage(String subjectSuffix,
                     TextMimeSubType bodyTextMimeSubType,
                     String bodyText)
    throws Exception;
}
