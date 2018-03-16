package com.shipping.domain.biz

import org.spin.data.core.AbstractEntity

sealed class AEntity : AbstractEntity()

data class User(val realName: String) : AEntity()
