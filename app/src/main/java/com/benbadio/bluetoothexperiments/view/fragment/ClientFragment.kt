package com.benbadio.bluetoothexperiments.view.fragment


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.benbadio.bluetoothexperiments.R
import com.benbadio.bluetoothexperiments.action.ClientActionCreator
import com.benbadio.bluetoothexperiments.action.ClientActions
import com.benbadio.bluetoothexperiments.action.Keys
import com.benbadio.bluetoothexperiments.store.ClientStore
import com.ptmr3.fluxx.FluxxReaction
import com.ptmr3.fluxx.FluxxReactionSubscriber
import com.ptmr3.fluxx.annotation.Reaction
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_client.*
import kotlinx.android.synthetic.main.fragment_client.view.*
import javax.inject.Inject

class ClientFragment : DaggerFragment(), FluxxReactionSubscriber {
    @Inject
    lateinit var appActionCreator: ClientActionCreator
    @Inject
    lateinit var mClientStore: ClientStore

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        registerReactionSubscriber(this)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_client, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.startScanButton.setOnClickListener { appActionCreator.startScan() }
        view.stopScanButton.setOnClickListener { appActionCreator.stopScan() }
        view.sendButton.setOnClickListener { appActionCreator.sendMessage(messageField.text.toString()) }
    }

    @Reaction(ClientActions.START_SCAN)
    fun onScanStarted() {
        view?.statusText?.text = getString(R.string.scanning)
    }

    @Reaction(ClientActions.STOP_SCAN)
    fun onScanStopped() {
        view?.statusText?.text = getString(R.string.scan_completed)
    }

    @Reaction(ClientActions.SEND_MESSAGE)
    fun onMessageSent(reaction: FluxxReaction) {
        view?.statusText?.text = reaction[Keys.MESSAGE_RECEIVED]
    }
}
