package com.benbadio.bluetoothexperiments.view.fragment


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.benbadio.bluetoothexperiments.R
import com.benbadio.bluetoothexperiments.action.Keys
import com.benbadio.bluetoothexperiments.action.ServerActionCreator
import com.benbadio.bluetoothexperiments.action.ServerActions
import com.benbadio.bluetoothexperiments.store.ServerStore
import com.benbadio.bluetoothexperiments.store.ServerStore.ServerReactions
import com.ptmr3.fluxx.FluxxReaction
import com.ptmr3.fluxx.FluxxReactionSubscriber
import com.ptmr3.fluxx.annotation.Reaction
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_server.*
import kotlinx.android.synthetic.main.fragment_server.view.*
import javax.inject.Inject

class ServerFragment : DaggerFragment(), FluxxReactionSubscriber {
    @Inject lateinit var serverActionCreator: ServerActionCreator
    @Inject lateinit var serverStore: ServerStore

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        registerReactionSubscriber(this)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_server, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.startAdvertisingButton.setOnClickListener { serverActionCreator.startAdvertising() }
        view.stopAdvertisingButton.setOnClickListener { serverActionCreator.stopAdvertising() }
    }

    override fun onResume() {
        super.onResume()
        serverActionCreator.startAdvertising()
    }

    override fun onPause() {
        super.onPause()
        serverActionCreator.stopAdvertising()
    }

    @Reaction(ServerActions.START_ADVERTISING)
    fun onAdvertisingStarted() {
        serverStatusText?.text = getString(R.string.advertising)
    }

    @Reaction(ServerActions.STOP_ADVERTISING)
    fun onAdvertisingStopped() {
        serverStatusText?.text = getString(R.string.stopped_advertising)
    }

    @Reaction(ServerReactions.RESPONSE_MESSAGE_SENT)
    fun onResponseMessageSent(reaction: FluxxReaction) {
        view?.serverStatusText?.text = reaction[Keys.MESSAGE_SENT]
    }
}
