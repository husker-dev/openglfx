//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
// Applied for HiDPI by Husker
//

package com.husker.joglfx;

import com.jogamp.common.util.PropertyAccess;
import com.jogamp.nativewindow.AbstractGraphicsConfiguration;
import com.jogamp.nativewindow.AbstractGraphicsScreen;
import com.jogamp.nativewindow.Capabilities;
import com.jogamp.nativewindow.CapabilitiesChooser;
import com.jogamp.nativewindow.GraphicsConfigurationFactory;
import com.jogamp.nativewindow.NativeSurface;
import com.jogamp.nativewindow.NativeWindow;
import com.jogamp.nativewindow.NativeWindowException;
import com.jogamp.nativewindow.NativeWindowFactory;
import com.jogamp.nativewindow.NativeWindowHolder;
import com.jogamp.nativewindow.SurfaceUpdatedListener;
import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.nativewindow.javafx.JFXAccessor;
import com.jogamp.nativewindow.util.Insets;
import com.jogamp.nativewindow.util.InsetsImmutable;
import com.jogamp.nativewindow.util.Point;
import com.jogamp.nativewindow.util.Rectangle;
import com.jogamp.newt.Display;
import com.jogamp.newt.Window.FocusRunnable;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.util.EDTUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import jogamp.newt.Debug;
import jogamp.newt.javafx.JFXEDTUtil;

public class NewtCanvasJFX extends Canvas implements NativeWindowHolder, WindowClosingProtocol {
    private static final boolean DEBUG = Debug.debug("Window");
    private static final boolean USE_JFX_EDT = PropertyAccess.getBooleanProperty("jogamp.newt.javafx.UseJFXEDT", true, true);
    private volatile Window parentWindow = null;
    private volatile AbstractGraphicsScreen screen = null;
    private WindowClosingMode newtChildClosingMode;
    private WindowClosingMode closingMode;
    private final Rectangle clientArea;
    private volatile NewtCanvasJFX.JFXNativeWindow nativeWindow;
    private volatile com.jogamp.newt.Window newtChild;
    private volatile boolean newtChildReady;
    private volatile boolean postSetSize;
    private volatile boolean postSetPos;
    private final EventHandler<WindowEvent> windowClosingListener;
    private final EventHandler<WindowEvent> windowShownListener;
    private final ChangeListener<Window> sceneWindowChangeListener;

