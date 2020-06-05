//
// Created by paul on 05/06/20.
//

#include "SoundEngine.h"
#include <filesystem>

oboe::DataCallbackResult Callback::onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) override
{
    if (!latencyTuner)
        latencyTuner = std::make_unique<oboe::LatencyTuner>(*oboeStream);

    if (tuneEnabled && oboeStream->getAudioApi() == oboe::AudioApi::AAudio) {
        latencyTuner->tune();
    }
}

void Callback::loadSfzString(std::string_view string)
{
    sfizz.loadSfzString(std::filesystem::current_path().native(), string);
}

SoundEngine::SoundEngine()
{

}

// From IRestartable
void SoundEngine::restart() override
{
    this->restart();
    int_fast32_t if()
}

// These methods reset the underlying stream with new properties

/**
 * Set the audio device which should be used for playback. Can be set to oboe::kUnspecified if
 * you want to use the default playback device (which is usually the built-in speaker if
 * no other audio devices, such as headphones, are attached).
 *
 * @param deviceId the audio device id, can be obtained through an {@link AudioDeviceInfo} object
 * using Java/JNI.
*/
void SoundEngine::setDeviceId(int32_t deviceId)
{
}

void SoundEngine::setChannelCount(int channelCount)
{

}

void SoundEngine::setAudioApi(oboe::AudioApi audioApi)
{

}

void SoundEngine::setBufferSizeInBursts(int32_t numBursts)
{

}

oboe::Result SoundEngine::createPlaybackStream(oboe::AudioStreamBuilder builder)
{

}
