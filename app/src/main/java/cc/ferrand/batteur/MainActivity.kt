package cc.ferrand.batteur

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import com.obsez.android.lib.filechooser.ChooserDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

// Midi Info
// https://developer.android.com/reference/android/media/midi/package-summary.html

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private val AUDIO_API_OPTIONS = arrayOf<String>("Unspecified", "OpenSL ES", "AAudio")
    private val noteNumberArray = ArrayList<String>()
    private var selectedNote : Int = 63

    init {
        for (i in 0..127)
            noteNumberArray.add(i.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model: MainViewModel by viewModels()
        setContentView(R.layout.activity_main)
        model.setDefaultParameters(applicationContext)
        setSupportActionBar(toolbar)

        var noteArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, noteNumberArray)
        noteArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        noteSpinner.onItemSelectedListener = this
        noteSpinner.adapter = noteArrayAdapter
        noteSpinner.setSelection(selectedNote)

        button.setOnClickListener {
            model.playNote(selectedNote, 0.5f)
        }

        btnFile.setOnClickListener {
            ChooserDialog(this)
                .withFilter(false, "sfz")
                .withChosenListener { s: String, file: File ->
                    Toast.makeText(this, "Chose " + s, Toast.LENGTH_SHORT).show()
                    model.loadSfzFile(s)
                }
                .build()
                .show()
        }

        btnBeatFile.setOnClickListener {
            ChooserDialog(this)
                .withFilter(false, "json")
                .withChosenListener { s: String, file: File ->
                    Toast.makeText(this, "Chose " + s, Toast.LENGTH_SHORT).show()
                    model.loadBeat(s)
                }
                .build()
                .show()
        }

        btnPlay.setOnClickListener { model.play() }
        btnStop.setOnClickListener { model.stop() }
        btnFill.setOnClickListener { model.fillIn() }
        btnNext.setOnClickListener { model.next() }
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
}
