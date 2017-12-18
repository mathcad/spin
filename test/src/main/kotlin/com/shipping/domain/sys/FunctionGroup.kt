package com.shipping.domain.sys

import com.shipping.domain.enums.FunctionTypeE
import org.hibernate.annotations.Type
import org.spin.data.core.AbstractEntity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * 功能组
 *
 * Created by xuweinan on 2017/07/20.
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_function_group")
class FunctionGroup(
    /**
     * 名称
     */
    @Column(length = 64, unique = true)
    var name: String = "",

    /**
     * 类型
     */
    @Type(type = "org.spin.data.extend.UserEnumType")
    var type: FunctionTypeE? = null,

    /**
     * 编码
     */
    @Column(length = 64, unique = true)
    var code: String? = null,

    /**
     * 图标
     */
    @Column(length = 64)
    var icon: String? = null

) : AbstractEntity() {
    companion object {
        private val serialVersionUID = -5089878281368192910L
    }
}
