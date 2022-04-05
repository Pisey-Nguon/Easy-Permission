package com.pisey.easypermission.helper


import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri.fromParts
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import org.jetbrains.anko.alert

/**
 * This fragment holds the single permission request and holds it until the flow is completed
 */
class PermissionCheckerFragment : Fragment() {

    private var easyPermissionsRequest: EasyPermissionsRequest? = null

    interface QuickPermissionsCallback {
        fun shouldShowRequestPermissionsRationale(easyPermissionsRequest: EasyPermissionsRequest?)
        fun onPermissionsGranted(easyPermissionsRequest: EasyPermissionsRequest?)
        fun onPermissionsPermanentlyDenied(easyPermissionsRequest: EasyPermissionsRequest?)
        fun onPermissionsDenied(easyPermissionsRequest: EasyPermissionsRequest?)
    }

    companion object {
        private const val TAG = "QuickPermissionsKotlin"
        private const val PERMISSIONS_REQUEST_CODE = 199
        fun newInstance(): PermissionCheckerFragment = PermissionCheckerFragment()
    }

    private var mListener: QuickPermissionsCallback? = null

    fun setListener(listener: QuickPermissionsCallback) {
        mListener = listener
        Log.d(TAG, "onCreate: listeners set")
    }

    private fun removeListener() {
        mListener = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: permission fragment created")
    }

    fun setRequestPermissionsRequest(easyPermissionsRequest: EasyPermissionsRequest?) {
        this.easyPermissionsRequest = easyPermissionsRequest
    }

    private fun removeRequestPermissionsRequest() {
        easyPermissionsRequest = null
    }

    fun clean() {
        if (easyPermissionsRequest != null) {
            // permission request flow is finishing
            // let the caller receive callback about it
            if (easyPermissionsRequest?.deniedPermissions?.size ?: 0 > 0)
                mListener?.onPermissionsDenied(easyPermissionsRequest)

            removeRequestPermissionsRequest()
            removeListener()
        } else {
            Log.w(
                TAG, "clean: QuickPermissionsRequest has already completed its flow. " +
                    "No further callbacks will be called for the current flow.")
        }
    }

    fun requestPermissionsFromUser() {
        if (easyPermissionsRequest != null) {
            Log.d(TAG, "requestPermissionsFromUser: requesting permissions")
            requestPermissions(easyPermissionsRequest?.permissions.orEmpty(), PERMISSIONS_REQUEST_CODE)
        } else {
            Log.w(
                TAG, "requestPermissionsFromUser: QuickPermissionsRequest has already completed its flow. " +
                    "Cannot request permissions again from the request received from the callback. " +
                    "You can start the new flow by calling runWithPermissions() { } again.")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "passing callback")

        // check if permissions granted
        handlePermissionResult(permissions, grantResults)
    }

    /**
     * Checks and takes the action based on permission results retrieved from onRequestPermissionsResult
     * and from the settings activity
     *
     * @param permissions List of Permissions
     * @param grantResults A list of permission result <b>Granted</b> or <b>Denied</b>
     */
    private fun handlePermissionResult(permissions: Array<String>, grantResults: IntArray) {
        // add a check with the permissions list
        // if the permissions list is empty, that means system has told that permissions request
        // is invalid somehow or discarded the previous request
        // this can happen in case when the multiple permissions requests are sent
        // simultaneously to the system
        if (permissions.isEmpty()) {
            Log.w(TAG, "handlePermissionResult: Permissions result discarded. You might have called multiple permissions request simultaneously")
            return
        }

        if (PermissionsUtil.hasSelfPermission(context, permissions)) {

            // set the denied permissions to empty as all the permissions are granted
            // this is required as clean will be called which can invoke on permissions denied
            // if it finds some permissions in the denied list
            easyPermissionsRequest?.deniedPermissions = emptyArray()

            // we are good to go!
            mListener?.onPermissionsGranted(easyPermissionsRequest)

            // flow complete
            clean()
        } else {
            // we are still missing permissions
            val deniedPermissions = PermissionsUtil.getDeniedPermissions(permissions, grantResults)
            easyPermissionsRequest?.deniedPermissions = deniedPermissions

            // check if rationale dialog should be shown or not
            var shouldShowRationale = true
            var isPermanentlyDenied = false
            for (i in 0 until deniedPermissions.size) {
                val deniedPermission = deniedPermissions[i]
                val rationale = shouldShowRequestPermissionRationale(deniedPermission)
                if (!rationale) {
                    shouldShowRationale = false
                    isPermanentlyDenied = true
                    break
                }
            }

            if (easyPermissionsRequest?.handlePermanentlyDenied == true && isPermanentlyDenied) {

                easyPermissionsRequest?.permanentDeniedMethod?.let {
                    // get list of permanently denied methods
                    easyPermissionsRequest?.permanentlyDeniedPermissions =
                        PermissionsUtil.getPermanentlyDeniedPermissions(this, permissions, grantResults)
                    mListener?.onPermissionsPermanentlyDenied(easyPermissionsRequest)
                    return
                }

                activity?.alert {
                    message = easyPermissionsRequest?.permanentlyDeniedMessage.orEmpty()
                    positiveButton("SETTINGS") {
                        openAppSettings()
                    }
                    negativeButton("CANCEL") {
                        clean()
                    }
                }?.apply { isCancelable = false }?.show()
                return
            }

            // if should show rationale dialog
            if (easyPermissionsRequest?.handleRationale == true && shouldShowRationale) {

                easyPermissionsRequest?.rationaleMethod?.let {
                    mListener?.shouldShowRequestPermissionsRationale(easyPermissionsRequest)
                    return
                }

                activity?.alert {
                    message = easyPermissionsRequest?.rationaleMessage.orEmpty()
                    positiveButton("TRY AGAIN") {
                        requestPermissionsFromUser()
                    }
                    negativeButton("CANCEL") {
                        clean()
                    }
                }?.apply { isCancelable = false }?.show()
                return
            }

            // if handlePermanentlyDenied = false and handleRationale = false
            // This will call permissionsDenied method
            clean()
        }
    }

    fun openAppSettings() {
        if (easyPermissionsRequest != null) {
            val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS,
                    fromParts("package", activity?.packageName, null))
            startActivityForResult(intent, PERMISSIONS_REQUEST_CODE)
        } else {
            Log.w(TAG, "openAppSettings: QuickPermissionsRequest has already completed its flow. Cannot open app settings")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val permissions = easyPermissionsRequest?.permissions ?: emptyArray()
            val grantResults = IntArray(permissions.size)
            permissions.forEachIndexed { index, s ->
                grantResults[index] = context?.let { ActivityCompat.checkSelfPermission(it, s) } ?: PackageManager.PERMISSION_DENIED
            }

            handlePermissionResult(permissions, grantResults)
        }
    }
}
