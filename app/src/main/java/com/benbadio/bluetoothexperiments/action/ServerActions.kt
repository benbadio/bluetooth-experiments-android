package com.benbadio.bluetoothexperiments.action

/**
 * Created by Ben Badio on 8/2/2018.
 */
interface ServerActions {
    fun startAdvertising()
    fun stopAdvertising()

    companion object {
        const val START_ADVERTISING = "startAdvertising"
        const val STOP_ADVERTISING = "stopAdvertising"
    }
}