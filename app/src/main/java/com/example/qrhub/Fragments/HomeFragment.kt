package com.example.qrhub.Fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.qrhub.R
import com.example.qrhub.ScannerOverlay
import com.example.qrhub.WebViewActivity
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class HomeFragment : Fragment() {

    // UI Elements
    private lateinit var previewView: PreviewView // Camera preview
    private lateinit var resultTextView: TextView // Display scanned result
    private lateinit var gallery: ImageView // Button to open gallery
    private lateinit var cardView: View // Card to show QR results
    private lateinit var scannerOverlay: ScannerOverlay // Overlay for the scanning box

    private val barcodeScanner = BarcodeScanning.getClient() // ML Kit Barcode Scanner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize UI components
        previewView = view.findViewById(R.id.previewView)
        resultTextView = view.findViewById(R.id.resultTextView)
        gallery = view.findViewById(R.id.gallery)
        cardView = view.findViewById(R.id.cardView)
        scannerOverlay = view.findViewById(R.id.scannerOverlay)

        // Handle gallery button click
        gallery.setOnClickListener { openGallery() }

        // Request camera permissions
        requestCameraPermission()

        return view
    }

    // Open gallery for selecting an image
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    // Handle gallery image selection
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                imageUri?.let { handleGalleryImage(it) }
            }
        }

    // Process image from gallery
    private fun handleGalleryImage(uri: Uri) {
        val image = InputImage.fromFilePath(requireContext(), uri)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    handleBarcode(barcode)
                }
            }
            .addOnFailureListener {
                showResult("Failed to Scan QR code from image")
            }
    }

    // Request camera permission
    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 1)
        } else {
            startCamera() // Start the camera if permission is granted
        }
    }

    // Initialize CameraX with a preview and analyzer
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
                    processImage(imageProxy)
                }
            }
            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // Process image frames for QR codes
    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    barcodes.firstOrNull()?.let { handleBarcode(it) }
                }
                .addOnCompleteListener { imageProxy.close() } // Close image proxy after processing
        }
    }

    // Handle detected QR codes
    private fun handleBarcode(barcode: Barcode) {
        val url = barcode.url?.url ?: barcode.displayValue
        if (url != null) {
            showResult(url)
            resultTextView.setOnClickListener {
                val intent = Intent(requireContext(), WebViewActivity::class.java)
                intent.putExtra("url", url)
                startActivity(intent)
            }
        } else {
            showResult("No QR Code detected")
        }
    }

    // Display result in the card
    private fun showResult(result: String) {
        resultTextView.text = result
        if (cardView.visibility == View.GONE) {
            cardView.visibility = View.VISIBLE
            cardView.alpha = 0f
            cardView.animate().alpha(1f).setDuration(300).start()
        }
    }
}
