//
// Created by paul on 05/06/20.
//

#ifndef BATTEUR_SOUNDENGINE_H
#define BATTEUR_SOUNDENGINE_H

#include <oboe/Oboe.h>
#include <oboe/LatencyTuner.h>
#include "sfizz.hpp"
#include "oboe/samples/shared/IRestartable.h"
#include "oboe/samples/shared/DefaultAudioStreamCallback.h"

class Callback: public DefaultAudioStreamCallback {
public:
    Callback(IRestartable& parent): DefaultAudioStreamCallback(parent) { }
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) override;
    void setBufferTuneEnabled(bool enabled) { tuneEnabled = enabled; }
    void loadSfzString(std::string_view string);
private:
    bool tuneEnabled = true;
    std::unique_ptr<oboe::LatencyTuner> latencyTuner;
    sfz::Sfizz sfizz;
};

class SoundEngine : public IRestartable {
public:
    SoundEngine();

    virtual ~SoundEngine() = default;

    // From IRestartable
    void restart() override;
    // These methods reset the underlying stream with new properties

    /**
     * Set the audio device which should be used for playback. Can be set to oboe::kUnspecified if
     * you want to use the default playback device (which is usually the built-in speaker if
     * no other audio devices, such as headphones, are attached).
     *
     * @param deviceId the audio device id, can be obtained through an {@link AudioDeviceInfo} object
     * using Java/JNI.
    */
    void setDeviceId(int32_t deviceId);
    void setChannelCount(int channelCount);
    void setAudioApi(oboe::AudioApi audioApi);
    void setBufferSizeInBursts(int32_t numBursts);
private:
    oboe::ManagedStream managedStream;
    std::unique_ptr<Callback> callback;

    oboe::Result createPlaybackStream(oboe::AudioStreamBuilder builder);
};


#endif //BATTEUR_SOUNDENGINE_H
