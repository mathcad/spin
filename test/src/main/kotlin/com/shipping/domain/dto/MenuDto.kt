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
class MenuDto {

    var id: Long? = null

    var name: String? = null

    var type: FunctionTypeE? = null

    var code: String? = null

    var icon: String? = null

    var link: String? = null

    var parent: Long? = null

    var idPath: String = ""

    var orderNo: Float = 0F

    var isLeaf: Boolean = false

    var children: MutableList<MenuDto> = ArrayList()

    companion object {

        fun toDto(function: Function): MenuDto {
            val dto = MenuDto()
            EntityUtils.copyTo(function, dto, "id", "name", "type", "code", "icon", "link", "idPath", "orderNo", "isLeaf")
            dto.parent = function.parent?.id
            return dto
        }
    }
}
