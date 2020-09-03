package cc.ferrand.batteur

import android.content.Context
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.obsez.android.lib.filechooser.ChooserDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener{
    private val AUDIO_API_OPTIONS = arrayOf<String>("Unspecified", "OpenSL ES", "AAudio")
    private val noteNumberArray = ArrayList<String>()
    private var engine : Long = 0
    private var selectedNote : Int = 63

    init {
        for (i in 0..127)
            noteNumberArray.add(i.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        engine = createEngine()
        setContentView(R.layout.activity_main)
        setDefaultParameters(applicationContext)
        loadSfzString(engine, "<region> sample=*sine loop_mode=one_shot ampeg_attack=0.1 ampeg_hold=1 ampeg_release=0.1")

        var noteArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, noteNumberArray)
        noteArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        noteSpinner.onItemSelectedListener = this
        noteSpinner.adapter = noteArrayAdapter
        noteSpinner.setSelection(selectedNote)

        button.setOnClickListener {
            playNote(engine, selectedNote, 0.5f)
        }

        btnFile.setOnClickListener {
            ChooserDialog(this)
                .withFilter(false, "sfz")
                .withChosenListener { s: String, file: File ->
                    Toast.makeText(this, "Chose " + s, Toast.LENGTH_SHORT).show()
                    loadSfzFile(engine, s)
                }
                .build()
                .show()
        }

        btnBeatFile.setOnClickListener {
            ChooserDialog(this)
                .withFilter(false, "json")
                .withChosenListener { s: String, file: File ->
                    Toast.makeText(this, "Chose " + s, Toast.LENGTH_SHORT).show()
                    loadBeat(engine, s)
                }
                .build()
                .show()
        }

        btnPlay.setOnClickListener { play(engine) }
        btnStop.setOnClickListener { stop(engine) }
        btnFill.setOnClickListener { fillIn(engine) }
        btnNext.setOnClickListener { next(engine) }
    }

    override fun onDestroy() {
        freeEngine(engine)
        super.onDestroy()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent?.id == noteSpinner.id) {
            selectedNote = position
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        if (parent?.id == noteSpinner.id) {
            parent.setSelection(selectedNote)
        }
    }

    external fun createEngine(): Long
    external fun freeEngine(engine: Long)
    external fun play(engine: Long)
    external fun stop(engine: Long)
    external fun fillIn(engine: Long)
    external fun next(engine: Long)
    external fun loadSfzString(engine: Long, sfz: String)
    external fun loadSfzFile(engine: Long, path: String)
    external fun loadBeat(engine: Long, path: String)
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
