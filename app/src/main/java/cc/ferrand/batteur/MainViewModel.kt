package cc.ferrand.batteur

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.media.midi.MidiOutputPort
import android.media.midi.MidiReceiver
import android.os.Handler
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private var midiManager: MidiManager? = null
    private var outputPort: MidiOutputPort? = null
    private val deviceListener = DeviceListener()
    private var lastMainDown = 0L
    private var lastMainUp = 0L
    private var mainCC = 30

    inner class DeviceListener : MidiManager.DeviceCallback() {
        override fun onDeviceAdded(device: MidiDeviceInfo?) {
//          val name = device.properties?.get("name")
            device?.also {
                val numPorts = it.outputPortCount
                if (numPorts > 0) {
                    midiManager?.openDevice(
                        it, { device ->
                            outputPort?.close()
                            outputPort = device?.openOutputPort(0)
                            outputPort?.connect(midiReceiver)
                        }, Handler(context.mainLooper)
                    )
                }
            }

            super.onDeviceAdded(device)
        }
    }

    private val midiReceiver = Receiver()

    inner class Receiver : MidiReceiver() {
        override fun onSend(msg: ByteArray?, offset: Int, count: Int, timestamp: Long) {
            msg ?: return
            if (count < 3)
                return

            val channel = msg[offset].toInt() and 0x0F

            when (msg[offset].toInt() and 0xF0) {
//                0x80 -> { }
//                0x90 -> { }
                0xB0 -> {
                    val number = msg[offset + 1].toInt()
                    val value = msg[offset + 2].toInt()
                    if (number == mainCC) {
                        if (value == 127) {
                            lastMainUp = timestamp
                        } else if (value == 0) {
                            val sincePressed = (timestamp - lastMainUp) / 1e9
                            val sinceLastDown = (timestamp - lastMainDown) / 1e9
                            if (isPlaying()) {
                                if (sinceLastDown < 0.75) {
                                    stop()
                                } else if (sincePressed < 0.75) {
                                    fillIn()
                                } else {
                                    next()
                                }
                            } else {
                                play()
                            }
                            lastMainDown = timestamp
                        }
                    }
//                    Handler(context.mainLooper).post {
//                        Toast.makeText(
//                            context,
//                            "CC $number/$value received at $timestamp",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
                }
                else -> {

                }
            }
        }
    }


    private var engine: Long = 0

    init {
        engine = mCreateEngine()
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            midiManager = context.getSystemService(Context.MIDI_SERVICE) as MidiManager
            midiManager?.registerDeviceCallback(deviceListener, Handler(context.mainLooper))
        }
    }

    fun play() {
        mPlay(engine)
    }

    fun stop() {
        mStop(engine)
    }

    fun fillIn() {
        mFillIn(engine)
    }

    fun isPlaying(): Boolean {
        return mIsPlaying(engine)
    }

    fun next() {
        mNext(engine)
    }

    fun getTempo(): Double {
        return mGetTempo(engine)
    }

    fun setTempo(tempo: Double){
        mSetTempo(engine, tempo)
    }

    fun playNote(number: Int, velocity: Float) {
        mPlayNote(engine, number, velocity)
    }

    fun loadSfzFile(path: String) {
        mLoadSfzFile(engine, path)
    }

    fun loadBeat(path: String) {
        mLoadBeat(engine, path)
    }

    override fun onCleared() {
        mFreeEngine(engine)
        midiManager?.unregisterDeviceCallback(deviceListener)
        super.onCleared()
    }

    fun setDefaultParameters(context: Context) {
        val audioService = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val sampleRate = audioService.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE).toInt()
        val framesPerBurst =
            audioService.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER).toInt()
        mSetDefaultStreamValues(sampleRate, framesPerBurst)
    }

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }

        private external fun mCreateEngine(): Long
        private external fun mFreeEngine(engine: Long)
        private external fun mPlay(engine: Long)
        private external fun mStop(engine: Long)
        private external fun mFillIn(engine: Long)
        private external fun mNext(engine: Long)
        private external fun mIsPlaying(engine: Long): Boolean
        private external fun mGetTempo(engine: Long): Double
        private external fun mSetTempo(engine: Long, tempo: Double)
        private external fun mLoadSfzString(engine: Long, sfz: String)
        private external fun mLoadSfzFile(engine: Long, path: String)
        private external fun mLoadBeat(engine: Long, path: String)
        private external fun mPlayNote(engine: Long, number: Int, velocity: Float)
        private external fun mSetAudioApi(engine: Long, audioApi: Int)
        private external fun mSetAudioDevice(engine: Long, deviceId: Int)
        private external fun mSetBufferSizeInBursts(engine: Long, bursts: Int)
        private external fun mSetDefaultStreamValues(sampleRate: Int, framesPerBurst: Int)

    }
}