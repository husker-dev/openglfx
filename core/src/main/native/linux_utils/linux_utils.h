#include <iostream>
#include <jni.h>
#include <GL/glx.h>
#include <X11/Xlib.h>
#include <X11/Xutil.h>

#define JNIEXPORT1 __attribute__((unused)) JNIEXPORT

typedef GLXContext(*glXCreateContextAttribsARBProc)(Display*, GLXFBConfig, GLXContext, Bool, const int*);

extern "C" {

    jlongArray createLongArray(JNIEnv* env, int size, jlong* array){
        jlongArray result = env->NewLongArray(size);
        env->SetLongArrayRegion(result, 0, size, array);
        return result;
    }

    JNIEXPORT1 jlongArray JNICALL Java_com_huskerdev_openglfx_utils_LinuxUtils_createContext(JNIEnv* env, jobject, jboolean isCore, jlong shareWith) {
        Display* display = XOpenDisplay(nullptr);
        Window win = XCreateSimpleWindow(display, DefaultRootWindow(display), 0, 0, 100, 100, 0, 0, 0);

        static int visual_attribs[] = {
            GLX_RENDER_TYPE, GLX_RGBA_BIT,
            GLX_DRAWABLE_TYPE, GLX_WINDOW_BIT,
            GLX_DOUBLEBUFFER, true,
            GLX_RED_SIZE, 1,
            GLX_GREEN_SIZE, 1,
            GLX_BLUE_SIZE, 1,
            None
        };

        int num_fbc = 0;
        GLXFBConfig* fbc = glXChooseFBConfig(display, DefaultScreen(display), visual_attribs, &num_fbc);

        auto glXCreateContextAttribsARB = (glXCreateContextAttribsARBProc)glXGetProcAddress((GLubyte*)"glXCreateContextAttribsARB");

        static int context_attribs[] = {
            GLX_CONTEXT_PROFILE_MASK_ARB, isCore ? GLX_CONTEXT_CORE_PROFILE_BIT_ARB : GLX_CONTEXT_COMPATIBILITY_PROFILE_BIT_ARB,
            None
        };

        GLXContext context = glXCreateContextAttribsARB(display, fbc[0], (GLXContext)shareWith, true, context_attribs);

        jlong array[] = { (jlong)display, (jlong)win, (jlong)context };
        return createLongArray(env, 3, array);
	}

    JNIEXPORT1 jlongArray JNICALL Java_com_huskerdev_openglfx_utils_LinuxUtils_getCurrentContext(JNIEnv* env, jobject) {
        jlong array[] = { (jlong)glXGetCurrentDisplay(), (jlong)glXGetCurrentDrawable(), (jlong)glXGetCurrentContext() };
        return createLongArray(env, 3, array);
	}

    JNIEXPORT1 jboolean JNICALL Java_com_huskerdev_openglfx_utils_LinuxUtils_setCurrentContext(JNIEnv* env, jobject, jlong display, jlong window, jlong context) {
        return glXMakeCurrent((Display*)display, (Window)window, (GLXContext)context);
	}
}