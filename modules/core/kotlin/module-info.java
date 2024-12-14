module openglfx {

    requires transitive kotlin.stdlib;
    requires transitive grapl.gl;

    requires javafx.base;
    requires javafx.graphics;

    exports com.huskerdev.openglfx;
    exports com.huskerdev.openglfx.canvas;
    exports com.huskerdev.openglfx.canvas.events;
    exports com.huskerdev.openglfx.effects;
    exports com.huskerdev.openglfx.image;
    exports com.huskerdev.openglfx.internal;
    exports com.huskerdev.openglfx.internal.canvas;
    exports com.huskerdev.openglfx.internal.platforms;
    exports com.huskerdev.openglfx.internal.platforms.win;
    exports com.huskerdev.openglfx.internal.platforms.macos;
    exports com.huskerdev.openglfx.internal.shaders;
}