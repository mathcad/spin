package com.shipping.domain.sys

import org.spin.data.core.AbstractEntity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * 文件
 *
 * Created by xuweinan on 2017/4/20.
 *
 * @author xuweinan
 */
@Entity
@Table(name = "sys_file")
class File : AbstractEntity() {

    /**
     * 全局唯一id
     */
    @Column(length = 128, unique = true)
    var guid: String? = null

    /**
     * 文件名
     */
    @Column
    var fileName: String? = null

    /**
     * 原始文件名
     */
    @Column
    var originName: String? = null

    /**
     * 文件存放路径
     */
    @Column(unique = true)
    var filePath: String? = null

    /**
     * 扩展名
     */
    @Column(length = 16)
    var extension: String? = null

    /**
     * 文件大小
     */
    @Column
    var size: Long? = null

    /**
     * 扩展属性
     */
    @Column
    var extAttr: String? = null

    /**
     * 是否私有
     */
    @Column
    var isPriv = false

    companion object {
        private val serialVersionUID = 5524987141178520509L
    }
}