import com.shipping.ShippingApplication
import com.shipping.service.FunctionService
import org.junit.Assert.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * <p>Created by xuweinan on 2018/2/8.</p>
 *
 * @author xuweinan
 */
@SpringBootTest(classes = [ShippingApplication::class])
@ExtendWith(SpringExtension::class)
class Tests {

    @Autowired
    private lateinit var funcService: FunctionService

    @Test
    fun testAutowiring() {
        assertNotNull(funcService)
    }
}
