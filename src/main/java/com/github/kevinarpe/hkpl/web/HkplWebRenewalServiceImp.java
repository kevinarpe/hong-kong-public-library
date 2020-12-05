package com.github.kevinarpe.hkpl.web;

import com.github.kklisura.cdt.protocol.types.dom.Node;
import com.github.kklisura.cdt.services.types.ChromeTab;
import com.google.common.collect.ImmutableList;
import com.googlecode.kevinarpe.papaya.annotation.EmptyContainerAllowed;
import com.googlecode.kevinarpe.papaya.argument.IntArgs;
import com.googlecode.kevinarpe.papaya.argument.ObjectArgs;
import com.googlecode.kevinarpe.papaya.container.ImmutableFullEnumMap;
import com.googlecode.kevinarpe.papaya.exception.ExceptionThrower;
import com.googlecode.kevinarpe.papaya.function.retry.RetryStrategyFactory;
import com.googlecode.kevinarpe.papaya.logging.slf4j.LoggerLevel;
import com.googlecode.kevinarpe.papaya.logging.slf4j.LoggerService;
import com.googlecode.kevinarpe.papaya.time.Clock;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.Chrome;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.ChromeDevToolsDomNode;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.ChromeDevToolsDomQuerySelectorFactory;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.ChromeDevToolsJavaScriptRemoteObjectType;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.ChromeDevToolsRuntimeService;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.ChromeService2;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.IncludeCommandLineAPI;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.IsNullResultAllowed;
import com.googlecode.kevinarpe.papaya.web.jericho_html.JerichoHtmlSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.regex.Pattern;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
// Scope: Global singleton
public final class HkplWebRenewalServiceImp
implements HkplWebRenewalService {

    private static final Logger logger = LoggerFactory.getLogger(HkplWebRenewalServiceImp.class);

    private final Clock clock;
    private final ChromeDevToolsDomQuerySelectorFactory domQuerySelectorFactory;
    private final ImmutableFullEnumMap<RetryStrategyType, RetryStrategyFactory> retryStrategyMap;
    private final ChromeDevToolsRuntimeService runtimeService;
    private final ChromeService2 chromeService2;
    private final LoggerService loggerService;
    private final ExceptionThrower exceptionThrower;

    public HkplWebRenewalServiceImp(Clock clock,
                                    ChromeDevToolsDomQuerySelectorFactory domQuerySelectorFactory,
                                    ImmutableFullEnumMap<RetryStrategyType, RetryStrategyFactory> retryStrategyMap,
                                    ChromeDevToolsRuntimeService runtimeService,
                                    ChromeService2 chromeService2,
                                    LoggerService loggerService,
                                    ExceptionThrower exceptionThrower) {

        this.clock = ObjectArgs.checkNotNull(clock, "clock");
        this.domQuerySelectorFactory =
            ObjectArgs.checkNotNull(domQuerySelectorFactory, "domQuerySelectorFactory");

        this.retryStrategyMap = ObjectArgs.checkNotNull(retryStrategyMap, "retryStrategyMap");
        this.runtimeService = ObjectArgs.checkNotNull(runtimeService, "runtimeService");
        this.chromeService2 = ObjectArgs.checkNotNull(chromeService2, "chromeService2");
        this.loggerService = ObjectArgs.checkNotNull(loggerService, "loggerService");
        this.exceptionThrower = ObjectArgs.checkNotNull(exceptionThrower, "exceptionThrower");
    }

    private static final Result EMPTY = new Result(ImmutableList.of(), ImmutableList.of());

    @Override
    public Result
    renew(Chrome chrome,
          HkplWebCheckedOutService.Result checkedOut,
          final int daysBeforeLastDay)
    throws Exception {

        IntArgs.checkMinValue(daysBeforeLastDay, 0, "daysBeforeLastDay");

        if (checkedOut.rowList.isEmpty()) {
            return EMPTY;
        }
        final OffsetDateTime localOffsetNow = clock.offsetNow();
        final LocalDate todayLocalDate = localOffsetNow.toLocalDate();

        // Intentional: Do this early to throw if missing.
        final ChromeDevToolsDomNode renewButtonDomNode =
            domQuerySelectorFactory.newInstance(checkedOut.chromeTab)
                .parentNodeIsDocument()
                .awaitQuerySelectorExactlyOne(
                    "#button\\.renew", retryStrategyMap.get(RetryStrategyType.SHORT));

        final Result result = _renew(checkedOut, daysBeforeLastDay, todayLocalDate);
        if (result.renewedRowList.size() > 0) {

            renewButtonDomNode.click();
            _confirmRenewal(chrome, checkedOut, result.renewedRowList);
        }
        return result;
    }

    private Result
    _renew(HkplWebCheckedOutService.Result checkedOut,
           final int daysBeforeLastDay,
           LocalDate todayLocalDate)
    throws Exception {

        final ImmutableList.Builder<HkplWebCheckedOutService.Result.Row> noMoreRenewalsAllowedRowListBuilder =
            ImmutableList.builder();
        final ImmutableList.Builder<HkplWebCheckedOutService.Result.Row> renewedRowListBuilder =
            ImmutableList.builder();

        final int size = checkedOut.rowList.size();
        for (int i = 0; i < size; ++i) {

            final HkplWebCheckedOutService.Result.Row row = checkedOut.rowList.get(i);
            final _RenewalStatus renewalStatus = _getRenewalStatus(daysBeforeLastDay, todayLocalDate, i, row);
            switch (renewalStatus) {
                case MISSING_RENEWAL_CHECKBOX: {
                    break;
                }
                case NO_MORE_REWEWALS_ALLOWED: {
                    noMoreRenewalsAllowedRowListBuilder.add(row);
                    break;
                }
                case TOO_EARLY_FOR_RENEWAL: {
                    break;
                }
                case MUST_RENEW: {
                    final String id = _getRenewalCheckboxId(i, row);
                    _tickTheBox(checkedOut, i, id);
                    renewedRowListBuilder.add(row);
                    break;
                }
                default: {
                    throw exceptionThrower.throwCheckedException(Exception.class,
                        "Internal error: Missing switch case for %s.%s",
                        renewalStatus.getClass().getSimpleName(), renewalStatus.name());
                }
            }
        }
        @EmptyContainerAllowed
        final ImmutableList<HkplWebCheckedOutService.Result.Row> renewedRowList = renewedRowListBuilder.build();

        @EmptyContainerAllowed
        final ImmutableList<HkplWebCheckedOutService.Result.Row> noMoreRenewalsAllowedRowList =
            noMoreRenewalsAllowedRowListBuilder.build();

        final Result x = new Result(noMoreRenewalsAllowedRowList, renewedRowList);
        return x;
    }

    private enum _RenewalStatus {

        MISSING_RENEWAL_CHECKBOX,
        NO_MORE_REWEWALS_ALLOWED,
        TOO_EARLY_FOR_RENEWAL,
        MUST_RENEW,
        ;
    }

    private _RenewalStatus
    _getRenewalStatus(final int daysBeforeLastDay,
                      LocalDate todayLocalDate,
                      final int rowIndex,
                      HkplWebCheckedOutService.Result.Row row) {

        if (null == row.nullableRenewalCheckboxTdElement) {

            loggerService.formatThenLog(logger, LoggerLevel.INFO,
                "Checked Out Row #%d [%s]: Missing renewal checkbox -- this is OK / skip",
                (1 + rowIndex), row.title);

            return _RenewalStatus.MISSING_RENEWAL_CHECKBOX;
        }

        if (row.maxRenewalCount == row.renewalCount) {

            loggerService.formatThenLog(logger, LoggerLevel.INFO,
                "Checked Out Row #%d [%s]: No more renewals allowed: Renewed %d of %d",
                (1 + rowIndex), row.title, row.renewalCount, row.maxRenewalCount);

            return _RenewalStatus.NO_MORE_REWEWALS_ALLOWED;
        }

        final LocalDate firstRenewalLocalDate = row.dueLocalDate.minusDays(daysBeforeLastDay);
        if (todayLocalDate.compareTo(firstRenewalLocalDate) < 0) {

            loggerService.formatThenLog(logger, LoggerLevel.INFO,
                "Checked Out Row #%d [%s]: Too early for renewal: Today is %s: Allowed to renew %d day(s) before due date %s",
                (1 + rowIndex), row.title, todayLocalDate, daysBeforeLastDay, row.dueLocalDate);

            return _RenewalStatus.TOO_EARLY_FOR_RENEWAL;
        }

        return _RenewalStatus.MUST_RENEW;
    }

    private String
    _getRenewalCheckboxId(final int rowIndex,
                          HkplWebCheckedOutService.Result.Row row)
    throws Exception {

        @Nullable
        final String id = row.nullableRenewalCheckboxTdElement.getAttributeValue("id");
        if (null == id) {

            throw exceptionThrower.throwCheckedException(Exception.class,
                "Checked Out Row #%d: Renewal checkbox TD element is missing 'id' attribute: %s",
                (1 + rowIndex), row.nullableRenewalCheckboxTdElement.toString());
        }
        return id;
    }

    private void
    _tickTheBox(HkplWebCheckedOutService.Result checkedOut,
                final int rowIndex,
                String id)
    throws Exception {

        final String jsTickTheBox = String.format(
            "("
                + "%nfunction() {"
                + "%n    const elm = document.getElementById(\"%s\");"
                + "%n    elm.checked = true;"
                + "%n    return elm.checked;"
                + "%n}"
                + "%n)()",
            id);

        final Boolean isChecked =
            runtimeService.evaluateJavaScriptExpression(
                checkedOut.chromeTab.getData().runtime,
                jsTickTheBox,
                IncludeCommandLineAPI.NO,
                ChromeDevToolsJavaScriptRemoteObjectType.BOOLEAN,
                IsNullResultAllowed.NO);

        if (false == isChecked) {

            throw exceptionThrower.throwCheckedException(Exception.class,
                "Checked Out Row #%d: Failed to tick the box:"
                    + "%n<javascript>"
                    + "%n%s"
                    + "%n</javascript>"
                ,
                (1 + rowIndex), jsTickTheBox);
        }
    }

    // Ex: "Renewal Results | Chamo"
    private static final Pattern PATTERN_PAGE_TITLE_AFTER_RENEWAL =
        Pattern.compile("Renewal\\s*Results", Pattern.CASE_INSENSITIVE);

    private void
    _confirmRenewal(Chrome chrome,
                    HkplWebCheckedOutService.Result checkedOut,
                    ImmutableList<HkplWebCheckedOutService.Result.Row> renewedRowList)
    throws Exception {

        // After button click, wait for the page title to update.
        chromeService2.awaitChromeTab(chrome, retryStrategyMap.get(RetryStrategyType.LONG),
            (ChromeTab _chromeTab) ->
                _chromeTab.isPageType()
                    &&
                    PATTERN_PAGE_TITLE_AFTER_RENEWAL.matcher(_chromeTab.getTitle()).find());

        final Node documentNode = checkedOut.chromeTab.getData().dom.getDocument();
        final Integer documentNodeId = documentNode.getNodeId();
        final String html = checkedOut.chromeTab.getData().dom.getOuterHTML(documentNodeId, null, null);
        final JerichoHtmlSource jerichoHtmlSource = new JerichoHtmlSource("After renewal", html);
        final String text = jerichoHtmlSource.source.getRenderer().toString();
        final String expectedText = "Renewal Results";
        final String expectedText2 = renewedRowList.size() + " item(s) successfully renewed.";
        if (false == text.contains(expectedText)
            ||
            false == text.contains(expectedText2)) {

            throw exceptionThrower.throwCheckedException(Exception.class,
                "Failed to confirm renewal results with text [%s] and [%s]"
                    + "%n<text>"
                    + "%n%s"
                    + "%n</text>"
                    + "%n<html>"
                    + "%n%s"
                    + "%n</html>"
                ,
                expectedText, expectedText2, text, html);
        }
    }
}
