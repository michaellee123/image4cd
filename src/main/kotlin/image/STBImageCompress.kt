package image

import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet
import org.lwjgl.stb.STBImage
import org.lwjgl.stb.STBImageResize
import org.lwjgl.stb.STBImageWrite
import java.io.File
import java.nio.ByteBuffer
import kotlin.Exception

object STBImageCompress : ImageCompress {
    override fun compress(input: File, output: File, target: Int): File {

        val width = IntArray(1)
        val height = IntArray(1)
        val channels = IntArray(1)
        val buffer = STBImage.stbi_load(input.path, width, height, channels, 0)

        //if target bigger than origin size or target == 0, copy and do noting
        if ((width[0] <= target && height[0] <= target) || target == 0) {
            input.copyTo(output, true)
            return output
        }

        if (buffer == null) {
            throw RuntimeException("Failed to load image")
        }

        val targetWidth: Int
        val targetHeight: Int
        if (width[0] < height[0]) {
            targetWidth = target
            targetHeight = target * height[0] / width[0]
        } else {
            targetHeight = target
            targetWidth = target * width[0] / height[0]
        }

        val outputTemp = File(output.parent, ".temp_${output.name}")
        val outputBuffer = ByteBuffer.allocateDirect(targetWidth * targetHeight * channels[0])
        val resized = STBImageResize.stbir_resize_uint8(
            buffer,
            width[0],
            height[0],
            0,
            outputBuffer,
            targetWidth,
            targetHeight,
            0,
            channels[0]
        )
        //check extension
        if (input.extension == "png") {
            STBImageWrite.stbi_write_png(
                outputTemp.path,
                targetWidth,
                targetHeight,
                channels[0],
                outputBuffer,
                targetWidth * channels[0]
            )
        } else {
            STBImageWrite.stbi_write_jpg(
                outputTemp.path,
                targetWidth,
                targetHeight,
                channels[0],
                outputBuffer,
                75
            )
        }
        if (!resized) {
            throw RuntimeException("Failed to resize image")
        } else {
            println("Resized image to $targetWidth x $targetHeight, saved to ${output.path}")
        }
        STBImage.stbi_image_free(buffer)

        val metadata = Imaging.getMetadata(input)

        if (metadata is JpegImageMetadata) {
            val outputStream = output.outputStream()
            try {
                val outputSet = metadata.exif.outputSet
                ExifRewriter().updateExifMetadataLossless(outputTemp, outputStream, outputSet)
            }catch (e:Exception){
                e.printStackTrace()
                outputTemp.renameTo(output)
            }finally {
                outputStream.close()
                outputTemp.delete()
            }
        } else {
            outputTemp.renameTo(output)
        }

        System.gc()

        return output
    }

}