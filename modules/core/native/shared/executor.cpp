#include "openglfx.h"

#include <iostream>


jni_gl(void, nInitGLFunctions)(JNIEnv* env, jobject) {
    a_glViewport = (glViewportPtr)a_GetProcAddress("glViewport");
    a_glTexParameteri = (glTexParameteriPtr)a_GetProcAddress("glTexParameteri");
    a_glTexImage2D = (glTexImage2DPtr)a_GetProcAddress("glTexImage2D");
    a_glReadPixels = (glReadPixelsPtr)a_GetProcAddress("glReadPixels");
    a_glFramebufferRenderbuffer = (glFramebufferRenderbufferPtr)a_GetProcAddress("glFramebufferRenderbuffer");
    a_glRenderbufferStorage = (glRenderbufferStoragePtr)a_GetProcAddress("glRenderbufferStorage");
    a_glFramebufferTexture2D = (glFramebufferTexture2DPtr)a_GetProcAddress("glFramebufferTexture2D");
    a_glBindTexture = (glBindTexturePtr)a_GetProcAddress("glBindTexture");
    a_glBindRenderbuffer = (glBindRenderbufferPtr)a_GetProcAddress("glBindRenderbuffer");
    a_glBindFramebuffer = (glBindFramebufferPtr)a_GetProcAddress("glBindFramebuffer");
    a_glGenTextures = (glGenTexturesPtr)a_GetProcAddress("glGenTextures");
    a_glGenRenderbuffers = (glGenRenderbuffersPtr)a_GetProcAddress("glGenRenderbuffers");
    a_glGenFramebuffers = (glGenFramebuffersPtr)a_GetProcAddress("glGenFramebuffers");
    a_glDeleteTextures = (glDeleteTexturesPtr)a_GetProcAddress("glDeleteTextures");
    a_glDeleteRenderbuffers = (glDeleteRenderbuffersPtr)a_GetProcAddress("glDeleteRenderbuffers");
    a_glDeleteFramebuffers = (glDeleteFramebuffersPtr)a_GetProcAddress("glDeleteFramebuffers");
    a_glFinish = (glFinishPtr)a_GetProcAddress("glFinish");
    a_glGetError = (glGetErrorPtr)a_GetProcAddress("glGetError");
    a_glCopyTexSubImage2D = (glCopyTexSubImage2DPtr)a_GetProcAddress("glCopyTexSubImage2D");

    a_glRenderbufferStorageMultisample = (glRenderbufferStorageMultisamplePtr)a_GetProcAddress("glRenderbufferStorageMultisample");
    a_glBlitFramebuffer = (glBlitFramebufferPtr)a_GetProcAddress("glBlitFramebuffer");
    a_glGetIntegerv = (glGetIntegervPtr)a_GetProcAddress("glGetIntegerv");

    a_glCreateShader = (glCreateShaderPtr)a_GetProcAddress("glCreateShader");
    a_glDeleteShader = (glDeleteShaderPtr)a_GetProcAddress("glDeleteShader");
    a_glShaderSource = (glShaderSourcePtr)a_GetProcAddress("glShaderSource");
    a_glCompileShader = (glCompileShaderPtr)a_GetProcAddress("glCompileShader");
    a_glCreateProgram = (glCreateProgramPtr)a_GetProcAddress("glCreateProgram");
    a_glAttachShader = (glAttachShaderPtr)a_GetProcAddress("glAttachShader");
    a_glLinkProgram = (glLinkProgramPtr)a_GetProcAddress("glLinkProgram");
    a_glUseProgram = (glUseProgramPtr)a_GetProcAddress("glUseProgram");
    a_glUseProgram = (glUseProgramPtr)a_GetProcAddress("glUseProgram");
    a_glGetUniformLocation = (glGetUniformLocationPtr)a_GetProcAddress("glGetUniformLocation");
    a_glGetAttribLocation = (glGetAttribLocationPtr)a_GetProcAddress("glGetAttribLocation");
    a_glUniform2f = (glUniform2fPtr)a_GetProcAddress("glUniform2f");
    a_glGetShaderiv = (glGetShaderivPtr)a_GetProcAddress("glGetShaderiv");
    a_glGetShaderInfoLog = (glGetShaderInfoLogPtr)a_GetProcAddress("glGetShaderInfoLog");

    a_glGenVertexArrays = (glGenVertexArraysPtr)a_GetProcAddress("glGenVertexArrays");
    a_glBindVertexArray = (glBindVertexArrayPtr)a_GetProcAddress("glBindVertexArray");
    a_glGenBuffers = (glGenBuffersPtr)a_GetProcAddress("glGenBuffers");
    a_glBindBuffer = (glBindBufferPtr)a_GetProcAddress("glBindBuffer");
    a_glBufferData = (glBufferDataPtr)a_GetProcAddress("glBufferData");
    a_glVertexAttribPointer = (glVertexAttribPointerPtr)a_GetProcAddress("glVertexAttribPointer");
    a_glEnableVertexAttribArray = (glEnableVertexAttribArrayPtr)a_GetProcAddress("glEnableVertexAttribArray");
    a_glDeleteBuffers = (glDeleteBuffersPtr)a_GetProcAddress("glDeleteBuffers");
    a_glDrawArrays = (glDrawArraysPtr)a_GetProcAddress("glDrawArrays");
}

