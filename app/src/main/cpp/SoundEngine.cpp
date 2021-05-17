//
// Created by paul on 05/06/20.
//

#include "SoundEngine.h"
#include "sfizz/external/filesystem/include/ghc/filesystem.hpp"
namespace fs = ghc::filesystem;
#include "oboe/samples/debug-utils/logging_macros.h"
#include <trace.h>

oboe::DataCallbackResult DataCallback::onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames)
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

    if (player == nullptr) {
        return oboe::DataCallbackResult::Stop;
    }

    std::unique_lock<SpinMutex> lock(processMutex, std::try_to_lock);
    if (!lock.owns_lock())
    {
        memset(buffers[0].data(), 0, numFrames * sizeof(float));
        memset(buffers[1].data(), 0, numFrames * sizeof(float));
        return oboe::DataCallbackResult::Continue;
    }
    batteur_tick(player, numFrames);

    auto* output = reinterpret_cast<float*>(audioData);
    float* audioBuffer[2] { buffers[0].data(), buffers[1].data() };
    const auto bufferSize = static_cast<int32_t>(buffers[0].size());
//    LOGD("Buffer size/num frames: %d %d", bufferSize, numFrames);
    int32_t renderIdx { 0 };
    while (numFrames > 0) {
        const auto frames = std::min(bufferSize, numFrames);
        sfizz.renderBlock(audioBuffer, frames);
        for (int32_t i = 0; i < frames; i++) {
            output[renderIdx + 2 * i] = buffers[0][i];
            output[renderIdx + 2 * i + 1] = buffers[1][i];
        }
        renderIdx += 2 * frames;
        numFrames -= frames;
    }

    if (Trace::isEnabled())
        Trace::endSection();

    return oboe::DataCallbackResult::Continue;
}

void SoundEngine::loadSfzString(const std::string& string)
{
    std::unique_lock<SpinMutex> lock(processMutex);
    sfizz.loadSfzString(fs::current_path().native(), string);
}

void SoundEngine::loadSfzFile(const std::string& path)
{
    std::unique_lock<SpinMutex> lock(processMutex);
    sfizz.loadSfzFile(path);
}

void SoundEngine::loadBeat(const std::string& path)
{
    batteur_stop(player);
    auto nextBeat = batteur_load_beat(path.c_str());
    if (nextBeat != nullptr) {
        batteur_load(player, nextBeat);
        batteur_free_beat(beat);
        beat = nextBeat;
    }
}

void SoundEngine::play()
{
    batteur_start(player);
}

bool SoundEngine::isPlaying()
{
    return batteur_playing(player);
}


void SoundEngine::stop()
{
    batteur_stop(player);
}

void SoundEngine::fillIn()
{
    batteur_fill_in(player);
}

void SoundEngine::next()
{
    batteur_next(player);
}

void SoundEngine::start()
{
    auto result = createPlaybackStream(
            *oboe::AudioStreamBuilder()
            .setChannelCount(2)
            ->setAudioApi(oboe::AudioApi::Unspecified)
    );
    if (result == oboe::Result::OK){
        std::unique_lock<SpinMutex> lock(processMutex);
        const auto sampleRate = managedStream->getSampleRate();
        sfizz.setSampleRate(sampleRate);
        if (player)
            batteur_set_sample_rate(player, sampleRate);

        const auto blockSize = managedStream->getFramesPerCallback();
        sfizz.setSamplesPerBlock(blockSize);
        callback->resizeBuffers(blockSize);

        result = managedStream->requestStart();
        if (result != oboe::Result::OK) {
            LOGE("Error starting stream. %s", oboe::convertToText(result));
        }
    } else {
        LOGE("Error creating playback stream. Error: %s", oboe::convertToText(result));
    }
}

void SoundEngine::batteurCallback(int delay, uint8_t number, uint8_t value, void* cbdata)
{
    auto* sfizz = (sfz::Sfizz*)cbdata;
    if (sfizz == nullptr)
        return;

    if (value > 0)
        sfizz->noteOn(delay, number, value);
    else
        sfizz->noteOff(delay, number, value);
}

SoundEngine::SoundEngine()
{
    sfizz.setSampleQuality(sfz::Sfizz::ProcessMode::ProcessLive, 1);
    sfizz.setNumVoices(24);
    batteur_note_cb(player, &SoundEngine::batteurCallback, &sfizz);
    start();
}

SoundEngine::~SoundEngine()
{
    batteur_free(player);
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
            ->setDataCallback(callback.get())
            ->openManagedStream(managedStream);
}

double SoundEngine::getTempo() const {
    return batteur_get_tempo(player);
}

void SoundEngine::setTempo(double tempo) {
    batteur_set_tempo(player, tempo);
}
