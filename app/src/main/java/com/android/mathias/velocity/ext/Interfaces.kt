package com.android.mathias.velocity.ext

import android.view.View

internal interface IBottomSheetListener {
    fun onRouteNameSaved(text: String)
}

internal interface IRvClickListener {
    fun itemLongClick(v: View, position: Int)
}