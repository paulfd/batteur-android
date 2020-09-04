package cc.ferrand.batteur

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.obsez.android.lib.filechooser.ChooserDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

// Midi Info
// https://developer.android.com/reference/android/media/midi/package-summary.html

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model: MainViewModel by viewModels()
        setContentView(R.layout.activity_main)
        model.setDefaultParameters(applicationContext)
        setSupportActionBar(toolbar)

        btnFile.setOnClickListener {
            ChooserDialog(this)
                .withFilter(false, "sfz")
                .withChosenListener { s: String, file: File ->
                    model.loadSfzFile(s)
                }
                .build()
                .show()
        }

        btnBeatFile.setOnClickListener {
            ChooserDialog(this)
                .withFilter(false, "json")
                .withChosenListener { s: String, file: File ->
                    model.loadBeat(s)
                    val tempo = model.getTempo().toInt()
                    edtTempo.setText(tempo.toString())
                }
                .build()
                .show()
        }

        edtTempo.addTextChangedListener {
            val str = it.toString()
            if (str.isNotEmpty()) {
                val tempo = it.toString().toDouble()
                if (tempo >= 20.0)
                    model.setTempo(tempo)
            }
        }

        btnPlay.setOnClickListener { model.play() }
        btnStop.setOnClickListener { model.stop() }
        btnFill.setOnClickListener { model.fillIn() }
        btnNext.setOnClickListener { model.next() }
    }

}
