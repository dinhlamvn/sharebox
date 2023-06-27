import com.dinhlam.sharebox.data.model.HttpErrorMessage
import com.dinhlam.sharebox.extensions.coerceMinMax
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.extensions.takeIfGreaterThanZero
import com.google.gson.Gson
import org.junit.Test
import java.time.Instant
import kotlin.random.Random

class UnitExtensionsTest {

    private val gson: Gson = Gson()

    @Test
    fun takeIfGreaterThanZero_test() {
        val a = 100
        assert(a.takeIfGreaterThanZero() != null)
    }

    @Test
    fun nowInUtcMillisTime_test() {
        val t1 = Instant.now().epochSecond
        val t2 = nowUTCTimeInMillis() / 1000
        assert(t1 == t2)
    }

    @Test
    fun coerceMinMax_test() {
        val a = Random.nextInt(0, 10)
        val b = a.coerceMinMax(5, 10)
        assert(b in 5..10)
    }

    @Test
    fun parseInlineClass_test() {
        val json = """{"code": 400, "message": "Name is required"}"""
        val httpErrorCode = gson.fromJson(json, HttpErrorMessage::class.java)

        assert(httpErrorCode.errorMessage == "Name is required")
    }
}