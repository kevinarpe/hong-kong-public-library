package com.github.kevinarpe.hkpl;

import com.github.kevinarpe.hkpl.email.HkplSendEmailService;
import com.github.kevinarpe.hkpl.web.HkplWebCheckedOutService;
import com.github.kevinarpe.hkpl.web.HkplWebCheckedOutServiceImp;
import com.github.kevinarpe.hkpl.web.HkplWebLoginService;
import com.github.kevinarpe.hkpl.web.HkplWebLoginServiceImp;
import com.github.kevinarpe.hkpl.web.HkplWebRenewalEmailService;
import com.github.kevinarpe.hkpl.web.HkplWebRenewalEmailServiceImp;
import com.github.kevinarpe.hkpl.web.HkplWebRenewalService;
import com.github.kevinarpe.hkpl.web.HkplWebRenewalServiceImp;
import com.github.kevinarpe.hkpl.web.RetryStrategyType;
import com.googlecode.kevinarpe.papaya.container.ImmutableFullEnumMap;
import com.googlecode.kevinarpe.papaya.exception.ExceptionThrower;
import com.googlecode.kevinarpe.papaya.exception.ExceptionThrowerImpl;
import com.googlecode.kevinarpe.papaya.exception.ThrowableToStringServiceFactory;
import com.googlecode.kevinarpe.papaya.function.retry.BasicRetryStrategyImp;
import com.googlecode.kevinarpe.papaya.function.retry.CollectionIndexMatcher;
import com.googlecode.kevinarpe.papaya.function.retry.RetryService;
import com.googlecode.kevinarpe.papaya.function.retry.RetryServiceImp;
import com.googlecode.kevinarpe.papaya.function.retry.RetryStrategyFactory;
import com.googlecode.kevinarpe.papaya.logging.slf4j.LoggerService;
import com.googlecode.kevinarpe.papaya.logging.slf4j.LoggerServiceImpl;
import com.googlecode.kevinarpe.papaya.string.MessageFormatter;
import com.googlecode.kevinarpe.papaya.string.MessageFormatterImpl;
import com.googlecode.kevinarpe.papaya.time.Clock;
import com.googlecode.kevinarpe.papaya.time.SystemClockImpl;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.ChromeDevToolsAppContext;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.ChromeLauncherService;
import com.googlecode.kevinarpe.papaya.web.jericho_html_parser.JerichoHtmlParserService;
import com.googlecode.kevinarpe.papaya.web.jericho_html_parser.JerichoHtmlParserServiceImp;

import java.time.Duration;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public final class AppContextImp
implements AppContext {

    private final ChromeLauncherService chromeLauncherService;
    private final HkplWebLoginService hkplWebLoginService;
    private final ImmutableFullEnumMap<RetryStrategyType, RetryStrategyFactory> retryStrategyMap;
    private final HkplWebCheckedOutService hkplWebCheckedOutService;
    private final HkplWebRenewalService hkplWebRenewalService;
    private final HkplWebRenewalEmailService hkplWebRenewalEmailService;

    @Override
    public ChromeLauncherService
    getChromeLauncherService() {
        return chromeLauncherService;
    }

    @Override
    public HkplWebLoginService
    getHkplWebLoginService() {
        return hkplWebLoginService;
    }

    @Override
    public ImmutableFullEnumMap<RetryStrategyType, RetryStrategyFactory>
    getRetryStrategyMap() {
        return retryStrategyMap;
    }

    @Override
    public HkplWebCheckedOutService
    getHkplWebCheckedOutService() {
        return hkplWebCheckedOutService;
    }

    @Override
    public HkplWebRenewalService
    getHkplWebRenewalService() {
        return hkplWebRenewalService;
    }

    @Override
    public HkplWebRenewalEmailService
    getHkplWebRenewalEmailService() {
        return hkplWebRenewalEmailService;
    }

    public AppContextImp(HkplSendEmailService hkplSendEmailService) {

        final MessageFormatter messageFormatter = MessageFormatterImpl.INSTANCE;

        final ExceptionThrower exceptionThrower = new ExceptionThrowerImpl(messageFormatter);

        final ThrowableToStringServiceFactory throwableToStringServiceFactory =
            ThrowableToStringServiceFactory.DEFAULT_IMPL;

        final LoggerService loggerService =
            new LoggerServiceImpl(
                throwableToStringServiceFactory,
                messageFormatter);

        final ChromeDevToolsAppContext chromeDevToolsAppContext = new ChromeDevToolsAppContext(messageFormatter);

        this.chromeLauncherService = chromeDevToolsAppContext.chromeLauncherService;

        final RetryService retryService = new RetryServiceImp(CollectionIndexMatcher.FIRST_AND_LAST_ONLY);

        this.retryStrategyMap =
            ImmutableFullEnumMap.<RetryStrategyType, RetryStrategyFactory>builder(RetryStrategyType.class)
                .put(RetryStrategyType.SHORT,
                    BasicRetryStrategyImp.factoryBuilder()
                        .maxRetryCount(9)
                        .beforeRetrySleepDuration(Duration.ofMillis(100))
                        .build())
                .put(RetryStrategyType.LONG,
                    BasicRetryStrategyImp.factoryBuilder()
                        .maxRetryCount(9)
                        .beforeRetrySleepDuration(Duration.ofSeconds(1))
                        .build())
                .build();

        this.hkplWebLoginService =
            new HkplWebLoginServiceImp(
                chromeDevToolsAppContext.chromeDevToolsDomQuerySelectorFactory,
                retryService,
                retryStrategyMap.getByEnum(RetryStrategyType.SHORT),
                loggerService,
                exceptionThrower);

        final JerichoHtmlParserService jerichoHtmlParserService =
            new JerichoHtmlParserServiceImp(exceptionThrower);

        this.hkplWebCheckedOutService =
            new HkplWebCheckedOutServiceImp(
                chromeDevToolsAppContext.chromeService2,
                retryStrategyMap,
                chromeDevToolsAppContext.chromeDevToolsDomQuerySelectorFactory,
                jerichoHtmlParserService,
                loggerService,
                exceptionThrower);

        final Clock clock = new SystemClockImpl();

        this.hkplWebRenewalService =
            new HkplWebRenewalServiceImp(
                clock,
                chromeDevToolsAppContext.chromeDevToolsDomQuerySelectorFactory,
                retryStrategyMap,
                chromeDevToolsAppContext.chromeDevToolsRuntimeService,
                chromeDevToolsAppContext.chromeService2,
                loggerService,
                exceptionThrower);

        this.hkplWebRenewalEmailService = new HkplWebRenewalEmailServiceImp(hkplSendEmailService);
    }
}
