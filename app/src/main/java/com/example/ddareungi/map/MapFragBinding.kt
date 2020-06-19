package com.example.ddareungi.map

import android.util.Log
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.yavski.fabspeeddial.FabSpeedDial


@BindingAdapter("app:setDist")
fun setDist(textView: TextView, dist: Float) {
    val distStr = String.format("%.2f", dist)
    if (dist >= 0) {
        textView.text = distStr + "km"
    }
    else {
        textView.text = ""
    }
}

@BindingAdapter("app:setSearchDestText")
fun setText(textView: TextView, marker: Marker?) {
    if(marker != null) {
        val place = marker.tag as Place
        textView.text = place.name
    }
}

@BindingAdapter("app:setFabDialVisibility")
fun setVisibility(fabSpeedDial: FabSpeedDial, focusOnMap: Boolean) {
    Log.i("MapFragBinding", "FocusOnMap(dial): ${focusOnMap}")
    if(focusOnMap) {
        fabSpeedDial.show()
    }
    else {
        fabSpeedDial.hide()
    }
}

@BindingAdapter("app:setFabVisibility")
fun setVisibility(fab: FloatingActionButton, focusOnMap: Boolean) {
    Log.i("MapFragBinding", "FocusOnMap: ${focusOnMap}")
    if(focusOnMap) {
        fab.show()
    }
    else {
        fab.hide()
    }
}
