package com.husker.openglfx.utils;

import com.jogamp.newt.opengl.GLWindow;
import com.sun.javafx.PlatformUtil;

public class NativeWindowUtils {

    static{
        LibUtils.loadInternalLib("openglfx");
    }

    public static void hideWindow(GLWindow window){
        if(PlatformUtil.isWindows())
            hideWindowWin32(window.getWindowHandle());
    }

    protected static native void hideWindowWin32(long hwnd);
}
