package karcero.demo.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import karcero.demo.KarceroDemo

object DesktopLauncher {
    // Tap the 'play' button in IntelliJ to run the desktop application.
    @JvmStatic fun main(arg: Array<String>) {
        LwjglApplication(KarceroDemo(), LwjglApplicationConfiguration().apply {
            width = 600
            height = 600
        })
    }
}
