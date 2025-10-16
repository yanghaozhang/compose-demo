package com.test.compose.ui.theme

/**
 * ==================================================================================
 * Type.kt - 字体排版系统定义文件
 * ==================================================================================
 * 
 * Material3 Typography（排版）系统：
 * - 预定义了 15 种文本样式
 * - 分为 Display、Headline、Title、Body、Label 五大类
 * - 每类有 Large、Medium、Small 三种尺寸
 * 
 * 学习要点：
 * - Material3 的字体层级系统
 * - TextStyle 的属性配置
 * - 如何自定义字体
 * - sp 单位的使用
 * 
 * 使用方式：
 * Text(
 *     text = "标题",
 *     style = MaterialTheme.typography.headlineLarge
 * )
 * 
 * ==================================================================================
 */

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ==================== Material3 Typography 定义 ====================

/**
 * Typography - 应用的字体排版系统
 * 
 * Material3 提供的 15 种预定义样式：
 * 
 * 📱 Display（展示）- 最大的文字，用于重要标题
 *    - displayLarge   (57sp)
 *    - displayMedium  (45sp)
 *    - displaySmall   (36sp)
 * 
 * 📰 Headline（标题）- 用于章节标题
 *    - headlineLarge  (32sp)
 *    - headlineMedium (28sp)
 *    - headlineSmall  (24sp)
 * 
 * 📝 Title（小标题）- 用于卡片标题、对话框标题
 *    - titleLarge     (22sp)
 *    - titleMedium    (16sp, 加粗)
 *    - titleSmall     (14sp, 加粗)
 * 
 * 📄 Body（正文）- 用于主要内容文字
 *    - bodyLarge      (16sp) ⭐ 默认配置
 *    - bodyMedium     (14sp)
 *    - bodySmall      (12sp)
 * 
 * 🏷️ Label（标签）- 用于按钮、小标签
 *    - labelLarge     (14sp, 加粗)
 *    - labelMedium    (12sp, 加粗)
 *    - labelSmall     (11sp, 加粗)
 */
val Typography = Typography(
    /**
     * bodyLarge - 大号正文样式
     * 
     * 用途：主要内容文字、列表项文字
     * 
     * 参数说明：
     * - fontFamily: 字体家族（Default 是系统默认字体）
     * - fontWeight: 字重（Normal = 400）
     * - fontSize: 字体大小（16sp，可随系统字体大小缩放）
     * - lineHeight: 行高（24sp，影响行间距）
     * - letterSpacing: 字间距（0.5sp）
     */
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,  // 使用系统默认字体
        fontWeight = FontWeight.Normal,   // 正常字重（400）
        fontSize = 16.sp,                 // 字体大小 16sp
        lineHeight = 24.sp,               // 行高 24sp（1.5倍行距）
        letterSpacing = 0.5.sp            // 字间距 0.5sp
    )
    
    // ==================== 如何自定义其他样式 ====================
    
    /*
     * 取消注释以下代码来自定义更多样式：
     */
    
    /* 
    // 大标题样式示例
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    
    // 小标签样式示例
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,  // 中等字重（500）
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

// ==================== TextStyle 属性详解 ====================

/*
 * TextStyle 可配置的主要属性：
 * 
 * 1. fontFamily: FontFamily - 字体家族
 *    - FontFamily.Default       // 系统默认
 *    - FontFamily.SansSerif     // 无衬线
 *    - FontFamily.Serif         // 衬线
 *    - FontFamily.Monospace     // 等宽
 *    - FontFamily.Cursive       // 手写体
 *    - 自定义字体（需要添加字体文件）
 * 
 * 2. fontWeight: FontWeight - 字体粗细
 *    - FontWeight.Thin          // 100
 *    - FontWeight.ExtraLight    // 200
 *    - FontWeight.Light         // 300
 *    - FontWeight.Normal        // 400（默认）
 *    - FontWeight.Medium        // 500
 *    - FontWeight.SemiBold      // 600
 *    - FontWeight.Bold          // 700
 *    - FontWeight.ExtraBold     // 800
 *    - FontWeight.Black         // 900
 * 
 * 3. fontSize: TextUnit - 字体大小
 *    - 使用 sp 单位（可缩放像素）
 *    - 会随系统字体大小设置变化
 *    - 例如：16.sp, 24.sp
 * 
 * 4. fontStyle: FontStyle - 字体样式
 *    - FontStyle.Normal         // 正常
 *    - FontStyle.Italic         // 斜体
 * 
 * 5. lineHeight: TextUnit - 行高
 *    - 控制行与行之间的距离
 *    - 通常是 fontSize 的 1.2-1.5 倍
 * 
 * 6. letterSpacing: TextUnit - 字间距
 *    - 字符之间的额外间距
 *    - 可以是负值（紧凑）或正值（松散）
 * 
 * 7. textAlign: TextAlign - 文本对齐
 *    - TextAlign.Left
 *    - TextAlign.Center
 *    - TextAlign.Right
 *    - TextAlign.Justify
 * 
 * 8. textDecoration: TextDecoration - 文本装饰
 *    - TextDecoration.None
 *    - TextDecoration.Underline     // 下划线
 *    - TextDecoration.LineThrough   // 删除线
 * 
 * 9. color: Color - 文字颜色
 *    - 通常不在 Typography 中设置
 *    - 而是通过 MaterialTheme.colorScheme 动态设置
 */

// ==================== 使用示例 ====================

/*
 * 1. 基本使用：
 * 
 * @Composable
 * fun Example() {
 *     Text(
 *         text = "标题文字",
 *         style = MaterialTheme.typography.headlineLarge
 *     )
 * }
 * 
 * 2. 自定义某些属性：
 * 
 * Text(
 *     text = "自定义文字",
 *     style = MaterialTheme.typography.bodyLarge.copy(
 *         color = Color.Red,        // 只改变颜色
 *         fontWeight = FontWeight.Bold  // 只改变字重
 *     )
 * )
 * 
 * 3. 完全自定义：
 * 
 * Text(
 *     text = "完全自定义",
 *     style = TextStyle(
 *         fontSize = 20.sp,
 *         fontWeight = FontWeight.Bold,
 *         color = Color.Blue
 *     )
 * )
 */

// ==================== 如何添加自定义字体 ====================

/*
 * 1. 将字体文件放到 res/font/ 目录
 *    例如：res/font/roboto_bold.ttf
 * 
 * 2. 创建 FontFamily：
 * 
 * val MyFontFamily = FontFamily(
 *     Font(R.font.roboto_regular, FontWeight.Normal),
 *     Font(R.font.roboto_bold, FontWeight.Bold)
 * )
 * 
 * 3. 在 Typography 中使用：
 * 
 * val Typography = Typography(
 *     bodyLarge = TextStyle(
 *         fontFamily = MyFontFamily,
 *         fontWeight = FontWeight.Normal,
 *         fontSize = 16.sp
 *     )
 * )
 */

// ==================== sp vs dp ====================

/*
 * sp (Scalable Pixels) - 可缩放像素：
 * - 用于字体大小
 * - 会随系统字体大小设置变化
 * - 推荐用于所有文字相关尺寸
 * 
 * dp (Density-independent Pixels) - 密度无关像素：
 * - 用于布局尺寸（宽度、高度、边距等）
 * - 不随系统字体设置变化
 * - 只根据屏幕密度缩放
 * 
 * 示例：
 * fontSize = 16.sp      // 字体大小，用 sp
 * padding = 16.dp       // 内边距，用 dp
 */