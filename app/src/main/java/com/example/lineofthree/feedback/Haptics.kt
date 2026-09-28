package com.example.lineofthree.feedback

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

enum class Haptic { PLACE, MOVE, LINE, REMOVE, WIN }

/** Maps game moments to platform haptic constants, from very light to distinct. */
object Haptics {
    fun perform(view: View, haptic: Haptic) {
        when (haptic) {
            Haptic.PLACE, Haptic.MOVE -> view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            Haptic.LINE -> view.performHapticFeedback(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
                else HapticFeedbackConstants.CONTEXT_CLICK
            )
            Haptic.REMOVE -> view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Haptic.WIN -> {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                view.postDelayed({
                    view.performHapticFeedback(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
                        else HapticFeedbackConstants.VIRTUAL_KEY
                    )
                }, 160)
            }
        }
    }
}
