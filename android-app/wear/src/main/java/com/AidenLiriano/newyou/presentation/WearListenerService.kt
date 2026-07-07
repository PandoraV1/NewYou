package com.AidenLiriano.newyou.presentation

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WearListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "WearListenerService"

        var pendingActivityId: Int = -1
        var pendingGuidedWorkout: GuidedWorkoutPlan? = null
        var pendingCustomWorkouts: String? = null
        var pendingAutoStart: Boolean = false
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        Log.d(TAG, "Message received in background service: $path")

        when {
            path.startsWith("/activity_id_response/") -> {
                val id = path.replace("/activity_id_response/", "").toIntOrNull()
                if (id != null) {
                    pendingActivityId = id
                    // Also update MainActivity directly if it is active
                    MainActivity.instance?.pendingActivityId = id
                }
            }

            path == "/guided_workout" -> {
                val data = String(messageEvent.data)
                val parts = data.split("|")
                if (parts.size >= 3) {
                    val typeId = parts[0].toIntOrNull() ?: return
                    val tierName = parts[1]
                    val goals = parts[2].split("~")
                    val plan = GuidedWorkoutPlan(typeId, tierName, goals)
                    pendingGuidedWorkout = plan
                    pendingAutoStart = true
                    MainActivity.instance?.let {
                        it.pendingGuidedWorkout = plan
                        it.pendingAutoStart = true
                    }
                }
            }

            path == "/custom_workouts_data" -> {
                val data = String(messageEvent.data)
                pendingCustomWorkouts = data
                MainActivity.instance?.receiveCustomWorkouts(data)
            }
        }
    }
}