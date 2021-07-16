package vast.ara.mmsretriever

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.text.MessageFormat

class MainActivity : AppCompatActivity() {

    private var userName = ""
    private var body = ""
    private var timestamp: Long = 0
    private var seen = false
    private var mmsId: String = ""
    private lateinit var userInfo: SharedPreferences
    private var isImage = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val contentResolver = contentResolver

        val smsUri = Uri.parse("content://sms")
        val smsInboxUri = Uri.parse("content://sms/inbox")
        val smsSentUri = Uri.parse("content://sms/sent")

        val mmsUri = Uri.parse("content://mms")
        val mmsInboxUri = Uri.parse("content://mms/inbox")
        val mmsSentUri = Uri.parse("content://mms/sent")

        val smsQuery = contentResolver.query(smsUri, null, null, null)
        val smsInboxQuery = contentResolver.query(smsInboxUri, null, null, null)
        val smsSentQuery = contentResolver.query(smsSentUri, null, null, null)

        val mmsQuery = contentResolver.query(mmsUri, null, null, null)
        val mmsInboxQuery = contentResolver.query(mmsInboxUri, null, null, null)
        val mmsSentQuery = contentResolver.query(mmsSentUri, null, null, null)

        smsQuery!!.moveToFirst()
        mmsQuery!!.moveToFirst()

        smsInboxQuery!!.moveToFirst()
        smsSentQuery!!.moveToFirst()

        mmsInboxQuery!!.moveToFirst()
        mmsSentQuery!!.moveToFirst()

        val smsTimestamp = java.lang.Long.valueOf(smsQuery.getString(4))
        val smsInboxTimestamp = java.lang.Long.valueOf(smsInboxQuery.getString(4))
        val smsSentTimestamp = java.lang.Long.valueOf(smsSentQuery.getString(4))

        val mmsTimestamp = java.lang.Long.valueOf(mmsQuery.getString(2)) * 1000
        val mmsInboxTimestamp = java.lang.Long.valueOf(mmsInboxQuery.getString(2)) * 1000
        val mmsSentTimestamp = java.lang.Long.valueOf(mmsSentQuery.getString(2)) * 1000

        if(mmsQuery!!.moveToFirst()) {
            do{
                for (i in 0..50) {
                    try{
                        val test = java.lang.Long.valueOf(mmsQuery.getString(i))
                        Log.d("test", i.toString() + " : " + test)
                    }
                    catch (e: Exception) { }
                    catch (e : IndexOutOfBoundsException) {
                        val buff = getAddressNumber(mmsQuery.getString(0)) ?: "no number"
                        Log.d("test", "timestamp error on " + buff)
                    }
                }
            } while (mmsQuery.moveToNext())
        }

        /*
        val cursor = getContentResolver().query(mmsUri, null, null, null, null)

        if (cursor!!.moveToFirst()) {
            do {
                for (i in 0..25){
                    try{
                        val address: String = getAddressNumber(id = cursor.getString(i)).toString()
                        Log.d("test",i.toString() + " : " + address)
                    }
                    catch (e: Exception){ }
                }
            } while (cursor.moveToNext())
        }*/

