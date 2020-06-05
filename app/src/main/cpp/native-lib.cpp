#include <jni.h>
#include <string>
#include <sfizz.h>
#include <oboe/Oboe.h>

extern "C" JNIEXPORT jlong JNICALL
Java_cc_ferrand_batteur_MainActivity_createSynth(
        JNIEnv* env,
        jobject /* this */) {
    auto synth = sfizz_create_synth();
    return (jlong)synth;
}

extern "C" JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_freeSynth(
        JNIEnv* env,
        jobject /* this */,
        jlong synthHandle) {
    auto synth = (sfizz_synth_t*)synthHandle;
    sfizz_free(synth);
}

extern "C" JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_loadSfzString(
        JNIEnv* env,
        jobject /* this */,
        jlong synthHandle,
        jstring sfzString) {
    const char* sfz = env->GetStringUTFChars(sfzString, NULL);
    auto synth = (sfizz_synth_t*) synthHandle;
    sfizz_load_string(synth, "/", sfz);
}

extern "C" JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_playNote(
        JNIEnv* env,
        jobject /* this */,
        jlong synthHandle,
        jint number,
        jfloat velocity) {
    auto synth = (sfizz_synth_t*) synthHandle;
    if (number > 0 && number < 128 && velocity >= 0.0f && velocity <= 1.0f)
        sfizz_send_note_on(synth, 0, number, (uint8_t)(velocity * 127.0f));
}

extern "C" JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_startOboe(
        JNIEnv* env,
        jobject /* this */) {
    auto result = oboe::AudioStreamBuilder().setSharingMode(oboe::SharingMode::Exclusive)
                ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
                ->setFormat(oboe::AudioFormat::Float)
                ->setCallback()
}
