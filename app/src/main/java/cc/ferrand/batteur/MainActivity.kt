package cc.ferrand.batteur

import android.content.Context
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.SimpleAdapter
import android.widget.Spinner
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val AUDIO_API_OPTIONS = arrayOf<String>("Unspecified", "OpenSL ES", "AAudio");
    private val engine = createEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setDefaultParameters(applicationContext)
        loadSfzString(engine, "<region> sample=*sine loop_mode=one_shot ampeg_attack=0.1 ampeg_hold=1 ampeg_release=0.1")
        button.setOnClickListener {
            playNote(engine, 63, 0.5f)
        }
//        freeEngine(engine)
    }

    external fun createEngine(): Long
    external fun freeEngine(engine: Long)
    external fun loadSfzString(engine: Long, sfz: String)
    external fun playNote(engine: Long, number: Int, velocity: Float)
    external fun setAudioApi(engine: Long, audioApi: Int)
    external fun setAudioDevice(engine: Long, deviceId: Int)
    external fun setBufferSizeInBursts(engine: Long, bursts: Int)

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }

        external fun setDefaultStreamValues(sampleRate: Int, framesPerBurst: Int)

        private fun setDefaultParameters(context: Context) {
            val audioService = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val sampleRate = audioService.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE).toInt()
            val framesPerBurst = audioService.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER).toInt()
            setDefaultStreamValues(sampleRate, framesPerBurst)
        }
    }
}
