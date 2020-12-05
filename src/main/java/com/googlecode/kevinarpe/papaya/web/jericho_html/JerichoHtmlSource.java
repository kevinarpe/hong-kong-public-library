package com.googlecode.kevinarpe.papaya.web.jericho_html;

import com.googlecode.kevinarpe.papaya.argument.StringArgs;
import net.htmlparser.jericho.Source;

/**
 * @author Kevin Connor ARPE (kevinarpe@gmail.com)
 */
public final class JerichoHtmlSource {

    public final String description;
    public final Source source;

    public JerichoHtmlSource(String description, String html) {

        this.description = StringArgs.checkNotEmptyOrWhitespace(description, "description");
        StringArgs.checkNotEmptyOrWhitespace(html, "html");
        this.source = new Source(html);
        this.source.fullSequentialParse();
    }

    public String
    getHtml() {
        final String x = source.toString();
        return x;
    }

    @Override
    public String
    toString() {
        return description;
    }
}
