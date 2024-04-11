package image

import io.ktor.util.encodeBase64
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.io.File
import kotlin.jvm.optionals.getOrNull

var appName = "ImageForComposeDesktop"

fun String.md5(): String {
    return java.security.MessageDigest.getInstance("MD5")
        .digest(toByteArray())
        .joinToString("") { "%02x".format(it) }
}

fun getThumbnailFile(input: File, width: Int): File {
    if (File(cacheDir).exists().not()) {
        File(cacheDir).mkdirs()
    }
    return File(
        cacheDir,
        "${input.path.md5()}-thumbnail-w$width.jpg"
    )
}

fun getDownloadFile(url: String): File {
    if (File(netCacheDir).exists().not()) {
        File(netCacheDir).mkdirs()
    }
    val fileName = url.md5()
    val file = File(netCacheDir, fileName + "." + url.substringAfterLast("."))
    if (file.parentFile.exists().not()) {
        file.mkdirs()
    }
    return file
}

fun clearThumbnailCache() {
    File(cacheDir).deleteRecursively()
}

fun dataPath(): String {
    return when (hostOs) {
        OS.Windows -> System.getenv("APPDATA") + "/" + appName
        OS.MacOS -> System.getProperty("user.home") + "/" + "Library/Application Support" + "/" + appName

        OS.Linux -> System.getProperty("user.home") + "/.$appName"
        else -> System.getProperty("user.dir", appName);
    }
}

private val cacheDir = dataPath() + "/cache/img"

private val netCacheDir = "$cacheDir/net"

interface ImageCompress {
    fun compress(input: File, output: File, target: Int): File
}

fun compress(input: File, output: File, target: Int): File {
    return STBImageCompress.compress(input, output, target)
}