package me.memoryandthought.weighty

import android.app.Application
import kotlinx.coroutines.runBlocking

class WeightyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        runBlocking {
            InjectorUtils.init(this@WeightyApplication)
        }
    }
}
