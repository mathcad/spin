package com.shipping;

import com.shipping.domain.dto.MenuDto;
import com.shipping.domain.enums.FunctionTypeE;
import org.junit.jupiter.api.Test;
import org.spin.core.util.EnumUtils;
import org.spin.core.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Created by xuweinan on 2017/9/3.</p>
 *
 * @author xuweinan
 */
public class MenuDtoTest {

    @Test
    public void testJson() {
        MenuDto root = new MenuDto();
        root.setId(1L);
        root.setCode("a");
        root.setIdPath("1,");
        root.setOrderNo(0);

        MenuDto sub1 = new MenuDto();
        sub1.setId(2L);
        sub1.setCode("a1");
        sub1.setIdPath("1,2,");
        sub1.setOrderNo(0);

        MenuDto sub2 = new MenuDto();
        sub2.setId(3L);
        sub2.setCode("a2");
        sub2.setIdPath("1,3,");
        sub2.setOrderNo(1);

        root.getChildren().add(sub1);
        root.getChildren().add(sub2);

        System.out.println(JsonUtils.toJson(root));

        List<MenuDto> ms = new ArrayList<>();
        ms.add(sub1);
        ms.add(root);
        ms.add(sub2);

        Map<Integer, List<MenuDto>> group = ms.stream().sorted((a, b) -> a.getOrderNo() - b.getOrderNo() >= 0 ? 1 : -1).collect(Collectors.groupingBy(m -> m.getIdPath().split(",").length));

        System.out.println(JsonUtils.toJson(group));
    }


    @Test
    public void testEnum() {
        FunctionTypeE typeE = EnumUtils.getEnum(FunctionTypeE.class, 1);
    }

}
