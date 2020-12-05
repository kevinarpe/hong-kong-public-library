package com.googlecode.kevinarpe.papaya.web.jericho_html;

import net.htmlparser.jericho.Attributes;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public interface JerichoHtmlAttributeMatcher {

    boolean isMatch(Attributes attributes);

    @Override
    String toString();
}
