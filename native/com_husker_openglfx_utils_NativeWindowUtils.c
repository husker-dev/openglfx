#include "com_husker_openglfx_utils_NativeWindowUtils.h"
#include "stdlib.h"
#include "stdio.h"
#include "windows.h"

JNIEXPORT void JNICALL Java_com_husker_openglfx_utils_NativeWindowUtils_hideWindowWin32(JNIEnv * env, jclass class, jlong hwnd){
    SetWindowLong(hwnd, GWL_EXSTYLE, WS_EX_NOACTIVATE);
}
