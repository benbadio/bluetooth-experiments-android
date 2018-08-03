package com.benbadio.bluetoothexperiments.di.component

import com.benbadio.bluetoothexperiments.App
import com.benbadio.bluetoothexperiments.di.module.ActionCreatorModule
import com.benbadio.bluetoothexperiments.di.module.AppModule
import com.benbadio.bluetoothexperiments.di.module.StoreModule
import com.benbadio.bluetoothexperiments.di.module.ViewModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 * Created by Ben Badio on 7/17/2018.
 */
@Singleton
@Component(modules = [(AndroidSupportInjectionModule::class), (AppModule::class), (ActionCreatorModule::class), (StoreModule::class), (ViewModule::class)])
interface AppComponent : AndroidInjector<App> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>()
}
