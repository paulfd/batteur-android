package cc.ferrand.batteur

import android.content.Context
import android.media.AudioManager
import androidx.lifecycle.ViewModel

class SoundEngineWrapper {

}

class MainViewModel: ViewModel() {
    private var engine : Long = 0
    init {
        engine = mCreateEngine()
    }

    fun play() { mPlay(engine) }
    fun stop() { mStop(engine) }
    fun fillIn() { mFillIn(engine) }
    fun next() { mNext(engine) }
    fun playNote(number: Int, velocity: Float) { mPlayNote(engine, number, velocity) }
    fun loadSfzFile(path: String) { mLoadSfzFile(engine, path) }
    fun loadBeat(path: String) { mLoadBeat(engine, path) }

    override fun onCleared() {
        mFreeEngine(engine)
        super.onCleared()
    }

    fun setDefaultParameters(context: Context) {
        val audioService = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val sampleRate = audioService.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE).toInt()
        val framesPerBurst = audioService.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER).toInt()
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