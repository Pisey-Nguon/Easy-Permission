package com.pisey.easypermission.helper

data class EasyPermissionsOptions(
    var handleRationale: Boolean = true,
    var rationaleMessage: String = "",
    var handlePermanentlyDenied: Boolean = true,
    var permanentlyDeniedMessage: String = "",
    var rationaleMethod: ((EasyPermissionsRequest) -> Unit)? = null,
    var permanentDeniedMethod: ((EasyPermissionsRequest) -> Unit)? = null,
    var permissionsDeniedMethod: ((EasyPermissionsRequest) -> Unit)? = null
)