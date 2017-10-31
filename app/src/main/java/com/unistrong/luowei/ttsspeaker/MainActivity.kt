package com.unistrong.luowei.ttsspeaker

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    private lateinit var mAudioManager: AudioManager
    private var maxVolume: Int = 0

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        volumeSeekBar.max = maxVolume
        volumeSeekBar.progress = currentVolume

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textView.text = progress.toString()
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        initTTs()
        val toMutableList = tts?.engines!!.map { it.name }
                .toMutableList()
        spinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, toMutableList)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val engine = (parent.getItemAtPosition(position) as String)
                initTTs(engine)
            }
        }
        toggleButton.setOnCheckedChangeListener { _, _ ->
            speakOrStop()
        }
        toggleButton.isChecked = false
    }

    private fun speakOrStop() {
        if (toggleButton.isChecked) {
            val string = editText.text.toString()
            val text = if (string.isEmpty()) {
                "The alpha to apply to the indicator when disabled"
            } else string
            com.unistrong.luowei.commlib.Log.d("speech:$text")
            val hashMap = HashMap<String, String>()
            hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, text)
            if (tts?.speak(text, TextToSpeech.QUEUE_ADD, hashMap) != TextToSpeech.SUCCESS) {
                Toast.makeText(this, "播放失败", Toast.LENGTH_LONG).show()
            }
        } else {
            tts?.stop()
        }
    }

    private fun initTTs(engine: String? = null) {
        com.unistrong.luowei.commlib.Log.d("engine=$engine")
        tts?.shutdown()
        tts = TextToSpeech(this, {
            //            tts!!.availableLanguages.forEach { Log.d("language=${it.language},country=${it.country}") }
            tts!!.setLanguage(Locale.US)
            speakOrStop()

        }, engine)
        tts!!.setOnUtteranceCompletedListener { speakOrStop() }
    }

    override fun onPause() {
        super.onPause()
        tts?.shutdown()
    }
}
