package com.shipping.domain.biz

import org.spin.data.core.AbstractEntity
import javax.persistence.Column
import javax.persistence.Entity

sealed class AEntity : AbstractEntity()

@Entity
data class User(@Column(length = 32) val realName: String) : AEntity()

class TUser : User("")
