package com.example.imageeditor

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.R.attr.bitmap
import android.R.attr.filterTouchesWhenObscured
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.graphics.toColor
import com.example.imageeditor.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubFilter
import org.w3c.dom.Text
import java.io.File
import java.io.FileOutputStream
import java.util.jar.Manifest


class MainActivity : Activity() {
    var imageView: ImageView? = null
    var imageUri: Uri? = null

    var button: Button? = null
    var filterButton: Button? = null
    var saveButton : Button? = null
    var grayScaleButton : Button? = null
    var binaryButton : Button? = null

    var brightnessText : TextView? = null
    var brightnessInt : Int = 0
    var binaryPressed : Boolean = false
    var originalImage : BitmapDrawable = BitmapDrawable()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        System.loadLibrary("NativeImageProcessor")

        Dexter.withContext(this)
                .withPermission(WRITE_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                    }
                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    }

                    override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, p1: PermissionToken?) {
                    }
                }
                ).check()

        val brightBar = findViewById<View>(R.id.BrightnessBar) as SeekBar
        brightBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                brightnessText?.text = "Brightness: " + (progress - 100)
                brightnessInt = progress - 100
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        brightnessText = findViewById(R.id.textBrightness)

        imageView = findViewById<View>(R.id.imageView) as ImageView

        filterButton = findViewById<View>(R.id.buttonApplyFilter) as Button
        filterButton!!.setOnClickListener { applyFilter() }

        button = findViewById<View>(R.id.buttonLoadPicture) as Button
        button!!.setOnClickListener { openGallery() }

        saveButton = findViewById<View>(R.id.buttonSavePicture) as Button
        saveButton!!.setOnClickListener { saveImage() }

        grayScaleButton = findViewById<View>(R.id.buttonApplyGrayScale) as Button
        grayScaleButton!!.setOnClickListener { ApplyGrayScale() }

        binaryButton = findViewById<View>(R.id.buttonMakeBinary) as Button
        binaryButton!!.setOnClickListener { ApplyBinary() }
    }

    private fun ApplyBinary() {
        if (binaryPressed == false) {
            val bitmapDrawable = imageView!!.drawable as BitmapDrawable
            var bitmap: Bitmap = bitmapDrawable.bitmap
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

            val bitmapHeight = bitmap.height
            val bitmapWidth = bitmap.width

            val pixels = IntArray(bitmapHeight * bitmapWidth)
            bitmap.getPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight)
            val brightnessFloat = 1f + (brightnessInt.toFloat() * 0.01f)


            val colorTransform: FloatArray = floatArrayOf(
                    85f, 85f, 85f, 0f, -128 * 255f,
                    85f, 85f, 85f, 0f, -128 * 255f,
                    85f, 85f, 85f, 0f, -128 * 255f,
                    0f, 0f, 0f, 1f, 0f,
            )

            val colorMatrix: ColorMatrix = ColorMatrix()
            colorMatrix.set(colorTransform)

            val colorFilter: ColorMatrixColorFilter = ColorMatrixColorFilter(colorMatrix)
            val paint: Paint = Paint()
            paint.setColorFilter(colorFilter)

            val canvas: Canvas = Canvas(bitmap)
            canvas.drawBitmap(bitmap, 0f, 0f, paint)

            imageView!!.setImageBitmap(bitmap)

            binaryButton!!.text = "Return to original image"

            binaryPressed = true
        }
        else
        {
            binaryButton!!.text = "Convert to binary"

            val d: Drawable = BitmapDrawable(resources, MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri))
            imageView!!.setImageDrawable(d)

            binaryPressed = false
        }



    }

    private fun ApplyGrayScale() {
        val bitmapDrawable = imageView!!.drawable as BitmapDrawable
        var bitmap: Bitmap = bitmapDrawable.bitmap
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val bitmapHeight = bitmap.height
        val bitmapWidth = bitmap.width

        val pixels = IntArray(bitmapHeight * bitmapWidth)
        bitmap.getPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight)

        val colorTransform : FloatArray = floatArrayOf(
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
        )

        val colorMatrix : ColorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)

        val colorFilter : ColorMatrixColorFilter = ColorMatrixColorFilter(colorMatrix)
        val paint : Paint = Paint()
        paint.setColorFilter(colorFilter)

        val canvas : Canvas = Canvas(bitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)


        imageView!!.setImageBitmap(bitmap)

    }


    private fun applyFilter() {
        val bitmapDrawable = imageView!!.drawable as BitmapDrawable
        var bitmap: Bitmap = bitmapDrawable.bitmap
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val bitmapHeight = bitmap.height
        val bitmapWidth = bitmap.width

        val pixels = IntArray(bitmapHeight * bitmapWidth)
        bitmap.getPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight)
        val brightnessFloat = 1f + (brightnessInt.toFloat() * 0.01f)


        val colorTransform : FloatArray = floatArrayOf(
                brightnessFloat, 0f, 0f, 0f, 0f,
                0f, brightnessFloat, 0f, 0f, 0f,
                0f, 0f, brightnessFloat, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
        )

        val colorMatrix : ColorMatrix = ColorMatrix()
        colorMatrix.set(colorTransform)

        val colorFilter : ColorMatrixColorFilter = ColorMatrixColorFilter(colorMatrix)
        val paint : Paint = Paint()
        paint.setColorFilter(colorFilter)

        val canvas : Canvas = Canvas(bitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        imageView!!.setImageBitmap(bitmap)

    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, PICK_IMAGE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.data
            val d: Drawable = BitmapDrawable(resources, MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri))
            imageView!!.setImageDrawable(d)


        }
    }


    private fun saveImage() {
        val root: String = Environment.getExternalStorageDirectory().toString()
        val dir: File = File(root + "/Pictures/Screenshots")
        dir.mkdirs()

        val bitmapDrawable = imageView!!.drawable as BitmapDrawable
        val bitmap: Bitmap = bitmapDrawable.bitmap

        var outputStream: FileOutputStream? = null


        val filename = String.format("%d.png", System.currentTimeMillis())
        val outFile: File = File(dir, filename)

        try {
            outputStream = FileOutputStream(outFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

        try {
            outputStream!!.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            outputStream!!.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    companion object {
        private const val PICK_IMAGE = 100
    }

}