package com.shipping.service;

import com.shipping.domain.dto.MenuDto;
import com.shipping.domain.dto.RegionDto;
import com.shipping.domain.enums.FunctionTypeE;
import com.shipping.domain.sys.Function;
import com.shipping.repository.sys.FunctionRepository;
import com.shipping.repository.sys.RegionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.collection.FixedVector;
import org.spin.core.session.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>Created by xuweinan on 2017/8/8.</p>
 *
 * @author xuweinan
 */
@Service
public class SystemService {
    private static final Logger logger = LoggerFactory.getLogger(SystemService.class);

    @Autowired
    private FunctionRepository functionDao;

    @Autowired
    private RegionRepository regionDao;

    private UserService userService;

    /**
     * 组装菜单树
     */
    public List<MenuDto> getMenus() {
        final List<MenuDto> result = new ArrayList<>();

        Map<Integer, List<MenuDto>> group = userService.getUserFunctions(SessionManager.getCurrentUser().getId()).get(FunctionTypeE.MEMU).stream()
            .map(MenuDto::toDto).collect(Collectors.groupingBy(f -> f.getIdPath().split(",").length));

        int maxDept = group.keySet().stream().max(Integer::compareTo).orElse(0), currDept = 1;
        FixedVector<List<MenuDto>> parent = new FixedVector<>(1);

        while (currDept <= maxDept) {
            group.get(currDept++).stream().collect(Collectors.toMap(MenuDto::getIdPath, f -> f)).forEach((k, v) -> {
                parent.put(result);
                Arrays.stream(k.split(",")).map(Long::valueOf).forEach(id -> {
                    Optional<MenuDto> p = parent.get().stream().filter(m -> id.equals(m.getId())).findFirst();
                    if (p.isPresent()) {
                        parent.put(p.get().getChildren());
                    } else {
                        parent.get().add(v);
                    }
                });
            });
        }
        return result;
    }

    public List<RegionDto> getRegions() {
        Map<Integer, List<RegionDto>> allRegion = regionDao.findAll().stream().map(RegionDto::new).collect(Collectors.groupingBy(RegionDto::getLevel));

        Map<String, RegionDto> cities = allRegion.get(2).stream().collect(Collectors.toMap(RegionDto::getValue, t -> t));
        Map<String, List<RegionDto>> work = allRegion.get(3).stream().collect(Collectors.groupingBy(RegionDto::getParent));
        work.forEach((p, rs) -> cities.get(p).setChildren(rs));

        Map<String, RegionDto> pronvinces = allRegion.get(1).stream().collect(Collectors.toMap(RegionDto::getValue, t -> t));
        work = allRegion.get(2).stream().collect(Collectors.groupingBy(RegionDto::getParent));
        work.forEach((p, rs) -> pronvinces.get(p).setChildren(rs));

        return allRegion.get(1);
    }

    private Map<String, Object> wraperFuncToMap(Function func) {
        Map<String, Object> f = new HashMap<>();
        f.put("id", func.getId().toString());
        f.put("name", func.getName());
        f.put("icon", func.getName());
        f.put("link", func.getName());
        if (Objects.nonNull(func.getParent())) {
            f.put("parent", func.getParent().getId());
        }
        f.put("visable", true);
        return f;
    }
}
