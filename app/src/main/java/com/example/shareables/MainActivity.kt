package com.example.shareables

import android.os.Bundle
import android.view.PixelCopy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.graphics.Bitmap
import android.os.Environment
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.shareables.ui.theme.ShareablesTheme
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShareablesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val captureController = rememberCaptureController()
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Canvas(
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp)
                .capturable(captureController)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Draw colorful circles
            for (i in 0..20) {
                drawCircle(
                    color = Color(
                        red = Math.random().toFloat(),
                        green = Math.random().toFloat(),
                        blue = Math.random().toFloat(),
                        alpha = 0.5f
                    ),
                    radius = (Math.random() * 100).toFloat(),
                    center = Offset(
                        x = (Math.random() * canvasWidth).toFloat(),
                        y = (Math.random() * canvasHeight).toFloat()
                    )
                )
            }
        }
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        Button(onClick = {
            // Capture content
            scope.launch {
                val bitmapAsync = captureController.captureAsync()
                try {
                    val bitmap: ImageBitmap = bitmapAsync.await()
                    bitmap.asAndroidBitmap()?.let { androidBitmap ->
                        saveBitmapToStorage(androidBitmap, context)
                    }
                    // Do something with `bitmap`.
                } catch (error: Throwable) {
                    error.printStackTrace()
                    // Error occurred, do something.
                }
            }
        }) {
            Text(text = "Capture")
        }
    }
}

private fun saveBitmapToStorage(bitmap: Bitmap, context: Context) {
    val filename = "canvas_${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }

    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    uri?.let { uri ->
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ShareablesTheme {
        Greeting("Android")
    }
}