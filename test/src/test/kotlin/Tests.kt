import com.shipping.ShippingApplication
import com.shipping.service.TestService
import org.junit.jupiter.api.Assertions.assertNotNull
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
    private lateinit var testService: TestService

    @Test
    fun testAutowiring() {
        assertNotNull(testService)
    }
}
