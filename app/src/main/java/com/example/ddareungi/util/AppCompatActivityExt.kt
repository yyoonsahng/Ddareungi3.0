/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.ddareungi.util


import android.content.pm.PackageManager
import android.support.annotation.IdRes
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import com.example.ddareungi.splash.SplashActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * The `fragment` is added to the container view with id `frameId`. The operation is
 * performed by the `fragmentManager`.
 */
fun AppCompatActivity.replaceFragmentInActivity(fragment: Fragment, @IdRes frameId: Int, appbarTitle: String) {
    supportFragmentManager.transact {
        replace(frameId, fragment)
    }
    appbar_title.text = appbarTitle
}

/**
 * The `fragment` is added to the container view with tag. The operation is
 * performed by the `fragmentManager`.
 */
fun AppCompatActivity.addFragmentToActivity(fragment: Fragment, tag: String) {
    supportFragmentManager.transact {
        add(fragment, tag)
    }
}

fun AppCompatActivity.setupActionBar(@IdRes toolbarId: Int, action: ActionBar.() -> Unit) {
    setSupportActionBar(findViewById(toolbarId))
    supportActionBar?.run {
        action()
    }
}

fun Fragment.checkLocationPermission(): Boolean {
    val requestPermission = (arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))
    val requestResult = BooleanArray(requestPermission.size)
    for (i in requestResult.indices) {
        requestResult[i] =
            ContextCompat.checkSelfPermission(requireContext(), requestPermission[i]) == PackageManager.PERMISSION_GRANTED
        if (!requestResult[i]) {
            return false
        }
    }
    return true
}

fun AppCompatActivity.requestLocationPermission() {
    val requestPermission = (arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))
    val requestCode = SplashActivity.MY_LOCATION_REQUEST
    ActivityCompat.requestPermissions(this, requestPermission, requestCode)
}

fun Fragment.checkCallPermission(): Boolean {
    val requestPermission = (arrayOf(android.Manifest.permission.CALL_PHONE))
    val requestResult = BooleanArray(requestPermission.size)
    for (i in requestResult.indices) {
        requestResult[i] =
            ContextCompat.checkSelfPermission(requireContext(), requestPermission[i]) == PackageManager.PERMISSION_GRANTED
        if (!requestResult[i]) {
            return false
        }
    }
    return true
}

fun AppCompatActivity.requestCallPermission() {
    val requestPermission = (arrayOf(android.Manifest.permission.CALL_PHONE))
    val requestCode = SplashActivity.CALL_REQUEST
    ActivityCompat.requestPermissions(this, requestPermission, requestCode)
}

/**
 * Runs a FragmentTransaction, then calls commit().
 */
private inline fun FragmentManager.transact(action: FragmentTransaction.() -> Unit) {
    beginTransaction().apply {
        action()
    }.commit()
}