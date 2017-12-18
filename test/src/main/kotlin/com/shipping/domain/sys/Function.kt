package com.shipping.domain.sys

import com.shipping.domain.enums.FunctionTypeE
import org.hibernate.annotations.Type
import org.spin.data.core.AbstractEntity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.persistence.Table

/**
 * 功能项
 *
 * Created by xuweinan on 2017/07/20.
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_function")
class Function(
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
    var code: String = "",

    /**
     * 图标
     */
    @Column(length = 64)
    var icon: String? = null,

    /**
     * 路径
     */
    @Column(length = 128)
    var link: String? = null,

    /**
     * 所需权限
     */
    @ManyToOne(fetch = FetchType.LAZY)
    var permission: Permission? = null,

    /**
     * 上级功能
     */
    @ManyToOne(fetch = FetchType.LAZY)
    var parent: Function? = null,

    /**
     * 功能组
     */
    @ManyToOne(fetch = FetchType.LAZY)
    var group: FunctionGroup? = null,

    /**
     * id线索
     */
    @Column
    var idPath: String = "",

    /**
     * 是否叶子节点(用于加速查找)
     */
    @Column
    var isLeaf: Boolean = true
) : AbstractEntity()
