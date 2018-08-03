package com.benbadio.bluetoothexperiments.store

import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import com.benbadio.bluetoothexperiments.Constants
import com.benbadio.bluetoothexperiments.Constants.CHARACTERISTIC_UUID
import com.benbadio.bluetoothexperiments.action.Keys
import com.benbadio.bluetoothexperiments.action.ServerActions
import com.ptmr3.fluxx.FluxxAction
import com.ptmr3.fluxx.FluxxStore
import com.ptmr3.fluxx.annotation.Action
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton





/**
 * Created by Ben Badio on 8/2/2018.
 */
@Singleton
class ServerStore
@Inject constructor(private val appContext: Context) : FluxxStore() {
    private val mBluetoothManager: BluetoothManager = appContext.getSystemService(Service.BLUETOOTH_SERVICE) as BluetoothManager
    private var mBluetoothAdapter: BluetoothAdapter? = mBluetoothManager.adapter
    private var mAdvertisingDisposable: Disposable? = null
    private var mBluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private var mGattServer: BluetoothGattServer? = null
    private val mGattServerCallback: GattServerCallback = GattServerCallback()
    private val mService = BluetoothGattService(Constants.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
    private val mWriteCharacteristic = BluetoothGattCharacteristic(
            Constants.CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE)
    private val mConnectedDevices = mutableListOf<BluetoothDevice>()

    private val mAdvertiseCallback: AdvertiseCallback? = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Timber.d("Peripheral advertising started.")
        }

        override fun onStartFailure(errorCode: Int) {
            Timber.d("Peripheral advertising failed: $errorCode")
        }
    }

    @Action(actionType = ServerActions.START_ADVERTISING)
    fun startAdvertising(action: FluxxAction) {
        publishReaction(ServerActions.START_ADVERTISING)
        mAdvertisingDisposable = Completable.fromAction {
            mBluetoothLeAdvertiser = mBluetoothAdapter!!.bluetoothLeAdvertiser
            mGattServer = mBluetoothManager.openGattServer(appContext, mGattServerCallback)
            mService.addCharacteristic(mWriteCharacteristic)
            mGattServer?.addService(mService)
            mBluetoothLeAdvertiser?.let {
                val settings = AdvertiseSettings.Builder()
                        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                        .setConnectable(true)
                        .setTimeout(0)
                        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                        .build()
                val parcelUuid = ParcelUuid(Constants.SERVICE_UUID)
                val data = AdvertiseData.Builder()
                        .setIncludeDeviceName(true)
                        .addServiceUuid(parcelUuid)
                        .build()
                it.startAdvertising(settings, data, mAdvertiseCallback)
            }
        }.subscribeOn(Schedulers.computation())
                .subscribe()

    }

    @Action(actionType = ServerActions.STOP_ADVERTISING)
    fun stopAdvertising(action: FluxxAction) {
        Completable.fromAction {
            mGattServer?.close()
            mBluetoothLeAdvertiser?.stopAdvertising(mAdvertiseCallback)
            mAdvertisingDisposable?.dispose()
        }.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onComplete = { publishReaction(ServerActions.STOP_ADVERTISING) })
    }

    private inner class GattServerCallback : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> mConnectedDevices.add(device!!)
                BluetoothProfile.STATE_DISCONNECTED -> mConnectedDevices.remove(device)
            }
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            if (characteristic!!.uuid == CHARACTERISTIC_UUID) {
                mGattServer!!.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                val newString = value!!.toString(Charsets.UTF_8).toUpperCase()
                characteristic.value= newString.toByteArray()
                for (connectedDevice in mConnectedDevices) {
                    mGattServer!!.notifyCharacteristicChanged(connectedDevice, characteristic, false)
                }
                Completable.fromAction { publishReaction(ServerReactions.RESPONSE_MESSAGE_SENT, Keys.MESSAGE_SENT, "Message Sent: $newString") }
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe()
            }
        }
    }

    object ServerReactions {
        const val RESPONSE_MESSAGE_SENT = "responesMessageSent"
    }
}