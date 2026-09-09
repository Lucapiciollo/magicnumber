package com.magicnumber.app.activity

import java.util.Random

internal fun <T> Iterable<T>.shuffled(random: Random): List<T> {
    val result = toMutableList()
    for (index in result.lastIndex downTo 1) {
        val swapIndex = random.nextInt(index + 1)
        val value = result[index]
        result[index] = result[swapIndex]
        result[swapIndex] = value
    }
    return result
}
