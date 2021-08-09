package com.husker.joglfx

import javafx.geometry.Point2D
import javafx.stage.Screen
import kotlin.math.max

class ScreenUtils {

    companion object{

        @JvmStatic val maxScreenPoint = lazy {
            var x = 0.0
            var y = 0.0
            Screen.getScreens().forEach {
                x = max(it.bounds.width * it.dpi, x)
                y = max(it.bounds.height * it.dpi, y)
            }
            return@lazy Point2D(x, y)
        }
    }


}