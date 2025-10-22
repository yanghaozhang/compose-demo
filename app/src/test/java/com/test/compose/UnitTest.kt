package com.test.compose

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 文件描述：
 * 创建人：YangHaoZhang
 * 创建时间：2025/10/20 16:39
 */
class UnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    /**
     * Merge overlapping interval and return the merged list
     *          Input: [[1,3],[2,6],[8,10],[15,18]]
     *          Output:  [[1,6],[8,10],[15,18]]
     */
    @Test
    fun test() {
        println(
            interval(
                listOf(
                    15 to 18,
                    1 to 3,
                    2 to 6,
                    8 to 10,
                    3 to 5,
                )
            )
        )
    }

    fun interval(list: List<Pair<Int, Int>>): MutableList<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        var temp: Pair<Int, Int>? = null
        for (pair in list.sortedBy {
            it.first
        }) {
            if (temp == null) {
                temp = pair.also {
                    result.add(it)
                }
            } else if (pair.first > temp.second) {
                temp = pair
                result.add(temp)
            } else if (pair.second > temp.second) {
                val newPair = Pair(temp.first, pair.second)
                result.remove(temp)
                result.add(newPair)
                temp = newPair
            }
        }
        return result
    }


}