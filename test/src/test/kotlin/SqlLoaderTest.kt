import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test
import org.spin.core.gson.annotation.PreventOverflow
import org.spin.core.security.AES
import org.spin.core.util.JsonUtils
import org.spin.core.util.MapUtils
import org.spin.data.sql.loader.FileSystemMdLoader
import org.spin.data.sql.resolver.BeetlResolver

/**
 * <p>Created by xuweinan on 2018/3/5.</p>
 *
 * @author xuweinan
 */
class SqlLoaderTest {

    @Test
    fun test() {
        val loader = FileSystemMdLoader()
        loader.templateResolver = BeetlResolver()
        val start = System.currentTimeMillis()
        val s = loader.getSQL("biz/example.test", MapUtils.ofMap("value", 3, "flag", true))
        val end = System.currentTimeMillis()
        println(end - start)


        val starts = System.currentTimeMillis()
        val ss = loader.getSQL("biz/example.test2", MapUtils.ofMap("value", 3, "flag", true))
        val ends = System.currentTimeMillis()
        println(ends - starts)
        println(s.sql)
    }

    @Test
    fun testDbPasswd() {
        println(AES.newInstance().withKey("c4b2a7d36f9a2e61").encrypt("admin"))
        println(AES.newInstance().withKey("c4b2a7d36f9a2e61").decrypt("Dx1rlLOAicWyJ+8tlWKFTg=="))
    }

    @Test
    fun testJson() {
        val order = Box()
        order.apply {
            id = 1264752392881049602L
            name = "a"
        }
        println(JsonUtils.toJson(order))
    }

    @Test
    fun testEq() {
        val o1 = null
        val o2 = Any()
//        o1.equals(o2)

        // == equals
        // === ==
        if (o1 != o2) {
            println("success")
        }
    }
}

@Serializable
class Box : AbEn() {
    var name: String? = null
}

@Serializable
open class AbEn {
    @PreventOverflow
    var id: Long? = null
}
