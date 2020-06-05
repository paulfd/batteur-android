package cc.ferrand.batteur

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val synth = createSynth()
        loadSfzString(synth, "<region> sample=*sine loop_mode=one_shot ampeg_attack=0.1 ampeg_hold=0.1 ampeg_release=0.1")
        freeSynth(synth)
    }

    external fun createSynth(): Long
    external fun freeSynth(synth: Long)
    external fun loadSfzString(synth: Long, sfz: String)
    external fun playNote(synth: Long, number: Int, velocity: Float)

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
