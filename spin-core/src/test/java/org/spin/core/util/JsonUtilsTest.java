package org.spin.core.util;

import org.junit.jupiter.api.Test;
import org.spin.core.gson.annotation.DatePattern;
import org.spin.core.gson.annotation.PreventOverflow;
import org.spin.core.gson.annotation.SerializedName;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.trait.Evaluatable;
import org.spin.core.trait.IntEvaluatable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/4/4</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class JsonUtilsTest {
    void test1() {
        String json = "[{types: [1,2]}]";

        Vos type = JsonUtils.fromJson("{type:1}", Vos.class);
        ServiceType serviceType = JsonUtils.fromJson("1", ServiceType.class);
        List<Vo> vos = JsonUtils.fromJson(json, new TypeToken<List<Vo>>() {
        });
        Vo[] vos1 = JsonUtils.fromJson(json, new TypeToken<Vo[]>() {
        });
        System.out.println();
    }


    @Test
    public void testEntityId() throws IOException {
//        E a = new E();
//        a.setId(81241321817279489L);
//        a.setXxx(LocalDateTime.now());
//        a.setExt(91241321817279489L);
//        BufferedWriter fos = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("D:\\res.log"))));
//        for (int i = 0; i < 1000000; ++i) {
//            a.setId(a.getId() + 1L);
//            fos.write(JsonUtils.toJson(a));
//            fos.newLine();
//        }
//        fos.close();
        String b = "{\"status\":\"2\", \"id\":81241321817279489,\"create_user_id\":'9007299254740992',\"updateUserId\":2,\"version\":0,\"orderNo\":0.0,\"valid\":true,xxx:'2018031212', first: 'Neptune'}";
        E c = JsonUtils.fromJsonWithUnderscore(b, E.class);
        System.out.println(c);
    }


    @Test
    void testMalformedType() {
        String s = JsonUtils.toJson(CollectionUtils.divide(CollectionUtils.ofArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 5));

        System.out.println(s);

        Vo1 vo = new Vo1();
        int[] ids = new int[2];
        ids[0] = 1;
        ids[1] = 2;
        vo.setIds(CollectionUtils.divide(CollectionUtils.ofArrayList(ids, ids), 2).get(0));


        System.out.println(JsonUtils.toJson(vo));

        Vo2 vo2 = new Vo2();

        User<String> user = new User<>();
        user.setId(1L);
        user.setName("aaaa");
        user.setValue("bbbb");

        vo2.setUser(user);


        System.out.println(JsonUtils.toJson(vo2));

        byte[] array = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        System.out.println(JsonUtils.toJson(ArrayUtils.divide(array, 7)));
    }


    @Test
    void testSetLong() {
        Set<Long> values = CollectionUtils.ofHashSet(1321645046073704449L, 1L, 2L, 1321645046916759554L);

        De de = new De();
        de.setVals(values);


        System.out.println(JsonUtils.toJson(de));
        System.out.println(JsonUtils.getDefaultGson().toJson(de));
        System.out.println(JsonUtils.toJson(values));

    }

    public static class De {

//        @PreventOverflow
        private Set<Long> vals;

        public Set<Long> getVals() {
            return vals;
        }

        public void setVals(Set<Long> vals) {
            this.vals = vals;
        }
    }

    String json = "[\n" +
        "    {\n" +
        "        \"updateUser\": \"\\u9ece\\u827a\\u6cc9\",\n" +
        "        \"employmentDate\": \"2014-04-18 00:00:00\",\n" +
        "        \"createId\": \"77\",\n" +
        "        \"updateId\": \"77\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"00ptbnd1442118981603852744768160\",\n" +
        "        \"createUser\": \"\\u9ece\\u827a\\u6cc9\",\n" +
        "        \"staffFrom\": \"BND\",\n" +
        "        \"staffId\": 109128,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"id\": \"00ptbnd1521343304160404267922816\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"employmentDate\": \"2017-04-21 11:00:00\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"id\": \"a9d14c970d3b40598a242f567026be44\",\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"00ptbnd1718471759160524663580416\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffId\": 1486100,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XQF\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"rpa\\u673a\\u5668\\u4eba\",\n" +
        "        \"staffId\": 1800146,\n" +
        "        \"createId\": \"79\",\n" +
        "        \"updateId\": \"79\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"00ptbnd1938990080160551827869116\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"createUser\": \"rpa\\u673a\\u5668\\u4eba\",\n" +
        "        \"id\": \"00ptbnd1135904203161052261580416\",\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"BND\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"rpa\\u673a\\u5668\\u4eba\",\n" +
        "        \"staffId\": 1800139,\n" +
        "        \"createId\": \"79\",\n" +
        "        \"updateId\": \"79\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"00ptbnd2014047038159669374138315\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"createUser\": \"rpa\\u673a\\u5668\\u4eba\",\n" +
        "        \"id\": \"00ptbnd8029631511610357996638161\",\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"BND\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"rpa\\u673a\\u5668\\u4eba\",\n" +
        "        \"staffId\": 1630109,\n" +
        "        \"createId\": \"79\",\n" +
        "        \"updateId\": \"79\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"00ptbnd3881717731606991208401160\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"createUser\": \"rpa\\u673a\\u5668\\u4eba\",\n" +
        "        \"id\": \"00ptbnd1676075191161423418044816\",\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"BND\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"rpa\\u673a\\u5668\\u4eba\",\n" +
        "        \"staffId\": 1516984,\n" +
        "        \"createId\": \"79\",\n" +
        "        \"updateId\": \"79\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"00ptbnd8217287011607753405569160\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"createUser\": \"rpa\\u673a\\u5668\\u4eba\",\n" +
        "        \"id\": \"00ptbnd1523782392161113077016916\",\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"BND\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"staffId\": 1800150,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"companyOpenId\": \"01b3f10590de11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"2f9ca89805014e83b4432a35e85c6538\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XJS\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"status\": 1,\n" +
        "        \"companyOpenId\": \"01b3f3b690de11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffFrom\": \"XJS\",\n" +
        "        \"id\": \"c88597dc7ce646978523bf872a3a6580\",\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isMainCompany\": 0\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"mq_consumer\",\n" +
        "        \"employmentDate\": \"2014-04-18 00:00:00\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"id\": \"ef77ebb7b4fa4a588b3af9030fcb949c\",\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"01b41b7290de11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffId\": 1185805,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XQC\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"\\u9648\\u653f\\u96c4\",\n" +
        "        \"employmentDate\": \"2019-10-12 00:00:00\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"updateId\": \"35762e5b51de4d34a63122cb7ecbd9d8\",\n" +
        "        \"staffId\": 922753,\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"02b35f3dadf74a0cb7adca4bedc5ffe0\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"createUser\": \"mq_consumer\",\n" +
        "        \"id\": \"9b134d5daf8a4a2ab5e78760d183bd0b\",\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XJS\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"employmentDate\": \"2019-12-16 14:31:08\",\n" +
        "        \"createId\": \"\",\n" +
        "        \"updateId\": \"\",\n" +
        "        \"staffId\": 1739988,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"05dda3cc89e340fab0506d9001d2c80f\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"id\": \"00ptbnd1866484721615630701288161\",\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"FLJR\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"staffId\": 1800141,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"companyOpenId\": \"08e0c39b719d48e798153ecd685c1ce8\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"7c2ea8238318416a93a559a87abc6472\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XJS\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"employmentDate\": \"2019-09-11 18:05:18\",\n" +
        "        \"createId\": \"\",\n" +
        "        \"updateId\": \"\",\n" +
        "        \"staffId\": 1764322,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"0976a292a9a44eed942358e33547f1ff\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"id\": \"00ptbnd9901706961615690082117161\",\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"FLJR\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"staffId\": 252138,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"companyOpenId\": \"1165d5b6c69b4881808422dbdf5a5f62\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"00ptbnd2939745841599737818382159\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XFH\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"staffId\": 1102110,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"companyOpenId\": \"1e35c995df6740099bf5adc92368f5a2\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"dfe4426de30a411d927abf996b34fbfb\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"SZFL\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"mq_consumer\",\n" +
        "        \"staffId\": 755082,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"24228abb157c4bc88a19dd26f04f1e80\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"createUser\": \"mq_consumer\",\n" +
        "        \"id\": \"72349e9655e3473cbbe26dd408a5f995\",\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XJS\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"employmentDate\": \"2020-05-13 00:00:00\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"staffId\": 917966,\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"36e5cae78fa845549962a8e9fdaad87d\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"createUser\": \"mq_consumer\",\n" +
        "        \"id\": \"991788149d3e4b46a92f0974ac51cec9\",\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XJS\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"staffId\": 1800140,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"companyOpenId\": \"3a2ccbdb82f14fad8ac3c03cc96e7015\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"2331e8ce45fc4fa9ac2b79a90af03848\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XJS\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"status\": 1,\n" +
        "        \"companyOpenId\": \"4416f62f3f644815944242abd506725d\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffFrom\": \"XJS\",\n" +
        "        \"id\": \"9fe4f8bc2c624ef8b3c56463444594dd\",\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isMainCompany\": 0\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"\\u8983\\u950b\",\n" +
        "        \"employmentDate\": \"2020-06-09 00:00:00\",\n" +
        "        \"createId\": \"12eeb14e09db4ab39da87ed28fbd62d8\",\n" +
        "        \"updateId\": \"12eeb14e09db4ab39da87ed28fbd62d8\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"4b8a0774f85e46d488eedacbc661038b\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"createUser\": \"\\u8983\\u950b\",\n" +
        "        \"staffFrom\": \"BND\",\n" +
        "        \"staffId\": 249920,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"id\": \"00ptbnd2833324711591692558064159\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"employmentDate\": \"2020-02-06 00:00:00\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"id\": \"51fc8c012705403b9a921615e305734e\",\n" +
        "        \"status\": -2,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"548f9e9ba87640e9bdf727fbefa72932\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffId\": 615166,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"SZFL\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"mq_consumer\",\n" +
        "        \"staffId\": 1098117,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"updateId\": \"961704fa821111e99c7e7cd30ad3a6a8\",\n" +
        "        \"companyOpenId\": \"642edaee0f5e4f639113fd1dbdc3dd08\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"df49c761115d43c09a9204074e8cf7f2\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XQC\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"staffId\": 1323276,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"companyOpenId\": \"6b8380869d514707a70ad049590533ca\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"cf47a8e88b704109a6dc639113459413\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XQF\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"\\u4f0d\\u6893\\u6b23\",\n" +
        "        \"staffId\": 899241,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"updateId\": \"94\",\n" +
        "        \"companyOpenId\": \"6b85d2018f9240eca9eda048e890b908\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"916e0294ce4843df9f54c9b8d628ef13\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"SZFL\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"\\u5f20\\u589e\\u8f89\",\n" +
        "        \"staffId\": 1053571,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"updateId\": \"00ptbnd4553410251605751772960160\",\n" +
        "        \"companyOpenId\": \"71994ce894dd4a4d8d1a2ff88a65e5df\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"d0d7091ec2164f0fa45d453035766264\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XJS\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"status\": 1,\n" +
        "        \"companyOpenId\": \"7715d9bcad134b849b224178258c2236\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffFrom\": \"XJS\",\n" +
        "        \"id\": \"c18a4da1a20c4b25a6bd655064dbf854\",\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isMainCompany\": 0\n" +
        "    },\n" +
        "    {\n" +
        "        \"employmentDate\": \"2020-02-07 00:00:00\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"id\": \"91cecec7ce9c4855899ce06443d5489b\",\n" +
        "        \"status\": -2,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"7724c7b9e5b042778ff5828013074e8d\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffId\": 900174,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"SZFL\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"\\u4f0d\\u6893\\u6b23\",\n" +
        "        \"staffId\": 1064068,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"updateId\": \"94\",\n" +
        "        \"companyOpenId\": \"94f00c766f3241ecbd6fc254bd639056\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"d52d9d7f018c42f6ae0f203ae7b5639d\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"SZFL\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"employmentDate\": \"2020-02-14 00:00:00\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"id\": \"6a64ea20095647e3bff3f11e4c537b5d\",\n" +
        "        \"status\": -2,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"9b0db01981d54ada85e5ff7aab7e1286\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffId\": 678764,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"SZFL\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"\\u9648\\u653f\\u96c4\",\n" +
        "        \"staffId\": 683437,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"updateId\": \"35762e5b51de4d34a63122cb7ecbd9d8\",\n" +
        "        \"jobNumber\": \"BL2752\",\n" +
        "        \"companyOpenId\": \"a245a1ae820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"6c413938820d11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 1,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"BND\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"mq_consumer\",\n" +
        "        \"staffId\": 1054165,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"updateId\": \"961704fa821111e99c7e7cd30ad3a6a8\",\n" +
        "        \"companyOpenId\": \"a245a51b820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"d118aecb2f624cca89460f9f2c72b98f\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XQC\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"\\u6768\\u5609\\u797a\",\n" +
        "        \"employmentDate\": \"2014-04-18 00:00:00\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"id\": \"6c723b02820d11e99c7e7cd30ad3a6a8\",\n" +
        "        \"status\": 1,\n" +
        "        \"updateId\": \"9723fa8f821111e99c7e7cd30ad3a6a8\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"a245a60e820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffId\": 684645,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 1,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"BND\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"employmentDate\": \"2020-08-04 00:00:00\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"id\": \"00ptbnd1693409874159652776280315\",\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"a245a773820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffId\": 143814,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XJS\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"staffId\": 152676,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"companyOpenId\": \"a245ac53820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"00ptbnd1737622825159197580072015\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XQY\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"mq_consumer\",\n" +
        "        \"employmentDate\": \"2014-04-18 00:00:00\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"id\": \"a5828ae812fa44819328f740f3c535ed\",\n" +
        "        \"status\": 1,\n" +
        "        \"updateId\": \"961704fa821111e99c7e7cd30ad3a6a8\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"a245b172820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffId\": 948133,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XQC\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"status\": 1,\n" +
        "        \"companyOpenId\": \"a245b324820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffFrom\": \"XJS\",\n" +
        "        \"id\": \"6498132337d9497092d2efec3af61464\",\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isMainCompany\": 0\n" +
        "    },\n" +
        "    {\n" +
        "        \"staffId\": 1800152,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"companyOpenId\": \"a245b5f8820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"76b981759c3140f1a2d93aee49c59d7a\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XJS\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"staffId\": 1800153,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"companyOpenId\": \"a245b88b820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"ed4225e521994435addabcc97c7b144a\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XJS\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"\\u4f0d\\u6893\\u6b23\",\n" +
        "        \"employmentDate\": \"2020-01-11 00:00:00\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"id\": \"890df5e6f8384c0ea99139db516c5242\",\n" +
        "        \"status\": 1,\n" +
        "        \"updateId\": \"94\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"a245bc2f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffId\": 878879,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"SZFL\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"\\u4f0d\\u6893\\u6b23\",\n" +
        "        \"employmentDate\": \"2020-01-11 00:00:00\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"id\": \"51696488dd9a4a88b18535b273357a3a\",\n" +
        "        \"status\": 1,\n" +
        "        \"updateId\": \"94\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"a245bc75820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffId\": 613799,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"SZFL\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"rpa\\u673a\\u5668\\u4eba\",\n" +
        "        \"staffId\": 1716116,\n" +
        "        \"createId\": \"79\",\n" +
        "        \"updateId\": \"79\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"a245bcb6820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"createUser\": \"rpa\\u673a\\u5668\\u4eba\",\n" +
        "        \"id\": \"00ptbnd9282917751615197406353161\",\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"BND\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"status\": 1,\n" +
        "        \"companyOpenId\": \"a245c40a820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffFrom\": \"XJS\",\n" +
        "        \"id\": \"5aa4dc984bc3483a82d6953b724064c6\",\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isMainCompany\": 0\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"\\u90b9\\u6c38\\u8d85\",\n" +
        "        \"staffId\": 683866,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"updateId\": \"0e583f48e0db489c80433129df30e6cd\",\n" +
        "        \"companyOpenId\": \"a245ff8b820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"6c5d10cb820d11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"BND\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"staffId\": 502835,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"companyOpenId\": \"a24603c1820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"23efefe8fd6b47f38d15f6ef1749a339\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"SZFL\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"staffId\": 1043428,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"companyOpenId\": \"a24605c0820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"ccb474489175413ea1f1724e0f1c7fab\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"SZFL\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"\\u4f0d\\u6893\\u6b23\",\n" +
        "        \"employmentDate\": \"2014-04-18 00:00:00\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"id\": \"83508fc6de97423085eb27b72e66a518\",\n" +
        "        \"status\": 1,\n" +
        "        \"updateId\": \"94\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"a2460609820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffId\": 864871,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"SZFL\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"mq_consumer\",\n" +
        "        \"employmentDate\": \"2019-08-01 17:56:45\",\n" +
        "        \"createId\": \"95e85a73821111e99c7e7cd30ad3a6a8\",\n" +
        "        \"updateId\": \"95e85a73821111e99c7e7cd30ad3a6a8\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"a24619ed820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"createUser\": \"\\u6881\\u6d01\\u83b9\",\n" +
        "        \"staffFrom\": \"BND\",\n" +
        "        \"staffId\": 1212354,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"id\": \"f54446ed51a74a7d95fced59e1c31946\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"staffId\": 1800149,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"companyOpenId\": \"a24623cf820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"ede73a8e84a9455393ef385eaaed9a6b\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XJS\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"staffId\": 887029,\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"companyOpenId\": \"a83e9fcd1f0d4f9a9563d85316a6a4fd\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"id\": \"8c72019bdba24adba1a2f0b4191dfa66\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"status\": 1,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"SZFL\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"status\": 1,\n" +
        "        \"companyOpenId\": \"a9f96ce748d14bcdbc5c31c727aea7a6\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffFrom\": \"XJS\",\n" +
        "        \"id\": \"adf802fbdaf74627a6b82b4af1fe90b0\",\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"isMainCompany\": 0\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"\\u9648\\u653f\\u96c4\",\n" +
        "        \"employmentDate\": \"2019-07-12 17:37:18\",\n" +
        "        \"createId\": \"961704fa821111e99c7e7cd30ad3a6a8\",\n" +
        "        \"updateId\": \"35762e5b51de4d34a63122cb7ecbd9d8\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"baaedd29e2884cbe8eaeaf826bac4a95\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"createUser\": \"\\u9648\\u5bb6\\u6cc9\",\n" +
        "        \"staffFrom\": \"BND\",\n" +
        "        \"staffId\": 836864,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"id\": \"77c904073af04df78214c519289d53b7\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"mq_consumer\",\n" +
        "        \"employmentDate\": \"2019-08-24 00:00:00\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"staffId\": 450742,\n" +
        "        \"status\": 1,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"be4ba82b30cf43c48062ecefe18fa4dc\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"createUser\": \"mq_consumer\",\n" +
        "        \"id\": \"118cbe3f8d3a43a08d1d7546d0bd662b\",\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"XJS\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"employmentDate\": \"2020-02-14 00:00:00\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"id\": \"fa8ff405bf5743cab3c1ccded9857936\",\n" +
        "        \"status\": -2,\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"e2710f14631b46c3a6c80d251f16f93d\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffId\": 1234841,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"SZFL\"\n" +
        "    },\n" +
        "    {\n" +
        "        \"updateUser\": \"\\u9648\\u653f\\u96c4\",\n" +
        "        \"employmentDate\": \"2014-04-18 00:00:00\",\n" +
        "        \"createDepartmentId\": 0,\n" +
        "        \"id\": \"5ec12d972d7446cab1a782eb50d7d558\",\n" +
        "        \"status\": 1,\n" +
        "        \"updateId\": \"35762e5b51de4d34a63122cb7ecbd9d8\",\n" +
        "        \"createVirtualOrgId\": 0,\n" +
        "        \"companyOpenId\": \"fe4662b837d54916a7b66459888dc5ab\",\n" +
        "        \"isPartTimeJob\": \"0\",\n" +
        "        \"staffId\": 646345,\n" +
        "        \"statusExtend\": 1,\n" +
        "        \"isMainCompany\": 0,\n" +
        "        \"openId\": \"c66e1f5f820c11e99c7e7cd30ad3a6a8\",\n" +
        "        \"createStationId\": 0,\n" +
        "        \"staffFrom\": \"BND\"\n" +
        "    }\n" +
        "]\n";


    @Test
    void testaaaa() {
        List<Map<String, Object>> stringObjectMap = JsonUtils.fromJson(json, new TypeToken<List<Map<String, Object>>>() {
        });
        System.out.println(stringObjectMap.size());

        Set<String> companyOpenId = stringObjectMap.stream().map(it -> it.get("companyOpenId").toString()).collect(Collectors.toSet());
        System.out.println(companyOpenId.size());
    }

    static class Vo1 {
        List<int[]> ids;

        public List<int[]> getIds() {
            return ids;
        }

        public void setIds(List<int[]> ids) {
            this.ids = ids;
        }
    }

    static class Vo2 {
        AUser<Long> user;

        public AUser<Long> getUser() {
            return user;
        }

        public void setUser(AUser<Long> user) {
            this.user = user;
        }
    }

    static abstract class AUser<T> {
        private T id;

        public T getId() {
            return id;
        }

        public void setId(T id) {
            this.id = id;
        }
    }

    static class User<T> extends AUser<Long> implements Evaluatable<T> {
        private String name;
        private T value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public T getValue() {
            return null;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }
}

class Vos {
    ServiceType type;
}

class Vo {
    private List<ServiceType> types;

    public List<ServiceType> getTypes() {
        return types;
    }

    public void setTypes(List<ServiceType> types) {
        this.types = types;
    }
}

/**
 * description 商家服务类型
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2019/4/1.</p >
 */
enum ServiceType implements IntEvaluatable {

    NO_REASON(1, "七天无理由退货"),
    LIGHTNING_REFUND(2, "闪电退款"),
    DAMAGE(4, "破损包赔"),
    NEXT_DAY(8, "次日达");

    ServiceType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private final int value;

    /**
     * 描述
     */
    private final String desc;

    @Override
    public int intValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 服务类型
     */
    public static int encode(ServiceType[] serviceTypes) {
        int services = 0;
        for (ServiceType serviceType : serviceTypes) {
            services |= serviceType.value;
        }
        return services;
    }

    public static int encode(Iterable<ServiceType> serviceTypes) {
        int services = 0;
        for (ServiceType serviceType : serviceTypes) {
            services |= serviceType.value;
        }
        return services;
    }

    public static List<ServiceType> decode(int services) {
        List<ServiceType> serviceTypes = new LinkedList<>();
        if (services < 16) {
            for (ServiceType serviceType : ServiceType.values()) {
                if ((serviceType.value & services) > 0) {
                    serviceTypes.add(serviceType);
                }
            }
        }
        return serviceTypes;
    }
}


enum Status implements Evaluatable<String> {
    A("1"), B("2");

    private String value;

    Status(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}

enum Type implements IntEvaluatable {
    C(1), D(2);

    private int value;

    Type(int value) {
        this.value = value;
    }

    @Override
    public int intValue() {
        return value;
    }
}


class E {
    @PreventOverflow
    private Long id;
    @DatePattern(write = "yyyyMMddHH")
    private LocalDateTime xxx;
    private Status status;
    private Type type;
    @PreventOverflow
    private Long ext;

    @SerializedName("first")
    private String firstName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getXxx() {
        return xxx;
    }

    public void setXxx(LocalDateTime xxx) {
        this.xxx = xxx;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getExt() {
        return ext;
    }

    public void setExt(Long ext) {
        this.ext = ext;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
