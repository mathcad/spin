package org.infrastructure.jpa.core.sqlmap;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by Arvin on 2016/6/25.
 */
public class SqlLoaderTest {
    @Test
    public void testGetSql() {
        Map<String, String> param = new HashMap<>();
        param.put("no", "pp");
        SqlLoader loader = new SqlLoader();
        loader.setSubDir("sqlmap");
        String template = loader.getSql("product.findProductTarget", param);
        template = loader.getSql("product.findProductTarget", param);
        template = loader.getSql("product.findProductTarget", param);
        System.out.println(template);
        assertTrue(true);
    }
}