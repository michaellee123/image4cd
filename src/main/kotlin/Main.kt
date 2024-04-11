import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import image.STBImage
import image.appName
import java.io.File
import java.io.FileFilter

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun App() {
    val extensions = arrayOf("png", "jpg", "jpeg")
    val files =
        File("/Users/michaellee/Pictures/pap.er").listFiles { pathname ->
            extensions.contains(
                pathname?.extension?.toLowerCase()
            )
        }
    val urls = arrayOf(
        "https://cdn.getmidnight.com/26ffcef53c44522efbfe7fef964a4058/2023/02/untitled-1-.png",
        "https://molo17.com/wp-content/uploads/2021/11/StudioCompose10.jpg",
        "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEjC97Z8BResg5dlPqczsRCFhP6zewWX0X0e7fVPG-G7PuUZwwZVsi9OPoqJYkgqT2h0FI95SsmWzVEgpt8b8HAqFiIxZ98TFtY4lE0b8UrtVJ2HrJebRwl6C9DslsQDl9KnBIrdHS6LtkY/s1600/jetpack+compose+icon_RGB.png",
        "https://miro.medium.com/v2/resize:fit:1400/1*8cX9s_JM3Skmk5tIPY5s1Q.png",
        "https://mathiasfrohlich.gallerycdn.vsassets.io/extensions/mathiasfrohlich/kotlin/1.7.1/1581441165235/Microsoft.VisualStudio.Services.Icons.Default",
        "https://pbs.twimg.com/profile_images/1399329694340747271/T5fbWxtN_400x400.png",
    )

    var bigImage: Any? by remember { mutableStateOf(null) }

    MaterialTheme {
        LazyVerticalGrid(
            GridCells.Adaptive(200.dp),
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item (span = {
                GridItemSpan(maxLineSpan)
            }){
                Text("Local Files")
            }
            items(files ?: emptyArray()) {
                STBImage(
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            bigImage = it
                        },
                    ContentScale.Crop,
                    data = it,
                    targetSize = 400
                )
            }
            item (span = {
                GridItemSpan(maxLineSpan)
            }){
                Text("Internet Files")
            }
            items(urls) {
                STBImage(
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            bigImage = it
                        },
                    ContentScale.Crop,
                    data = it,
                    targetSize = 400
                )
            }
        }

    }
    if (bigImage != null) {
        Popup(onDismissRequest = {
            bigImage = null
        }) {
            STBImage(
                modifier = Modifier.background(Color(0x8000000)).fillMaxSize().onClick {
                    bigImage = null
                },
                ContentScale.Fit,
                data = bigImage,
                targetSize = 0
            )
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "ImageForComposeDesktop") {
        App()
    }
}
