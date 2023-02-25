package com.denicks21.recorder

import android.Manifest.permission
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {
    lateinit var startTV: TextView
    lateinit var saveTV: TextView
    lateinit var playTV: TextView
    lateinit var stopTV: TextView
    lateinit var statusTV: TextView
    lateinit var pauseTV: TextView
    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    var mFileName: File? = null
    var playingAudio: Boolean = false
    private val mPlayToPauseAnim: Animation? = null
    private var mPauseToPlay: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTV = findViewById(R.id.idTVstatus)
        startTV = findViewById(R.id.btnRecord)
        saveTV = findViewById(R.id.btnSave)
        playTV = findViewById(R.id.btnPlay)
        stopTV = findViewById(R.id.btnStop)
        pauseTV = findViewById(R.id.textPlay)

        //Per animar el play i pause
        //mPlayToPauseAnim = AnimatedVectorDrawableCompat.create(context, R.drawable.btn_rec_play)
        //mPauseToPlay = AnimatedVectorDrawableCompat.create(context, R.drawable.btn_rec_pause)

        startTV.setOnClickListener {
            startRecording()
        }

        saveTV.setOnClickListener {
            saveRecording()
        }

        playTV.setOnClickListener {
            //(playTV.drawable as AnimationDrawable).start()
            //If the audio is playing then pause it, else play it
            if (playingAudio) {
                pauseAudio()
            } else {
                playAudio()
            }
        }

        stopTV.setOnClickListener {
            stopPlaying()
        }
    }

    private fun startRecording() {

        // Check permissions
        if (CheckPermissions()) {

            // Save file
            mFileName = File(getExternalFilesDir("")?.absolutePath,"Record.3gp")

            // If file exists then increment counter
            var n = 0
            while (mFileName!!.exists()) {
                n++
                mFileName = File(getExternalFilesDir("")?.absolutePath,"Record$n.3gp")
            }

            // Initialize the class MediaRecorder
            mRecorder = MediaRecorder()

            // Set source to get audio
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)

            // Set the format of the file
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)

            // Set the audio encoder
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            // Set the save path
            mRecorder!!.setOutputFile(mFileName)
            try {
                // Preparation of the audio file
                mRecorder!!.prepare()
            } catch (e: IOException) {
                Log.e("TAG", "prepare() failed")
            }
            // Start the audio recording
            mRecorder!!.start()
            statusTV.text = "Recording in progress"
        } else {
            // Request permissions
            RequestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // If permissions accepted ->
        when (requestCode) {
            REQUEST_AUDIO_PERMISSION_CODE -> if (grantResults.size > 0) {
                val permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (permissionToRecord && permissionToStore) {

                    // Message
                    Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_LONG).show()

                } else {

                    // Message
                    Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun CheckPermissions(): Boolean {

        // Check permissions
        val result =
            ContextCompat.checkSelfPermission(applicationContext, permission.WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun RequestPermissions() {

        // Request permissions
        ActivityCompat.requestPermissions(this,
            arrayOf(permission.RECORD_AUDIO, permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_AUDIO_PERMISSION_CODE)
    }

    private fun playAudio() {

        // Use the MediaPlayer class to listen to recorded audio files
        mPlayer = MediaPlayer()
        try {
            // Preleva la fonte del file audio
            mPlayer!!.setDataSource(mFileName.toString())

            // Fetch the source of the mPlayer
            mPlayer!!.prepare()

            // Start the mPlayer
            mPlayer!!.start()
            pauseTV.text = getString(R.string.pause)
            playTV.background = getDrawable(R.drawable.btn_rec_pause)
            statusTV.text = "Listening recording"
            playingAudio = true
        } catch (e: IOException) {
            Log.e("TAG", "prepare() failed")
            // Message
            Toast.makeText(getApplicationContext(), "Registration not started", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveRecording() {

        // Stop recording
        if (mFileName == null) {

            // Message
            Toast.makeText(getApplicationContext(), "Registration not started", Toast.LENGTH_LONG).show()

        } else {
            mRecorder!!.stop()

            // Message to confirm save file
            val savedUri = Uri.fromFile(mFileName)
            val msg = "File saved: " + savedUri!!.lastPathSegment
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show()

            // Release the class mRecorder
            mRecorder!!.release()
            mRecorder = null
            statusTV.text = "Recording interrupted"
        }
    }

    private fun stopPlaying() {

        try {
            // Stop playing the audio file
            mPlayer!!.stop()

            // Release the class mPlayer
            mPlayer!!.release()
            mPlayer = null
            statusTV.text = "Playing stopped"
            if (playingAudio) {
                playingAudio = false
                pauseTV.text = getString(R.string.play)
                playTV.background = getDrawable(R.drawable.btn_rec_play)
            }
        } catch (e: IOException) {

            // Message
            Toast.makeText(getApplicationContext(), "Registration not started", Toast.LENGTH_LONG).show()
        }
    }

    private fun pauseAudio(){

        // Pause the audio file
        mPlayer!!.pause()
        pauseTV.text = getString(R.string.play)
        playTV.background = getDrawable(R.drawable.btn_rec_play)
        statusTV.text = "Listening paused"
        playingAudio = false
    }

    companion object {
        const val REQUEST_AUDIO_PERMISSION_CODE = 1
    }
}