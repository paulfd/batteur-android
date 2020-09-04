package cc.ferrand.batteur

import android.content.Context
import android.content.pm.PackageManager
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiDeviceStatus
import android.media.midi.MidiManager
import android.os.Handler
import android.widget.Toast

class MidiListener(val context: Context) : MidiManager.DeviceCallback() {
    private var midiManager: MidiManager? = null

    init {
         if(context.packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI)) {
             Toast.makeText(context, "Has MIDI!", Toast.LENGTH_SHORT).show()
             midiManager = context.getSystemService(Context.MIDI_SERVICE) as MidiManager
         }
    }

    fun listenForChanges() {
        midiManager?.registerDeviceCallback(this, Handler(context.mainLooper))
    }

    fun stopListeningForChanges() {
        midiManager?.unregisterDeviceCallback(this)
    }

    override fun onDeviceAdded(device: MidiDeviceInfo?) {
        Toast.makeText(context, "MIDI device connected!", Toast.LENGTH_SHORT).show()
        super.onDeviceAdded(device)
    }

    override fun onDeviceRemoved(device: MidiDeviceInfo?) {
        Toast.makeText(context, "MIDI device disconnected!", Toast.LENGTH_SHORT).show()
        super.onDeviceRemoved(device)
    }

    override fun onDeviceStatusChanged(status: MidiDeviceStatus?) {
        super.onDeviceStatusChanged(status)
    }
}