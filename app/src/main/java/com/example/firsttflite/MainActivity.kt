package com.example.firsttflite

import android.content.DialogInterface
import android.content.res.AssetManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import org.tensorflow.lite.Interpreter


class MainActivity : AppCompatActivity() {
    private lateinit var tflite: Interpreter
    private lateinit var tflitemodel: ByteBuffer
    private lateinit var txtValue: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            tflitemodel = loadModelFile(this.assets, "model.tflite")
            tflite = Interpreter(tflitemodel)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        val convertButton: Button = findViewById<Button>(R.id.convertButton)
        convertButton.setOnClickListener {
            doInference()
        }

        txtValue = findViewById<EditText>(R.id.txtValue)
    }

    private fun loadModelFile(assetManager: AssetManager, modelpath: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(modelpath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun doInference() {
        val userVal: Float = txtValue.text.toString().toFloat()
        val inputVal: FloatArray = floatArrayOf(userVal)
        val outputVal: ByteBuffer = ByteBuffer.allocateDirect(4)
        outputVal.order(ByteOrder.nativeOrder())
        tflite.run(inputVal, outputVal)
        outputVal.rewind()
        val f: Float = outputVal.float
        val builder = AlertDialog.Builder(this)

        with(builder) {
            setTitle("TFLite Interpreter")
            setMessage("Your Value is : $f")
            setNeutralButton("OK") { dialog, _ -> dialog.cancel() }
        }

        builder.show()
    }
}
