package com.fitmate.app.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.fitmate.app.model.ActivityLevel
import com.fitmate.app.model.Difficulty
import com.fitmate.app.model.ExerciseCategory
import com.fitmate.app.model.FitnessGoal
import com.fitmate.app.model.Gender
import com.fitmate.app.model.Reminder
import com.fitmate.app.model.ReminderType
import com.fitmate.app.model.UserProfile
import com.fitmate.app.model.WorkoutLog
import org.json.JSONException
import org.json.JSONObject

object ProfileMapper : JsonMapper<UserProfile> {
    override fun toJson(item: UserProfile): JSONObject = JSONObject().apply {
        put("name", item.name)
        put("age", item.age)
        put("heightCm", item.heightCm)
        put("weightKg", item.weightKg)
        put("gender", item.gender.name)
        put("activityLevel", item.activityLevel.name)
        put("goal", item.goal.name)
        put("experience", item.experience.name)
    }

    override fun fromJson(json: JSONObject) = UserProfile(
        name = json.getString("name"),
        age = json.getInt("age"),
        heightCm = json.getDouble("heightCm"),
        weightKg = json.getDouble("weightKg"),
        gender = Gender.valueOf(json.getString("gender")),
        activityLevel = ActivityLevel.valueOf(json.getString("activityLevel")),
        goal = FitnessGoal.valueOf(json.getString("goal")),
        experience = Difficulty.valueOf(json.getString("experience"))
    )
}

object WorkoutLogMapper : JsonMapper<WorkoutLog> {
    override fun toJson(item: WorkoutLog): JSONObject = JSONObject().apply {
        put("id", item.id)
        put("exerciseName", item.exerciseName)
        put("category", item.category.name)
        put("minutes", item.minutes)
        put("calories", item.calories)
        put("timestamp", item.timestamp)
    }

    override fun fromJson(json: JSONObject) = WorkoutLog(
        id = json.getString("id"),
        exerciseName = json.getString("exerciseName"),
        category = ExerciseCategory.valueOf(json.getString("category")),
        minutes = json.getInt("minutes"),
        calories = json.getDouble("calories"),
        timestamp = json.getLong("timestamp")
    )
}

object ReminderMapper : JsonMapper<Reminder> {
    override fun toJson(item: Reminder): JSONObject = JSONObject().apply {
        put("id", item.id)
        put("type", item.type.name)
        put("hour", item.hour)
        put("minute", item.minute)
        put("enabled", item.enabled)
    }

    override fun fromJson(json: JSONObject) = Reminder(
        id = json.getString("id"),
        type = ReminderType.valueOf(json.getString("type")),
        hour = json.getInt("hour"),
        minute = json.getInt("minute"),
        enabled = json.getBoolean("enabled")
    )
}

/** The profile is a single object rather than a list, so it gets its own tiny store. */
class ProfileRepository(private val prefs: SharedPreferences) {

    fun load(): UserProfile? {
        val raw = prefs.getString(KEY, null) ?: return null
        return try {
            ProfileMapper.fromJson(JSONObject(raw))
        } catch (e: JSONException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun save(profile: UserProfile) {
        prefs.edit { putString(KEY, ProfileMapper.toJson(profile).toString()) }
    }

    private companion object {
        const val KEY = "profile"
    }
}
