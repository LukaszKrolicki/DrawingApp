package eu.pl.snk.senseibunny.drawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.get
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView?=null
    private var mImageButtonCurrentPaint: ImageButton?=null
    private var imgButton: ImageButton?=null
    private var is_permission: Boolean ?=false
    private var customProgress:Dialog?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView=findViewById(R.id.drawing_view)
        drawingView?.setSizeForBrush(22.0F)
        imgButton=findViewById(R.id.img_btn)

        val ib_brush: ImageButton = findViewById(R.id.id_brush)

        val LinearLayoutPaintColors=findViewById<LinearLayout>(R.id.ll_paint_colors)
        mImageButtonCurrentPaint= LinearLayoutPaintColors[2] as ImageButton

        ib_brush.setOnClickListener{
            showBrushSizeDialog()
        }

        val ib_undo: ImageButton = findViewById(R.id.img_undo)
        ib_undo.setOnClickListener{
            drawingView?.onClickUndo()
        }

        val ib_back: ImageButton = findViewById(R.id.img_back)
        ib_back.setOnClickListener{
            drawingView?.onClickBack()
        }

        imgButton?.setOnClickListener{
            is_permission=true
            requestStoragePermission()
        }

        val ib_save: ImageButton=findViewById(R.id.id_save)
        ib_save.setOnClickListener{
            is_permission=false
            requestStoragePermission()
            ProgressDialogFun()
            if(isReadStorageAllowed()){
                lifecycleScope.launch {
                    val flDrawingView: FrameLayout = findViewById(R.id.fl_drawing_view_container)
                    saveBitmapFile(getBitmapFromView(flDrawingView))
                }
            }
        }
    }

    private fun shareImage(result:String){
        MediaScannerConnection.scanFile(this, arrayOf(result),null){
            path,uri->
            val shareIntent=Intent()
            shareIntent.action=Intent.ACTION_SEND // IT ALLOWS US TO SEND ITEMS
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "image/png"//what type of
            startActivity(Intent.createChooser(shareIntent,"Share"))
        }

    }

    private  fun cancelProgressDialog(){
        if(customProgress!=null){
            customProgress?.dismiss()
            customProgress=null
        }
    }
    private fun ProgressDialogFun(){
        customProgress=Dialog(this)

        customProgress?.setContentView(R.layout.progress)

        customProgress?.show()
    }
    private fun isReadStorageAllowed(): Boolean{

        val result=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
    }

    private val StorageLauncher: ActivityResultLauncher<Array<String>> = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()){
            permissions->
        permissions.entries.forEach{
            val permissionName=it.key
            val isGranted=it.value
            if(isGranted) {
                if(permissionName==Manifest.permission.READ_EXTERNAL_STORAGE){
                    Toast.makeText(this, "permission granted for Read", Toast.LENGTH_LONG).show()

                    //Getting into gallery
                    if(is_permission==true){
                        val pickInent= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        GalleryLaucnher.launch(pickInent)
                    }
                }
                else{
                    Toast.makeText(this, "permission granted for Write", Toast.LENGTH_LONG).show()
                }
            }
            else{
                if(permissionName==Manifest.permission.READ_EXTERNAL_STORAGE){
                    Toast.makeText(this, "permission denied for Read", Toast.LENGTH_LONG).show()
                }
                else{
                    Toast.makeText(this, "permission denied for Camera", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) && shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            showRationaleDialog(
                "App requires storage access",
                "Allow read storage to be able to do this"
            )
        } else {
            StorageLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }


    private fun showBrushSizeDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size) //How it should look like
        brushDialog.setTitle("Brush Size: ") //set tiltle
        val smallBtn:ImageButton = brushDialog.findViewById(R.id.ib_small_brush) // Taking the id from layout

        smallBtn.setOnClickListener{
            drawingView?.setSizeForBrush(10.0F)
            brushDialog.dismiss()
        }

        val mediumBtn:ImageView = brushDialog.findViewById(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener{
            drawingView?.setSizeForBrush(20.0F)
            brushDialog.dismiss()
        }

        val largeBtn:ImageView = brushDialog.findViewById(R.id.ib_large_brush)
        largeBtn.setOnClickListener{
            drawingView?.setSizeForBrush(25.0F)
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    fun paintClicked(view: View){
        if(view !== mImageButtonCurrentPaint){
            val imageButton=view as ImageButton
            val colorTag = imageButton.tag.toString()
            drawingView?.setColor(colorTag)

            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_pressed)
            )

            mImageButtonCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_normal)
            )

            mImageButtonCurrentPaint=view
        }
    }

    private fun showRationaleDialog(title: String, message: String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("Cancel"){dialog, _->dialog.dismiss()}
        builder.create().show()
    }


    private val GalleryLaucnher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode== RESULT_OK && result.data!=null){
                val imageBackground:ImageView=findViewById(R.id.iv_background)

                imageBackground.setImageURI(result.data?.data) // setting background of our app
            }
    }

    private fun getBitmapFromView(view:View):Bitmap{ //get a view and convert it to bitmap
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888) // we create bitmap
        val canvas = Canvas(returnedBitmap)//canvas will draw into bitmap
        val bgDrawable = view.background
        if(bgDrawable !=null){
            bgDrawable.draw(canvas) //draw background into bitmap
        }
        else{
            canvas.drawColor(Color.WHITE) //instead draw white
        }

        view.draw(canvas)//draw lines

        return  returnedBitmap //we made sandwich maker
    }

    private suspend fun saveBitmapFile(mBitmap: Bitmap?): String{
        var result=""
        withContext(Dispatchers.IO){
            if(mBitmap!=null){
                try{
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG,90, bytes) //image will be stored as png, with 90% quality

                    val f = File(externalCacheDir?.absoluteFile.toString() + File.separator + "KidsDrawingApp_" + System.currentTimeMillis()/1000+ ".png") // pathname where our application is

                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()

                    result=f.absolutePath

                    runOnUiThread{
                        cancelProgressDialog()
                        if(result.isNotEmpty()){
                            Toast.makeText(this@MainActivity,"File saved: $result", Toast.LENGTH_LONG).show()
                            shareImage(result)
                        }
                        else{
                            Toast.makeText(this@MainActivity,"Somethig went wrong", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                catch(e: Exception){
                    result=""
                    e.printStackTrace()
                }

            }
        }
        return result
    }

}