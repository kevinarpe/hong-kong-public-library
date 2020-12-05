package com.googlecode.kevinarpe.papaya.web.jericho_html;

import com.googlecode.kevinarpe.papaya.annotation.EmptyContainerAllowed;
import com.googlecode.kevinarpe.papaya.annotation.ReadOnlyContainer;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Segment;

import java.util.Collection;
import java.util.List;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public interface JerichoHtmlParserService {

    /**
     * This is a convenience method to call
     * {@link #getNonEmptyElementListByTagName(JerichoHtmlSource, Segment, String)}
     */
    @ReadOnlyContainer
    List<Element>
    getNonEmptyElementListByTag(JerichoHtmlSource source,
                                Segment jerichoHtmlSegment,
                                HtmlElementTag htmlElementTag)
    throws Exception;

    /**
     * @param htmlElementTag
     *        case insensitive
     *
     * @throws Exception
     *         if zero matching elements are found
     */
    @ReadOnlyContainer
    List<Element>
    getNonEmptyElementListByTagName(JerichoHtmlSource source,
                                    Segment jerichoHtmlSegment,
                                    String htmlElementTag)
    throws Exception;

    /**
     * This is a convenience method to call
     * {@link #getElementByTagNameAndAttributes(JerichoHtmlSource, Segment, String, Collection)}
     */
    Element
    getElementByTagAndAttributes(JerichoHtmlSource source,
                                 Segment jerichoHtmlSegment,
                                 HtmlElementTag htmlElementTag,
                                 @EmptyContainerAllowed
                                 @ReadOnlyContainer
                                 Collection<? extends JerichoHtmlAttributeMatcher> matchers)
    throws Exception;

    /**
     * @param htmlElementTag
     *        case insensitive
     *
     * @throws Exception
     *         if zero or more than one matching element is found
     */
    Element
    getElementByTagNameAndAttributes(JerichoHtmlSource source,
                                     Segment jerichoHtmlSegment,
                                     String htmlElementTag,
                                     @EmptyContainerAllowed
                                     @ReadOnlyContainer
                                     Collection<? extends JerichoHtmlAttributeMatcher> matchers)
    throws Exception;
}
