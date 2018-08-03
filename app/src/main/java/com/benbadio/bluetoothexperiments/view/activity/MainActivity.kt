package com.benbadio.bluetoothexperiments.view.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.benbadio.bluetoothexperiments.R
import com.benbadio.bluetoothexperiments.action.ClientActionCreator
import com.benbadio.bluetoothexperiments.action.ClientActions
import com.benbadio.bluetoothexperiments.store.ClientStore
import com.ptmr3.fluxx.FluxxReaction
import com.ptmr3.fluxx.FluxxReactionSubscriber
import com.ptmr3.fluxx.annotation.Reaction
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity(), FluxxReactionSubscriber {
    @Inject lateinit var mClientStore: ClientStore
    @Inject lateinit var appActionCreator: ClientActionCreator
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReactionSubscriber(this)
        setContentView(R.layout.activity_main)
        val host: NavHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment? ?: return
        setupBottomNavMenu(host.navController)
        navController = Navigation.findNavController(this, R.id.nav_host)
        bottom_nav_view?.let { NavigationUI.setupWithNavController(it, navController) }
    }

    private fun setupBottomNavMenu(navController: NavController) {
        findViewById<BottomNavigationView>(R.id.bottom_nav_view)?.let { bottomNavView ->
            NavigationUI.setupWithNavController(bottomNavView, navController)
        }
    }

    override fun onResume() {
        super.onResume()
        when {
            !mClientStore.hasBluetoothLE -> finish()
            !mClientStore.isBluetoothAvailable -> appActionCreator.requestBluetoothEnable()
        }
    }

    @Reaction(ClientActions.REQUEST_LOCATION_PERMISSION)
    fun onLocationPermissionRequested(reaction: FluxxReaction) {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FINE_LOCATION)
    }

    @Reaction(ClientActions.REQUEST_BLUETOOTH_ENABLE)
    fun onBluetoothEnableRequested(reaction: FluxxReaction) {
        startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)
        Timber.d("Requested user enables Bluetooth. Try starting the scan again.")
    }

    companion object {
        const val REQUEST_ENABLE_BT: Int = 7070
        const val REQUEST_FINE_LOCATION: Int = 7071
    }
}
