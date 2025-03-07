package com.nikame.glengine

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nikame.glengine.gObjects.AbstractGObject
import androidx.annotation.CallSuper
import androidx.lifecycle.lifecycleScope
import com.nikame.glengine.graphics.MainRender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

//todo для реализации движка нужно дополнительно создать:
// - класс с набором графических объектов, набор внутри класса должен отрисовываться как единое целое
// и использовать заданные анимации, а также реализовывать управление объектом извне
// - класс, реализующий стандартные анимации - повороты, смещения итд
// - класс, отвечающий за камеру и её поведение - получение управления извне или следование за указанным объектом,
// логику изменения параметров - с плавной задержкой или чёткое следование
// - логику контроля фреймов (имеются в виду не графические фреймы а фреймы обновления данных, т.к. это разные вещи)
// определить метод с задержками для поддержания заданного уровня и балансировщик для компенсации времени между кадрами,
// например. если кард раз в 1 тик, а произошёл через 1.3 тика, то координаты нужно считать как старые+скорость*1,3 или что-то типа того

/***
 * This is a blank activity designed to start the resource initialization process.
 *
 * Inherit from this class in your launcher activity.
 * Prepare a set of drawables by overwriting buildGObjects. Call super in onCreate after your code.
 * If you need to know the progress of the initialization, call setInitProgressListener in onCreate before call super
 */
abstract class GraphicActivity : AppCompatActivity() {
    var onProgressChanged: ((progress: Int, status: String) -> Unit)?=null

    private lateinit var glSurfaceView: GLSurfaceView
    var isRenderAdded = false

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (notSupportES2()) {
            finish()
            return
        }

        MainRender.defRender = lifecycleScope.async(Dispatchers.Default) {
            return@async MainRender(buildParams(), buildGObjects())
        }
        onProgressChanged?.let { it(0, "start") }


        glSurfaceView = GLSurfaceView(this)
        glSurfaceView.setEGLContextClientVersion(2)

        lifecycleScope.launch {
            glSurfaceView.setRenderer(
                MainRender.getRender()
            )
            setContentView(glSurfaceView)
            isRenderAdded = true
            onProgressChanged?.let { it(100, "finish") }
        }
    }

    override fun onPause() {
        super.onPause()

        if (isRenderAdded)
            glSurfaceView.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (isRenderAdded)
            glSurfaceView.onResume()
    }

    abstract fun buildParams(): MainRender.Companion.Params

    abstract fun buildGObjects(): ArrayList<AbstractGObject>

    private fun notSupportES2(): Boolean {
        return (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).deviceConfigurationInfo.reqGlEsVersion < 0x20000
    }
}

