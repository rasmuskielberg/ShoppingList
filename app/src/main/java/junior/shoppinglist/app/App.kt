package junior.shoppinglist.app

import android.app.Application


/* Singleton to hold the application instance */
class App : Application() {

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

}