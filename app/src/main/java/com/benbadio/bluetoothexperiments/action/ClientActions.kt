package com.benbadio.bluetoothexperiments.action

/**
 * Created by Ben Badio on 7/17/2018.
 */
interface ClientActions {
    fun requestLocationPermission()
    fun requestBluetoothEnable()
    fun startScan()
    fun stopScan()
    fun sendMessage(message: String)

    companion object {
        const val REQUEST_LOCATION_PERMISSION = "requestLocationPermission"
        const val REQUEST_BLUETOOTH_ENABLE = "requestBluetoothEnable"
        const val START_SCAN = "startScan"
        const val STOP_SCAN = "stopScan"
        const val SEND_MESSAGE = "sendMessage"
    }
}
