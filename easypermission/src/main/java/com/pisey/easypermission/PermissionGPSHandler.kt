package com.pisey.easypermission

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
class PermissionGPSHandler(private val fragment: Fragment, private val forceGranted: Boolean) {


    private var permissionGPSExecutor: (() -> Unit)? = null

    private val context: Context by lazy { fragment.requireContext() }

    private val gpsRequiredResult = fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (isLocationEnabled()) {
            // Do something with the location updates
            permissionGPSExecutor?.invoke()
        } else {
            // The user did not allow the location services, handle it accordingly
            showDialogForceGPSPermissionDenied()
        }
    }

    private fun isLocationEnabled():Boolean{
        // Get the LocationManager instance
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Check if the GPS provider or the network provider are enabled
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return isGpsEnabled || isNetworkEnabled
    }

    private fun showDialogForceGPSPermissionDenied() {
        AppAlertDialogUtil.showDialog(
            fragment.requireContext(),
            title = "Permission Required",
            message = "Please turn on GPS to use this feature.",
            positiveButtonTitle = "Turn On",
            negativeButtonTitle = if (!forceGranted) "Don't Allow" else null,
            onPositionClicked = {  gpsRequiredResult.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))}
        )
    }

    fun runGPSPermission(execute: () -> Unit) {
        permissionGPSExecutor = execute
        if (isLocationEnabled()){
            execute.invoke()
        }else{
            gpsRequiredResult.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }
}