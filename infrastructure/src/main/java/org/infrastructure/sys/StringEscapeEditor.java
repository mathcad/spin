package org.infrastructure.sys;

import java.beans.PropertyEditorSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.infrastructure.util.StringUtils;

public class StringEscapeEditor extends PropertyEditorSupport {

    private boolean escapeHTML = true;
    private boolean escapeJavaScript = true;
    private boolean escapeSQL = true;
    private boolean escapeStyle = true;
    
    private static final String REGEX_SCRIPT = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
    private static final String REGEX_STYLE = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
    private static final String REGEX_HTML = "<[^>]+>"; // 定义HTML标签的正则表达式

    public StringEscapeEditor() {
    	super();
    }

    public StringEscapeEditor(boolean escapeHTML, boolean escapeJavaScript, boolean escapeSQL) {
    	this(escapeHTML, escapeJavaScript, escapeSQL, false);
    }
    public StringEscapeEditor(boolean escapeHTML, boolean escapeJavaScript, boolean escapeSQL, boolean escapeStyle) {
    	super();
        this.escapeHTML = escapeHTML;
        this.escapeJavaScript = escapeJavaScript;
        this.escapeSQL = escapeSQL;
        this.escapeStyle = escapeStyle;
}

    public void setAsText(String text) {
            if (text == null) {
                    setValue(null);
            } else {
                    String value = text;
                    if (escapeHTML) {
                            value = escapeHtml(value);
                    }
                    if (escapeJavaScript) {
                            value = escapeJavaScript(value);
                    }
                    if (escapeSQL) {
                            value = escapeSql(value);
                    }
                    if (escapeStyle) {
                        value = escapeStyle(value);
                }
                    setValue(value);
            }
    }

    public String getAsText() {
            Object value = getValue();
            return value != null ? value.toString() : "";
    }
    
    private static String escapeHtml(String value) {
        Pattern p_html = Pattern.compile(REGEX_HTML, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(value);
        return m_html.replaceAll(""); // 过滤html标签
    }
    
    private static String escapeJavaScript(String value) {
        Pattern p_html = Pattern.compile(REGEX_SCRIPT, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(value);
        return m_html.replaceAll(""); // 过滤script标签
    }
    
    private static String escapeStyle(String value) {
        Pattern p_html = Pattern.compile(REGEX_STYLE, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(value);
        return m_html.replaceAll(""); // 过滤style标签
    }
    
    /**
     * <p>Escapes the characters in a <code>String</code> to be suitable to pass to
     * an SQL query.</p>
     *
     * <p>For example,
     * <pre>statement.executeQuery("SELECT * FROM MOVIES WHERE TITLE='" + 
     *   StringEscapeUtils.escapeSql("McHale's Navy") + 
     *   "'");</pre>
     * </p>
     *
     * <p>At present, this method only turns single-quotes into doubled single-quotes
     * (<code>"McHale's Navy"</code> => <code>"McHale''s Navy"</code>). It does not
     * handle the cases of percent (%) or underscore (_) for use in LIKE clauses.</p>
     *
     * @param str  the string to escape, may be null
     * @return a new String, escaped for SQL, <code>null</code> if null string input
     */
    private static String escapeSql(String str) {
        if (str == null) {
            return null;
        }
        return StringUtils.replace(str, "'", "''");
    }
}
