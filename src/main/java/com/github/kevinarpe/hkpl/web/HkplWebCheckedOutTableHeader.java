package com.github.kevinarpe.hkpl.web;

import com.googlecode.kevinarpe.papaya.argument.ObjectArgs;

import java.util.regex.Pattern;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public enum HkplWebCheckedOutTableHeader {

    // Ex: (1) "Select for renewal", (2) "Title", (3) "Units", (4) "Barcode", (5) "Due Date", (6) "Times Renewed"
    RENEWAL_CHECKBOX(
        Pattern.compile("^\\s*Select\\s+for\\s+renewal\\s*$", Pattern.CASE_INSENSITIVE)),
    TITLE(
        Pattern.compile("^\\s*Title\\s*$", Pattern.CASE_INSENSITIVE)),
    UNITS(
        Pattern.compile("^\\s*Units\\s*$", Pattern.CASE_INSENSITIVE)),
    BARCODE(
        Pattern.compile("^\\s*Barcode\\s*$", Pattern.CASE_INSENSITIVE)),
    DUE_DATE(
        Pattern.compile("^\\s*Due\\s+Date\\s*$", Pattern.CASE_INSENSITIVE)),
    RENEWAL_COUNT(
        Pattern.compile("^\\s*Times\\s+Renewed\\s*$", Pattern.CASE_INSENSITIVE)),
    ;

    public final Pattern pattern;

    private HkplWebCheckedOutTableHeader(Pattern pattern) {

        this.pattern = ObjectArgs.checkNotNull(pattern, "pattern");
    }
}