jni_gl(void, glDeleteFramebuffers)(JNIEnv* env, jobject, jint fbo) {
    a_glDeleteFramebuffers(1, (GLuint*)&fbo);
}

jni_gl(void, glDeleteRenderbuffers)(JNIEnv* env, jobject, jint rbo) {
    a_glDeleteRenderbuffers(1, (GLuint*)&rbo);
}

jni_gl(void, glDeleteTextures)(JNIEnv* env, jobject, jint texture) {
    a_glDeleteTextures(1, (GLuint*)&texture);
}

jni_gl(jint, glGenFramebuffers)(JNIEnv* env, jobject) {
    GLuint framebuffer = 0;
    a_glGenFramebuffers(1, &framebuffer);
    return framebuffer;
}

jni_gl(jint, glGenRenderbuffers)(JNIEnv* env, jobject) {
    GLuint renderbuffer;
    a_glGenRenderbuffers(1, &renderbuffer);
    return renderbuffer;
}

jni_gl(jint, glGenTextures)(JNIEnv* env, jobject) {
    GLuint texture;
    a_glGenTextures(1, &texture);
    return texture;
}

jni_gl(void, glBindFramebuffer)(JNIEnv* env, jobject, jint target, jint fbo) {
    a_glBindFramebuffer(target, fbo);
}

jni_gl(void, glBindRenderbuffer)(JNIEnv* env, jobject, jint target, jint rbo) {
    a_glBindRenderbuffer(target, rbo);
}

jni_gl(void, glBindTexture)(JNIEnv* env, jobject, jint target, jint texture) {
    a_glBindTexture(target, texture);
}

jni_gl(void, glFramebufferTexture2D)(JNIEnv* env, jobject, jint target, jint attachment, jint texture, jint texId, jint level) {
    a_glFramebufferTexture2D(target, attachment, texture, texId, level);
}

jni_gl(void, glRenderbufferStorage)(JNIEnv* env, jobject, jint target, jint internalFormat, jint width, jint height) {
    a_glRenderbufferStorage(target, internalFormat, width, height);
}

jni_gl(void, glFramebufferRenderbuffer)(JNIEnv* env, jobject, jint target, jint attachment, jint renderbufferTarget, jint renderbuffer) {
    a_glFramebufferRenderbuffer(target, attachment, renderbufferTarget, renderbuffer);
}

jni_gl(void, glReadPixels)(JNIEnv* env, jobject, jint x, jint y, jint width, jint height, jint format, jint type, jobject pixels) {
    char* bb = (char*)env->GetDirectBufferAddress(pixels);
    a_glReadPixels(x, y, width, height, format, type, bb);
}

jni_gl(void, glTexImage2D)(JNIEnv* env, jobject, jint target, jint level, jint internalFormat, jint width, jint height, jint border, jint format, jint type, jobject pixels) {
    char* bb = pixels ? (char*)env->GetDirectBufferAddress(pixels) : NULL;
    a_glTexImage2D(target, level, internalFormat, width, height, border, format, type, bb);
}

jni_gl(void, glTexParameteri)(JNIEnv* env, jobject, jint target, jint pname, jint param) {
    a_glTexParameteri(target, pname, param);
}

jni_gl(void, glViewport)(JNIEnv* env, jobject, jint x, jint y, jint w, jint h) {
    a_glViewport(x, y, w, h);
}

jni_gl(void, glFinish)(JNIEnv* env, jobject) {
    a_glFinish();
}

jni_gl(void, glCopyTexSubImage2D)(JNIEnv* env, jobject, jint target, jint level, jint xoffset, jint yoffset, jint x, jint y, jint width, jint height) {
    a_glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
}

jni_gl(void, glRenderbufferStorageMultisample)(JNIEnv* env, jobject, jint target, jint samples, jint internalformat, jint width, jint height) {
    a_glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
}

jni_gl(void, glBlitFramebuffer)(JNIEnv* env, jobject, jint srcX0, jint srcY0, jint srcX1, jint srcY1, jint dstX0, jint dstY0, jint dstX1, jint dstY1, jint mask, jint filter) {
    a_glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
}

