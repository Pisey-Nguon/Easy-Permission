package com.pisey.easypermission

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.GET_META_DATA
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class PermissionSingleHandler(private val fragment: Fragment,private val forceGranted:Boolean) {



    private val context: Context by lazy { fragment.requireContext() }
    private var singlePermissionGranted: (() -> Unit)? = null
    private var permission: String? = null

    private val requestPermissionSettings = fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (permission == null) return@registerForActivityResult
        if (!context.hasSelfPermission(permission!!)) {
            showDialogRequestPermission(permission)
        } else {
            singlePermissionGranted?.invoke()
        }

    }


    private val requestSinglePermissionNoCallbackLauncher = fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        val info = context.packageManager.getPermissionInfo(permission!!, GET_META_DATA)
        val label = context.getString(info.labelRes)
        when {
            isGranted -> {
                singlePermissionGranted?.let { it() }
            }

            !fragment.shouldShowRequestPermissionRationale(permission!!) -> {
                val fullDescription = StringBuilder()
                    .append("This lets you")
                    .append(" ")
                    .append(label)
                    .append(".")
                    .append("To enable this by click App Settings below and allow this permission").toString()
                showDialogAllowInSettings(fullDescription)
            }

            else -> {
                val fullDescription = StringBuilder()
                    .append("This lets you")
                    .append(" ")
                    .append(label)
                    .append(".")
                    .append("Please allow this permission to use this feature.").toString()
                showDialogSinglePermissionDenied(fullDescription)
            }
        }

    }

    fun runSinglePermission(permission: String, onGranted: () -> Unit) {
        singlePermissionGranted = onGranted
        this.permission = permission
        when {
            context.hasSelfPermission(permission) -> {
                onGranted()
            }

            else -> {
                requestSinglePermissionNoCallbackLauncher.launch(permission)
            }
        }
    }

    private fun showDialogSinglePermissionDenied(fullDescription: String) {
        AppAlertDialogUtil.showDialog(
            context,
            title = "Permission Required",
            message = fullDescription,
            positiveButtonTitle = "Allow",
            negativeButtonTitle = if (!forceGranted) "Don't Allow" else null,
            onPositionClicked = {  requestSinglePermissionNoCallbackLauncher.launch(permission) }
        )
    }


    private fun showDialogRequestPermission(permission: String?) {
        if (permission == null) return
        val info = context.packageManager.getPermissionInfo(permission, GET_META_DATA)
        val label = context.getString(info.labelRes)
        val fullDescription = StringBuilder().append("This lets you")
            .append(" ")
            .append(label)
            .append(".")
            .append("To enable this by click App Settings below and allow this permission").toString()
        showDialogAllowInSettings(fullDescription)
    }

    private fun showDialogAllowInSettings(fullDescription: String) {
        AppAlertDialogUtil.showDialog(
            context,
            title = "",
            message = fullDescription,
            positiveButtonTitle = "App Setting",
            negativeButtonTitle = if (!forceGranted) "Don't Allow" else null,
            onPositionClicked = { startApplicationDetailsActivity() }
        )
    }

    private fun startApplicationDetailsActivity() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri: Uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        requestPermissionSettings.launch(intent)
    }

}