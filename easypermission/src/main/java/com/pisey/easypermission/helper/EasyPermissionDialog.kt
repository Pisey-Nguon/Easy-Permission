package com.pisey.easypermission.helper

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.pisey.easypermission.getListOfShortNamePermission

object EasyPermissionDialog {
    private fun rationaleCallback(context: Context?,req: EasyPermissionsRequest) {
        // this will be called when permission is denied once or more time. Handle it your way
        val myMessage = "These permissions are required to perform this feature. Please allow us to use this feature. "
        context?.let{
            val builder = AlertDialog.Builder(it)
            builder.setMessage(myMessage)
                .setPositiveButton("TRY AGAIN"
                ) { _, _ ->
                    req.proceed()
                }
                .setNegativeButton("CANCEL"
                ) { _, _ ->
                    req.cancel()
                }
                .setCancelable(false)
            builder.create().show()
        }
    }


    private fun permissionsPermanentlyDenied(context: Context?,req: EasyPermissionsRequest) {
        // this will be called when some/all permissions required by the method are permanently
        // denied. Handle it your way.
        val permissions = getListOfShortNamePermission(req.deniedPermissions)
        val myMessage =  "Some permissions are permanently denied which are required to perform this operation. Please open app settings to grant these permissions: ${permissions.joinToString(", ")}"
        context?.let{
            val builder = AlertDialog.Builder(it)
            builder.setMessage(myMessage)
                .setPositiveButton("SETTINGS"
                ) { _, _ ->
                    req.openAppSettings()
                }
                .setNegativeButton("CANCEL"
                ) { _, _ ->
                    req.cancel()
                }
                .setCancelable(false)
            builder.create().show()
        }
    }

    private fun whenPermAreDenied(context: Context?,req: EasyPermissionsRequest) {
        // handle something when permissions are not granted and the request method cannot be called
    }

    fun easyPermissionsOption(context: Context?) = EasyPermissionsOptions(
        handleRationale = true,
        rationaleMessage = "Custom rational message",
        permanentlyDeniedMessage = "Custom permanently denied message",
        rationaleMethod = { req -> rationaleCallback(context,req) },
        permissionsDeniedMethod = {req -> whenPermAreDenied(context,req) },
        permanentDeniedMethod = { req -> permissionsPermanentlyDenied(context,req) }
    )
}