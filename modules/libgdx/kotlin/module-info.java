module openglfx.libgdx {

    requires transitive openglfx;

    requires org.lwjgl;
    requires org.lwjgl.opengl;
    requires javafx.controls;

    exports com.huskerdev.openglfx.libgdx;
    exports com.huskerdev.openglfx.libgdx.events;
    exports com.huskerdev.openglfx.libgdx.internal;
}