package com.fitmate.app.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.fitmate.app.model.Identifiable
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** GENERIC repository contract: works for any type T that has an id. */
interface Repository<T : Identifiable> {
    fun getAll(): List<T>

    /** Inserts the item, or replaces the stored item with the same id. */
    fun save(item: T)

    fun delete(id: String)
}

/** Converts a T to and from JSON. Keeps storage code out of the model classes (single responsibility). */
interface JsonMapper<T> {
    fun toJson(item: T): JSONObject
    fun fromJson(json: JSONObject): T
}

/**
 * One generic class stores workout logs *and* reminders: it keeps a JSON array in SharedPreferences
 * and delegates the per-type conversion to the injected [JsonMapper].
 */
class PrefsRepository<T : Identifiable>(
    private val prefs: SharedPreferences,
    private val key: String,
    private val mapper: JsonMapper<T>
) : Repository<T> {

    override fun getAll(): List<T> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { mapper.fromJson(array.getJSONObject(it)) }
        } catch (e: JSONException) {
            emptyList()
        }
    }

    override fun save(item: T) {
        val items = getAll().toMutableList()
        val index = items.indexOfFirst { it.id == item.id }
        if (index >= 0) items[index] = item else items.add(item)
        write(items)
    }

    override fun delete(id: String) = write(getAll().filterNot { it.id == id })

    private fun write(items: List<T>) {
        val array = JSONArray()
        items.forEach { array.put(mapper.toJson(it)) }
        prefs.edit { putString(key, array.toString()) }
    }
}
