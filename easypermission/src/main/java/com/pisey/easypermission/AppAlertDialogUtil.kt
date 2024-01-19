package com.pisey.easypermission

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AlertDialog

object AppAlertDialogUtil {
    private var alertDialog: AlertDialog? = null

    fun showDialog(
        context: Context,
        title: String,
        message: String,
        onPositionClicked: (() -> Unit)? = null,
        onNegativeClicked: (() -> Unit)? = null,
        positiveButtonTitle: String? = null,
        negativeButtonTitle: String? = null,
    ):AlertDialog? {
        val activity = when(context) {
            is Activity -> context
            is ContextWrapper -> context.baseContext as? Activity
            else -> null
        }

        if (activity?.isFinishing == true) {
            return null
        }

        if (alertDialog != null && alertDialog?.isShowing == true) alertDialog?.dismiss()
        val builder = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
        positiveButtonTitle?.let {
            builder.setPositiveButton(positiveButtonTitle) { dialog, _ ->
                dialog.dismiss()
                onPositionClicked?.invoke()
            }
        }
        negativeButtonTitle?.let {
            builder.setNegativeButton(negativeButtonTitle) { dialog, _ ->
                dialog.dismiss()
                onNegativeClicked?.invoke()
            }
        }
        alertDialog = builder.create()
        alertDialog?.show()
        return alertDialog
    }
}