package org.spin.kotlin.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class APIExtensionKtTest {

    @Test
    fun test1() {
        val o = O("a","b")
        println(o)
        assertTrue(o.getPropertyValue("name") == "a")
    }
}

data class O(var name:String, var address:String)
