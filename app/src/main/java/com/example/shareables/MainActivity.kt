package com.example.shareables

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.shareables.ui.theme.ShareablesTheme
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShareablesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ShareableCanvas(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShareableCanvas(modifier: Modifier = Modifier) {
    val captureController = rememberCaptureController()
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Canvas(
            modifier = Modifier
                .size(301.dp)
                .padding(16.dp)
                .capturable(captureController)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Draw colorful circles
            for (i in 0..10) {
                val radius = maxOf(50f, (Math.random() * minOf(canvasWidth, canvasHeight) * 0.2f).toFloat())
                drawCircle(
                    color = Color(
                        red = Math.random().toFloat(),
                        green = Math.random().toFloat(),
                        blue = Math.random().toFloat(),
                        alpha = 0.5f
                    ),
                    radius = radius,
                    center = Offset(
                        x = (radius + Math.random() * (canvasWidth - 2 * radius)).toFloat(),
                        y = (radius + Math.random() * (canvasHeight - 2 * radius)).toFloat()
                    )
                )
            }
        }
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        Button(onClick = {
            scope.launch {
                val bitmapAsync = captureController.captureAsync()
                val bitmap: ImageBitmap = bitmapAsync.await()
                bitmap.asAndroidBitmap()?.let { androidBitmap ->
                    shareImageToInstagramStories(androidBitmap, context)
                }
            }
        }) {
            Text(text = "Share to Instagram")
        }
        Button(onClick = {
            scope.launch {
                val bitmapAsync = captureController.captureAsync()
                val bitmap: ImageBitmap = bitmapAsync.await()
                bitmap.asAndroidBitmap()?.let { androidBitmap ->
                    shareImageToFacebookStories(androidBitmap, context)
                }
            }
        }) {
            Text(text = "Share to Facebook")
        }
        Button(onClick = {
            scope.launch {
                val bitmapAsync = captureController.captureAsync()
                val bitmap: ImageBitmap = bitmapAsync.await()
                bitmap.asAndroidBitmap()?.let { androidBitmap ->
                    shareImageToOtherApps(androidBitmap, context)
                }
            }
        }) {
            Text(text = "Share to Other Apps")
        }
    }
}

private suspend fun shareImageToInstagramStories(bitmap: Bitmap, context: Context) {
    val contentUri = saveBitmapToCacheAndGetUri(context, bitmap)

    val storiesIntent = Intent("com.instagram.share.ADD_TO_STORY").apply {
        type = "image/png"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra("interactive_asset_uri", contentUri)
        putExtra("top_background_color", "#FFE6F2FF")
        putExtra("bottom_background_color", "#FF007dff")
        clipData = android.content.ClipData.newRawUri("", contentUri)
    }
    context.grantUriPermission(
        "com.instagram.android", contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
    )

    context.startActivity(storiesIntent)
}

private suspend fun shareImageToFacebookStories(bitmap: Bitmap, context: Context) {
    val contentUri = saveBitmapToCacheAndGetUri(context, bitmap)

    println(contentUri?.path)
    val storiesIntent = Intent("com.facebook.stories.ADD_TO_STORY").apply {
        type = "image/png"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra("interactive_asset_uri", contentUri)
        putExtra("top_background_color", "#EE4645")
        putExtra("bottom_background_color", "#0054a1")
        clipData = android.content.ClipData.newRawUri("", contentUri)
//        putExtra("com.facebook.platform.extra.APPLICATION_ID", "APP ID") // TODO add app id, probably not needed
    }
    context.grantUriPermission(
        "com.facebook.katana", contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
    );
    context.startActivity(storiesIntent)
}

private fun saveBitmapToCacheAndGetUri(context: Context, bitmap: Bitmap): Uri? {
    val file = File(context.cacheDir, "canvas.png")
    FileOutputStream(file).use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

private suspend fun shareImageToOtherApps(bitmap: Bitmap, context: Context) {
    val contentUri = saveBitmapToCacheAndGetUri(context, bitmap)

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        clipData = android.content.ClipData.newRawUri("", contentUri)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share image using"))
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ShareablesTheme {
        ShareableCanvas()
    }
}