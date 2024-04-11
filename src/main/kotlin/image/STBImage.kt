package image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import api.api
import api.runNet
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

suspend fun downloadFile(url: String, file: File) {
    return api {
        val tempFile = File(file.parent, "${file.name}.tmp")
        if (tempFile.exists()) {
            tempFile.delete()
        }
        prepareGet(url).execute {
            val channel: ByteReadChannel = it.body()
            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                while (!packet.isEmpty) {
                    val bytes = packet.readBytes()
                    tempFile.appendBytes(bytes)
                }
            }
            System.gc()
            tempFile.renameTo(file)
            println("Download completed: ${file.absolutePath}")
        }
    }
}

@Composable
fun STBImage(
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Inside,
    data: Any?,
    targetSize: Int = 0,
) {
    var painter: BitmapPainter? by remember { mutableStateOf(null) }
    var loadFailed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    if (painter != null) {
        Image(
            painter = painter!!,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier
        )
    }
    if (isLoading) {
        Box(
            modifier = modifier
                .background(Color(0x44000000))
                .padding(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color.Gray,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp).align(Center)
            )
        }
    }
    if (!isLoading && loadFailed) {
        Box(modifier.background(Color(0x44000000))) {
            Column(
                modifier = Modifier.align(Center),
                horizontalAlignment = CenterHorizontally,
            ) {
                Text(
                    "Load Failed",
                    color = Color.Gray
                )
                Button({
                    isLoading = true
                    loadImage(targetSize, data, {
                        painter = it
                    }, {
                        loadFailed = !it
                        isLoading = false
                    })
                }) {
                    Text("Retry")
                }
            }
        }
    }

    LaunchedEffect(data) {
        isLoading = true
        loadImage(targetSize, data, {
            painter = it
        }, {
            loadFailed = !it
            isLoading = false
        })
    }
}

fun loadImage(targetSize: Int, data: Any?, onPainterCreate: (BitmapPainter) -> Unit, onLoadFinish: (Boolean) -> Unit) {
    var success = true
    runNet {
        onError {
            it.printStackTrace()
            success = false
        }
        onFinish {
            onLoadFinish(success)
        }
        run {
            if (data is String) {
                val file = getDownloadFile(data)
                if (file.exists().not()) downloadFile(data, file)
                val thumbnailFile = File(
                    file.parent,
                    "${file.nameWithoutExtension}-thumbnail-w$targetSize.jpg"
                )
                if (thumbnailFile.exists()) {
                    thumbnailFile
                } else {
                    compress(
                        file,
                        thumbnailFile,
                        targetSize
                    )
                }.let {
                    onPainterCreate(
                        BitmapPainter(
                            org.jetbrains.skia.Image.makeFromEncoded(it.readBytes())
                                .toComposeImageBitmap()
                        )
                    )
                }
            } else if (data is File) {
                val thumbnailFile = getThumbnailFile(data, targetSize)
                if (thumbnailFile.exists()) {
                    thumbnailFile
                } else {
                    compress(
                        data,
                        thumbnailFile,
                        targetSize
                    )
                }.let {
                    onPainterCreate(
                        BitmapPainter(
                            org.jetbrains.skia.Image.makeFromEncoded(it.readBytes())
                                .toComposeImageBitmap()
                        )
                    )
                }
            } else {
                throw Throwable("Not support data type")
            }
        }
    }
}