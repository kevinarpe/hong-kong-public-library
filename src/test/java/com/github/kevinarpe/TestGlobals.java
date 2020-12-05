package com.github.kevinarpe;

import com.googlecode.kevinarpe.papaya.exception.ExceptionThrower;
import com.googlecode.kevinarpe.papaya.exception.ExceptionThrowerImpl;
import com.googlecode.kevinarpe.papaya.exception.ThrowableToStringServiceFactory;
import com.googlecode.kevinarpe.papaya.logging.slf4j.LoggerService;
import com.googlecode.kevinarpe.papaya.logging.slf4j.LoggerServiceImpl;
import com.googlecode.kevinarpe.papaya.string.ThrowingMessageFormatterImpl;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public final class TestGlobals {

    public static final ExceptionThrower exceptionThrower =
        new ExceptionThrowerImpl(ThrowingMessageFormatterImpl.INSTANCE);

    public static final LoggerService loggerService =
        new LoggerServiceImpl(ThrowableToStringServiceFactory.DEFAULT_IMPL,
            // Intentional: We want to throw in tests when message format fails.
            ThrowingMessageFormatterImpl.INSTANCE);
}
