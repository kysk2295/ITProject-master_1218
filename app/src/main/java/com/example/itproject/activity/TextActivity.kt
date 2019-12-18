package com.example.itproject.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.*
import android.widget.Toast
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.itproject.R
import kotlinx.android.synthetic.main.activity_text.*

class TextActivity : AppCompatActivity() {

    private lateinit var dataPath: String
    private lateinit var tess: TessBaseAPI
    private val RESULT_OCR: Int = 100
    private val messageHandler: MessageHandler = MessageHandler()
    private var assetsCopied = false
    private lateinit var bitMap: Bitmap
    private lateinit var array_word : ArrayList<String>
    private lateinit var array_isClicked : ArrayList<Boolean>
    private lateinit var array_textView : ArrayList<TextView>
    private lateinit var dialog : AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)

        array_word = ArrayList()
        array_isClicked = ArrayList()
        array_textView = ArrayList()

        val builder : AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater : LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        builder.setView(inflater.inflate(R.layout.dialog_loading, null))
        builder.setCancelable(false)
        dialog = builder.create()

        val sf: SharedPreferences =
            applicationContext!!.getSharedPreferences("assetsCopied", Context.MODE_PRIVATE)
        assetsCopied = sf.getBoolean("assetsCopied", false)

        if (!assetsCopied) {
            copyTask().execute()
        }
        else OCRTask().execute()

        TextActivity_check.setOnClickListener {

            if(array_word.size > 0) {
                val intent = Intent(applicationContext, MakeSetActivity::class.java)
                intent.putExtra("array_word", array_word)
                startActivity(intent)
                finish()
            }
        }
        TextACtivity_back.setOnClickListener {
            finish()
        }
    }

    inner class OCRThread(bm: Bitmap) : Thread() {

        private var bitMap: Bitmap = bm

        override fun run() {
            super.run()

            tess.setImage(bitMap)
            val OCRresult = tess.utF8Text

            val message: Message = Message.obtain()
            message.what = RESULT_OCR
            message.obj = OCRresult
            messageHandler.sendMessage(message)

        }
    }

    @SuppressLint("HandlerLeak")
    inner class MessageHandler : Handler() {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            when (msg.what) {
                RESULT_OCR -> {
                    val array_text: Array<String> =
                        msg.obj.toString().split("\\W+".toRegex()).toTypedArray()
                    val textSize = 16f
                    val size = Point()
                    windowManager.defaultDisplay.getSize(size)
                    val displayWidth =
                        size.x - (40 * resources.displayMetrics.density + 0.5f).toInt()
                    val lParams_textView: ViewGroup.LayoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    val lParams_linearLayout: ViewGroup.LayoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    var textViewWidth = 0
                    var linearLayout = LinearLayout(applicationContext)
                    linearLayout.layoutParams = lParams_linearLayout
                    linearLayout_scroll.addView(linearLayout)

                    //텍스트 생성
                    array_text.forEachIndexed { index, c ->

                        array_isClicked.add(false)

                        var textView = TextView(applicationContext)
                        textView.layoutParams = lParams_textView
                        textView.text = c
                        textView.textSize = textSize
                        textView.setTextColor(Color.BLACK)

                        //단어 클릭 시 선택, 다시 클릭 시 삭제
                        textView.setOnClickListener {

                            if(!array_isClicked[index]) {
                                //중복 체크
                                array_textView.forEachIndexed {i, t ->

                                    if(t.text == c && array_isClicked[i]) {

                                        t.background = null
                                        array_word.remove(c)
                                        array_isClicked[i] = false

                                    }
                                }

                                it.setBackgroundResource(R.drawable.textview_background)
                                array_word.add(c)
                                array_isClicked[index] = true


                            }

                            else {
                                it.background = null
                                array_word.remove(c)
                                array_isClicked[index] = false

                            }

                        }

                        textView.measure(0, 0)
                        textViewWidth += textView.measuredWidth

                        if(textViewWidth > displayWidth) {

                            textViewWidth = textView.measuredWidth
                            //재선언하여 다음 줄에 사용하기 위함
                            linearLayout = LinearLayout(applicationContext)
                            linearLayout.layoutParams = lParams_linearLayout
                            linearLayout_scroll.addView(linearLayout)
                            linearLayout.addView(textView)

                        }

                        else
                            linearLayout.addView(textView)

                        array_textView.add(textView)

                        textView = TextView(applicationContext)
                        textView.textSize = textSize
                        textView.text = " "
                        textView.measure(0, 0)
                        textViewWidth += textView.measuredWidth

                        if(textViewWidth > displayWidth) {

                            textViewWidth = 0
                            //재선언하여 다음 줄에 사용하기 위함
                            linearLayout = LinearLayout(applicationContext)
                            linearLayout.layoutParams = lParams_linearLayout
                            linearLayout_scroll.addView(linearLayout)

                        }

                        else linearLayout.addView(textView)

                    }


                    Toast.makeText(applicationContext, "인식 완료", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
    }

    private fun copyAssets() {
        val assetManager = assets
        var files: Array<String>? = null
        try {
            files = assetManager.list("tessdata/")
        } catch (e: IOException) {
            Log.e("tag", "Failed to get asset file list.", e)
        }

        val myDir = File("${getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/tessdata")
        if (!myDir.exists()) {
            myDir.mkdir()
        }

        if (files != null)
            for (filename in files) {
                var ins: InputStream? = null
                var ous: OutputStream? = null
                try {
                    ins = assetManager.open("tessdata/${filename}")
                    val outFile =
                        File("${getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/tessdata/", filename)
                    ous = FileOutputStream(outFile)

                    copyFile(ins, ous)
                } catch (e: IOException) {
                    Log.e("tag", "Failed to copy asset file: $filename", e)
                } finally {
                    if (ins != null) {
                        try {
                            ins.close()
                        } catch (e: IOException) {
                            // NOOP
                        }

                    }
                    if (ous != null) {
                        try {
                            ous.close()
                        } catch (e: IOException) {
                            // NOOP
                        }

                    }
                }
            }
    }


    @Throws(IOException::class)
    private fun copyFile(ins: InputStream, ous: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int = ins.read(buffer)
        while (read != -1) {
            ous.write(buffer, 0, read)
            read = ins.read(buffer)
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class copyTask : AsyncTask<Void, Void, Void>() {

        override fun onPreExecute() {
            super.onPreExecute()

            dialog.show()

        }

        override fun doInBackground(vararg p0: Void?): Void? {
            copyAssets()
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            OCRTask().execute()
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class OCRTask : AsyncTask<Void, Void, Void>() {

        override fun onPreExecute() {
            super.onPreExecute()

            if (assetsCopied) {
                dialog.show()
            }

        }

        override fun doInBackground(vararg params: Void?): Void? {

            val intent_: Intent? = intent
            val imageUri: Uri = Uri.parse(intent_!!.getStringExtra("uri"))
            val lang = "eng+kor"
            bitMap =
                MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, imageUri)
            dataPath = "${getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/"
            tess = TessBaseAPI()
            tess.init(dataPath, lang)
            val ocrThread = OCRThread(bitMap)
            ocrThread.isDaemon = true
            ocrThread.start()

            return null

        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            val sf: SharedPreferences =
                applicationContext!!.getSharedPreferences("assetsCopied", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sf.edit()

            editor.putBoolean("assetsCopied", true)
            editor.apply()
        }
    }

}