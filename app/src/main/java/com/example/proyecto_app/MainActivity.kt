package com.example.proyecto_app

import android.Manifest
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var labelText: TextView
    private lateinit var btnDescribe: Button

    // Última etiqueta detectada de forma "estable"
    private var lastLabel: String? = null
    private var lastLabelTime: Long = 0L

    // Para no spamear la API
    private var isDescribing: Boolean = false

    private val requestCamera =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera() else {
                Toast.makeText(this, "Se requiere la cámara", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        labelText = findViewById(R.id.labelText)
        btnDescribe = findViewById(R.id.btnDescribe)

        // Pedir permiso de cámara
        requestCamera.launch(Manifest.permission.CAMERA)

        // Botón para pedir la descripción IA
        btnDescribe.setOnClickListener {
            val label = lastLabel
            if (label == null) {
                Toast.makeText(this, "Apunta a un objeto primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isDescribing) {
                Toast.makeText(this, "Ya estoy describiendo, espera un momento…", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            isDescribing = true
            labelText.text = "$label: generando descripción IA..."

            lifecycleScope.launch {
                val description = OpenAIClient.describeObject(label)
                labelText.text = "$label: $description"
                isDescribing = false
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val imageLabeler = ImageLabeling.getClient(
                ImageLabelerOptions.DEFAULT_OPTIONS
            )

            analysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy: ImageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )

                    imageLabeler.process(image)
                        .addOnSuccessListener { labels ->
                            val top = labels.maxByOrNull { it.confidence }
                            val now = System.currentTimeMillis()

                            if (top != null && top.confidence > 0.7f) {
                                val label = top.text

                                // Solo actualizamos si es una etiqueta nueva o ha pasado un tiempo
                                if (label != lastLabel || now - lastLabelTime > 3000L) {
                                    lastLabel = label
                                    lastLabelTime = now
                                    labelText.text = "Detectado: $label"
                                }
                            } else {
                                // Si no está seguro, no spameamos el texto todo el rato
                                // Puedes descomentar si quieres:
                                // labelText.text = "No estoy seguro…"
                            }
                        }
                        .addOnFailureListener {
                            // Si ML Kit falla, no llamamos a la IA
                            // y solo mostramos un mensaje simple
                            labelText.text = "Error analizando imagen."
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }

            val selector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, selector, preview, analysis
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Error iniciando cámara", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }
}
