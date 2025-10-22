package com.test.compose

import java.util.Arrays
import java.util.LinkedList
import kotlin.math.max
import kotlin.math.min

/**
 * 文件描述：
 * 创建人：YangHaoZhang
 * 创建时间：2025/10/19 14:34
 */
class UnitTest2 {

    private val stack = LinkedList<Pair<Int, Int>>()

    fun MinStack() {
    }

    fun push(`val`: Int) {
        if (stack.isEmpty()) {
            stack.push(`val` to `val`)
        } else {
            stack.push(`val` to min(`val`, getMin()))
        }
    }

    fun pop() {
        stack.pop()
    }

    fun top(): Int {
        return stack.peek().first
    }

    fun getMin(): Int {
        return stack.peek().second
    }

    class ListNode(var `val`: Int) {
        var next: ListNode? = null
    }

    fun reverseList(head: ListNode?): ListNode? {
        if (head == null) {
            return null
        }
        var pre: ListNode? = null
        var cur = head
        while (cur != null) {
            val curNode = cur
            cur = cur.next
            curNode.next = pre
            pre = curNode
        }
        return pre
    }

    fun mergeTwoLists(list1: ListNode?, list2: ListNode?): ListNode? {
        if (list1 == null || list2 == null) {
            return list1 ?: list2
        }
//        var first: ListNode? = null
//        var pre: ListNode? = null
//        var cur1 = list1
//        var cur2 = list2
//        while (cur1 != null || cur2 != null) {
//            if (cur1 == null) {
//                pre?.next = cur2
//                pre = cur2
//                cur2 = cur2?.next
//            } else if (cur2 == null) {
//                pre?.next = cur1
//                pre = cur1
//                cur1 = cur1.next
//            } else {
//                if (cur1.`val` < cur2.`val`) {
//                    if (first == null) {
//                        first = cur1
//                    }
//                    pre?.next = cur1
//                    pre = cur1
//                    cur1 = cur1.next
//                } else {
//                    if (first == null) {
//                        first = cur2
//                    }
//                    pre?.next = cur2
//                    pre = cur2
//                    cur2 = cur2.next
//                }
//            }
//        }

        val result: ListNode
        if (list1.`val` < list2.`val`) {
            result = list1
            result.next = mergeTwoLists(list1.next, list2)
        } else {
            result = list2
            result.next = mergeTwoLists(list1, list2.next)
        }
        return result
    }

    fun moveZeroes(nums: IntArray): Unit {
//        var leftIndex = 0
//        var rightIndex = 0
//        while (rightIndex < nums.size) {
//            while (rightIndex < nums.size && nums[rightIndex] == 0) {
//                rightIndex++
//            }
//            if (rightIndex >= nums.size) {
//                break
//            }
//            if (leftIndex != rightIndex) {
//                nums[leftIndex] = nums[rightIndex]
//                nums[rightIndex] = 0
//            }
//            leftIndex++
//            rightIndex++
//        }

        var leftIndex = 0
        var rightIndex = 0
        while (rightIndex < nums.size) {
            if (nums[rightIndex] == 0) {
                rightIndex++
            } else if (leftIndex == rightIndex) {
                leftIndex++
                rightIndex++
            } else {
                nums[leftIndex] = nums[rightIndex]
                nums[rightIndex] = 0
                leftIndex++
                rightIndex++
            }
        }

    }

    fun maxArea(height: IntArray): Int {
        var max = Int.MIN_VALUE
        var left = 0
        var right = height.size - 1
        while (left < right) {
            max = max(max, min(height[left], height[right]) * (right - left))
            if (height[left] < height[right]) {
                left++
            } else {
                right--
            }
        }
        return max
    }

    fun checkInclusion(s1: String, s2: String): Boolean {
        val diffArray = IntArray(26)
        for (n in s1.indices) {
            ++diffArray[s1[n] - 'a']
        }
        var diff = s1.length
        for ((index, charAdd) in s2.withIndex()) {
            val countAdd = diffArray[charAdd - 'a']
            if (countAdd > 0) {
                diff--
            } else {
                diff++
            }
            diffArray[charAdd - 'a'] = countAdd - 1
            if (index >= s1.length) {
                val charCut = s2[index - s1.length]
                val countCut = diffArray[charCut - 'a']
                if (countCut < 0) {
                    diff--
                } else {
                    diff++
                }
                diffArray[charCut - 'a'] = countCut + 1
            }
            if (diff == 0) {
                return true
            }
        }
        return false
    }

    fun numIslands(grid: Array<CharArray>): Int {
        var count = 0
        for ((r, rows) in grid.withIndex()) {
            for ((c, column) in rows.withIndex()) {
                if (column == '1') {
                    count++
                    makeArroundLandsChang(grid, r, c)
                }
            }
        }
        return count
    }

    private fun makeArroundLandsChang(grid: Array<CharArray>, r: Int, c: Int) {
        if (r < 0 || r >= grid.size) {
            return
        }
        if (c < 0 || c >= grid[0].size) {
            return
        }
        if (grid[r][c] != '1') {
            return
        }
        grid[r][c] = '2'
        makeArroundLandsChang(grid, r - 1, c)
        makeArroundLandsChang(grid, r + 1, c)
        makeArroundLandsChang(grid, r, c - 1)
        makeArroundLandsChang(grid, r, c + 1)
    }

    fun maxDepth(root: TreeNode?): Int {
        return getDeep(root, 0)
    }

    fun getDeep(root: TreeNode?, deep: Int): Int {
        if (root == null) {
            return deep
        }
        val left = getDeep(root.left, deep + 1)
        val right = getDeep(root.right, deep + 1)
        return max(left, right)
    }

    class TreeNode(var `val`: Int) {
        var left: TreeNode? = null
        var right: TreeNode? = null
    }

    fun climbStairs(n: Int): Int {
        var nCut2 = 0
        var nCut1 = 0
        var count = 0
        for (i in 0..n) {
            when (i) {
                0 -> Unit
                1 -> count = 1
                2 -> {
                    count = 2
                    nCut1 = 2
                    nCut2 = 1
                }

                else -> {
                    val add12 = nCut1 + nCut2
                    count = add12
                    nCut2 = nCut1
                    nCut1 = add12
                }
            }
        }

        return count
    }


    fun maxSubArray(nums: IntArray): Int {
        var countLeft = 0
        var max = nums[0]
        for ((index, i) in nums.withIndex()) {
            if (countLeft <= 0) {
                countLeft = i
            } else if (i < 0) {
                countLeft += i
                if (countLeft < 0) {
                    countLeft = 0
                }
            } else if (i > 0) {
                countLeft += i
            }
            max = max(max, countLeft)
        }
        return max
    }

    fun rob(nums: IntArray): Int {
        if (nums.isEmpty()) {
            return 0
        }
        var last2 = 0
        var last1 = nums[0]
        var cur = last1
        for (k in 2..nums.size) {
            cur = max(last2 + nums[k - 1], last1)
            last2 = last1
            last1 = cur
        }
        return cur
    }

    fun coinChange(coins: IntArray, amount: Int): Int {
        val max = amount + 1
        val dp = IntArray(amount + 1)
        Arrays.fill(dp, max)
        dp[0] = 0
        for (i in 1 .. amount) {
            for (coin in coins) {
                if (coin <= i) {
                    dp[i] = min(dp[i], dp[i - coin] + 1)
                }
            }
        }
        return if (dp[amount] > amount) -1 else dp[amount]
    }
}