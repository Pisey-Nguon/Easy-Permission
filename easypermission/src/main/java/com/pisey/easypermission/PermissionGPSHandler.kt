package com.pisey.easypermission

import android.content.Context
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
class PermissionGPSHandler(private val fragment: Fragment, private val forceGranted: Boolean) {


    private var permissionGPSExecutor: (() -> Unit)? = null
    private var intentSenderRequest:IntentSenderRequest? = null

    private val context: Context by lazy { fragment.requireContext() }

    private val resolutionForResult = fragment.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        if (it.resultCode == AppCompatActivity.RESULT_OK) permissionGPSExecutor?.invoke() else showDialogForceGPSPermissionDenied()
    }

    private fun showDialogForceGPSPermissionDenied() {
        AppAlertDialogUtil.showDialog(
            fragment.requireContext(),
            title = "Permission Required",
            message = "Please turn on GPS to use this feature.",
            positiveButtonTitle = "Turn On",
            negativeButtonTitle = if (!forceGranted) "Don't Allow" else null,
            onPositionClicked = { intentSenderRequest?.let {resolutionForResult.launch(it)  }  }
        )
    }

    fun runGPSPermission(execute: () -> Unit) {
        permissionGPSExecutor = execute
        LocationUtils.enableLoc(fragment.requireActivity(),
            allowRequest = { permissionGPSExecutor?.invoke() },
            launchRequest = {
                intentSenderRequest = it
                resolutionForResult.launch(it)
            }
        )
    }
}