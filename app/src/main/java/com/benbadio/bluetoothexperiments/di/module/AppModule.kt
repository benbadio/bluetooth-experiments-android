package com.benbadio.bluetoothexperiments.di.module

import android.content.Context
import com.benbadio.bluetoothexperiments.App
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Ben Badio on 7/31/2018.
 */
@Module
class AppModule {
    @Provides
    @Singleton
    fun provideApplicationContext(app: App): Context = app.applicationContext
}