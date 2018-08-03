package com.benbadio.bluetoothexperiments.action

import com.ptmr3.fluxx.FluxxActionCreator
import javax.inject.Inject

/**
 * Created by Ben Badio on 8/2/2018.
 */
class ServerActionCreator
@Inject
constructor() : FluxxActionCreator(), ServerActions {

    override fun startAdvertising() {
        publishAction(ServerActions.START_ADVERTISING)
    }

    override fun stopAdvertising() {
        publishAction(ServerActions.STOP_ADVERTISING)
    }
}