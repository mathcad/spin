package com.shipping.domain.dto

import com.shipping.domain.sys.Region

/**
 *
 * Created by xuweinan on 2017/9/13.
 *
 * @author xuweinan
 */
data class RegionDto(
    var label: String? = null,
    var value: String? = null,
    var children: List<RegionDto>? = null,
    @Transient
    var parent: String? = null,
    @Transient
    var level: Int? = null) {

    constructor(region: Region) : this(
        label = region.name,
        value = region.code,
        level = region.level.value,
        parent = region.parentCode
    )
}
