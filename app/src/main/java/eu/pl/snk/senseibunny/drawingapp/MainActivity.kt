package eu.pl.snk.senseibunny.drawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView=findViewById(R.id.drawing_view)
        drawingView?.setSizeForBrush(22.0F)

        val ib_brush: ImageButton = findViewById(R.id.id_brush)

        ib_brush.setOnClickListener{
            showBrushSizeDialog()
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
}