jni_gl(jint, glGetInteger)(JNIEnv* env, jobject, jint pname) {
    GLint data = 0;
    a_glGetIntegerv(pname, &data);
    return (jint)data;
}

jni_gl(jint, glCreateShader)(JNIEnv* env, jobject, jint type) {
    return a_glCreateShader(type);
}

jni_gl(void, glDeleteShader)(JNIEnv* env, jobject, jint shader) {
    a_glDeleteShader(shader);
}

jni_gl(void, glShaderSource)(JNIEnv* env, jobject, jint shader, jstring source) {
    const char* sourceCh = env->GetStringUTFChars(source, 0);
    a_glShaderSource(shader, 1, &sourceCh, NULL);
    env->ReleaseStringUTFChars(source, sourceCh);
}

jni_gl(void, glCompileShader)(JNIEnv* env, jobject, jint shader) {
    a_glCompileShader(shader);
}

jni_gl(jint, glCreateProgram)(JNIEnv* env, jobject) {
    return a_glCreateProgram();
}

jni_gl(void, glAttachShader)(JNIEnv* env, jobject, jint program, jint shader) {
    a_glAttachShader(program, shader);
}

jni_gl(void, glLinkProgram)(JNIEnv* env, jobject, jint program) {
    a_glLinkProgram(program);
}

jni_gl(void, glUseProgram)(JNIEnv* env, jobject, jint program) {
    a_glUseProgram(program);
}

jni_gl(jint, glGetUniformLocation)(JNIEnv* env, jobject, jint program, jstring name) {
    const char* nameCh = env->GetStringUTFChars(name, 0);

    jint location = (jint)a_glGetUniformLocation(program, nameCh);
    env->ReleaseStringUTFChars(name, nameCh);
    return location;
}

jni_gl(jint, glGetAttribLocation)(JNIEnv* env, jobject, jint program, jstring name) {
    const char* nameCh = env->GetStringUTFChars(name, 0);

    jint location = (jint)a_glGetAttribLocation(program, nameCh);
    env->ReleaseStringUTFChars(name, nameCh);
    return location;
}

jni_gl(void, glUniform2f)(JNIEnv* env, jobject, jint program, jfloat value1, jfloat value2) {
    a_glUniform2f(program, value1, value2);
}

jni_gl(jint, glGetShaderi)(JNIEnv* env, jobject, jint shader, jint pname) {
    GLint result = 0;
    a_glGetShaderiv(shader, pname, &result);
    return result;
}

#define GL_INFO_LOG_LENGTH 0x8B84
jni_gl(jstring, glGetShaderInfoLog)(JNIEnv* env, jobject, jint shader) {
    GLint maxLength = 0;
    a_glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &maxLength);

    char* errorLog = new char[maxLength];
    a_glGetShaderInfoLog(shader, maxLength, &maxLength, &errorLog[0]);

    jstring result = env->NewStringUTF(errorLog);
    delete[] errorLog;
    return result;
}

jni_gl(jint, glGenVertexArrays)(JNIEnv* env, jobject) {
    unsigned int vao;
    a_glGenVertexArrays(1, &vao);
    return vao;
}

jni_gl(void, glBindVertexArray)(JNIEnv* env, jobject, jint vao) {
    a_glBindVertexArray(vao);
}

jni_gl(jint, glGenBuffers)(JNIEnv* env, jobject) {
    unsigned int vbo;
    a_glGenBuffers(1, &vbo);
    return vbo;
}

jni_gl(void, glBindBuffer)(JNIEnv* env, jobject, jint target, jint buffer) {
    a_glBindBuffer(target, buffer);
}

jni_gl(void, glBufferData)(JNIEnv* env, jobject, jint target, jobject verticesBuffer, jint type) {
    GLfloat* vertices = (GLfloat*)env->GetDirectBufferAddress(verticesBuffer);
    jlong length = env->GetDirectBufferCapacity(verticesBuffer) * 4;
    a_glBufferData(target, length, vertices, type);
}

jni_gl(void, glVertexAttribPointer)(JNIEnv* env, jobject, jint index, jint size, jint type, jboolean normalized, jint stride, jlong offset) {
    a_glVertexAttribPointer(index, size, type, normalized, stride, (void*)offset);
}

jni_gl(void, glEnableVertexAttribArray)(JNIEnv* env, jobject, jint index) {
    a_glEnableVertexAttribArray(index);
}

jni_gl(void, glDeleteBuffers)(JNIEnv* env, jobject, jint buffer) {
    a_glDeleteBuffers(1, (GLuint*)&buffer);
}

jni_gl(void, glDrawArrays)(JNIEnv* env, jobject, jint mode, jint first, jint count) {
    a_glDrawArrays(mode, first, count);
}

jni_gl(jint, glGetError)(JNIEnv* env, jobject) {
    return (jint)a_glGetError();
}