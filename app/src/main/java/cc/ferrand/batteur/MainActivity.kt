package cc.ferrand.batteur

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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


    private fun loadBeat(beatPath: String, model: MainViewModel) {
        model.loadBeat(beatPath)
        val tempo = model.getTempo().toInt()
        edtTempo.setText(tempo.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model: MainViewModel by viewModels()
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        tvTempo.requestFocus() // To avoid the tempo edit selected by default

        val drumAdapter =
            ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, model.drumList)
        drumAdapter.setNotifyOnChange(true)
        spnDrums.adapter = drumAdapter

        spnDrums.setOnTouchListener { v, event ->
            if (event?.action == MotionEvent.ACTION_UP)
                model.drumList[0] = "Choose an SFZ file..."

            false
        }

        spnDrums.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                model.drumList[0] = "Choose an SFZ file..."

            false
        }

        spnDrums.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                Log.d("spnDrums", "Item $position selected !")

                if (position == model.currentDrumIndex)
                    return

                if (position == 0) {
                    ChooserDialog(parent.context)
                        .withFilter(false, "sfz")
                        .withChosenListener { s: String, file: File ->
                            model.loadSfzFile(s)
                            model.customDrumName = file.nameWithoutExtension
                            model.currentDrumIndex = position
                            model.drumList[0] = model.customDrumName
                            drumAdapter.notifyDataSetChanged()
                        }
                        .withOnDismissListener {
                            spnDrums.setSelection(model.currentDrumIndex)
                            model.drumList[0] = model.customDrumName
                            drumAdapter.notifyDataSetChanged()
                        }
                        .build()
                        .show()

                } else {
                    val drumFilePath =
                        "${dataDir.path}/${model.drumAssetDirectory}/${model.drumList[position]}${model.drumFileExtension}"
                    model.loadSfzFile(drumFilePath)
                    Log.d("spnDrums", "Beat path $drumFilePath")
                    model.currentDrumIndex = position
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                model.drumList[0] = model.customDrumName
            }
        }

        val beatAdapter =
            ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, model.beatList)
        beatAdapter.setNotifyOnChange(true)
        spnBeats.adapter = beatAdapter

        spnBeats.setOnTouchListener { v, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                Log.d("On Touch", "UP")
                model.beatList[0] = "Choose a file..."
            }

            false
        }

        spnBeats.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                model.beatList[0] = "Choose a file..."

            false
        }

        spnBeats.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                Log.d("spnBeats", "Item $position selected !")
                if (position == model.currentBeatIndex)
                    return

                if (position == 0) {
                    ChooserDialog(parent.context)
                        .withFilter(false, "json")
                        .withChosenListener { s: String, file: File ->
                            loadBeat(s, model)
                            changeButtonsToPlay()
                            model.customBeatName = file.nameWithoutExtension
                            model.currentBeatIndex = position
                            model.beatList[0] = model.customBeatName
                            beatAdapter.notifyDataSetChanged()
                        }
                        .withOnDismissListener {
                            spnBeats.setSelection(model.currentBeatIndex)
                            model.beatList[0] = model.customBeatName
                            beatAdapter.notifyDataSetChanged()
                        }
                        .build()
                        .show()
                } else {
                    val beatFilePath =
                        "${dataDir.path}/${model.beatAssetDirectory}/${model.beatList[position]}${model.beatFileExtension}"
                    Log.d("spnBeats", "Beat path $beatFilePath")
                    loadBeat(beatFilePath, model)
                    changeButtonsToPlay()
                    model.currentBeatIndex = position
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                model.beatList[0] = model.customBeatName
            }
        }

        if (model.currentBeatIndex == -1)
            spnBeats.setSelection(1)
        else
            spnBeats.setSelection(model.currentBeatIndex)

        if (model.currentDrumIndex == -1)
            spnDrums.setSelection(1)
        else
            spnDrums.setSelection(model.currentDrumIndex)

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
                    avgTimeDiff += (tapTimes[i] - tapTimes[i - 1]).toDouble()
                }
                avgTimeDiff /= 3
                val tapTempo = round(60.0 / (avgTimeDiff / 1000.0))
                if (tapTempo > 20.0 && tapTempo < 300.0) {
                    model.setTempo(tapTempo)
                    edtTempo.setText(tapTempo.toInt().toString())
                }
            }
        }

        if (model.isPlaying()) {
            changeButtonsToStop()
        }

        btnPlay.setOnClickListener {
            if (model.isPlaying()) {
                model.stop()
                changeButtonsToPlay()
            } else {
                model.play()
                changeButtonsToStop()
            }
        }
        btnFill.setOnClickListener { model.fillIn() }
        btnNext.setOnClickListener { model.next() }
    }


    private fun changeButtonsToPlay() {
        btnPlay.setBackgroundColor(applicationContext.getColor(R.color.colorPrimaryDark))
        btnPlay.text = "Play"
        btnFill.isEnabled = false
        btnNext.isEnabled = false
    }

    private fun changeButtonsToStop() {
        btnPlay.setBackgroundColor(applicationContext.getColor(R.color.design_default_color_error))
        btnPlay.text = "Stop"
        btnFill.isEnabled = true
        btnNext.isEnabled = true
    }
}
