package cc.ferrand.batteur

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.obsez.android.lib.filechooser.ChooserDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import kotlin.math.round

// Midi Info
// https://developer.android.com/reference/android/media/midi/package-summary.html

class MainActivity : AppCompatActivity() {
    private var tapTimes = ArrayList<Long>()
    private val tapThreshold = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model: MainViewModel by viewModels()
        setContentView(R.layout.activity_main)
        model.setDefaultParameters(applicationContext)
        setSupportActionBar(toolbar)

        tvTempo.requestFocus() // To avoid the tempo edit selected by default

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

        btnTap.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (tapTimes.size > 0 && (tapTimes.last() - currentTime) > tapThreshold)
                tapTimes.clear()

            tapTimes.add(currentTime)

            if (tapTimes.size > 3) {
                var avgTimeDiff = 0.0
                for (i in tapTimes.size - 3 until tapTimes.size) {
                    avgTimeDiff += (tapTimes[i] - tapTimes[i-1]).toDouble()
                }
                avgTimeDiff /= 3
                val tapTempo = round(60.0 / (avgTimeDiff / 1000.0))
                if (tapTempo > 20.0 && tapTempo < 300.0) {
                    model.setTempo(tapTempo)
                    edtTempo.setText(tapTempo.toInt().toString())
                }
            }
        }

        btnPlay.setOnClickListener { model.play() }
        btnStop.setOnClickListener { model.stop() }
        btnFill.setOnClickListener { model.fillIn() }
        btnNext.setOnClickListener { model.next() }
    }

}
