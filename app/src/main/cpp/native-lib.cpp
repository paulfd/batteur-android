#include <jni.h>
#include <string>
#include <oboe/Oboe.h>
#include "SoundEngine.h"
#include "logging_macros.h"


extern "C" JNIEXPORT jlong JNICALL
Java_cc_ferrand_batteur_MainActivity_createEngine(
        JNIEnv* env,
        jobject /* this */) {
    SoundEngine* engine = new(std::nothrow) SoundEngine();
    return reinterpret_cast<jlong>(engine);
}

extern "C" JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_freeEngine(
        JNIEnv* env,
        jobject /* this */,
        jlong engineHandle) {
    delete reinterpret_cast<SoundEngine *>(engineHandle);
}

extern "C" JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_loadSfzString(
        JNIEnv* env,
        jobject /* this */,
        jlong engineHandle,
        jstring sfzString) {
    const char* sfz = env->GetStringUTFChars(sfzString, NULL);
    auto engine = reinterpret_cast<SoundEngine*>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
    engine->loadSfzString(sfz);
}

extern "C" JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_loadSfzFile(
        JNIEnv* env,
        jobject /* this */,
        jlong engineHandle,
        jstring sfzFile) {
    const char* sfz = env->GetStringUTFChars(sfzFile, NULL);
    auto engine = reinterpret_cast<SoundEngine*>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
    engine->loadSfzFile(sfz);
}

extern "C" JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_loadBeat(
        JNIEnv* env,
        jobject /* this */,
        jlong engineHandle,
        jstring sfzFile) {
    const char* beat = env->GetStringUTFChars(sfzFile, NULL);
    auto engine = reinterpret_cast<SoundEngine*>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
    engine->loadBeat(beat);
}

extern "C" JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_play(
        JNIEnv* env,
        jobject /* this */,
        jlong engineHandle) {
    auto engine = reinterpret_cast<SoundEngine*>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
    engine->play();
}

extern "C" JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_fillIn(
        JNIEnv* env,
        jobject /* this */,
        jlong engineHandle) {
    auto engine = reinterpret_cast<SoundEngine*>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
    engine->fillIn();
}

extern "C" JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_next(
        JNIEnv* env,
        jobject /* this */,
        jlong engineHandle) {
    auto engine = reinterpret_cast<SoundEngine*>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
    engine->next();
}

extern "C" JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_stop(
        JNIEnv* env,
        jobject /* this */,
        jlong engineHandle) {
    auto engine = reinterpret_cast<SoundEngine*>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
    engine->stop();
}


extern "C" JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_playNote(
        JNIEnv* env,
        jobject /* this */,
        jlong engineHandle,
        jint number,
        jfloat velocity) {
    auto engine = reinterpret_cast<SoundEngine*>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
    const auto intVelocity = [velocity]() -> uint8_t {
        if (velocity < 0.0f)
            return 0;
        if (velocity > 127.0f)
            return 127;
        return static_cast<uint8_t>(127.0f * velocity);
    }();
    engine->playNote(number, intVelocity);
}

extern "C" JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_setAudioApi(
        JNIEnv* env,
        jobject /* this */,
        jlong engineHandle,
        jint audioApi) {
    auto engine = reinterpret_cast<SoundEngine*>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
    oboe::AudioApi api = static_cast<oboe::AudioApi>(audioApi);
    engine->setAudioApi(api);
}

extern "C" JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_setBufferSizeInBursts(
        JNIEnv* env,
        jobject /* this */,
        jlong engineHandle,
        jint bursts) {
    auto engine = reinterpret_cast<SoundEngine*>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_setAudioDevice(
        JNIEnv* env,
        jobject /* this */,
        jlong engineHandle,
        jint deviceId) {
    auto engine = reinterpret_cast<SoundEngine*>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_00024Companion_setDefaultStreamValues(
        JNIEnv *env,
       jobject thiz,
       jint sampleRate,
       jint framesPerBurst) {
    oboe::DefaultStreamValues::SampleRate = (int32_t) sampleRate;
    oboe::DefaultStreamValues::FramesPerBurst = (int32_t) framesPerBurst;
}