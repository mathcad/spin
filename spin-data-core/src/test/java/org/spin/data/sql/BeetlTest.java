package org.spin.data.sql;

import org.junit.jupiter.api.Test;
import org.spin.core.trait.FriendlyEnum;
import org.spin.core.util.MapUtils;
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
            "switch (value) {\n" +
            "    case 1:\n" +
            "        table = 'demo_a';\n" +
            "        break;\n" +
            "    case 2:\n" +
            "        table=\"demo_b\";\n" +
            "        break;\n" +
            "    default:\n" +
            "        table=\"undefined\";\n" +
            "}\n" +
            "```\n" +
            "select\n" +
            "    ${enum('org.spin.data.sql.BeetlTest$Type', 'a.type', 'type')}\n" +
            "from ${table} t\n" +
            "where 1=1\n" +
            "    ${valid(table, \"and t.name like '\" + table + \"'\")}\n" +
            "    ${has(flag) ? \"and t.flag = \" + flag}\n" +
            "    @var a = [1,2,3];\n" +
            "    ${valid(a, 'and t.id in (')}\n" +
            "    @for(var i=0;i<a.~size;i++){\n" +
            "        ${a[i]},\n" +
            "    @}\n" +
            "    ${valid(a, ')')}";
        System.out.println(template);
        TemplateResolver resolver = new BeetlResolver();
        System.out.println(resolver.resolve("1", template, MapUtils.ofMap("value", 3, "flag", true)));
    }

    public enum Type implements FriendlyEnum<String> {
        C("1"), D("2");

        private String value;

        Type(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }
}

