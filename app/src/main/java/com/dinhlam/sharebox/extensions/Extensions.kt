package com.dinhlam.sharebox.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.os.VibrationEffect
import android.os.VibratorManager
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.dinhlam.sharebox.R
import java.io.Serializable

inline fun <reified R> Any?.cast(): R? {
    return this as? R
}

inline fun <reified R> Any?.castNonNull(): R {
    return this as R
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(key: String): T? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getParcelableExtra(key, T::class.java)
    }
    return getParcelableExtra(key) as? T
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Bundle.getParcelableExtraCompat(key: String): T? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getParcelable(key, T::class.java)
    }
    return getParcelable(key) as? T
}

@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Intent.getSerializableExtraCompat(key: String): T? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getSerializableExtra(key, T::class.java)
    }
    return getSerializableExtra(key) as? T
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent.getParcelableArrayListExtraCompat(key: String): ArrayList<T>? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getParcelableArrayListExtra(key, T::class.java)
    }
    return getParcelableArrayListExtra(key)
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Bundle.getParcelableArrayListExtraCompat(key: String): ArrayList<T>? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getParcelableArrayList(key, T::class.java)
    }
    return getParcelableArrayList(key)
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Bundle.getParcelableArrayExtraCompat(key: String): Array<T>? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getParcelableArray(key, T::class.java)
    }
    return getParcelableArray(key).cast()
}

inline fun <reified T> Context.getSystemServiceCompat(name: String): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getSystemService(T::class.java)
    } else {
        getSystemService(name) as T
    }
}

inline fun <reified T : Enum<T>> enumByNameIgnoreCase(input: String, default: T): T {
    return enumValues<T>().firstOrNull { enum -> enum.name.equals(input, true) } ?: default
}

@Suppress("DEPRECATION")
fun Context.vibrate(timing: Long) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemServiceCompat<VibratorManager>(Context.VIBRATOR_MANAGER_SERVICE).defaultVibrator
    } else {
        getSystemServiceCompat(Context.VIBRATOR_SERVICE)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(timing, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(timing)
    }
}

fun PackageManager.queryIntentActivitiesCompat(
    intent: Intent, flags: Int = 0
): List<ResolveInfo> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION") queryIntentActivities(intent, flags)
    }
}

inline fun Context.ifPermissionGranted(permission: String, block: () -> Unit) {
    if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
        block()
    }
}

fun Context.getColorCompat(@ColorRes colorRes: Int) = ContextCompat.getColor(this, colorRes)

fun Context.getDrawableCompat(@DrawableRes drawableRes: Int) =
    ContextCompat.getDrawable(this, drawableRes)

fun Context.copy(text: String?) {
    val clipboard = getSystemServiceCompat<ClipboardManager>(Context.CLIPBOARD_SERVICE)
    clipboard.setPrimaryClip(ClipData.newPlainText("sharebox", text))
    Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show()
}
