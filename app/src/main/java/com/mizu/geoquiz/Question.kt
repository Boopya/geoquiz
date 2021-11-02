package com.mizu.geoquiz

import androidx.annotation.StringRes

data class Question(@StringRes val textResId: Int,
                    val answer: Boolean,
                    var isAnswered: Boolean = false,
                    var isCheated: Boolean = false)
