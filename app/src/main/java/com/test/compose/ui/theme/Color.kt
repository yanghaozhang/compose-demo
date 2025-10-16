package com.test.compose.ui.theme

/**
 * ==================================================================================
 * Color.kt - 颜色定义文件
 * ==================================================================================
 * 
 * Material3 颜色系统：
 * - 使用色调变体（Tonal Variants）
 * - 支持深色和浅色主题
 * - 80 = 浅色主题使用（亮度 80%）
 * - 40 = 深色主题使用（亮度 40%）
 * 
 * 颜色命名规则：
 * - 颜色名 + 亮度值
 * - 例如：Purple80 表示紫色，亮度 80%
 * 
 * 学习要点：
 * - Material3 的色调系统
 * - 深色/浅色主题适配
 * - Color 构造函数的使用
 * 
 * ==================================================================================
 */

import androidx.compose.ui.graphics.Color

// ==================== 浅色主题颜色（亮度较高） ====================

/**
 * Purple80 - 浅色主题的主色调
 * 
 * 颜色值：0xFFD0BCFF
 * - 0xFF：Alpha 通道（完全不透明）
 * - D0BCFF：RGB 值（紫色，高亮度）
 * 
 * 用途：在浅色主题中作为 primary、primaryContainer 等
 */
val Purple80 = Color(0xFFD0BCFF)

/**
 * PurpleGrey80 - 浅色主题的辅助色
 * 
 * 用途：secondary 相关颜色
 */
val PurpleGrey80 = Color(0xFFCCC2DC)

/**
 * Pink80 - 浅色主题的第三色调
 * 
 * 用途：tertiary 相关颜色
 */
val Pink80 = Color(0xFFEFB8C8)

// ==================== 深色主题颜色（亮度较低） ====================

/**
 * Purple40 - 深色主题的主色调
 * 
 * 颜色值：0xFF6650a4
 * - 亮度更低（40%），适合深色背景
 * 
 * 用途：在深色主题中作为 primary、primaryContainer 等
 * 
 * Material3 设计原则：
 * - 深色主题使用较暗的颜色
 * - 避免过亮导致眼睛疲劳
 * - 保持足够的对比度
 */
val Purple40 = Color(0xFF6650a4)

/**
 * PurpleGrey40 - 深色主题的辅助色
 */
val PurpleGrey40 = Color(0xFF625b71)

/**
 * Pink40 - 深色主题的第三色调
 */
val Pink40 = Color(0xFF7D5260)

// ==================== 如何使用这些颜色 ====================

/*
 * 在 Theme.kt 中：
 * 
 * lightColorScheme(
 *     primary = Purple40,        // 浅色主题用深色（对比度高）
 *     secondary = PurpleGrey40,
 *     tertiary = Pink40
 * )
 * 
 * darkColorScheme(
 *     primary = Purple80,        // 深色主题用浅色（对比度高）
 *     secondary = PurpleGrey80,
 *     tertiary = Pink80
 * )
 * 
 * 在代码中使用：
 * 
 * Text(
 *     text = "Hello",
 *     color = MaterialTheme.colorScheme.primary  // 自动根据主题选择颜色
 * )
 */

// ==================== Color 构造函数说明 ====================

/*
 * Color 有多种构造方式：
 * 
 * 1. 十六进制（最常用）：
 *    Color(0xFFRRGGBB)
 *    Color(0xAARRGGBB)  // AA 是 alpha 通道
 * 
 * 2. RGB 分量（0-255）：
 *    Color(red = 255, green = 0, blue = 0)
 * 
 * 3. RGBA 分量（0.0-1.0）：
 *    Color(red = 1f, green = 0f, blue = 0f, alpha = 1f)
 * 
 * 4. 预定义颜色：
 *    Color.Red
 *    Color.Blue
 *    Color.White
 *    Color.Transparent
 * 
 * 示例：
 *    val myRed = Color(0xFFFF0000)      // 纯红色
 *    val myBlue = Color(red = 0, green = 0, blue = 255)
 *    val myGreen = Color(0f, 1f, 0f)
 *    val transparent = Color(0x80FFFFFF) // 半透明白色
 */