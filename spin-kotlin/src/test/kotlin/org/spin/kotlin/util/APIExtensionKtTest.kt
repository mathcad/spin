package org.spin.kotlin.util

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class APIExtensionKtTest {


    @Test
    fun test1() {
        val sum: ((Int, Int, Int) -> Int) = { a, b, c -> a + b + c }
        sum(1, 2, 3)
        println(sum.curried()(1)(2)(3))
        println(sum.curried2()(1)(2)(3))
//        println(sum(1,2))
        val o = O("a", "b")
        assertTrue(o.getPropertyValue("name") == "a")
    }
}

data class O(var name: String, var address: String)

fun <P1, P2, P3, R> Function3<P1, P2, P3, R>.curried() = fun(p1: P1) = fun(p2: P2) = fun(p3: P3) = this(p1, p2, p3)

fun <P1, P2, P3, R> Function3<P1, P2, P3, R>.curried2(): (P1) -> (P2) -> (P3) -> R = { p1: P1 ->
    { p2: P2 ->
        { p3: P3 ->
            this(p1, p2, p3)
        }
    }
}

