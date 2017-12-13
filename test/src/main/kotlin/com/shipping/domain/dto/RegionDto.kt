package com.shipping.domain.dto

import com.shipping.domain.sys.Region

/**
 *
 * Created by xuweinan on 2017/9/13.
 *
 * @author xuweinan
 */
class RegionDto(region: Region) {
    var label: String? = null
    var value: String? = null
    var children: List<RegionDto>? = null
    @Transient
    var parent: String? = null
    @Transient
    var level: Int? = null

    init {
        this.label = region.name
        this.value = region.code
        this.level = region.level?.value
        this.parent = region.parentCode
    }
}
