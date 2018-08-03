package com.benbadio.bluetoothexperiments.action

import com.ptmr3.fluxx.FluxxActionCreator
import javax.inject.Inject

/**
 * Created by Ben Badio on 7/17/2018.
 */
class ClientActionCreator
@Inject
constructor() : FluxxActionCreator(), ClientActions {
    override fun sendMessage(message: String) {
        publishAction(ClientActions.SEND_MESSAGE, Keys.MESSAGE_SENT, message)
    }

    override fun startScan() {
        publishAction(ClientActions.START_SCAN)
    }

    override fun stopScan() {
        publishAction(ClientActions.STOP_SCAN)
    }

    override fun requestBluetoothEnable() {
        publishAction(ClientActions.REQUEST_BLUETOOTH_ENABLE)
    }

    override fun requestLocationPermission() {
        publishAction(ClientActions.REQUEST_LOCATION_PERMISSION)
    }
}
