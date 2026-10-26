package org.personal.todo.common.extentions

import java.math.BigDecimal
import java.math.RoundingMode

/** Rounds a monetary amount to 2 decimal places (half-up), e.g. 1003.32080328 -> 1003.32. */
fun Double.toMoney(): Double = BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP).toDouble()
