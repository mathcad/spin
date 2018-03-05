import org.junit.jupiter.api.Test
import org.spin.core.util.MapUtils
import org.spin.data.sql.dbtype.MySQLDatabaseType
import org.spin.data.sql.loader.ArchiveMdLoader
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
        loader.dbType = MySQLDatabaseType()
        val start = System.currentTimeMillis()
        val s = loader.getSQL("biz/example.test", MapUtils.ofMap("value", 3, "flag", true))
        val end = System.currentTimeMillis()
        println(end - start)


        val starts = System.currentTimeMillis()
        val ss = loader.getSQL("biz/example.test2", MapUtils.ofMap("value", 3, "flag", true))
        val ends = System.currentTimeMillis()
        println(ends - starts)
        println(s.template)
    }
}
