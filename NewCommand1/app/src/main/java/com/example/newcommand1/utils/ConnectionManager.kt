package com.example.newcommand1.utils

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import javax.sql.ConnectionEventListener


object ConnectionManager {

    private var listeners: MutableSet<WeakReference<ConnectionEventListener>> = mutableSetOf()

    private val deviceGattMap = ConcurrentHashMap<BluetoothDevice, BluetoothGatt>()
    private val operationQueue = ConcurrentLinkedQueue<BleOperationType>()
    private var pendingOperation: BleOperationType? = null

    fun registerListener(listener: ConnectionEventListener) {
        if (listeners.map { it.get() }.contains(listener)) { return }
        listeners.add(WeakReference(listener))
        listeners = listeners.filter { it.get() != null }.toMutableSet()
        log("Added listener $listener, ${listeners.size} listeners total")
    }

    fun unregisterListener(listener: ConnectionEventListener) {
        // Removing elements while in a loop results in a java.util.ConcurrentModificationException
        var toRemove: WeakReference<ConnectionEventListener>? = null
        listeners.forEach {
            if (it.get() == listener) {
                toRemove = it
            }
        }
        toRemove?.let {
            listeners.remove(it)
            log("Removed listener ${it.get()}, ${listeners.size} listeners total")
        }
    }

//    fun connect(device: BluetoothDevice, context: Context) {
//        if (device.isConnected()) {
//            log("Already connected to ${device.address}!")
//        } else {
//            enqueueOperation(Connect(device, context.applicationContext))
//        }
//    }

}