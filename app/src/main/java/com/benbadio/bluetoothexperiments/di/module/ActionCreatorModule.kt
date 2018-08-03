package com.benbadio.bluetoothexperiments.di.module

import com.benbadio.bluetoothexperiments.action.ClientActionCreator
import com.benbadio.bluetoothexperiments.action.ServerActionCreator
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Ben Badio on 7/17/2018.
 */
@Module
class ActionCreatorModule {
    @Singleton
    @Provides
    fun provideClientActionCreator(): ClientActionCreator = ClientActionCreator()

    @Singleton
    @Provides
    fun provideServerActionCreator(): ServerActionCreator = ServerActionCreator()
}
