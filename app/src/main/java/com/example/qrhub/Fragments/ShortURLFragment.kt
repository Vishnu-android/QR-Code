package com.example.myapplicationvg

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.qrhub.BitlyApiInterface
import com.example.qrhub.BitlyRequest
import com.example.qrhub.BitlyResponse
import com.example.qrhub.R
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ShortURLFragment : Fragment() {

    // Retrofit setup
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api-ssl.bitly.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val api: BitlyApiInterface = retrofit.create(BitlyApiInterface::class.java)

    private val bitlyToken = "d46f439eb41b4f750d5ca72cd34acb3be33e3917"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_short_u_r_l, container, false)

        // Initialize UI elements
        val etLongUrl: TextInputEditText = view.findViewById(R.id.etLongUrl)
        val btnShorten: Button = view.findViewById(R.id.btnShorten)
        val tvShortenedUrl: TextView = view.findViewById(R.id.tvShortenedUrl)
        val CopyUrl: ImageView = view.findViewById(R.id.CopyUrl)


        // Set up button click listener for shortening the URL
        btnShorten.setOnClickListener {
            val longUrl = etLongUrl.text.toString()
            if (longUrl.isNotEmpty()) {
                shortenBitlyUrl(longUrl, tvShortenedUrl, CopyUrl)
            } else {
                Toast.makeText(context, "Please enter a valid URL", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up image click listener for copying URL to clipboard
        CopyUrl.setOnClickListener {
            val shortUrl = tvShortenedUrl.text.toString()
            if (shortUrl.isNotEmpty()) {
                val clipboard = requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Shortened URL", shortUrl)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun shortenBitlyUrl(longUrl: String, outputView: TextView, copyButton: ImageView) {
        val request = BitlyRequest(long_url = longUrl)

        // Use a coroutine to make the API call
        CoroutineScope(Dispatchers.IO).launch {
            api.shortenUrl(bitlyToken, request).enqueue(object : Callback<BitlyResponse> {
                override fun onResponse(call: Call<BitlyResponse>, response: Response<BitlyResponse>) {
                    if (response.isSuccessful) {
                        val shortUrl = response.body()?.link
                        requireActivity().runOnUiThread {
                            outputView.text = shortUrl
                            copyButton.isEnabled = true // Enable copy button
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            Toast.makeText(context, "Failed to shorten URL", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(call: Call<BitlyResponse>, t: Throwable) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }
}
