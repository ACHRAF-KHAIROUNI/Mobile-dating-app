package fr.unica.miage.komeet

import android.app.Application
import fr.unica.miage.komeet.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class KoMeetApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@KoMeetApplication)
            modules(appModule)
        }
    }
}
