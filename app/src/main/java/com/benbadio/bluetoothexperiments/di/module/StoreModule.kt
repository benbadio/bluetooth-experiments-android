package com.benbadio.bluetoothexperiments.di.module

import android.content.Context
import com.benbadio.bluetoothexperiments.store.ClientStore
import com.benbadio.bluetoothexperiments.store.ServerStore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Ben Badio on 7/17/2018.
 */
@Module
class StoreModule {
    @Provides
    @Singleton
    fun provideClientStore(context: Context): ClientStore = ClientStore(context)

    @Provides
    @Singleton
    fun provideServerStore(context: Context): ServerStore = ServerStore(context)
}
