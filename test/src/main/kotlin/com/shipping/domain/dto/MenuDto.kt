package com.shipping.domain.dto

import com.shipping.domain.enums.FunctionTypeE
import com.shipping.domain.sys.Function
import org.spin.data.util.EntityUtils
import java.util.*

/**
 *
 * Created by xuweinan on 2017/9/3.
 *
 * @author xuweinan
 */
data class MenuDto(
    var id: Long? = null,

    var name: String? = null,

    var type: FunctionTypeE? = null,

    var code: String? = null,

    var icon: String? = null,

    var link: String? = null,

    var parent: Long? = null,

    var idPath: String = "",

    var orderNo: Float = 0F,

    var isLeaf: Boolean = false,

    var children: MutableList<MenuDto> = ArrayList()
) {
    constructor(function: Function) : this(function.id,
        function.name,
        function.type,
        function.code,
        function.icon,
        function.link,
        function.parent?.id,
        function.idPath,
        function.orderNo,
        function.isLeaf
    )
}
