package com.benbadio.bluetoothexperiments.store

import android.Manifest
import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import android.support.v4.content.ContextCompat
import com.benbadio.bluetoothexperiments.Constants
import com.benbadio.bluetoothexperiments.Constants.CHARACTERISTIC_UUID
import com.benbadio.bluetoothexperiments.Constants.SERVICE_UUID
import com.benbadio.bluetoothexperiments.action.ClientActions
import com.benbadio.bluetoothexperiments.action.Keys
import com.ptmr3.fluxx.FluxxAction
import com.ptmr3.fluxx.FluxxStore
import com.ptmr3.fluxx.annotation.Action
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Created by Ben Badio on 7/17/2018.
 */
@Singleton
class ClientStore
@Inject constructor(private val appContext: Context) : FluxxStore() {
    private val mBluetoothManager: BluetoothManager = appContext.getSystemService(Service.BLUETOOTH_SERVICE) as BluetoothManager
    private var mBluetoothAdapter: BluetoothAdapter? = mBluetoothManager.adapter
    private var mScanCallback: BtleScanCallback? = null
    private var mBluetoothLeScanner: BluetoothLeScanner? = mBluetoothAdapter!!.bluetoothLeScanner
    private var mIsScanning: Boolean = false
    private val mScanResults = HashMap<String, BluetoothDevice>()
    private var mScanDisposable: Disposable? = null
    private var mGatt: BluetoothGatt? = null
    private var mInitialized: Boolean = false

    private var mGatServerConnected = false
    val isBluetoothAvailable: Boolean get() = mBluetoothAdapter?.isEnabled ?: false

    val isLocationPermitted: Boolean get() = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    val hasBluetoothLE: Boolean get() = appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

    @Action(actionType = ClientActions.REQUEST_LOCATION_PERMISSION)
    fun requestLocationPermission(action: FluxxAction) {
        publishReaction(ClientActions.REQUEST_LOCATION_PERMISSION)
    }

    @Action(actionType = ClientActions.REQUEST_BLUETOOTH_ENABLE)
    fun requestBluetoothEnable(action: FluxxAction) {
        publishReaction(ClientActions.REQUEST_BLUETOOTH_ENABLE)
    }

    @Action(actionType = ClientActions.START_SCAN)
    fun startScan(action: FluxxAction) {
        when {
            !isBluetoothAvailable -> publishReaction(ClientActions.REQUEST_BLUETOOTH_ENABLE)
            !isLocationPermitted -> publishReaction(ClientActions.REQUEST_LOCATION_PERMISSION)
            !mIsScanning -> {
                publishReaction(ClientActions.START_SCAN)
                mScanDisposable = Completable.fromAction {
                    val filters = arrayListOf<ScanFilter>(
                            ScanFilter.Builder()
                                    .setServiceUuid(ParcelUuid(SERVICE_UUID))
                                    .build())
                    val settings = ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                            .build()
                    mScanCallback = BtleScanCallback(mScanResults)
                    mBluetoothLeScanner!!.startScan(filters, settings, mScanCallback)
                    mIsScanning = true
                }.timeout(30, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.computation())
                        .subscribeBy(onError = { stopScan(action) })
            }
        }
    }

    @Action(actionType = ClientActions.STOP_SCAN)
    fun stopScan(action: FluxxAction? = null) {
        if (mIsScanning && mBluetoothAdapter != null && mBluetoothAdapter!!.isEnabled && mBluetoothLeScanner != null) {
            mBluetoothLeScanner!!.stopScan(mScanCallback)
            scanComplete()
        }
        mScanDisposable?.dispose()
        mScanCallback = null
        mIsScanning = false
    }

    @Action(actionType = ClientActions.SEND_MESSAGE)
    fun sendMessage(action: FluxxAction) {
        if (mGatServerConnected && mInitialized) {
            val service = mGatt!!.getService(SERVICE_UUID)
            val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID)
            val message: String = action[Keys.MESSAGE_SENT]
            val messageBytes = message.toByteArray(Charsets.UTF_8)
            characteristic.value = messageBytes
            val success: Boolean = mGatt!!.writeCharacteristic(characteristic)

        }
    }

    private fun scanComplete() {
        if (!mScanResults.isEmpty()) {
            for (deviceAddress in mScanResults.keys) {
                Timber.d("Found device: $deviceAddress")
            }
        }
        publishReaction(ClientActions.STOP_SCAN)
    }

    inner class BtleScanCallback(val mScanResults: HashMap<String, BluetoothDevice>) : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            addScanResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                addScanResult(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Timber.e("BLE Scan Failed with code %s", errorCode)
        }

        private fun addScanResult(result: ScanResult) {
            stopScan()
            connectDevice(result.device)
        }

    }

    private fun connectDevice(device: BluetoothDevice) {
        val gattClientCallback = GattClientCallback()
        mGatt = device.connectGatt(appContext, false, gattClientCallback)
        Timber.d("Device Connected: ${device.address}")
    }

    private inner class GattClientCallback : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (status == BluetoothGatt.GATT_FAILURE) {
                disconnectGattServer()
                return
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                Timber.d("Client connected!")
                disconnectGattServer()
                return
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mGatServerConnected = true
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectGattServer()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt!!.getService(SERVICE_UUID)
                val characteristic = service.getCharacteristic(Constants.CHARACTERISTIC_UUID)
                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                mInitialized = gatt.setCharacteristicNotification(characteristic, true)
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Timber.d("Successful Write! Characteristic: ${characteristic!!.value.toString(Charsets.UTF_8)}")
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            Completable.fromAction { publishReaction(ClientActions.SEND_MESSAGE, Keys.MESSAGE_RECEIVED, characteristic!!.value.toString(Charsets.UTF_8)) }
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe()
        }
    }

    fun disconnectGattServer() {
        mGatt?.let {
            it.disconnect()
            it.close()
        }
        mGatServerConnected = false
        mInitialized = false
    }
}