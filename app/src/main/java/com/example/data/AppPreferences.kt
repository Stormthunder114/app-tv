package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.UserAccount
import org.json.JSONArray
import org.json.JSONObject

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("assist_plus_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_M3U_URL = "m3u_url"
        private const val KEY_IS_M3U_MODE = "is_m3u_mode"
        private const val KEY_EXP_DATE = "exp_date"
        private const val KEY_STATUS = "status"
        private const val KEY_SERVER_NAME = "server_name"
        private const val KEY_FAVORITES = "favorites_set"
        private const val KEY_AUTO_LOGIN = "auto_login"
        private const val KEY_SAVED_ACCOUNTS = "saved_accounts_list"
    }

    fun saveAccount(account: UserAccount) {
        prefs.edit().apply {
            putString(KEY_SERVER_URL, account.serverUrl)
            putString(KEY_USERNAME, account.username)
            putString(KEY_PASSWORD, account.password)
            putString(KEY_M3U_URL, account.m3uUrl)
            putBoolean(KEY_IS_M3U_MODE, account.isM3uMode)
            putString(KEY_EXP_DATE, account.expDate)
            putString(KEY_STATUS, account.status)
            putString(KEY_SERVER_NAME, account.serverName)
            putBoolean(KEY_AUTO_LOGIN, true)
            apply()
        }
        addAccountToSavedList(account)
    }

    fun getSavedAccounts(): List<UserAccount> {
        val jsonString = prefs.getString(KEY_SAVED_ACCOUNTS, null) ?: return emptyList()
        val list = mutableListOf<UserAccount>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    UserAccount(
                        username = obj.optString("username", ""),
                        password = obj.optString("password", ""),
                        serverUrl = obj.optString("serverUrl", ""),
                        serverName = obj.optString("serverName", "Servidor"),
                        expDate = obj.optString("expDate", ""),
                        status = obj.optString("status", "Active"),
                        isM3uMode = obj.optBoolean("isM3uMode", false),
                        m3uUrl = obj.optString("m3uUrl", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun removeSavedAccount(username: String) {
        val current = getSavedAccounts().toMutableList()
        current.removeAll { it.username.equals(username, ignoreCase = true) }
        saveAccountsList(current)
    }

    private fun addAccountToSavedList(account: UserAccount) {
        if (account.username.isBlank() && !account.isM3uMode) return
        val current = getSavedAccounts().toMutableList()
        current.removeAll {
            if (account.isM3uMode) it.m3uUrl == account.m3uUrl
            else it.username.equals(account.username, ignoreCase = true)
        }
        current.add(0, account)
        saveAccountsList(current.take(15))
    }

    private fun saveAccountsList(list: List<UserAccount>) {
        try {
            val jsonArray = JSONArray()
            for (acc in list) {
                val obj = JSONObject().apply {
                    put("username", acc.username)
                    put("password", acc.password)
                    put("serverUrl", acc.serverUrl)
                    put("serverName", acc.serverName)
                    put("expDate", acc.expDate)
                    put("status", acc.status)
                    put("isM3uMode", acc.isM3uMode)
                    put("m3uUrl", acc.m3uUrl)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_SAVED_ACCOUNTS, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSavedAccount(): UserAccount? {
        val isM3u = prefs.getBoolean(KEY_IS_M3U_MODE, false)
        val serverUrl = prefs.getString(KEY_SERVER_URL, null) ?: ""
        val m3uUrl = prefs.getString(KEY_M3U_URL, null) ?: ""
        val username = prefs.getString(KEY_USERNAME, null) ?: ""
        val password = prefs.getString(KEY_PASSWORD, null) ?: ""

        if (!isM3u && (serverUrl.isBlank() || username.isBlank())) {
            return null
        }
        if (isM3u && m3uUrl.isBlank()) {
            return null
        }

        return UserAccount(
            username = username,
            password = password,
            serverUrl = serverUrl,
            serverName = prefs.getString(KEY_SERVER_NAME, "Servidor Salvo") ?: "Servidor",
            expDate = prefs.getString(KEY_EXP_DATE, "") ?: "",
            status = prefs.getString(KEY_STATUS, "Active") ?: "Active",
            isM3uMode = isM3u,
            m3uUrl = m3uUrl
        )
    }

    fun clearAccount() {
        prefs.edit().apply {
            remove(KEY_SERVER_URL)
            remove(KEY_USERNAME)
            remove(KEY_PASSWORD)
            remove(KEY_M3U_URL)
            remove(KEY_IS_M3U_MODE)
            remove(KEY_EXP_DATE)
            remove(KEY_STATUS)
            remove(KEY_SERVER_NAME)
            putBoolean(KEY_AUTO_LOGIN, false)
            apply()
        }
    }

    fun getFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun toggleFavorite(id: String): Boolean {
        val current = getFavorites().toMutableSet()
        val isFav = if (current.contains(id)) {
            current.remove(id)
            false
        } else {
            current.add(id)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        return isFav
    }
}
