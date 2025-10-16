package com.test.compose.ui.theme

/**
 * ==================================================================================
 * Theme.kt - 应用主题定义文件
 * ==================================================================================
 * 
 * 这是整个应用主题系统的核心文件，负责：
 * 1. 组合颜色方案和字体系统
 * 2. 支持深色/浅色主题自动切换
 * 3. 支持 Android 12+ 的动态颜色
 * 4. 为整个应用提供统一的 MaterialTheme
 * 
 * Material3 主题系统架构：
 * - ColorScheme（颜色方案）：定义应用的所有颜色
 * - Typography（字体系统）：定义应用的所有文字样式
 * - Shapes（形状系统）：定义圆角等形状（本项目使用默认）
 * 
 * 学习要点：
 * - MaterialTheme 的作用和组成
 * - 深色/浅色主题适配
 * - 动态颜色（Material You）
 * - 如何在应用中使用主题
 * 
 * ==================================================================================
 */

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme  // 检测系统是否为深色模式
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme      // 深色主题配色方案
import androidx.compose.material3.dynamicDarkColorScheme   // 动态深色配色（Android 12+）
import androidx.compose.material3.dynamicLightColorScheme  // 动态浅色配色（Android 12+）
import androidx.compose.material3.lightColorScheme     // 浅色主题配色方案
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// ==================== 深色主题配色方案 ====================

/**
 * DarkColorScheme - 深色主题的颜色方案
 * 
 * 设计原则：
 * - 使用较亮的颜色（如 Purple80）保持对比度
 * - 背景色较暗，文字和元素较亮
 * - 减少眼睛疲劳，适合夜间使用
 * 
 * Material3 颜色角色：
 * - primary: 主色调，用于最重要的元素（按钮、FAB等）
 * - secondary: 辅助色，用于次要元素
 * - tertiary: 第三色，用于对比和强调
 * - 每种颜色都有对应的 container 和 on 变体
 */
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,         // 主色：亮紫色（深色背景上显眼）
    secondary = PurpleGrey80,   // 辅色：亮紫灰色
    tertiary = Pink80           // 第三色：亮粉色
    
    // 可以自定义更多颜色：
    /* 
    primaryContainer = ...,     // 主色容器背景
    onPrimary = ...,           // 主色上的文字颜色
    onPrimaryContainer = ...,  // 主色容器上的文字颜色
    
    background = ...,          // 应用背景色
    onBackground = ...,        // 背景上的文字颜色
    
    surface = ...,             // 表面色（卡片、对话框等）
    onSurface = ...,           // 表面上的文字颜色
    
    error = ...,               // 错误色
    onError = ...              // 错误色上的文字颜色
    */
)

// ==================== 浅色主题配色方案 ====================

/**
 * LightColorScheme - 浅色主题的颜色方案
 * 
 * 设计原则：
 * - 使用较暗的颜色（如 Purple40）保持对比度
 * - 背景色较亮，文字和元素较暗
 * - 适合白天使用，视觉清晰
 * 
 * 颜色对比：
 * - 深色主题：浅色背景 + 深色元素 = Purple40
 * - 浅色主题：深色背景 + 浅色元素 = Purple80
 */
private val LightColorScheme = lightColorScheme(
    primary = Purple40,         // 主色：深紫色（浅色背景上显眼）
    secondary = PurpleGrey40,   // 辅色：深紫灰色
    tertiary = Pink40           // 第三色：深粉色

    // 更多颜色自定义示例：
    /* 
    background = Color(0xFFFFFBFE),      // 浅色背景
    surface = Color(0xFFFFFBFE),         // 表面色
    onPrimary = Color.White,             // 主色上用白色文字
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),    // 背景上用深色文字
    onSurface = Color(0xFF1C1B1F),
    */
)

// ==================== 主题组合函数 ====================

/**
 * ComposeTheme - 应用主题组合函数
 * 
 * 这是整个应用的主题入口，包裹在最外层
 * 所有子组件都可以通过 MaterialTheme 访问主题资源
 * 
 * 特性：
 * 1. 自动跟随系统深色模式
 * 2. 支持 Android 12+ 动态颜色（Material You）
 * 3. 统一管理颜色和字体
 * 
 * @param darkTheme 是否使用深色主题，默认跟随系统
 * @param dynamicColor 是否启用动态颜色（Android 12+），默认启用
 * @param content 应用的 UI 内容
 * 
 * 使用示例：
 * ```
 * setContent {
 *     ComposeTheme {
 *         // 你的应用内容
 *         MyApp()
 *     }
 * }
 * ```
 */