    public NewtCanvasJFX(com.jogamp.newt.Window var1) {
        this.newtChildClosingMode = WindowClosingMode.DISPOSE_ON_CLOSE;
        this.closingMode = WindowClosingMode.DISPOSE_ON_CLOSE;
        this.clientArea = new Rectangle();
        this.nativeWindow = null;
        this.newtChild = null;
        this.newtChildReady = false;
        this.postSetSize = false;
        this.postSetPos = false;
        this.windowClosingListener = new EventHandler<WindowEvent>() {
            public final void handle(WindowEvent var1) {
                if (NewtCanvasJFX.DEBUG) {
                    System.err.println("NewtCanvasJFX.Event.DISPOSE, " + var1 + ", closeOp " + NewtCanvasJFX.this.closingMode);
                }

                if (WindowClosingMode.DISPOSE_ON_CLOSE == NewtCanvasJFX.this.closingMode) {
                    NewtCanvasJFX.this.destroy();
                } else {
                    var1.consume();
                }

            }
        };
        this.windowShownListener = new EventHandler<WindowEvent>() {
            public final void handle(WindowEvent var1) {
                if (NewtCanvasJFX.DEBUG) {
                    System.err.println("NewtCanvasJFX.Event.SHOWN, " + var1);
                }

                NewtCanvasJFX.this.repaintAction(true);
            }
        };
        this.sceneWindowChangeListener = new ChangeListener<Window>() {
            public void changed(ObservableValue<? extends Window> var1, Window var2, Window var3) {
                if (NewtCanvasJFX.DEBUG) {
                    System.err.println("NewtCanvasJFX.Event.Window, " + var2 + " -> " + var3);
                }

                if (NewtCanvasJFX.this.updateParentWindowAndScreen()) {
                    NewtCanvasJFX.this.repaintAction(NewtCanvasJFX.this.isVisible());
                }

            }
        };
        this.updateParentWindowAndScreen();
        ChangeListener var2 = new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> var1, Number var2, Number var3) {
                if (NewtCanvasJFX.DEBUG) {
                    System.err.println("NewtCanvasJFX.Event.Size, " + var2.doubleValue() + " -> " + var3.doubleValue() + ", has " + NewtCanvasJFX.this.getWidth() + "x" + NewtCanvasJFX.this.getHeight());
                }

                NewtCanvasJFX.this.updateSizeCheck((int)NewtCanvasJFX.this.getWidth(), (int)NewtCanvasJFX.this.getHeight());
                NewtCanvasJFX.this.repaintAction(NewtCanvasJFX.this.isVisible());
            }
        };
        this.widthProperty().addListener(var2);
        this.heightProperty().addListener(var2);
        this.visibleProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> var1, Boolean var2, Boolean var3) {
                if (NewtCanvasJFX.DEBUG) {
                    System.err.println("NewtCanvasJFX.Event.Visible, " + var2 + " -> " + var3 + ", has " + NewtCanvasJFX.this.isVisible());
                }

                NewtCanvasJFX.this.repaintAction(var3);
            }
        });
        this.sceneProperty().addListener(new ChangeListener<Scene>() {
            public void changed(ObservableValue<? extends Scene> var1, Scene var2, Scene var3) {
                if (NewtCanvasJFX.DEBUG) {
                    System.err.println("NewtCanvasJFX.Event.Scene, " + var2 + " -> " + var3 + ", has " + NewtCanvasJFX.this.getScene());
                    if (null != var3) {
                        Window var4 = var3.getWindow();
                        System.err.println("NewtCanvasJFX.Event.Scene window " + var4 + " (showing " + (null != var4 ? var4.isShowing() : 0) + ")");
                    }
                }

                if (NewtCanvasJFX.this.updateParentWindowAndScreen()) {
                    NewtCanvasJFX.this.repaintAction(NewtCanvasJFX.this.isVisible());
                }

            }
        });
        if (null != var1) {
            this.setNEWTChild(var1);
        }

    }

    private final void repaintAction(boolean var1) {
        if (var1 && this.validateNative(true) && this.newtChildReady) {
            if (this.postSetSize) {
                this.newtChild.setSize(this.clientArea.getWidth(), this.clientArea.getHeight());
                this.postSetSize = false;
            }

            if (this.postSetPos) {
                this.newtChild.setPosition(this.clientArea.getX(), this.clientArea.getY());
                this.postSetPos = false;
            }

            this.newtChild.windowRepaint(0, 0, this.clientArea.getWidth(), this.clientArea.getHeight());
        }

    }

    private final void updatePosSizeCheck() {
        Bounds var1 = this.localToScene(this.getBoundsInLocal());
        this.updatePosCheck((int)var1.getMinX(), (int)var1.getMinY());
        this.updateSizeCheck((int)this.getWidth(), (int)this.getHeight());
    }

    private final void updatePosCheck(int var1, int var2) {
        Rectangle var4 = this.clientArea;
        boolean var3 = var1 != var4.getX() || var2 != var4.getY();
        if (var3) {
            this.clientArea.setX(var1);
            this.clientArea.setY(var2);
        }

        if (DEBUG) {
            long var6 = this.newtChildReady ? this.newtChild.getSurfaceHandle() : 0L;
            System.err.println("NewtCanvasJFX.updatePosCheck: posChanged " + var3 + ", (" + Thread.currentThread().getName() + "): newtChildReady " + this.newtChildReady + ", " + this.clientArea.getX() + "/" + this.clientArea.getY() + " " + this.clientArea.getWidth() + "x" + this.clientArea.getHeight() + " - surfaceHandle 0x" + Long.toHexString(var6));
        }

        if (var3) {
            if (this.newtChildReady) {
                this.newtChild.setPosition(this.clientArea.getX(), this.clientArea.getY());
            } else {
                this.postSetPos = true;
            }
        }

    }

    private final void updateSizeCheck(int var1, int var2) {
        Rectangle var4 = this.clientArea;
        boolean var3 = var1 != var4.getWidth() || var2 != var4.getHeight();
        if (var3) {
            this.clientArea.setWidth((int) ((var1 + 1.0) * getScene().getWindow().getOutputScaleX()));
            this.clientArea.setHeight((int) ((var2 + 1.0) * getScene().getWindow().getOutputScaleY()));
        }

        if (DEBUG) {
            long var6 = this.newtChildReady ? this.newtChild.getSurfaceHandle() : 0L;
            System.err.println("NewtCanvasJFX.updateSizeCheck: sizeChanged " + var3 + ", (" + Thread.currentThread().getName() + "): newtChildReady " + this.newtChildReady + ", " + this.clientArea.getX() + "/" + this.clientArea.getY() + " " + this.clientArea.getWidth() + "x" + this.clientArea.getHeight() + " - surfaceHandle 0x" + Long.toHexString(var6));
        }

        if (var3) {
            if (this.newtChildReady) {
                this.newtChild.setSize(this.clientArea.getWidth(), this.clientArea.getHeight());
            } else {
                this.postSetSize = true;
            }
        }

    }

    private boolean updateParentWindowAndScreen() {
        Scene var1 = this.getScene();
        if (null != var1) {
            Window var2 = var1.getWindow();
            if (DEBUG) {
                System.err.println("NewtCanvasJFX.updateParentWindowAndScreen: Scene " + var1 + ", Window " + var2 + " (showing " + (null != var2 ? var2.isShowing() : 0) + ")");
            }

            if (var2 != this.parentWindow) {
                this.destroyImpl(false);
            }

            this.parentWindow = var2;
            if (null != var2) {
                this.screen = JFXAccessor.getScreen(JFXAccessor.getDevice(this.parentWindow), -1);
                this.parentWindow.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, this.windowClosingListener);
                this.parentWindow.addEventHandler(WindowEvent.WINDOW_SHOWN, this.windowShownListener);
                return true;
            }

            var1.windowProperty().addListener(this.sceneWindowChangeListener);
        } else {
            if (DEBUG) {
                System.err.println("NewtCanvasJFX.updateParentWindowAndScreen: Null Scene");
            }

            if (null != this.parentWindow) {
                this.destroyImpl(false);
            }
        }

        return false;
    }

    public void destroy() {
        this.destroyImpl(true);
    }

    private void destroyImpl(boolean var1) {
        if (DEBUG) {
            System.err.println("NewtCanvasJFX.dispose: (has parent " + (null != this.parentWindow) + ", hasNative " + (null != this.nativeWindow) + ",\n\t" + this.newtChild);
        }

        if (null != this.newtChild) {
            if (DEBUG) {
                System.err.println("NewtCanvasJFX.dispose.1: EDTUtil cur " + this.newtChild.getScreen().getDisplay().getEDTUtil());
            }

            if (null != this.nativeWindow) {
                this.configureNewtChild(false);
                this.newtChild.setVisible(false);
                this.newtChild.reparentWindow((NativeWindow)null, -1, -1, 0);
            }

            if (var1) {
                this.newtChild.destroy();
                this.newtChild = null;
            }
        }

        if (null != this.parentWindow) {
            this.parentWindow.getScene().windowProperty().removeListener(this.sceneWindowChangeListener);
            this.parentWindow.removeEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, this.windowClosingListener);
            this.parentWindow.removeEventHandler(WindowEvent.WINDOW_SHOWN, this.windowShownListener);
            this.parentWindow = null;
        }

        if (null != this.screen) {
            this.screen.getDevice().close();
            this.screen = null;
        }

        this.nativeWindow = null;
    }

    private final boolean validateNative(boolean var1) {
        if (null != this.nativeWindow) {
            return true;
        } else if (null == this.parentWindow) {
            return false;
        } else {
            this.updatePosSizeCheck();
            if (0 < this.clientArea.getWidth() && 0 < this.clientArea.getHeight()) {
                long var2 = JFXAccessor.getWindowHandle(this.parentWindow);
                if (0L == var2) {
                    return false;
                } else {
                    this.screen.getDevice().open();
                    int var4 = JFXAccessor.getNativeVisualID(this.screen.getDevice(), var2);
                    boolean var5 = NativeWindowFactory.isNativeVisualIDValidForProcessing(var4);
                    if (DEBUG) {
                        System.err.println("NewtCanvasJFX.validateNative() windowHandle 0x" + Long.toHexString(var2) + ", visualID 0x" + Integer.toHexString(var4) + ", valid " + var5);
                    }

                    if (var5) {
                        Capabilities var6 = new Capabilities();
                        GraphicsConfigurationFactory var7 = GraphicsConfigurationFactory.getFactory(this.screen.getDevice(), var6);
                        AbstractGraphicsConfiguration var8 = var7.chooseGraphicsConfiguration(var6, var6, (CapabilitiesChooser)null, this.screen, var4);
                        if (DEBUG) {
                            System.err.println("NewtCanvasJFX.validateNative() factory: " + var7 + ", windowHandle 0x" + Long.toHexString(var2) + ", visualID 0x" + Integer.toHexString(var4) + ", chosen config: " + var8);
                        }

                        if (null == var8) {
                            throw new NativeWindowException("Error choosing GraphicsConfiguration creating window: " + this);
                        }

                        this.nativeWindow = new NewtCanvasJFX.JFXNativeWindow(var8, var2);
                        if (var1) {
                            this.reparentWindow(true);
                        }
                    }

                    return null != this.nativeWindow;
                }
            } else {
                return false;
            }
        }
    }

    public com.jogamp.newt.Window setNEWTChild(com.jogamp.newt.Window var1) {
        com.jogamp.newt.Window var2 = this.newtChild;
        if (DEBUG) {
            System.err.println("NewtCanvasJFX.setNEWTChild.0: win " + newtWinHandleToHexString(var2) + " -> " + newtWinHandleToHexString(var1));
        }

        if (null != this.newtChild) {
            this.reparentWindow(false);
            this.newtChild = null;
        }

        this.newtChild = var1;
        if (null != this.newtChild && this.validateNative(false)) {
            this.reparentWindow(true);
        }

        return var2;
    }

    private void reparentWindow(boolean var1) {
        if (null != this.newtChild) {
            if (DEBUG) {
                System.err.println("NewtCanvasJFX.reparentWindow.0: add=" + var1 + ", win " + newtWinHandleToHexString(this.newtChild) + ", EDTUtil: cur " + this.newtChild.getScreen().getDisplay().getEDTUtil());
            }

            this.newtChild.setFocusAction((FocusRunnable)null);
            if (!var1) {
                this.configureNewtChild(false);
                this.newtChild.setVisible(false);
                this.newtChild.reparentWindow((NativeWindow)null, -1, -1, 0);
            } else {
                assert null != this.nativeWindow && null != this.parentWindow;

                this.updatePosSizeCheck();
                int var2 = this.clientArea.getX();
                int var3 = this.clientArea.getY();
                int var4 = this.clientArea.getWidth();
                int var5 = this.clientArea.getHeight();
                if (USE_JFX_EDT) {
                    Display var6 = this.newtChild.getScreen().getDisplay();
                    EDTUtil var7 = var6.getEDTUtil();
                    if (!(var7 instanceof JFXEDTUtil)) {
                        JFXEDTUtil var8 = new JFXEDTUtil(var6);
                        if (DEBUG) {
                            System.err.println("NewtCanvasJFX.reparentWindow.1: replacing EDTUtil " + var7 + " -> " + var8);
                        }

                        var8.start();
                        var6.setEDTUtil(var8);
                    }
                }

                this.newtChild.setSize(var4, var5);
                this.newtChild.reparentWindow(this.nativeWindow, var2, var3, 2);
                this.newtChild.setPosition(var2, var3);
                this.newtChild.setVisible(true);
                this.configureNewtChild(true);
                this.newtChild.sendWindowEvent(100);
            }

            if (DEBUG) {
                System.err.println("NewtCanvasJFX.reparentWindow.X: add=" + var1 + ", win " + newtWinHandleToHexString(this.newtChild) + ", EDTUtil: cur " + this.newtChild.getScreen().getDisplay().getEDTUtil());
            }

        }
    }

    private void configureNewtChild(boolean var1) {
        this.newtChildReady = var1;
        if (null != this.newtChild) {
            this.newtChild.setKeyboardFocusHandler((KeyListener)null);
            if (var1) {
                this.newtChildClosingMode = this.newtChild.setDefaultCloseOperation(WindowClosingMode.DO_NOTHING_ON_CLOSE);
            } else {
                this.newtChild.setFocusAction((FocusRunnable)null);
                this.newtChild.setDefaultCloseOperation(this.newtChildClosingMode);
            }
        }

    }

    public com.jogamp.newt.Window getNEWTChild() {
        return this.newtChild;
    }

    public NativeWindow getNativeWindow() {
        return this.nativeWindow;
    }

    public NativeSurface getNativeSurface() {
        return this.nativeWindow;
    }

    public WindowClosingMode getDefaultCloseOperation() {
        return this.closingMode;
    }

    public WindowClosingMode setDefaultCloseOperation(WindowClosingMode var1) {
        WindowClosingMode var2 = this.closingMode;
        this.closingMode = var1;
        return var2;
    }

    boolean isParent() {
        return null != this.newtChild;
    }

    boolean isFullscreen() {
        return null != this.newtChild && this.newtChild.isFullscreen();
    }

    private final void requestFocusNEWTChild() {
        if (this.newtChildReady) {
            this.newtChild.setFocusAction((FocusRunnable)null);
            this.newtChild.requestFocus();
        }

    }

    public void requestFocus() {
        //access$501(this);
        this.requestFocusNEWTChild();
    }

    static String newtWinHandleToHexString(com.jogamp.newt.Window var0) {
        return null != var0 ? toHexString(var0.getWindowHandle()) : "nil";
    }

    static String toHexString(long var0) {
        return "0x" + Long.toHexString(var0);
    }

    private class JFXNativeWindow implements NativeWindow {
        private final AbstractGraphicsConfiguration config;
        private final long nativeWindowHandle;
        private final InsetsImmutable insets;

        public JFXNativeWindow(AbstractGraphicsConfiguration var2, long var3) {
            this.config = var2;
            this.nativeWindowHandle = var3;
            this.insets = new Insets(0, 0, 0, 0);
        }

        public int lockSurface() throws NativeWindowException, RuntimeException {
            return 3;
        }

        public void unlockSurface() {
        }

        public boolean isSurfaceLockedByOtherThread() {
            return false;
        }

        public Thread getSurfaceLockOwner() {
            return null;
        }

        public boolean surfaceSwap() {
            return false;
        }

        public void addSurfaceUpdatedListener(SurfaceUpdatedListener var1) {
        }

        public void addSurfaceUpdatedListener(int var1, SurfaceUpdatedListener var2) throws IndexOutOfBoundsException {
        }

        public void removeSurfaceUpdatedListener(SurfaceUpdatedListener var1) {
        }

        public long getSurfaceHandle() {
            return 0L;
        }

        public int getWidth() {
            return this.getSurfaceWidth();
        }

        public int getHeight() {
            return this.getSurfaceHeight();
        }

        public final int[] convertToWindowUnits(int[] var1) {
            return var1;
        }

        public final int[] convertToPixelUnits(int[] var1) {
            return var1;
        }

        public int getSurfaceWidth() {
            return NewtCanvasJFX.this.clientArea.getWidth();
        }

        public int getSurfaceHeight() {
            return NewtCanvasJFX.this.clientArea.getHeight();
        }

        public final NativeSurface getNativeSurface() {
            return this;
        }

        public AbstractGraphicsConfiguration getGraphicsConfiguration() {
            return this.config;
        }

        public long getDisplayHandle() {
            return this.config.getScreen().getDevice().getHandle();
        }

        public int getScreenIndex() {
            return this.config.getScreen().getIndex();
        }

        public void surfaceUpdated(Object var1, NativeSurface var2, long var3) {
        }

        public void destroy() {
        }

        public NativeWindow getParent() {
            return null;
        }

        public long getWindowHandle() {
            return this.nativeWindowHandle;
        }

        public InsetsImmutable getInsets() {
            return this.insets;
        }

        public int getX() {
            return NewtCanvasJFX.this.clientArea.getX();
        }

        public int getY() {
            return NewtCanvasJFX.this.clientArea.getY();
        }

        public Point getLocationOnScreen(Point var1) {
            Point var2 = NativeWindowFactory.getLocationOnScreen(this);
            return null != var1 ? var1.translate(var2) : var2;
        }

        public boolean hasFocus() {
            return NewtCanvasJFX.this.isFocused();
        }
    }
}
