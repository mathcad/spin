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
        System.out.println(JsonUtils.toJson(CollectionUtils.divide(array, 7)));
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
