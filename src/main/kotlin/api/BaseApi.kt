package api

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.utils.clientDispatcher
import io.ktor.http.Cookie
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.InternalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@OptIn(InternalAPI::class)
val netThreadPool = Dispatchers.clientDispatcher(4,"Net Thread Pool")

fun getHttpClient(): HttpClient = HttpClient(CIO) {

    install(HttpTimeout) {
        this.requestTimeoutMillis = 15000
        this.connectTimeoutMillis = 15000
        this.socketTimeoutMillis = 15000
    }

    install(HttpCookies) {}

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        })
    }
    install(Logging) {
        level = LogLevel.ALL
        logger = object : Logger {
            override fun log(message: String) {
                println(message)
            }
        }
    }
}

class NetDSL {
    var onStart: () -> Unit = {}
    var onFinish: () -> Unit = {}
    var onError: (Throwable) -> Unit = {}
    var run: suspend CoroutineScope.() -> Unit = {}

    fun onStart(block: () -> Unit) {
        onStart = block
    }

    fun onFinish(block: () -> Unit) {
        onFinish = block
    }

    fun onError(block: (Throwable) -> Unit) {
        onError = block
    }

    fun run(block: suspend CoroutineScope.() -> Unit) {
        run = block
    }

}

public fun runNet(net: NetDSL.() -> Unit): Job {
    val dsl = NetDSL().apply(net)
    return CoroutineScope(netThreadPool).launch {
        dsl.onStart()
        try {
            dsl.run(this)
        } catch (e: Throwable) {
            when (e) {
                is HttpRequestTimeoutException,
                is ConnectTimeoutException -> {
                    dsl.onError(Throwable("Network Timeout"))
                }

                else -> {
                    dsl.onError(e)
                }
            }
        } finally {
            dsl.onFinish()
        }
    }
}

suspend fun <T> api(doNet: suspend HttpClient.() -> T): T {
    getHttpClient().apply {
        val result = doNet(this)
        close()
        return result
    }
}

fun String.pathParam(vararg params: Pair<String, Any>): String {
    var result = this
    params.forEach {
        result = result.replace("{${it.first}}", it.second.toString())
    }
    return result
}
