package com.redhat.cloud.policies.engine.handlers.util;

/**
 * A link between two resources
 *
 * @author Heiko W. Rupp
 * @since 0.0.1
 */
public class Link {

    private String rel;
    private String href;

    public Link() {
    }

    public Link(String rel, String href) {
        this.rel = rel;
        this.href = href;
    }


    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @Override
    public String toString() {
        return
                href + "; " +
                        "rel='" + rel + '\'';
    }

    /**
     * Return the link in the format of RFC 5988 Web Linking.
     *
     * See <a href="http://tools.ietf.org/html/rfc5988#page-7">RFC 5988 Web Linking</a>
     *
     * @return String that contains the link with href and rel
     */
    public String rfc5988String() {
        StringBuilder builder = new StringBuilder();
        builder.append("<")
                .append(href)
                .append(">; rel=\"")
                .append(rel)
                .append("\"");
        return builder.toString();
    }
}
