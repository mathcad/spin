package org.spin.data.sql;

import org.junit.jupiter.api.Test;
import org.spin.core.util.MapUtils;
import org.spin.data.core.UserEnumColumn;
import org.spin.data.sql.resolver.BeetlResolver;
import org.spin.data.sql.resolver.TemplateResolver;

/**
 * <p>Created by xuweinan on 2018/3/2.</p>
 *
 * @author xuweinan
 */
public class BeetlTest {

    @Test
    public void testTmpl() {
        String template = "```js\n" +
            "var table = '';\n" +
            "var v = value;\n" +
            "switch (v) {\n" +
            "    case 1:\n" +
            "        table = 'demo1';\n" +
            "        break;\n" +
            "    case 2:\n" +
            "        table=\"demo2\";\n" +
            "        break;\n" +
            "}\n" +
            "```\n" +
            "select ${enum('org.spin.data.sql.BeetlTest$Type', 'a.type', 'type')} from ${table};";
        System.out.println(template);
        TemplateResolver resolver = new BeetlResolver();
        System.out.println(resolver.resolve("1", template, MapUtils.ofMap("value", 1)));
    }

    public enum Type implements UserEnumColumn {
        C(1), D(2);

        private int value;
        Type(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }
}