@Composable
fun ComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),  // 默认跟随系统设置
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,                 // 启用动态颜色
    content: @Composable () -> Unit
) {
    /**
     * 颜色方案选择逻辑：
     * 
     * 1. Android 12+ 且启用动态颜色
     *    -> 使用系统壁纸提取的颜色（Material You）
     * 
     * 2. 深色主题
     *    -> 使用 DarkColorScheme
     * 
     * 3. 浅色主题
     *    -> 使用 LightColorScheme
     * 
     * when 表达式执行顺序从上到下，匹配第一个为 true 的条件
     */
    val colorScheme = when {
        // 优先级 1：动态颜色（Android 12+）
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                // 动态深色方案：从系统壁纸提取颜色
                dynamicDarkColorScheme(context)
            } else {
                // 动态浅色方案
                dynamicLightColorScheme(context)
            }
        }

        // 优先级 2：深色主题
        darkTheme -> DarkColorScheme
        
        // 优先级 3：浅色主题（默认）
        else -> LightColorScheme
    }

    /**
     * MaterialTheme - Material3 的主题提供者
     * 
     * 组成部分：
     * - colorScheme: 颜色方案（上面选择的）
     * - typography: 字体系统（Type.kt 中定义的）
     * - shapes: 形状系统（使用默认的）
     * - content: 子组件内容
     * 
     * 工作原理：
     * 1. MaterialTheme 通过 CompositionLocal 向下传递主题
     * 2. 子组件通过 MaterialTheme.colorScheme 访问颜色
     * 3. 子组件通过 MaterialTheme.typography 访问字体
     */
    MaterialTheme(
        colorScheme = colorScheme,    // 应用颜色方案
        typography = Typography,       // 应用字体系统
        content = content              // 渲染子组件
    )
}

// ==================== 如何使用主题 ====================

/*
 * 1. 在应用入口使用：
 * 
 * class MainActivity : ComponentActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContent {
 *             ComposeTheme {              // 包裹整个应用
 *                 Surface {               // 提供背景色
 *                     MyApp()
 *                 }
 *             }
 *         }
 *     }
 * }
 * 
 * 2. 访问主题颜色：
 * 
 * @Composable
 * fun MyButton() {
 *     Button(
 *         colors = ButtonDefaults.buttonColors(
 *             containerColor = MaterialTheme.colorScheme.primary,
 *             contentColor = MaterialTheme.colorScheme.onPrimary
 *         )
 *     ) {
 *         Text("按钮")
 *     }
 * }
 * 
 * 3. 访问主题字体：
 * 
 * Text(
 *     text = "标题",
 *     style = MaterialTheme.typography.headlineLarge
 * )
 * 
 * 4. 强制使用深色/浅色主题：
 * 
 * ComposeTheme(darkTheme = true) {  // 强制深色
 *     // ...
 * }
 * 
 * ComposeTheme(darkTheme = false) {  // 强制浅色
 *     // ...
 * }
 * 
 * 5. 禁用动态颜色：
 * 
 * ComposeTheme(dynamicColor = false) {
 *     // 使用固定的 Purple40/Purple80 配色
 * }
 */

// ==================== Material3 颜色角色完整列表 ====================

/*
 * ColorScheme 包含的所有颜色角色：
 * 
 * 主色系（Primary）：
 * - primary              // 主色
 * - onPrimary            // 主色上的内容色
 * - primaryContainer     // 主色容器
 * - onPrimaryContainer   // 主色容器上的内容色
 * 
 * 辅色系（Secondary）：
 * - secondary
 * - onSecondary
 * - secondaryContainer
 * - onSecondaryContainer
 * 
 * 第三色系（Tertiary）：
 * - tertiary
 * - onTertiary
 * - tertiaryContainer
 * - onTertiaryContainer
 * 
 * 错误色系（Error）：
 * - error
 * - onError
 * - errorContainer
 * - onErrorContainer
 * 
 * 背景色系（Background）：
 * - background           // 应用背景
 * - onBackground         // 背景上的文字
 * 
 * 表面色系（Surface）：
 * - surface              // 卡片、对话框等表面
 * - onSurface            // 表面上的文字
 * - surfaceVariant       // 表面变体
 * - onSurfaceVariant     // 表面变体上的文字
 * 
 * 其他：
 * - outline              // 边框色
 * - outlineVariant       // 边框变体色
 * - scrim                // 遮罩层颜色
 * - inverseSurface       // 反色表面
 * - inverseOnSurface     // 反色表面上的内容
 * - inversePrimary       // 反色主色
 * - surfaceTint          // 表面着色
 */

// ==================== 动态颜色（Material You）说明 ====================

/*
 * 动态颜色（Android 12+）：
 * 
 * 特性：
 * - 自动从用户壁纸提取颜色
 * - 生成协调的配色方案
 * - 让应用融入系统美学
 * 
 * 工作原理：
 * 1. 系统分析用户壁纸的主色调
 * 2. 生成一套协调的颜色方案
 * 3. 应用使用这些颜色替代默认配色
 * 
 * 兼容性：
 * - Android 12 (API 31) 及以上支持
 * - 低版本自动降级到静态配色
 * 
 * 如何测试：
 * 1. 在 Android 12+ 设备上运行
 * 2. 更换壁纸
 * 3. 观察应用颜色变化
 */

// ==================== 自定义主题提示 ====================

/*
 * 如何创建自己的主题：
 * 
 * 1. 修改 Color.kt：
 *    - 定义你的品牌颜色
 *    - 创建深色和浅色变体
 * 
 * 2. 修改 Theme.kt：
 *    - 在 lightColorScheme 中使用你的颜色
 *    - 在 darkColorScheme 中使用对应的深色变体
 * 
 * 3. 修改 Type.kt：
 *    - 自定义字体家族
 *    - 调整各级字体大小
 * 
 * 4. 测试：
 *    - 在浅色和深色模式下测试
 *    - 确保对比度足够
 *    - 检查可访问性
 * 
 * 推荐工具：
 * - Material Theme Builder: https://material-foundation.github.io/material-theme-builder/
 *   可视化创建 Material3 主题并导出代码
 */