package com.spin.mybatis;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/4/2</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class DataPermissionTest {
    private static final Pattern DATA_PERMISSION_PATTERN = Pattern.compile("~DataPerm-(\\w+)(-([^~]+))?~", Pattern.DOTALL);

    @Test
    void testIntc() {
        String sql = "UPDATE uaac_approval_flow\n" +
            "        ~DataPerm-mmmm-WHERE 1=1 OR~" +
            "        SET approval_opinion = #{approvalFlow.approvalOpinion},\n" +
            "        \tupdate_by = #{approvalFlow.updateBy},\n" +
            "        \tupdate_username = #{approvalFlow.updateUsername},\n" +
            "        ~DataPerm-dd~" +
            "        \tupdate_time = NOW()\n" +
            "        WHERE id = #{approvalFlow.id}\n" +
            "        ~DataPerm-ss-AND~";

        Matcher matcher = DATA_PERMISSION_PATTERN.matcher(sql);
        while (matcher.find()) {
            sql = matcher.replaceFirst("【" + matcher.group(3) + "******" + matcher.group(1) + "】");
            matcher = DATA_PERMISSION_PATTERN.matcher(sql);
        }

        System.out.println(sql);
    }


}
