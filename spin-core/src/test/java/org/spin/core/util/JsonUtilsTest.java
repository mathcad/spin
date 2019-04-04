package org.spin.core.util;

import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.trait.IntegerEvaluatable;

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
    public static void main(String[] args) {
        String json = "[{types: [1,2]}]";

        Vos type = JsonUtils.fromJson("{type:1}", Vos.class);
        ServiceType serviceType = JsonUtils.fromJson("1", ServiceType.class);
        List<Vo> vos = JsonUtils.fromJson(json, new TypeToken<List<Vo>>() {
        });
        Vo[] vos1 = JsonUtils.fromJson(json, new TypeToken<Vo[]>() {
        });
        System.out.println();
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
enum ServiceType implements IntegerEvaluatable {

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
    public Integer getValue() {
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
