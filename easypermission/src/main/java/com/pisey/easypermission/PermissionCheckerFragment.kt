package com.pisey.easypermission


import androidx.fragment.app.Fragment

/**
 * This fragment holds the single permission request and holds it until the flow is completed
 */

class PermissionCheckerFragment(forceGranted:Boolean) : Fragment() {
    constructor() : this(false)
    private val permissionSingleHandler = PermissionSingleHandler(this,forceGranted)
    private val permissionMultiHandler = PermissionMultiHandler(this,forceGranted)
    private val permissionGPSHandler = PermissionGPSHandler(this,forceGranted)

    fun requestPermissionsFromUser(vararg permissions: String, onGranted: () -> Unit) {
        if (permissions.size == 1) permissionSingleHandler.runSinglePermission(permission = permissions[0], onGranted = onGranted)
        else permissionMultiHandler.runMultiplePermission(permissions = permissions, onGranted = onGranted)
    }

    fun requestPermissionGPSFromUser(onGranted: () -> Unit) {
        permissionGPSHandler.runGPSPermission(onGranted)
    }


}
