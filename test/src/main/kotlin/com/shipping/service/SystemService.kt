package com.shipping.service

import com.shipping.domain.dto.MenuDto
import com.shipping.domain.dto.RegionDto
import com.shipping.domain.enums.FunctionTypeE
import com.shipping.domain.sys.Function
import com.shipping.domain.sys.Region
import mu.KLogging
import org.spin.core.collection.FixedVector
import org.spin.core.session.SessionManager
import org.spin.core.util.MapUtils
import org.spin.data.extend.RepositoryContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.Objects
import kotlin.collections.ArrayList

/**
 *
 * Created by xuweinan on 2017/8/8.
 *
 * @author xuweinan
 */
@Service
class SystemService {
    companion object : KLogging()

    @Autowired
    private lateinit var repoCtx: RepositoryContext

    @Autowired
    private lateinit var userService: UserService

    /**
     * 组装菜单树
     */
    val menus: List<MenuDto>
        get() {
            val result = ArrayList<MenuDto>()

            val group = userService.getUserFunctions(SessionManager.getCurrentUser().id)[FunctionTypeE.MEMU]!!.map { MenuDto(it) }.groupBy { it.idPath.split(",").size }
//                .stream()
//                .map<MenuDto>(Function({ MenuDto.toDto(it) })).collect<Map<Int, List<MenuDto>>, Any>(Collectors.groupingBy { f -> f.idPath!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size })

            val maxDept = group.keys.stream().max { obj, anotherInteger -> obj.compareTo(anotherInteger) }.orElse(0)
            var currDept = 1
            val parent = FixedVector<MutableList<MenuDto>>(1)

            while (currDept <= maxDept) {
                group[currDept++]!!.forEach { d ->
                    parent.put(result)
                    d.idPath.split(",").map { it.toLong() }.forEach {
                        val p = parent.get().firstOrNull { m -> it == m.id }
                        if (null != p) {
                            parent.put(p.children)
                        } else {
                            parent.get().add(d)
                        }
                    }
                }
            }
            return result
        }

    val regions: List<RegionDto>
        get() {
            val allRegion = repoCtx.findAll(Region::class.java).map { ::RegionDto.call() }.groupBy { it.level!! }

            val cities = allRegion[2]!!.map { it.value!! to it }.toMap()
            var work = allRegion[3]!!.groupBy { it.parent }
            work.forEach { p, rs -> cities[p]!!.children = rs }

            val pronvinces = allRegion[1]!!.map { it.value!! to it }.toMap()
            work = allRegion[2]!!.groupBy { it.parent }
            work.forEach { p, rs -> pronvinces[p]?.children = rs }

            return allRegion[1]!!
        }

    private fun wraperFuncToMap(func: Function): Map<String, Any?> {
        val f: MutableMap<String, Any?> = MapUtils.ofMap("id", func.id?.toString(), "name", func.name, "icon", func.icon, "link", func.link, "visable", true)
        if (Objects.nonNull(func.parent)) {
            f["parent"] = func.parent!!.id
        }
        return f
    }
}
