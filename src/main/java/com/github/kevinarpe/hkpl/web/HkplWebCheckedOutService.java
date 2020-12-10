package com.github.kevinarpe.hkpl.web;

import com.google.common.collect.ImmutableList;
import com.googlecode.kevinarpe.papaya.annotation.EmptyContainerAllowed;
import com.googlecode.kevinarpe.papaya.annotation.EmptyStringAllowed;
import com.googlecode.kevinarpe.papaya.argument.CollectionArgs;
import com.googlecode.kevinarpe.papaya.argument.IntArgs;
import com.googlecode.kevinarpe.papaya.argument.ObjectArgs;
import com.googlecode.kevinarpe.papaya.argument.StringArgs;
import com.googlecode.kevinarpe.papaya.container.ImmutableFullEnumMap;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.Chrome;
import com.googlecode.kevinarpe.papaya.web.chrome_dev_tools.ChromeDevToolsTab;
import net.htmlparser.jericho.Element;

import javax.annotation.Nullable;
import java.time.LocalDate;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public interface HkplWebCheckedOutService {

    Result parse(Chrome chrome)
    throws Exception;

    public static final class Result {

        public final ChromeDevToolsTab chromeTab;
        @EmptyContainerAllowed
        public final ImmutableList<Result.Row> rowList;

        public Result(ChromeDevToolsTab chromeTab,
                      @EmptyContainerAllowed
                      ImmutableList<Result.Row> rowList) {

            this.chromeTab = ObjectArgs.checkNotNull(chromeTab, "chromeTab");
            this.rowList = ObjectArgs.checkNotNull(rowList, "rowList");
        }

        public static final class Row {

            public static final class TdElement {

                public final Element tdElement;
                @EmptyStringAllowed
                public final String text;

                public TdElement(Element tdElement) {

                    this.tdElement = ObjectArgs.checkNotNull(tdElement, "tdElement");
                    this.text = tdElement.getRenderer().toString().stripLeading().stripTrailing();
                }
            }

            public final ImmutableList<Element> tdElementList;
            public final ImmutableFullEnumMap<HkplWebCheckedOutTableHeader, Result.Row.TdElement> tableHeaderToTdElementMap;
            /**
             * Nullable?  Yes.  If already renewed today, the checkbox is replaced with text: "Already Renewed".
             */
            @Nullable
            public final Element nullableRenewalCheckboxTdElement;
            public final String title;
            public final LocalDate dueLocalDate;
            public final int renewalCount;
            public final int maxRenewalCount;

            public Row(ImmutableList<Element> tdElementList,
                       ImmutableFullEnumMap<HkplWebCheckedOutTableHeader, Result.Row.TdElement> tableHeaderToTdElementMap,
                       @Nullable
                       Element nullableRenewalCheckboxTdElement,
                       String title,
                       LocalDate dueLocalDate,
                       final int renewalCount,
                       final int maxRenewalCount) {

                this.tdElementList = CollectionArgs.checkNotEmpty(tdElementList, "tdElementList");

                this.tableHeaderToTdElementMap =
                    ObjectArgs.checkNotNull(tableHeaderToTdElementMap, "tableHeaderToTdElementMap");

                this.nullableRenewalCheckboxTdElement = nullableRenewalCheckboxTdElement;
                this.title = StringArgs.checkNotEmptyOrWhitespace(title, "title");
                this.dueLocalDate = ObjectArgs.checkNotNull(dueLocalDate, "dueLocalDate");
                this.renewalCount = IntArgs.checkMinValue(renewalCount, 0, "renewalCount");
                this.maxRenewalCount = IntArgs.checkMinValue(maxRenewalCount, renewalCount, "maxRenewalCount");
            }
        }
    }
}
