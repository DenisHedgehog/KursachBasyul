package org.exazoom.kursovayabasyul.utils

import android.content.SharedPreferences
import android.app.Activity
import android.content.Context


private const val ACTIVE_USER = "activeUser"
private const val ACTIVE_EMP = "activeEmp"
private lateinit var sPref: SharedPreferences

fun saveActiveUser(activity: Activity, activeUser: String) {
    sPref = activity.getSharedPreferences("activeUserConfig", Context.MODE_PRIVATE)
    val editor = sPref.edit()
    editor.putString(ACTIVE_USER, activeUser)
    editor.apply()
}

fun getActiveUser(activity: Activity): String? {
    sPref = activity.getSharedPreferences("activeUserConfig", Context.MODE_PRIVATE)
    return sPref.getString(ACTIVE_USER, null)
}

fun removeActiveUser(activity: Activity) {
    sPref = activity.getSharedPreferences("activeUserConfig", Context.MODE_PRIVATE)
    val editor = sPref.edit()
    editor.remove(ACTIVE_USER)
    editor.apply()
}

fun isActiveUserExist(activity: Activity): Boolean = getActiveUser(activity) is String

fun saveActiveEmp(activity: Activity, activeEmp: String) {
    sPref = activity.getSharedPreferences("activeUserConfig", Context.MODE_PRIVATE)
    val editor = sPref.edit()
    editor.putString(ACTIVE_EMP, activeEmp)
    editor.apply()
}

fun getActiveEmp(activity: Activity): String? {
    sPref = activity.getSharedPreferences("activeUserConfig", Context.MODE_PRIVATE)
    return sPref.getString(ACTIVE_EMP, null)
}

fun removeActiveEmp(activity: Activity) {
    sPref = activity.getSharedPreferences("activeUserConfig", Context.MODE_PRIVATE)
    val editor = sPref.edit()
    editor.remove(ACTIVE_EMP)
    editor.apply()
}

fun isActiveEmpExist(activity: Activity): Boolean = getActiveEmp(activity) is String