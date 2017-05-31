package com.epam.androidlab.task9

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.toast
import java.io.*


class MainActivity : AppCompatActivity() {

    val CHOOSE_FILE_REQUEST_CODE = 0
    var textIsEditable = true
    lateinit var mEditText: EditText
    lateinit var mOpenButton: Button
    lateinit var mSaveButton: Button
    lateinit var currentFile: Uri

    private fun initVariables() {
        mEditText = findViewById(R.id.edit_text) as EditText
        mOpenButton = findViewById(R.id.open_bnt) as Button
        mSaveButton = findViewById(R.id.save_bnt) as Button
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initVariables()
        toggleTextEditing()

        mOpenButton.setOnClickListener {
            if (textIsEditable) toggleTextEditing()
            val mFileIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            mFileIntent.addCategory(Intent.CATEGORY_OPENABLE)
            mFileIntent.type = "text/plain"
            startActivityForResult(Intent.createChooser(mFileIntent, "Choose text file:"), CHOOSE_FILE_REQUEST_CODE)
        }
        mSaveButton.setOnClickListener {
            disableEditing()
            doAsyncResult {
                saveTextToFile(mEditText.text.toString(), currentFile)
                runOnUiThread { toast(resources.getString(R.string.toast_save)) }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, returnData: Intent?) {
        super.onActivityResult(requestCode, resultCode, returnData)
        if (resultCode == Activity.RESULT_OK && returnData?.data != null) {
            currentFile = returnData.data
            doAsyncResult {
                val str = getTextFromFile(currentFile)
                runOnUiThread {
                    mEditText.setText(str)
                    toggleTextEditing()
                    toast(resources.getString(R.string.toast_open))
                }
            }
        }
    }


    private fun toggleTextEditing() {
        if (textIsEditable) {
            disableEditing()
            textIsEditable = false
        } else {
            enableEditing()
            textIsEditable = true
        }
    }

    private fun enableEditing() {
        mEditText.visibility = EditText.VISIBLE
        mSaveButton.visibility = Button.VISIBLE
    }

    private fun disableEditing() {
        mEditText.visibility = EditText.INVISIBLE
        mSaveButton.visibility = Button.INVISIBLE
    }

    private fun getTextFromFile(uri: Uri): String {
        val fileDescriptor: FileDescriptor
        val stringBuilder = StringBuilder()
        try {
            fileDescriptor = contentResolver.openFileDescriptor(currentFile, "r").fileDescriptor
            val reader = InputStreamReader(FileInputStream(fileDescriptor), "UTF-8")
            val bufferedReader = BufferedReader(reader)
            try {
                var line = bufferedReader.readLine()

                while (line != null) {
                    stringBuilder.append(line)
                    stringBuilder.append(System.lineSeparator())
                    line = bufferedReader.readLine()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                bufferedReader.close()
                reader.close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }


    private fun saveTextToFile(text: String, uri: Uri) {
        val fileDescriptor: FileDescriptor
        try {
            fileDescriptor = contentResolver.openFileDescriptor(currentFile, "w").fileDescriptor
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return
        }
        val writer = FileWriter(fileDescriptor)
        val bufferedWriter = BufferedWriter(writer)
        try {
            bufferedWriter.write(text)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            bufferedWriter.close()
            writer.close()
        }
    }
}
