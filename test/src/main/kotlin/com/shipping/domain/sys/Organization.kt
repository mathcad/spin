package com.shipping.domain.sys

import com.shipping.domain.enums.OrganizationTypeE
import org.hibernate.annotations.Type
import org.spin.data.core.AbstractEntity
import javax.persistence.*

/**
 * 组织机构
 *
 * Created by xuweinan on 2017/07/20.
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_organ")
class Organization(
        /**
         * 名称
         */
        @Column(length = 64, unique = true)
        var name: String = "",

        /**
         * 编码
         */
        @Column(length = 64, unique = true)
        var code: String? = null,

        /**
         * 简称
         */
        @Column(length = 16)
        var alias: String? = null,

        /**
         * 联系方式
         */
        @Column(length = 32)
        var tel: String? = null,

        /**
         * 地址
         */
        @Column(length = 128)
        var address: String? = null,

        /**
         * 类型
         */
        @Type(type = "org.spin.data.extend.UserEnumType")
        var type: OrganizationTypeE? = null,

        /**
         * 上级机构
         */
        @ManyToOne(fetch = FetchType.LAZY)
        var parent: Organization? = null,

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

) : AbstractEntity() {
    companion object {
        private const val serialVersionUID = 8610089447094514827L
    }
}
