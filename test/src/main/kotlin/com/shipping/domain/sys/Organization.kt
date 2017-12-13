package com.shipping.domain.sys

import com.shipping.domain.enums.OrganizationTypeE
import org.hibernate.annotations.Type
import org.spin.data.core.AbstractEntity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.persistence.Table

/**
 * 组织机构
 *
 * Created by xuweinan on 2017/07/20.
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_organ")
class Organization : AbstractEntity() {

    /**
     * 名称
     */
    @Column(length = 64, unique = true)
    var name: String? = null

    /**
     * 编码
     */
    @Column(length = 64, unique = true)
    var code: String? = null

    /**
     * 简称
     */
    @Column(length = 16)
    private val alias: String? = null

    /**
     * 联系方式
     */
    @Column(length = 32)
    private val tel: String? = null

    /**
     * 地址
     */
    @Column(length = 128)
    private val address: String? = null

    /**
     * 类型
     */
    @Type(type = "org.spin.data.extend.UserEnumType")
    var type: OrganizationTypeE? = null

    /**
     * 上级机构
     */
    @ManyToOne(fetch = FetchType.LAZY)
    var parent: Organization? = null

    /**
     * id线索
     */
    @Column
    var idPath: String? = null

    /**
     * 是否叶子节点(用于加速查找)
     */
    @Column
    var isLeaf = true

    @Column
    private val remark: String? = null

    companion object {
        private val serialVersionUID = 8610089447094514827L
    }
}