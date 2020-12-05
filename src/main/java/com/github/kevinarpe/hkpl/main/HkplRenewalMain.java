package com.github.kevinarpe.hkpl.main;

import com.googlecode.kevinarpe.papaya.exception.ThrowableUtils;
import com.googlecode.kevinarpe.papaya.java_mail.TextMimeSubType;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.Chrome;
import com.github.kevinarpe.hkpl.AppContext;
import com.github.kevinarpe.hkpl.AppContextImp;
import com.github.kevinarpe.hkpl.args.CommandLineArguments;
import com.github.kevinarpe.hkpl.args.HkplRenewalMainArgs;
import com.github.kevinarpe.hkpl.email.HkplEmailServiceBuilderFactory;
import com.github.kevinarpe.hkpl.email.HkplSendEmailService;
import com.github.kevinarpe.hkpl.email.HkplSendEmailServiceBuilder;
import com.github.kevinarpe.hkpl.web.HkplWebCheckedOutService;
import com.github.kevinarpe.hkpl.web.HkplWebRenewalService;
import com.github.kevinarpe.hkpl.web.HkplWebUserCredentials;
import com.github.kevinarpe.hkpl.web.RetryStrategyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public final class HkplRenewalMain {

    private static final Logger logger = LoggerFactory.getLogger(HkplRenewalMain.class);

    public static void main(String[] argArr)
    throws Exception {

        final HkplRenewalMainArgs args = new HkplRenewalMainArgs();
        CommandLineArguments.parseArgsOrExitOnFailure("hong-kong-public-library-renewal", argArr, args);

        final HkplSendEmailService hkplSendEmailService = _createHkplEmailService(args);
        try {
            _main(args, hkplSendEmailService);
        }
        catch (Exception e) {

            logger.error("Unexpected error", e);
            final String eStr = ThrowableUtils.toStringWithStackTrace(e);
            hkplSendEmailService.sendMessage("Unexpected error", TextMimeSubType.PLAIN, eStr);
            System.exit(1);  // non-zero / failure
        }
    }

    private static void
    _main(HkplRenewalMainArgs args, HkplSendEmailService hkplSendEmailService)
    throws Exception {

        final AppContext appContext = new AppContextImp(hkplSendEmailService);

        final Chrome chrome =
            appContext.getChromeLauncherService().launchChrome(
                args.isChromeHeadless(),
                appContext.getRetryStrategyMap().getByEnum(RetryStrategyType.SHORT));

        final HkplWebUserCredentials userCredentials =
            new HkplWebUserCredentials(args.hkplWebUsername(), args.hkplWebPassword());

        if (appContext.getHkplWebLoginService().tryDoLogin(chrome, userCredentials)) {

            final HkplWebCheckedOutService.Result checkedOut = appContext.getHkplWebCheckedOutService().parse(chrome);

            final int daysBeforeLastDay = 1;
            final HkplWebRenewalService.Result renewalResult =
                appContext.getHkplWebRenewalService().renew(chrome, checkedOut, daysBeforeLastDay);

            appContext.getHkplWebRenewalEmailService().sendMessage(renewalResult);

            checkedOut.chromeTab.awaitClose(appContext.getRetryStrategyMap().get(RetryStrategyType.SHORT));
        }
        chrome.chromeTab0.awaitClose(appContext.getRetryStrategyMap().get(RetryStrategyType.SHORT));
        chrome.chromeLauncher.close();
        logger.info("Done");
    }

    private static HkplSendEmailService
    _createHkplEmailService(HkplRenewalMainArgs args) {

        final HkplSendEmailServiceBuilder b =
            HkplEmailServiceBuilderFactory.INSTANCE.newInstance()
                .smtpHost(args.smtpHost(), args.smtpPort(), args.smtpAlwaysTrustSsl())
                .fromEmailAddress(args.emailAddress())
                .toEmailAddress(args.emailAddress());

        if (null != args.nullableSmtpUsername()) {

            b.smtpUsernameAndPassword(args.nullableSmtpUsername(), args.nullableSmtpPassword());
        }
        final HkplSendEmailService x = b.build();
        return x;
    }
}
