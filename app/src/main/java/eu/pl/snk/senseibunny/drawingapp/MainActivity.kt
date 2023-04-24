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
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView?=null
    private var mImageButtonCurrentPaint: ImageButton?=null
    private var imgButton: ImageButton?=null
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
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
                showRationaleDialog("App requires storage access", "Allow read storage to be able to do this")
            }
            else{
                StorageLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
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

    private val StorageLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){
        isGranted ->
        if(isGranted) {
            Toast.makeText(this, "permission granted", Toast.LENGTH_LONG).show()

            //Getting into gallery
            val pickInent= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            GalleryLaucnher.launch(pickInent)
        }
        else{
            Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
        }
    }

}