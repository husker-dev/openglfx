#include "openglfx.h"


jni_utils(jobject, createDirectBuffer)(JNIEnv* env, jobject, jint size) {
    return env->NewDirectByteBuffer(new char[size], size);
}

jni_utils(void, cleanDirectBuffer)(JNIEnv* env, jobject, jobject directBuffer) {
    char* buffer = (char*)env->GetDirectBufferAddress(directBuffer);
    delete[] buffer;
}