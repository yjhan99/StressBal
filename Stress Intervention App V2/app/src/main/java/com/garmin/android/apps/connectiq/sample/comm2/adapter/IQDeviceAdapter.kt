/**
 * Copyright (C) 2015 Garmin International Ltd.
 * Subject to Garmin SDK License Agreement and Wearables Application Developer Agreement.
 */
package com.garmin.android.apps.connectiq.sample.comm2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.garmin.android.apps.connectiq.sample.comm2.R
import com.garmin.android.apps.connectiq.sample.comm2.Utils.mPreferences
import com.garmin.android.connectiq.IQDevice
import com.garmin.android.connectiq.IQDevice.IQDeviceStatus

class IQDeviceAdapter(
    private val onItemClickListener: (IQDevice) -> Unit
) : ListAdapter<IQDevice, IQDeviceViewHolder>(IQDeviceItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IQDeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.main_item_recycler, parent, false)
        return IQDeviceViewHolder(view, onItemClickListener)
    }

    override fun onBindViewHolder(holder: IQDeviceViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    fun updateDeviceStatus(device: IQDevice, status: IQDeviceStatus?) {
        val index = currentList
            .indexOfFirst { it.deviceIdentifier == device.deviceIdentifier }
        currentList[index].status = status
        notifyItemChanged(index)
    }
}

private class IQDeviceItemDiffCallback : DiffUtil.ItemCallback<IQDevice>() {
    override fun areItemsTheSame(oldItem: IQDevice, newItem: IQDevice): Boolean =
        oldItem.deviceIdentifier == newItem.deviceIdentifier

    override fun areContentsTheSame(oldItem: IQDevice, newItem: IQDevice): Boolean =
        oldItem.deviceIdentifier == newItem.deviceIdentifier
            && oldItem.friendlyName == newItem.friendlyName
            && oldItem.status == newItem.status

}

class IQDeviceViewHolder(
    private val view: View,
    private val onItemClickListener: (IQDevice) -> Unit
) : RecyclerView.ViewHolder(view) {

    fun bindTo(device: IQDevice) {
        val deviceName = when (device.friendlyName) {
            null -> device.deviceIdentifier.toString()
            else -> device.friendlyName
        }

        view.findViewById<TextView>(R.id.device_name).text = deviceName
        //view.findViewById<TextView>(android.R.id.text2).text = device.status?.name
        mPreferences.prefs.setString("isConnected", device.status?.name.toString())

        view.setOnClickListener {
            onItemClickListener(device)
        }
    }
}