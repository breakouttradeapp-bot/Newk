package com.kundaliai.app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import java.text.SimpleDateFormat
import java.util.*

fun View.visible() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun Context.showToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
fun Context.showLongToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

fun String.capitalizeWords() = split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
    observe(owner) { value ->
        observer(value)
    }
}

fun getCurrentTimeMillis() = System.currentTimeMillis()

fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return sdf.format(date)
}
