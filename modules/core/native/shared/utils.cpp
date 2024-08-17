#include "openglfx.h"


jni_utils(jobject, createDirectBuffer)(JNIEnv* env, jobject, jint size) {
    return env->NewDirectByteBuffer(new char[size], size);
}

jni_utils(void, cleanDirectBuffer)(JNIEnv* env, jobject, jobject directBuffer) {
    void* buffer = env->GetDirectBufferAddress(directBuffer);
    delete[] buffer;
}