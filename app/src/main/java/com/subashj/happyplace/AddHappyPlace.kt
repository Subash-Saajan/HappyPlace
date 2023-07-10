package com.subashj.happyplace

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.subashj.happyplace.databinding.ActivityAddHappyPlaceBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlace : AppCompatActivity(),View.OnClickListener {
    private var binding : ActivityAddHappyPlaceBinding ?= null
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setSupportActionBar(binding?.toolAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolAddPlace?.setOnClickListener{
            onBackPressed()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener{
            view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)

        }
        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.et_date -> {
                DatePickerDialog(this@AddHappyPlace,dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
                updateDateInView()
            }
            R.id.tv_add_image  ->{
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select action")
                val pictureDialogItem = arrayOf("Select Photo from Gallery",
                    "Capture photo from camera")
                pictureDialog.setItems(pictureDialogItem){
                    _,which ->
                    when (which){
                        0->choosePhotoFromGallery()
                        1->takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }

        }

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (resultCode == GALLERY){
                if (data != null) {
                    val contentURI = data.data
                    try {
                        contentURI?.let { uri ->
                            val selectedImageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                            val saveImageTOInternalStorage = saveImageTOInternalStorage(selectedImageBitmap)
                            Log.e("Saved Image: ","path::$saveImageTOInternalStorage")
                            binding?.ivPlaceImageAdd?.setImageBitmap(selectedImageBitmap)                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@AddHappyPlace, "Failed to Load Image!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@AddHappyPlace, "Failed to get data from gallery!", Toast.LENGTH_SHORT).show()
                }
            }else if (resultCode == CAMERA){
                val thumbnail : Bitmap = data!!.extras!!.get("data") as Bitmap
                val saveImageTOInternalStorage = saveImageTOInternalStorage(thumbnail)
                Log.e("Saved Image: ","path::$saveImageTOInternalStorage")
                binding?.ivPlaceImageAdd?.setImageBitmap(thumbnail)

            }
        }
    }

    private fun takePhotoFromCamera() {
        Dexter.withActivity(this)
            .withPermissions(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()){
                        val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(galleryIntent,CAMERA)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>,
                    token: PermissionToken
                ) {
                    showRationalDialogforPermission()
                }
            }).onSameThread()
            .check()
    }

    private fun choosePhotoFromGallery() {
        Dexter.withActivity(this)
            .withPermissions(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()){
                        val galleryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(galleryIntent,GALLERY)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>,
                    token: PermissionToken
                ) {
                        showRationalDialogforPermission()
                }
            }).onSameThread()
            .check()
    }
    private fun showRationalDialogforPermission() {
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permissions!")
            .setPositiveButton("Go To Settings") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }
    private fun saveImageTOInternalStorage(bitmap: Bitmap): Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY,Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")
        try {
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e:IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)

    }
    companion object{
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }
}