package com.birthday.celebrate.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "birthday_prefs")

class BirthdayRepository(private val context: Context) {

    private val gson = Gson()
    private object Keys { val DATA = stringPreferencesKey("birthday_data_json") }

    val birthdayDataFlow: Flow<BirthdayData> = context.dataStore.data.map { prefs ->
        prefs[Keys.DATA]?.let {
            try { gson.fromJson(it, BirthdayData::class.java) }
            catch (e: Exception) { BirthdayData() }
        } ?: BirthdayData()
    }

    suspend fun saveBirthdayData(data: BirthdayData) {
        context.dataStore.edit { it[Keys.DATA] = gson.toJson(data) }
    }

    suspend fun updateChildName(name: String) = saveBirthdayData(getCurrent().copy(childName = name))
    suspend fun updateAge(age: Int)            = saveBirthdayData(getCurrent().copy(age = age))
    suspend fun updateParentMessage(msg: String) = saveBirthdayData(getCurrent().copy(parentMessage = msg))

    // Save music URI from phone storage
    suspend fun updateMusic(uri: String?, title: String) =
        saveBirthdayData(getCurrent().copy(musicUri = uri, musicTitle = title))

    suspend fun updateSlidePhoto(slideId: Int, uri: String?) {
        val c = getCurrent()
        saveBirthdayData(c.copy(slides = c.slides.map { if (it.id == slideId) it.copy(photoUri = uri) else it }))
    }

    suspend fun updateSlideMessage(slideId: Int, message: String) {
        val c = getCurrent()
        saveBirthdayData(c.copy(slides = c.slides.map { if (it.id == slideId) it.copy(message = message) else it }))
    }

    suspend fun resetToDefaults() = saveBirthdayData(BirthdayData())

    private suspend fun getCurrent(): BirthdayData = birthdayDataFlow.first()
}