        /*
        when {
            smsTimestamp > mmsTimestamp -> {
                when {
                    smsInboxTimestamp > smsSentTimestamp -> {
                        // Incoming SMS
                        address = smsQuery.getString(2)
                        body = smsQuery.getString(12)
                        timestamp = smsInboxTimestamp
                        seen = false
                        userName = address
                    }
                    smsInboxTimestamp < smsSentTimestamp -> {
                        // Outgoing SMS
                        address = smsQuery.getString(2)
                        body = smsQuery.getString(12)
                        timestamp = smsSentTimestamp
                        seen = true
                        userName = "me"
                    }
                    else -> { /*if two long values are the same*/ }
                }
            }
            smsTimestamp < mmsTimestamp -> {
                when {
                    mmsInboxTimestamp > mmsSentTimestamp -> {
                        // Incoming MMS
                        mmsId = mmsInboxQuery.getString(0)
                        val selectionPart = "mid=$mmsId"
                        val mmsUri = Uri.parse("content://mms/part")
                        val cursor = getContentResolver().query(mmsUri, null, selectionPart, null, null)
                        timestamp = mmsInboxTimestamp

                        if (cursor!!.moveToFirst()) {
                            do {
                                val partId = cursor.getString(cursor.getColumnIndex("_id"))
                                val type = cursor.getString(cursor.getColumnIndex("ct"))

                                // Looking for the Text of the MMS
                                if ("text/plain" == type) {
                                    val data = cursor.getString(cursor.getColumnIndex("_data"))
                                    body =
                                        if (data != null) ({
                                            // implementation of this method below
                                            getMmsText(partId)
                                        }).toString()
                                        else {
                                            cursor.getString(cursor.getColumnIndex("text"))
                                        }
                                    // Log.d("test", "mms : " + body)
                                }

                                //Looking for the Image of the MMS
                                if ("image/jpeg" == type ||
                                    "image/gif" == type ||
                                    "image/jpg" == type ||
                                    "image/png" == type) {
                                    val time = System.currentTimeMillis()
                                    getMmsImage(partId, time.toString())
                                }
                            } while (cursor.moveToNext())
                        }

                        address = getAddressNumber(mmsQuery.getString(0)).toString()
                        seen = false
                        userName = address
                        cursor.close()
                    }
                    mmsInboxTimestamp < mmsSentTimestamp -> {
                        // Outgoing MMS
                        val mmsId = mmsSentQuery.getString(0)
                        val selectionPart = "mid=$mmsId"
                        val mmsUri = Uri.parse("content://mms/part")
                        val cursor = getContentResolver().query(mmsUri, null, selectionPart, null, null)
                        timestamp = mmsSentTimestamp
                        if (cursor!!.moveToFirst()) {

                            do {
                                val partId = cursor.getString(cursor.getColumnIndex("_id"))
                                val type = cursor.getString(cursor.getColumnIndex("ct"))

                                // Looking for the Text of the MMS
                                if ("text/plain" == type) {
                                    val data = cursor.getString(cursor.getColumnIndex("_data"))
                                    body =
                                        if (data != null) ({
                                            // implementation of this method below
                                            getMmsText(partId)
                                        }).toString()
                                        else {
                                            cursor.getString(cursor.getColumnIndex("text"))
                                        }
                                    // Log.d("test", "mms : " + body)
                                }

                                //Looking for the Image of the MMS
                                if ("image/jpeg" == type ||
                                    "image/bmp" == type ||
                                    "image/gif" == type ||
                                    "image/jpg" == type ||
                                    "image/png" == type) {
                                    val time = System.currentTimeMillis()
                                    getMmsImage(partId, time.toString())
                                   // body = user!!.uid + "/" + time.toString() // this will contain something like this, [wuD1ddgRssPLl6av9CfTwZwdgUj1/1625143366072]
                                }
                            } while (cursor.moveToNext())
                        }

                        address = getAddressNumber(mmsQuery.getString(0)).toString()
                        seen = true
                        userName = "me"

                        cursor.close()
                    }
                    else -> { /* if two long values are the same */ }
                }

                getAddressNumber(mmsQuery.getString(0)).toString()
            }
            else -> { /* if two long values are the same */ }
        }
        */
        smsQuery.close()
        smsInboxQuery.close()
        smsSentQuery.close()
        mmsQuery.close()
        mmsInboxQuery.close()
        mmsSentQuery.close()
    }

    private fun getMmsText(id: String): String {
        val partURI = Uri.parse("content://mms/part/" + id)
        var `is`: InputStream? = null
        val sb = StringBuilder()
        try {
            `is` = contentResolver.openInputStream(partURI)
            if (`is` != null) {
                val isr = InputStreamReader(`is`, "UTF-8")
                val reader = BufferedReader(isr)
                var temp: String = reader.readLine()
                while (temp != null) {
                    sb.append(temp)
                    temp = reader.readLine()
                }
            }
        } catch (e: IOException) {
        } finally {
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (e: IOException) {
                }
            }
        }
        return sb.toString()
    }

    private fun getMmsImage(_id: String, time: String) {

        val imageUri = Uri.parse("content://mms/part/$_id")

        var `is`: InputStream? = null
        var bitmap: Bitmap? = null
        try {
            `is` = contentResolver.openInputStream(imageUri)
            bitmap = BitmapFactory.decodeStream(`is`)
        } catch (e: IOException) {
        } finally {
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (e: IOException) {
                }
            }
        }
    }

    private fun getAddressNumber(id: String): String? {
        val selectionAdd = "msg_id=$id"
        val uriStr: String = MessageFormat.format("content://mms/{0}/addr", id)
        val uriAddress = Uri.parse(uriStr)
        val cAdd = contentResolver.query(
            uriAddress, null,
            selectionAdd, null, null
        )

        var name: String? = null
        if (cAdd!!.moveToFirst()) {
            try{
                do {
                    val number = cAdd.getString(cAdd.getColumnIndex("address"))
                    Log.d("test", "MMSNUMBER : " + number)
                    if (number != null) {
                        try {
                            number.replace("-", "").toLong()
                            name = number
                        } catch (nfe: NumberFormatException) {
                            if (name == null) {
                                name = number
                            }
                        }
                    }
                }while (cAdd.moveToNext())
            }catch (e: IndexOutOfBoundsException){

            }
        }
        else if (cAdd != null) {
            cAdd.close()
        }
        return name
    }
}