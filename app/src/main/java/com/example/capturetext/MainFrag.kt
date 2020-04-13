package com.example.capturetext

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.capturetext.databinding.FragmentMainBinding
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText

class MainFrag : Fragment() {
    lateinit var binding: FragmentMainBinding
    val REQUEST_IMAGE_CAPTURE = 1
    val PIC_IMAGE = 0
    var imageBitmap: Bitmap? = null
    lateinit var imageUri: Uri
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        binding.captureBtn.setOnClickListener {
            binding.displayTxt.setText("")
            dispatchTakePictureIntent()
        }
        binding.detectBtn.setOnClickListener {
            detect_text()
        }
        binding.chooseBtn.setOnClickListener {
            choose()
        }
        return binding.root
    }

    private fun choose() {
        val gallery = Intent()
        gallery.setType("image/*")
        gallery.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(gallery, "Select Picture"), PIC_IMAGE)
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(context!!.packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageBitmap = data?.extras?.get("data") as Bitmap
            binding.capturedImage.setImageBitmap(imageBitmap)
        }
        if (requestCode == PIC_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.data!!
            imageBitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, imageUri);
            binding.capturedImage.setImageBitmap(imageBitmap);
        }
    }

    fun detect_text() {
        if (imageBitmap != null) {
            val firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap!!)
            val firebaseVisionText = FirebaseVision.getInstance().visionTextDetector
            firebaseVisionText.detectInImage(firebaseVisionImage).addOnCompleteListener {
                if (it.isSuccessful) {
                    displaytxt(it.result!!)
                } else {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun displaytxt(Visiontext: FirebaseVisionText) {
        val list = Visiontext.blocks
        binding.displayTxt.setText("")
        if (list.size == 0) {
            Toast.makeText(context, "No Text Detected", Toast.LENGTH_SHORT).show()
        } else {
            for (i in list) {
                val lines = i.lines
                binding.displayTxt.append("\n")
                for (j in lines) {
                    val element = j.elements
                    binding.displayTxt.append("\n")
                    for (k in element) {
                        binding.displayTxt.append(k.text)
                        binding.displayTxt.append(" ")
                    }
                }
            }
        }
    }
}
