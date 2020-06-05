#include <jni.h>
#include <string>
#include <sfizz.h>

extern "C" JNIEXPORT jstring JNICALL
Java_cc_ferrand_batteur_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_cc_ferrand_batteur_MainActivity_createSynth(
        JNIEnv* env,
        jobject /* this */) {
    auto synth = sfizz_create_synth();
    sfizz_free(synth);
}
