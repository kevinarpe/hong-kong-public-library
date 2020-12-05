package com.googlecode.kevinarpe.papaya.web.jericho_html;

import com.googlecode.kevinarpe.papaya.annotation.EmptyStringAllowed;
import com.googlecode.kevinarpe.papaya.argument.ObjectArgs;
import com.googlecode.kevinarpe.papaya.argument.StringArgs;
import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;

import javax.annotation.Nullable;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
// Scope: Multiple global singletons
public final class JerichoHtmlAttributeMatcherImp
implements JerichoHtmlAttributeMatcher {

    /**
     * @param attrName
     *        case insensitive
     */
    public static JerichoHtmlAttributeMatcherImp
    withoutValue(String attrName) {

        final JerichoHtmlAttributeMatcherImp x =
            new JerichoHtmlAttributeMatcherImp(attrName, false, null);
        return x;
    }

    /**
     * @param attrName
     *        case insensitive
     *
     * @param attrValue
     *        case insensitive
     */
    public static JerichoHtmlAttributeMatcherImp
    withValue(String attrName,
              @EmptyStringAllowed
              String attrValue) {

        ObjectArgs.checkNotNull(attrValue, "attrValue");
        final JerichoHtmlAttributeMatcherImp x = new JerichoHtmlAttributeMatcherImp(attrName, true, attrValue);
        return x;
    }

    private final String attrName;
    private final boolean hasValue;
    @Nullable
    @EmptyStringAllowed
    private final String nullableAttrValue;

    private JerichoHtmlAttributeMatcherImp(String attrName,
                                           boolean hasValue,
                                           @Nullable
                                           @EmptyStringAllowed
                                           String nullableAttrValue) {

        this.attrName = StringArgs.checkNotEmptyOrWhitespace(attrName, "attrName");
        this.hasValue = hasValue;
        this.nullableAttrValue = nullableAttrValue;

        if (hasValue == (null == nullableAttrValue)) {

            final String msg =
                "hasValue == (null == nullableAttrValue): " + hasValue + " != (null != " + nullableAttrValue + ")";
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public boolean
    isMatch(Attributes attributes) {

        @Nullable
        final Attribute attribute = attributes.get(attrName);
        if (null == attribute) {
            return false;
        }
        if (hasValue != attribute.hasValue()) {
            return false;
        }
        if (false == attribute.hasValue()) {
            return true;
        }
        @EmptyStringAllowed
        final String value = attribute.getValue();
        final boolean x = value.equalsIgnoreCase(nullableAttrValue);
        return x;
    }

    @Override
    public String toString() {

        if (hasValue) {

            final String x = "[" + attrName + "=" + nullableAttrValue + "]";
            return x;
        }
        else {
            return "[" + attrName + "] without value";
        }
    }
}
