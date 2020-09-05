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
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val assets = getApplication<Application>().assets
    private val dataDir = getApplication<Application>().dataDir

    private var midiManager: MidiManager? = null
    private var outputPort: MidiOutputPort? = null
    private val deviceListener = DeviceListener()
    private var lastMainDown = 0L
    private var lastMainUp = 0L
    private var mainCC = 30
    private val overwriteAssets = false

    val drumAssetDirectory = "drums"
    val drumSamplesAssetDirectory = "drums/samples"
    val beatAssetDirectory = "beats"
    val beatFileExtension = ".json"
    val drumFileExtension = ".sfz"
    val drumList = ArrayList<String>()
    val beatList = ArrayList<String>()
    var currentBeatIndex = -1
    var currentDrumIndex = -1
    var customDrumName = ""
    var customBeatName = ""


    init {
        copyAssets()
        populateSpinnerLists()
        setDefaultParameters()
    }

    private fun copyAssetToFile(file: File, asset: String) {
        if (!file.exists() && !file.createNewFile()) {
            Log.e(
                "copyAssets",
                "File ${file.absolutePath} did not exist, and it could not be created"
            )
            return
        }

        Log.d("copyAssets", "Copying $asset to ${file.absolutePath}")

        val input = assets.open(asset)
        val output = file.outputStream()
        val buffer = ByteArray(1024)
        var read = input.read(buffer)
        while (read != -1) {
            output.write(buffer, 0, read)
            read = input.read(buffer)
        }
    }

    private fun copyAssets() {
        val drumList = assets.list(drumAssetDirectory) ?: return
        val drumSampleList = assets.list(drumSamplesAssetDirectory) ?: return
        val beatList = assets.list(beatAssetDirectory) ?: return

        val dataDirPath = dataDir.path
        File("$dataDirPath/$drumAssetDirectory").mkdirs()
        File("$dataDirPath/$drumSamplesAssetDirectory").mkdirs()
        File("$dataDirPath/$beatAssetDirectory").mkdirs()

        for (asset in drumList) {
            if ("$drumAssetDirectory/$asset" == drumSamplesAssetDirectory)
                continue

            val dataFile = File("$dataDirPath/$drumAssetDirectory/$asset")
            if (!dataFile.exists() || overwriteAssets)
                copyAssetToFile(dataFile, "$drumAssetDirectory/$asset")
        }

        for (asset in drumSampleList) {
            val dataFile = File("$dataDirPath/$drumSamplesAssetDirectory/$asset")
            if (!dataFile.exists() || overwriteAssets)
                copyAssetToFile(dataFile, "$drumSamplesAssetDirectory/$asset")
        }

        for (asset in beatList) {
            val dataFile = File("$dataDirPath/$beatAssetDirectory/$asset")
            if (!dataFile.exists() || overwriteAssets)
                copyAssetToFile(dataFile, "$beatAssetDirectory/$asset")
        }
    }

    private fun populateSpinnerLists() {
        drumList.clear()
        val drumFileList = File("${dataDir.path}/$drumAssetDirectory").list() ?: return
        for (filename in drumFileList) {
            if (filename.endsWith(drumFileExtension))
                drumList.add(filename.substring(0, filename.length - drumFileExtension.length))
        }

        val standardIdx = drumList.indexOf("Standard")
        if (standardIdx > 0) {
            drumList[standardIdx] = drumList[0]
            drumList[0] = "Standard"
            drumList.subList(1, drumList.size - 1).sort()
        }
        drumList.add(0, "")

        beatList.clear()
        val beatFileList = File("${dataDir.path}/$beatAssetDirectory").list() ?: return
        for (filename in beatFileList) {
            if (filename.endsWith(beatFileExtension))
                beatList.add(filename.substring(0, filename.length - beatFileExtension.length))
        }

        val bluesIdx = beatList.indexOf("Blues")
        if (bluesIdx > 0) {
            beatList[bluesIdx] = beatList[0]
            beatList[0] = "Blues"
            beatList.subList(1, beatList.size - 1).sort()
        }

        beatList.add(0, "")
    }

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
//                    if (number == mainCC) {
                    if (true) { // any CC
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

    private fun setDefaultParameters() {
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
