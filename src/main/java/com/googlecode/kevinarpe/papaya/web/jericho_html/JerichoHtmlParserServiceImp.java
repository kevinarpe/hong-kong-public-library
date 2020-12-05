package com.googlecode.kevinarpe.papaya.web.jericho_html;

import com.googlecode.kevinarpe.papaya.annotation.EmptyContainerAllowed;
import com.googlecode.kevinarpe.papaya.annotation.ReadOnlyContainer;
import com.googlecode.kevinarpe.papaya.argument.ObjectArgs;
import com.googlecode.kevinarpe.papaya.concurrent.ThreadLocalWithReset;
import com.googlecode.kevinarpe.papaya.concurrent.ThreadLocalsWithReset;
import com.googlecode.kevinarpe.papaya.exception.ExceptionThrower;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Segment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
// Scope: Global singleton
public final class JerichoHtmlParserServiceImp
implements JerichoHtmlParserService {

    private final ExceptionThrower exceptionThrower;

    public JerichoHtmlParserServiceImp(ExceptionThrower exceptionThrower) {

        this.exceptionThrower = ObjectArgs.checkNotNull(exceptionThrower, "exceptionThrower");
    }

    @ReadOnlyContainer
    @Override
    public List<Element>
    getNonEmptyElementListByTag(JerichoHtmlSource source,
                                Segment jerichoHtmlSegment,
                                HtmlElementTag htmlElementTag)
    throws Exception {

        @ReadOnlyContainer
        final List<Element> x = getNonEmptyElementListByTagName(source, jerichoHtmlSegment, htmlElementTag.tag);
        return x;
    }

    /**
     * @param htmlElementTag
     *        case insensitive
     *
     * @throws Exception
     *         if zero matching elements are found
     */
    @ReadOnlyContainer
    @Override
    public List<Element>
    getNonEmptyElementListByTagName(JerichoHtmlSource source,
                                    Segment jerichoHtmlSegment,
                                    String htmlElementTag)
    throws Exception {

        @EmptyContainerAllowed
        @ReadOnlyContainer
        final List<Element> list = jerichoHtmlSegment.getAllElements(htmlElementTag);
        if (list.isEmpty()) {
            throw exceptionThrower.throwCheckedException(Exception.class,
                "HTML source [%s]: Failed to find any elements with tag [%s]",
                source.description, htmlElementTag);
        }
        return list;
    }

    @Override
    public Element
    getElementByTagAndAttributes(JerichoHtmlSource source,
                                 Segment jerichoHtmlSegment,
                                 HtmlElementTag htmlElementTag,
                                 @EmptyContainerAllowed
                                 @ReadOnlyContainer
                                 Collection<? extends JerichoHtmlAttributeMatcher> matchers)
    throws Exception {

        final Element x = getElementByTagNameAndAttributes(source, jerichoHtmlSegment, htmlElementTag.tag, matchers);
        return x;
    }

    private static final ThreadLocalWithReset<ArrayList<Element>> threadLocalList =
        ThreadLocalsWithReset.newInstanceForArrayList();

    @Override
    public Element
    getElementByTagNameAndAttributes(JerichoHtmlSource source,
                                     Segment jerichoHtmlSegment,
                                     String htmlElementTag,
                                     @EmptyContainerAllowed
                                     @ReadOnlyContainer
                                     Collection<? extends JerichoHtmlAttributeMatcher> matchers)
    throws Exception {

        final ArrayList<Element> matchList = threadLocalList.getAndReset();

        @ReadOnlyContainer
        final List<Element> list = getNonEmptyElementListByTagName(source, jerichoHtmlSegment, htmlElementTag);
        for (final Element element : list) {

            if (_isMatch(element, matchers)) {
                matchList.add(element);
            }
        }
        final int matchCount = matchList.size();
        switch (matchCount) {
            case 0:
                throw exceptionThrower.throwCheckedException(Exception.class,
                    "HTML source [%s]: Failed to find any elements with tag [%s] and attributes %s",
                    source.description, htmlElementTag, matchers);
            case 1:
                final Element x = matchList.get(0);
                return x;
            default:
                throw exceptionThrower.throwCheckedException(Exception.class,
                    "HTML source [%s]: Found multiple elements with tag [%s] and attributes %s",
                    source.description, htmlElementTag, matchers);
        }
    }

    private boolean
    _isMatch(Element element,
             @ReadOnlyContainer
             @EmptyContainerAllowed
             Collection<? extends JerichoHtmlAttributeMatcher> matchers) {

        final Attributes attributes = element.getAttributes();

        for (final JerichoHtmlAttributeMatcher m : matchers) {

            if (false == m.isMatch(attributes)) {
                return false;
            }
        }
        return true;
    }
}
