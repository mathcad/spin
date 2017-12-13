package com.shipping.service

import com.shipping.domain.dto.MenuDto
import com.shipping.domain.dto.RegionDto
import com.shipping.domain.enums.FunctionTypeE
import com.shipping.domain.sys.Function
import com.shipping.repository.sys.FunctionRepository
import com.shipping.repository.sys.RegionRepository
import org.slf4j.LoggerFactory
import org.spin.core.collection.FixedVector
import org.spin.core.session.SessionManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.Objects
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 *
 * Created by xuweinan on 2017/8/8.
 *
 * @author xuweinan
 */
@Service
class SystemService {

    @Autowired
    private val functionDao: FunctionRepository? = null

    @Autowired
    private val regionDao: RegionRepository? = null

    private val userService: UserService? = null

    /**
     * 组装菜单树
     */
    val menus: List<MenuDto>
        get() {
            val result = ArrayList<MenuDto>()

            val group = userService!!.getUserFunctions(SessionManager.getCurrentUser().id)[FunctionTypeE.MEMU]!!.
                map { MenuDto.toDto(it) }.groupBy { it.idPath!!.split(",").size }
//                .stream()
//                .map<MenuDto>(Function({ MenuDto.toDto(it) })).collect<Map<Int, List<MenuDto>>, Any>(Collectors.groupingBy { f -> f.idPath!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size })

            val maxDept = group.keys.stream().max { obj, anotherInteger -> obj.compareTo(anotherInteger) }.orElse(0)
            var currDept = 1
            val parent = FixedVector<MutableList<MenuDto>>(1)

            while (currDept <= maxDept) {
                group[currDept++]!!.forEach { d ->
                    parent.put(result)
                    d.idPath!!.split(",").map { it.toLong() }.forEach {
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
            val allRegion = regionDao!!.findAll().map { ::RegionDto.call() }.groupBy { it.level!! }

            val cities = allRegion[2]!!.map { it.value!! to it }.toMap()
            var work = allRegion[3]!!.groupBy { it.parent }
            work.forEach { p, rs -> cities[p]!!.children = rs }

            val pronvinces = allRegion[1]!!.map { it.value!! to it }.toMap()
            work = allRegion[2]!!.groupBy { it.parent }
            work.forEach { p, rs -> pronvinces[p]?.children = rs }

            return allRegion[1]!!
        }

    private fun wraperFuncToMap(func: Function): Map<String, Any?> {
        val f: MutableMap<String, Any?> = HashMap()
        f.put("id", func.id!!.toString())
        f.put("name", func.name)
        f.put("icon", func.name)
        f.put("link", func.name)
        if (Objects.nonNull(func.parent)) {
            f.put("parent", func.parent!!.id)
        }
        f.put("visable", true)
        return f
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SystemService::class.java)
    }
}
