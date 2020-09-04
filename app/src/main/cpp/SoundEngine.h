//
// Created by paul on 05/06/20.
//

#ifndef BATTEUR_SOUNDENGINE_H
#define BATTEUR_SOUNDENGINE_H

#include <oboe/Oboe.h>
#include <oboe/LatencyTuner.h>
#include <vector>
#include <array>
#include "sfizz.hpp"
#include "batteur.h"
#include "oboe/samples/shared/IRestartable.h"
#include "oboe/samples/shared/DefaultAudioStreamCallback.h"

class Callback: public DefaultAudioStreamCallback {
public:
    Callback() = delete;
    Callback(IRestartable& parent, sfz::Sfizz& sfizz, batteur_player_t* player)
    : DefaultAudioStreamCallback(parent)
    , sfizz(sfizz)
    , player(player)
    {
        if (player == nullptr)
            std::terminate();

        for (auto& buffer: buffers)
            buffer.resize(oboe::DefaultStreamValues::FramesPerBurst);
    }
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) override;
private:
    std::array<std::vector<float>, 2> buffers;
    sfz::Sfizz& sfizz;
    batteur_player_t* player;
};

class SoundEngine : public IRestartable {
public:
    SoundEngine();
    virtual ~SoundEngine();

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
    void playNote(int number, uint8_t velocity)
    {
        sfizz.noteOn(0, number, velocity);
    }
    void setBufferSizeInBursts(int32_t numBursts);
    void loadSfzFile(std::string_view path);
    void loadSfzString(std::string_view string);
    void loadBeat(std::string_view string);
    void play();
    void stop();
    void fillIn();
    void next();
    bool isPlaying();
private:
    sfz::Sfizz sfizz;
    batteur_player_t* player { batteur_new() };
    batteur_beat_t* beat;
    oboe::ManagedStream managedStream;
    std::unique_ptr<Callback> callback { std::make_unique<Callback>(*this, sfizz, player) };
    oboe::Result createPlaybackStream(oboe::AudioStreamBuilder builder);
    static void batteurCallback(int delay, uint8_t number, uint8_t value, void* cbdata);
    void start();

};


#endif //BATTEUR_SOUNDENGINE_H
