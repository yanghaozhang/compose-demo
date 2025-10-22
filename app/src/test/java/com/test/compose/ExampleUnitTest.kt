package com.test.compose

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.abs
import kotlin.math.max

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testCode() {
        println(twoSum(listOf(2, 7, 11, 15).toIntArray(), 9).contentToString())
        println(twoSum(listOf(3, 2, 4).toIntArray(), 6).contentToString())
        println(twoSum(listOf(3, 3).toIntArray(), 6).contentToString())
    }

    fun twoSum(nums: IntArray, target: Int): IntArray {
        val length = nums.size
        for (indexA in 0 until length) {
            for (indexB in indexA + 1 until length) {
                val itemA = nums[indexA]
                val itemB = nums[indexB]
                if (itemA + itemB == target) {
                    return listOf(indexA, indexB).toIntArray()
                }
            }
        }
        return IntArray(2)
    }


    fun containsDuplicate(nums: IntArray): Boolean {
        val set = mutableSetOf<Int>()

        var count = 0
        for (item in nums) {
            set.add(item)
            count++
            if (set.size != count) {
                return true
            }
        }

        return false
    }

    fun intersect(nums1: IntArray, nums2: IntArray): IntArray {
        val result = mutableListOf<Int>()
        val map = mutableMapOf<Int, Int>()
        for (i in nums1) {
            map[i] = map.getOrDefault(i, 0) + 1
        }
        for (i in nums2) {
            val count = map[i] ?: 0
            if (count > 0) {
                result.add(i)
                map[i] = map.getOrDefault(i, 0) - 1
            }
        }
        return result.toIntArray()
    }

    fun sortString(s: CharArray): Unit {
        var i = 1
        while (i < s.size) {
            if (i < 1) {
                i++
            } else if (s[i] < s[i - 1]) {
                val c = s[i]
                s[i] = s[i - 1]
                s[i - 1] = c
                i--
            } else {
                i++
            }
        }
    }

    fun reverseString(s: CharArray): Unit {
        for (i in 0 until s.size / 2) {
            val c = s[i]
            s[i] = s[s.size - i]
            s[s.size - i] = c
        }

        var left = 0
        var right = s.size - 1
        while (left < right) {
            val c = s[left]
            s[left] = s[right]
            s[right] = c
            left++
            right--
        }
    }

    fun isPalindrome(s: String): Boolean {
        var left = 0
        var right = s.length - 1
        val cut = abs('A' - 'a')
        while (left < right) {
            val leftChat = s[left]
            val rightChat = s[right]
            if (!isNumOrLetter(leftChat)) {
                left++
            } else if (!isNumOrLetter(rightChat)) {
                right--
            } else if (
                (isNum(leftChat) && leftChat == rightChat)
                ||
                (!isNum(leftChat) && !isNum(rightChat) && (leftChat == rightChat || abs(leftChat - rightChat) == cut))
            ) {
                left++
                right--
            } else {
                return false
            }
        }
        return true
    }

    private fun isNum(c: Char): Boolean {
        return c in '0'..'9'
    }

    private fun isLetter(c: Char): Boolean {
        return (c in 'a'..'z') || (c in 'A'..'Z')
    }

    private fun isNumOrLetter(c: Char): Boolean {
        return isNum(c) || isLetter(c)
    }

    fun lengthOfLongestSubstring(s: String): Int {
        var maxLength = 0
        var left = 0
        var right = 0
        while (right < s.length && left < s.length) {
            val leftChat = s[left]
            val rightChat = s[right]
            if (left >= right) {
                right++
                maxLength = max(maxLength, 1)
            } else if (leftChat == rightChat) {
                left++
            } else if (right < s.length - 1) {
                val str = s.substring(left, right + 1)
                val nextChar = str.indexOf(s[right + 1])
                if (nextChar >= 0) {
                    maxLength = max(maxLength, right - left + 1)
                    left += nextChar + 2
                    right++
                } else {
                    right++
                }
                maxLength = max(maxLength, right - left + 1)
            } else {
                maxLength = max(maxLength, right - left + 1)
                break
            }
        }
        return maxLength
    }

    @Test
    fun lengthOfLongestSubstring2Test() {
        println(lengthOfLongestSubstring2("bbba"))
    }

    fun lengthOfLongestSubstring2(s: String): Int {
        val set = hashSetOf<Int>()
        var maxLength = 0
        var right = 0
        for ((left, c) in s.withIndex()) {
            while (right < s.length && set.add(s[right].code)) {
                right++
            }
            maxLength = max(maxLength, right - left)
            set.remove(c.code)
        }
        return maxLength
    }

    fun isValid(s: String): Boolean {
        val list = mutableListOf<Int>()
        for (c in s) {
            val num = sToNum(c)
            if (num < 0 && (list.isEmpty() || list.last() != -num)) {
                return false
            }
            if (num > 0) {
                list.add(num)
            } else {
                list.removeLast()
            }
        }
        return list.isEmpty()
    }

    fun sToNum(c: Char): Int {
        return when (c) {
            '(' -> 10
            ')' -> -10
            '{' -> 100
            '}' -> -100
            '[' -> 1000
            ']' -> -1000
            else -> 0
        }
    }
}