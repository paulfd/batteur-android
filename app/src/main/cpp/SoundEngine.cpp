//
// Created by paul on 05/06/20.
//

#include "SoundEngine.h"
#include "sfizz/src/external/ghc/filesystem.hpp"
namespace fs = ghc::filesystem;
#include "oboe/samples/debug-utils/logging_macros.h"
#include <trace.h>

oboe::DataCallbackResult Callback::onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames)
{
    if (Trace::isEnabled()) {
        const auto underrunCountResult = oboeStream->getXRunCount();
        const int bufferSize = oboeStream->getBufferSizeInFrames();
        Trace::beginSection("numFrames %d, Underruns %d, buffer size %d",
            numFrames, underrunCountResult.value(), bufferSize);
    }
    const auto numChannels = oboeStream->getChannelCount();
    if (numChannels != 2) {
        LOGE("Not a stereo channel?? Got %d", numChannels);
        return oboe::DataCallbackResult::Stop;
    }

    auto* output = reinterpret_cast<float*>(audioData);
    float* audioBuffer[2] { buffers[0].data(), buffers[1].data() };
    const auto bufferSize = static_cast<int32_t>(buffers[0].size());
    int32_t renderIdx { 0 };
    while (numFrames > 0) {
        const auto frames = std::min(bufferSize, numFrames);
        sfizz.renderBlock(audioBuffer, frames);
        for (int32_t i = 0; i < frames; i++) {
            output[renderIdx + 2 * i] = buffers[0][i];
            output[renderIdx + 2 * i + 1] = buffers[1][i];
        }
        renderIdx += frames;
        numFrames -= frames;
    }
    if (Trace::isEnabled())
        Trace::endSection();
    return oboe::DataCallbackResult::Continue;
}

void Callback::loadSfzString(std::string_view string)
{
    sfizz.loadSfzString(fs::current_path().native(), std::string(string));
}

void SoundEngine::start()
{
    auto result = createPlaybackStream(
            *oboe::AudioStreamBuilder()
            .setChannelCount(2)
            ->setAudioApi(oboe::AudioApi::Unspecified)
    );
    if (result == oboe::Result::OK){
        managedStream->start();
    } else {
        LOGE("Error creating playback stream. Error: %s", oboe::convertToText(result));
    }
}

SoundEngine::SoundEngine()
{
    start();
}

// From IRestartable
void SoundEngine::restart()
{
    start();
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
    createPlaybackStream(*oboe::AudioStreamBuilder(*managedStream).
            setDeviceId(deviceId));
    LOGD("Device ID is now %d", managedStream->getDeviceId());
}

void SoundEngine::setChannelCount(int channelCount)
{
    createPlaybackStream(*oboe::AudioStreamBuilder(*managedStream)
            .setChannelCount(channelCount));
    LOGD("Channel count is now %d", managedStream->getChannelCount());
}

void SoundEngine::setAudioApi(oboe::AudioApi audioApi)
{
    createPlaybackStream(*oboe::AudioStreamBuilder(*managedStream)
            .setAudioApi(audioApi));
    LOGD("AudioAPI is now %d", managedStream->getAudioApi());
}

void SoundEngine::setBufferSizeInBursts(int32_t numBursts)
{
    auto result = managedStream->setBufferSizeInFrames(
            numBursts * managedStream->getFramesPerBurst());
    if (result) {
        LOGD("Buffer size successfully changed to %d", result.value());
    } else {
        LOGW("Buffer size could not be changed, %d", result.error());
    }
}

oboe::Result SoundEngine::createPlaybackStream(oboe::AudioStreamBuilder builder)
{
    return builder.setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setFormat(oboe::AudioFormat::Float)
            ->setCallback(callback.get())
            ->openManagedStream(managedStream);
}
