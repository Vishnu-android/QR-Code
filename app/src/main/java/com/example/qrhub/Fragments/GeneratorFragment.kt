package com.example.qrhub.Fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.qrhub.R
import com.google.android.material.textfield.TextInputEditText
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

class GeneratorFragment : Fragment() {

    private lateinit var inputText: TextInputEditText
    private lateinit var generateButton: Button
    private lateinit var qrImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_generator, container, false)

        // Initialize views
        inputText = view.findViewById(R.id.inputText)
        generateButton = view.findViewById(R.id.generateButton)
        qrImageView = view.findViewById(R.id.qrImageView)



        generateButton.setOnClickListener {
            val text = inputText.text.toString()
            if (text.isNotBlank()) {
                generateQRCode(text)
            } else {
                Toast.makeText(requireContext(), "Please enter text", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun generateQRCode(text: String) {
        val qrCodeWriter = QRCodeWriter()
        try {
            // Generate the QR Code as a 500x500 bitmap
            val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 500, 500)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            qrImageView.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error generating QR Code", Toast.LENGTH_SHORT).show()
        }
    }
}
