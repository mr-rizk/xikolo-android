package de.xikolo.storages

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import de.xikolo.App
import de.xikolo.R
import de.xikolo.utils.PlaybackSpeedUtil

class ApplicationPreferences {

    private val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(App.getInstance())

    private val context: Context = App.getInstance()

    var storage: String?
        get() = getString(context.getString(R.string.preference_storage), context.getString(R.string.settings_default_value_storage))
        set(value) = putString(context.getString(R.string.preference_storage), value)

    var isVideoQualityLimitedOnMobile: Boolean
        get() = getBoolean(context.getString(R.string.preference_video_quality))
        set(value) = putBoolean(context.getString(R.string.preference_video_quality), value)

    var isDownloadNetworkLimitedOnMobile: Boolean
        get() = getBoolean(context.getString(R.string.preference_download_network))
        set(value) = putBoolean(context.getString(R.string.preference_download_network), value)

    var videoPlaybackSpeed: PlaybackSpeedUtil
        get() = PlaybackSpeedUtil.get(getString(
            context.getString(R.string.preference_video_playback_speed),
            context.getString(R.string.settings_default_value_video_playback_speed)
        ))
        set(speed) = putString(context.getString(R.string.preference_video_playback_speed), speed.toString())

    var videoSubtitlesLanguage: String?
        get() {
            val language = getString(
                context.getString(R.string.preference_video_subtitles_language),
                context.getString(R.string.settings_default_value_video_subtitles_language)
            )
            return if (!language.equals(context.getString(R.string.settings_default_value_video_subtitles_language))) language else null
        }
        set(language) {
            val value = language
                ?: context.getString(R.string.settings_default_value_video_subtitles_language)
            putString(
                context.getString(R.string.preference_video_subtitles_language),
                value
            )
        }

    var confirmBeforeDeleting: Boolean
        get() = getBoolean(context.getString(R.string.preference_confirm_delete))
        set(value) = putBoolean(context.getString(R.string.preference_confirm_delete), value)


    var usedSecondScreen: Boolean
        get() = getBoolean(context.getString(R.string.preference_used_second_screen), false)
        set(value) = putBoolean(context.getString(R.string.preference_used_second_screen), value)

    var firstAndroid4DeprecationWarningShown: Boolean
        get() = getBoolean(context.getString(R.string.preference_first_android_4_deprecation_dialog), false)
        set(value) = putBoolean(context.getString(R.string.preference_first_android_4_deprecation_dialog), value)

    var secondAndroid4DeprecationWarningShown: Boolean
        get() = getBoolean(context.getString(R.string.preference_second_android_4_deprecation_dialog), false)
        set(value) = putBoolean(context.getString(R.string.preference_second_android_4_deprecation_dialog), value)

    private fun getBoolean(key: String, defValue: Boolean = true) = preferences.getBoolean(key, defValue)

    private fun putBoolean(key: String, value: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    private fun getString(key: String, defValue: String? = null): String? = preferences.getString(key, defValue)

    private fun putString(key: String, value: String?) {
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun delete() {
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }

    fun contains(key: String): Boolean = preferences.contains(key)

    fun setToDefault(key: String, default: String) = putString(key, getString(key, default))

}
