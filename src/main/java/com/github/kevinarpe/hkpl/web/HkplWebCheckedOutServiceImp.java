package com.github.kevinarpe.hkpl.web;

import com.github.kklisura.cdt.services.types.ChromeTab;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.googlecode.kevinarpe.papaya.annotation.EmptyContainerAllowed;
import com.googlecode.kevinarpe.papaya.annotation.ReadOnlyContainer;
import com.googlecode.kevinarpe.papaya.argument.CollectionArgs;
import com.googlecode.kevinarpe.papaya.argument.IntArgs;
import com.googlecode.kevinarpe.papaya.argument.ObjectArgs;
import com.googlecode.kevinarpe.papaya.container.ImmutableFullEnumMap;
import com.googlecode.kevinarpe.papaya.exception.ExceptionThrower;
import com.googlecode.kevinarpe.papaya.function.count.AnyCountMatcher;
import com.googlecode.kevinarpe.papaya.function.retry.RetryStrategyFactory;
import com.googlecode.kevinarpe.papaya.logging.slf4j.LoggerLevel;
import com.googlecode.kevinarpe.papaya.logging.slf4j.LoggerService;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.Chrome;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.ChromeDevToolsDomNode;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.ChromeDevToolsDomQuerySelectorFactory;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.ChromeDevToolsRuntimeService;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.ChromeDevToolsTab;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.ChromeService2;
import com.googlecode.kevinarpe.papaya.web.jericho_html.HtmlElementTag;
import com.googlecode.kevinarpe.papaya.web.jericho_html.JerichoHtmlParserService;
import com.googlecode.kevinarpe.papaya.web.jericho_html.JerichoHtmlSource;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
// Scope: Global singleton
public final class HkplWebCheckedOutServiceImp
implements HkplWebCheckedOutService {

    private static final Logger logger = LoggerFactory.getLogger(HkplWebCheckedOutServiceImp.class);

    private final ChromeService2 chromeService2;
    private final ImmutableFullEnumMap<RetryStrategyType, RetryStrategyFactory> retryStrategyMap;
    private final ChromeDevToolsRuntimeService runtimeService;
    private final ChromeDevToolsDomQuerySelectorFactory domQuerySelectorFactory;
    private final JerichoHtmlParserService jerichoHtmlParserService;
    private final LoggerService loggerService;
    private final ExceptionThrower exceptionThrower;

    public HkplWebCheckedOutServiceImp(ChromeService2 chromeService2,
                                       ImmutableFullEnumMap<RetryStrategyType, RetryStrategyFactory> retryStrategyMap,
                                       ChromeDevToolsRuntimeService runtimeService,
                                       ChromeDevToolsDomQuerySelectorFactory domQuerySelectorFactory,
                                       JerichoHtmlParserService jerichoHtmlParserService,
                                       LoggerService loggerService,
                                       ExceptionThrower exceptionThrower) {

        this.chromeService2 = ObjectArgs.checkNotNull(chromeService2, "chromeService2");
        this.retryStrategyMap = ObjectArgs.checkNotNull(retryStrategyMap, "retryStrategyMap");
        this.runtimeService = ObjectArgs.checkNotNull(runtimeService, "runtimeService");
        this.domQuerySelectorFactory =
            ObjectArgs.checkNotNull(domQuerySelectorFactory, "domQuerySelectorFactory");
        this.jerichoHtmlParserService =
            ObjectArgs.checkNotNull(jerichoHtmlParserService, "jerichoHtmlParserService");
        this.loggerService = ObjectArgs.checkNotNull(loggerService, "loggerService");
        this.exceptionThrower = ObjectArgs.checkNotNull(exceptionThrower, "exceptionThrower");
    }

    @Override
    public Result
    parse(Chrome chrome)
    throws Exception {

        final ChromeDevToolsTab chromeTab =
            chromeService2.awaitChromeTab(chrome, retryStrategyMap.get(RetryStrategyType.LONG),
                (ChromeTab _chromeTab) ->
                    _chromeTab.isPageType()
                        &&
                        "https://webcat.hkpl.gov.hk/wicket/bookmarkable/com.vtls.chamo.webapp.component.patron.PatronAccountPage?0&theme=WEB&locale=en"
                            .equals(_chromeTab.getUrl()));

        final ImmutableFullEnumMap<HkplWebCheckedOutTableHeader, Integer> tableHeaderToIndexMap =
            _createTableHeaderToIndexMap(chromeTab);

        @EmptyContainerAllowed
        final ImmutableList<Result.Row> rowList = _createRowList(chromeTab, tableHeaderToIndexMap);
        final Result x = new Result(chromeTab, rowList);
        return x;
    }

    private ImmutableFullEnumMap<HkplWebCheckedOutTableHeader, Integer>
    _createTableHeaderToIndexMap(ChromeDevToolsTab chromeTab)
    throws Exception {

        // Checked Out
        // Table headers: document.querySelectorAll("#checkout thead tr.headers")
        // Ex: (1) "Select for renewal", (2) "Title", (3) "Units", (4) "Barcode", (5) "Due Date", (6) "Times Renewed"
        // Table body: document.querySelectorAll("#checkout tbody tr")
        // Ex(1): <td><div><input type="checkbox" id="id12" name="renewalCheckboxGroup" value="check48" class="wicket-id8"/></div></td>
        // Ex(2): <td><div><a href="...">Brick Lane / Monica Ali.</a></div></td>
        // Ex(3): <td><div></div></td>
        // Ex(4): <td><div>38888101184327</div></td>
        // Ex(5): <td><div>2020-11-27</div></td>
        // Ex(6): <td><div>1 of 5</div></td>

        // Ex: "<tr class="headers"><th><span>Select for renewal</span></th>...</tr>"
        final String tableHeaderRowHtml =
            domQuerySelectorFactory.newInstance(chromeTab)
                .parentNodeIsDocument()
                .awaitQuerySelectorExactlyOneThenCall(
                    "#checkout thead tr.headers", retryStrategyMap.get(RetryStrategyType.SHORT),
                    (ChromeDevToolsDomNode n) -> n.getOuterHTML());

        final JerichoHtmlSource jerichoHtmlSourceTableHeaderRow =
            new JerichoHtmlSource(
                chromeTab.getData().chromeTab.getTitle() + ":tableHeaderRowHtml", tableHeaderRowHtml);

        @ReadOnlyContainer
        final List<Element> thElementList =
            jerichoHtmlParserService.getNonEmptyElementListByTag(
                jerichoHtmlSourceTableHeaderRow, jerichoHtmlSourceTableHeaderRow.source, HtmlElementTag.TH);

        final ImmutableFullEnumMap.Builder<HkplWebCheckedOutTableHeader, Integer> b =
            ImmutableFullEnumMap.builder(HkplWebCheckedOutTableHeader.class);

        final int size = thElementList.size();
        for (int i = 0; i < size; ++i) {

            final Element thElement = thElementList.get(i);
            final Renderer renderer = thElement.getRenderer();
            // Ex: "Select for renewal" or "Title"
            final String tableHeader = renderer.toString().stripLeading().stripTrailing();
            final HkplWebCheckedOutTableHeader header = _matchTableHeader(tableHeader);
            if (b.getReadOnlyMap().containsKey(header)) {

                final int otherThElementIndex = b.getReadOnlyMap().get(header);
                final Element otherThElement = thElementList.get(otherThElementIndex);
                final String otherTableHeader = otherThElement.getRenderer().toString().stripLeading().stripTrailing();
                throw exceptionThrower.throwCheckedException(Exception.class,
                    "Known table header %s matches multiple table headers: [%s] and [%s]",
                    header.name(), otherTableHeader, tableHeader);
            }
            b.put(header, i);
        }
        final ImmutableFullEnumMap<HkplWebCheckedOutTableHeader, Integer> x = b.build();
        return x;
    }

    private HkplWebCheckedOutTableHeader
    _matchTableHeader(String tableHeader)
    throws Exception {

        @Nullable
        HkplWebCheckedOutTableHeader nullableHeader = null;

        for (final HkplWebCheckedOutTableHeader header : HkplWebCheckedOutTableHeader.values()) {

            final Matcher matcher = header.pattern.matcher(tableHeader);
            if (matcher.find()) {

                if (null != nullableHeader) {

                    throw exceptionThrower.throwCheckedException(Exception.class,
                        "Checked Out Table Header [%s] matches two known headers: %s and %s",
                        tableHeader, nullableHeader.name(), header.name());
                }
                nullableHeader = header;
            }
        }
        if (null == nullableHeader) {

            throw exceptionThrower.throwCheckedException(Exception.class,
                "Checked Out Table Header [%s] failed to matches any known headers: %s",
                tableHeader, Joiner.on(", ").join(HkplWebCheckedOutTableHeader.values()));
        }
        return nullableHeader;
    }

    @EmptyContainerAllowed
    private ImmutableList<Result.Row>
    _createRowList(ChromeDevToolsTab chromeTab,
                   ImmutableFullEnumMap<HkplWebCheckedOutTableHeader, Integer> tableHeaderToIndexMap)
    throws Exception {

        @EmptyContainerAllowed
        final ImmutableList<ChromeDevToolsDomNode> tableBodyRowDomNodeList =
            domQuerySelectorFactory.newInstance(chromeTab)
                .parentNodeIsDocument()
                .expectedCount(AnyCountMatcher.INSTANCE)
                .awaitQuerySelectorAll("#checkout tbody tr", retryStrategyMap.get(RetryStrategyType.SHORT));

        final ImmutableList.Builder<Result.Row> rowListBuilder = ImmutableList.builder();

        final int tableBodyRowCount = tableBodyRowDomNodeList.size();
        for (int i = 0; i < tableBodyRowCount; ++i) {

            final ChromeDevToolsDomNode tableBodyRowDomNode = tableBodyRowDomNodeList.get(i);

            final String tableBodyRowHtml =
                chromeTab.getData().dom.getOuterHTML(tableBodyRowDomNode.nodeId(), null, null);

            final JerichoHtmlSource jerichoHtmlSourceTableBodyRow =
                new JerichoHtmlSource(
                    chromeTab.getData().chromeTab.getTitle() + ":tableBodyRowHtml:" + i, tableBodyRowHtml);

            @Nullable
            final _Data data = _tryGetTdElements(i, tableHeaderToIndexMap, jerichoHtmlSourceTableBodyRow);
            if (null == data) {
                continue;
            }
            @Nullable
            final Element nullableRenewalCheckboxTdElement =
                _tryGetRenewalCheckboxTdElement(i, data.tableHeaderToTdElementMap);

            final Result.Row.TdElement titleTdElement =
                data.tableHeaderToTdElementMap.get(HkplWebCheckedOutTableHeader.TITLE);
            // Ex: "Brick Lane / Monica Ali."
            final String title = titleTdElement.text;

            final LocalDate dueLocalDate = _parseDueLocalDate(i, data.tableHeaderToTdElementMap);

            final _RenewalCount renewalCount = _parseRenewalCount(i, data.tableHeaderToTdElementMap);

            final Result.Row row =
                new Result.Row(data.tdElementList, data.tableHeaderToTdElementMap, nullableRenewalCheckboxTdElement,
                    title, dueLocalDate, renewalCount.renewalCount, renewalCount.maxRenewalCount);

            rowListBuilder.add(row);
        }
        @EmptyContainerAllowed
        final ImmutableList<Result.Row> x = rowListBuilder.build();
        return x;
    }

    private static final class _Data {

        public final ImmutableList<Element> tdElementList;
        public final ImmutableFullEnumMap<HkplWebCheckedOutTableHeader, Result.Row.TdElement>
            tableHeaderToTdElementMap;

        private _Data(ImmutableList<Element> tdElementList,
                      ImmutableFullEnumMap<HkplWebCheckedOutTableHeader, Result.Row.TdElement>
                          tableHeaderToTdElementMap) {

            this.tdElementList = CollectionArgs.checkNotEmpty(tdElementList, "tdElementList");

            this.tableHeaderToTdElementMap =
                ObjectArgs.checkNotNull(tableHeaderToTdElementMap, "tableHeaderToTdElementMap");
        }
    }

    @Nullable
    private _Data
    _tryGetTdElements(final int rowIndex,
                      ImmutableFullEnumMap<HkplWebCheckedOutTableHeader, Integer> tableHeaderToIndexMap,
                      JerichoHtmlSource jerichoHtmlSourceTableBodyRow) {

        @EmptyContainerAllowed
        final ImmutableList<Element> tdElementList =
            ImmutableList.copyOf(jerichoHtmlSourceTableBodyRow.source.getAllElements(HtmlElementTag.TD.tag));

        // Usually, the last table row is a dummy row: <tr style="display: none;"><td></td></tr>
        if (tdElementList.size() < tableHeaderToIndexMap.size()) {

            loggerService.formatThenLog(logger, LoggerLevel.INFO,
                "Table row #%d: Skipping because tdElementList.size() < headerToIndexMap.size(): %d != %d",
                (1 + rowIndex), tdElementList.size(), tableHeaderToIndexMap.size());

            return null;
        }

        final ImmutableFullEnumMap.Builder<HkplWebCheckedOutTableHeader, Result.Row.TdElement> b =
            ImmutableFullEnumMap.builder(HkplWebCheckedOutTableHeader.class);

        for (final HkplWebCheckedOutTableHeader header : HkplWebCheckedOutTableHeader.values()) {

            final int index = tableHeaderToIndexMap.get(header);
            final Element tdElement = tdElementList.get(index);
            final Result.Row.TdElement tdElement2 = new Result.Row.TdElement(tdElement);
            b.put(header, tdElement2);
        }
        final ImmutableFullEnumMap<HkplWebCheckedOutTableHeader, Result.Row.TdElement> tableHeaderToTdElementMap =
            b.build();

        final _Data x = new _Data(tdElementList, tableHeaderToTdElementMap);
        return x;
    }

    private static final Pattern PATTERN_ALREADY_RENEWED =
        Pattern.compile("^Already\\s*Renewed$", Pattern.CASE_INSENSITIVE);

    @Nullable
    private Element
    _tryGetRenewalCheckboxTdElement(final int rowIndex,
                                    ImmutableFullEnumMap<HkplWebCheckedOutTableHeader, Result.Row.TdElement>
                                        tableHeaderToTdElementMap)
    throws Exception {

        final Result.Row.TdElement renewalCheckboxTdElement =
            tableHeaderToTdElementMap.get(HkplWebCheckedOutTableHeader.RENEWAL_CHECKBOX);

        if (PATTERN_ALREADY_RENEWED.matcher(renewalCheckboxTdElement.text).find()) {
            return null;
        }
        @EmptyContainerAllowed
        @ReadOnlyContainer
        final List<Element> renewalCheckboxElementList =
            renewalCheckboxTdElement.tdElement.getAllElements(HtmlElementTag.INPUT.tag);

        if (1 != renewalCheckboxElementList.size()) {

            throw exceptionThrower.throwCheckedException(Exception.class,
                "Checked Out Table Row #%d: Expected exactly one renewal checkbox, but found %d",
                (1 + rowIndex), renewalCheckboxElementList.size());
        }
        final Element x = renewalCheckboxElementList.get(0);
        return x;
    }

    private LocalDate
    _parseDueLocalDate(final int rowIndex,
                       ImmutableFullEnumMap<HkplWebCheckedOutTableHeader, Result.Row.TdElement>
                           tableHeaderToTdElementMap)
    throws Exception {

        final Result.Row.TdElement dueDateTdElement =
            tableHeaderToTdElementMap.get(HkplWebCheckedOutTableHeader.DUE_DATE);
        // Ex: "2020-11-27"
        final String dueLocalDateStr = dueDateTdElement.text;

        try {
            final LocalDate x = LocalDate.parse(dueLocalDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return x;
        }
        catch (Exception e) {
            throw exceptionThrower.throwChainedCheckedException(Exception.class,
                e,
                "Checked Out Table Row #%d: Failed to parse due date as local date: [%s]",
                (1 + rowIndex), dueLocalDateStr);
        }
    }

    private static final class _RenewalCount {

        public final int renewalCount;
        public final int maxRenewalCount;

        private _RenewalCount(final int renewalCount,
                              final int maxRenewalCount) {

            this.renewalCount = IntArgs.checkMinValue(renewalCount, 0, "renewalCount");
            this.maxRenewalCount = IntArgs.checkMinValue(maxRenewalCount, renewalCount, "maxRenewalCount");
        }
    }

    private static final Pattern RENEWAL_COUNT_PATTERN = Pattern.compile("^\\s*(\\d+)\\s*of\\s*(\\d+)\\s*$");

    private _RenewalCount
    _parseRenewalCount(final int rowIndex,
                       ImmutableFullEnumMap<HkplWebCheckedOutTableHeader, Result.Row.TdElement>
                           tableHeaderToTdElementMap)
    throws Exception {

        final Result.Row.TdElement renewalCountTdElement =
            tableHeaderToTdElementMap.get(HkplWebCheckedOutTableHeader.RENEWAL_COUNT);

        // Ex: "0 of 5" or "1 of 5"
        final String renewalCountStr = renewalCountTdElement.text;

        final Matcher matcher = RENEWAL_COUNT_PATTERN.matcher(renewalCountStr);
        if (false == matcher.find()) {

            throw exceptionThrower.throwCheckedException(Exception.class,
                "Checked Out Table Row #%d: Failed to parse renewal count string: [%s]",
                (1 + rowIndex), renewalCountStr);
        }
        // Ex: "0" or "1"
        final String renewalCountStr2 = matcher.group(1);
        int renewalCount = -1;
        try {
            renewalCount = Integer.parseInt(renewalCountStr2);
        }
        catch (Exception e) {
            throw exceptionThrower.throwChainedCheckedException(Exception.class,
                e,
                "Checked Out Table Row #%d: Renewal Count string [%s]: Failed to parse renewal count [%s] as integer",
                (1 + rowIndex), renewalCountStr, renewalCountStr2);
        }
        // Ex: "5"
        final String maxRenewalCountStr = matcher.group(2);
        int maxRenewalCount = -1;
        try {
            maxRenewalCount = Integer.parseInt(maxRenewalCountStr);
        }
        catch (Exception e) {
            throw exceptionThrower.throwChainedCheckedException(Exception.class,
                e,
                "Checked Out Table Row #%d: Renewal Count string [%s]: Failed to parse max renewal count [%s] as integer",
                (1 + rowIndex), renewalCountStr, maxRenewalCountStr);
        }
        final _RenewalCount x = new _RenewalCount(renewalCount, maxRenewalCount);
        return x;
    }
}
