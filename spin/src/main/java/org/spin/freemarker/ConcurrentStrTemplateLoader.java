package org.spin.freemarker;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.utility.StringUtil;

import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ConcurrentStrTemplateLoader implements TemplateLoader {
    private final Map<String, StringTemplateSource> templates = new ConcurrentHashMap<>();

    public void putTemplate(String name, String templateSource) {
        putTemplate(name, templateSource, System.currentTimeMillis());
    }

    public void putTemplate(String name, String templateSource, long lastModified) {
        templates.put(name, new StringTemplateSource(name, templateSource, lastModified));
    }

    @Override
    public void closeTemplateSource(Object templateSource) {
    }

    @Override
    public Object findTemplateSource(String name) {
        return templates.get(name);
    }

    public boolean containsTemplate(String name) {
        return this.templates.get(name) != null;
    }

    @Override
    public long getLastModified(Object templateSource) {
        return ((ConcurrentStrTemplateLoader.StringTemplateSource) templateSource).lastModified;
    }

    @Override
    public Reader getReader(Object templateSource, String encoding) {
        return new StringReader(((ConcurrentStrTemplateLoader.StringTemplateSource) templateSource).source);
    }

    private static class StringTemplateSource {
        private final String name;
        private final String source;
        private final long lastModified;

        StringTemplateSource(String name, String source, long lastModified) {
            if (name == null) {
                throw new IllegalArgumentException("name == null");
            }
            if (source == null) {
                throw new IllegalArgumentException("source == null");
            }
            if (lastModified < -1L) {
                throw new IllegalArgumentException("lastModified < -1L");
            }
            this.name = name;
            this.source = source;
            this.lastModified = lastModified;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof StringTemplateSource && name.equals(((StringTemplateSource) obj).name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClassNameForToString(this));
        sb.append("(Map { ");
        int cnt = 0;
        for (String s : templates.keySet()) {
            cnt++;
            if (cnt != 1) {
                sb.append(", ");
            }
            if (cnt > 10) {
                sb.append("...");
                break;
            }
            sb.append(StringUtil.jQuote(s));
            sb.append("=...");
        }
        if (cnt != 0) {
            sb.append(' ');
        }
        sb.append("})");
        return sb.toString();
    }

    public static String getClassNameForToString(TemplateLoader templateLoader) {
        final Class tlClass = templateLoader.getClass();
        final Package tlPackage = tlClass.getPackage();
        return tlPackage == Configuration.class.getPackage() || tlPackage == TemplateLoader.class.getPackage()
                ? getSimpleName(tlClass) : tlClass.getName();
    }

    // [Java 5] Replace with Class.getSimpleName()
    private static String getSimpleName(final Class tlClass) {
        final String name = tlClass.getName();
        int lastDotIdx = name.lastIndexOf('.');
        return lastDotIdx < 0 ? name : name.substring(lastDotIdx + 1);
    }
}

