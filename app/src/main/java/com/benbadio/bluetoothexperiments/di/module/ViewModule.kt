package com.benbadio.bluetoothexperiments.di.module

import com.benbadio.bluetoothexperiments.view.activity.MainActivity
import com.benbadio.bluetoothexperiments.view.fragment.ClientFragment
import com.benbadio.bluetoothexperiments.view.fragment.ServerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Ben Badio on 7/31/2018.
 */
@Module
abstract class ViewModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivityInjector(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeClientFragmentInjector(): ClientFragment

    @ContributesAndroidInjector
    abstract fun contributeServerFragment(): ServerFragment
}