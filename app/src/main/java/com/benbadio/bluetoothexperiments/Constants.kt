package com.benbadio.bluetoothexperiments

import java.util.*

/**
 * Created by Ben Badio on 8/2/2018.
 */
object Constants {
    const val UUID_STRING = "c1494f7a-3cda-4c46-8fb9-a5f3d7bcd3d7"
    const val CHARACTERISTIC_UUID_STRING = "3f853048-8e28-4eb2-9deb-aaa8109b9b0e"
    val SERVICE_UUID: UUID = UUID.fromString(UUID_STRING)
    val CHARACTERISTIC_UUID = UUID.fromString(CHARACTERISTIC_UUID_STRING)
}