package com.sduduzog.slimlauncher.utils

import android.view.View
import com.sduduzog.slimlauncher.data.model.HomeApp

interface OnShitDoneToAppsListener {
    fun onAppsUpdated(list: List<HomeApp>)
    fun onAppMenuClicked(view: View, app: HomeApp)
}