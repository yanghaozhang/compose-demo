package com.test.compose

/**
 * ==================================================================================
 * TextButtonActivity - Text 和 Button 组件学习示例
 * ==================================================================================
 * 
 * 本文件展示了 Compose 中两个最基础也是最常用的组件：
 * 1. Text - 文本显示组件
 * 2. Button - 按钮组件及其各种变体
 * 
 * 学习要点：
 * - Text 的各种样式和属性（颜色、字体、对齐等）
 * - Button 的不同类型（Filled、Outlined、Text、Icon等）
 * - 如何使用 remember 管理本地状态
 * - 如何响应用户交互（点击事件）
 * - Material3 的样式系统（typography、colorScheme）
 * 
 * ==================================================================================
 */

// ==================== 导入区域 ====================
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke  // BorderStroke：边框描边
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState  // rememberScrollState：滚动状态
import androidx.compose.foundation.verticalScroll  // verticalScroll：垂直滚动
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color  // Color：颜色类
import androidx.compose.ui.text.font.FontStyle  // FontStyle：字体样式（正常、斜体）
import androidx.compose.ui.text.font.FontWeight  // FontWeight：字体粗细
import androidx.compose.ui.text.style.TextAlign  // TextAlign：文本对齐方式
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp  // sp：可缩放像素单位，用于字体大小
import com.test.compose.ui.theme.ComposeTheme

// ==================== Activity ====================

/**
 * TextButtonActivity - Text 和 Button 测试页面
 * 
 * 展示了 Text 和 Button 组件的各种用法
 */
class TextButtonActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("Text & Button 测试") },
                            // navigationIcon：导航图标（通常是返回按钮）
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {  // finish()：关闭当前 Activity
                                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                ) { innerPadding ->
                    TextButtonScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// ==================== 主屏幕 ====================

/**
 * TextButtonScreen - 主屏幕内容
 * 
 * 展示各种 Text 和 Button 的使用示例
 * 
 * 状态管理：
 * - clickCount：点击计数器
 * - selectedButton：最后点击的按钮名称
 */
@Composable
fun TextButtonScreen(modifier: Modifier = Modifier) {
    /**
     * remember + mutableIntStateOf：记住可变的 Int 状态
     * 
     * by 关键字：
     * - Kotlin 的属性委托语法
     * - 自动处理 .value 的读写
     * - var clickCount by xxx 等价于 var clickCount.value = xxx
     */
    var clickCount by remember { mutableIntStateOf(0) }
    var selectedButton by remember { mutableStateOf("None") }
    
    /**
     * Column + verticalScroll：可滚动的垂直布局
     * 
     * 为什么需要 rememberScrollState()：
     * - 滚动需要状态来记住当前位置
     * - remember 确保配置更改时保持滚动位置
     * - verticalScroll() 让 Column 可以滚动
     */
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())  // 启用垂直滚动
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)  // 子元素间距 16dp
    ) {
        
        // ==================== Text 组件示例 ====================
        
        SectionTitle("Text 组件示例")
        
        /**
         * 基础文本
         * 
         * Text 组件：
         * - Compose 中显示文本的基础组件
         * - 替代传统的 TextView
         * - 支持丰富的样式和格式化
         */
        Text(
            text = "这是基础文本",
            style = MaterialTheme.typography.bodyLarge  // 使用主题预定义的样式
        )
        
        /**
         * 大标题文本
         * 
         * typography：Material3 的字体系统
         * - displayLarge, displayMedium, displaySmall
         * - headlineLarge, headlineMedium, headlineSmall
         * - titleLarge, titleMedium, titleSmall
         * - bodyLarge, bodyMedium, bodySmall
         * - labelLarge, labelMedium, labelSmall
         */
        Text(
            text = "大标题文本",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold  // 加粗
        )
        
        /**
         * 中等标题 + 自定义颜色
         * 
         * colorScheme：Material3 的颜色系统
         * - primary, onPrimary
         * - secondary, onSecondary
         * - surface, onSurface
         * - 等等...
         */
        Text(
            text = "中等标题文本",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary  // 主题色
        )
        
        /**
         * 斜体文本
         * 
         * FontStyle.Italic：斜体
         * FontStyle.Normal：正常
         */
        Text(
            text = "斜体文本示例",
            fontStyle = FontStyle.Italic,
            fontSize = 18.sp  // sp：可缩放像素，会随系统字体大小设置变化
        )
        
        /**
         * 居中对齐的文本
         * 
         * TextAlign：文本对齐方式
         * - Center：居中
         * - Start：开始（左对齐，RTL 语言是右对齐）
         * - End：结束
         * - Justify：两端对齐
         */
        Text(
            text = "居中对齐的文本",
            modifier = Modifier.fillMaxWidth(),  // 填充宽度才能看到对齐效果
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary
        )
        
        /**
         * 自动换行的长文本
         * 
         * Text 组件特点：
         * - 默认自动换行
         * - 超出宽度时自动折行
         * - 可以通过 maxLines 限制行数
         */
        Text(
            text = "这是一段很长的文本，用来演示文本换行的效果。在Compose中，Text组件会自动处理文本换行，当文本超出可用宽度时会自动换到下一行。",
            style = MaterialTheme.typography.bodyMedium
        )
        
        /**
         * Divider：分割线组件
         * 用于视觉上分隔内容区域
         */
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        // ==================== Button 组件示例 ====================
        
        SectionTitle("Button 组件示例")
        
        /**
         * 信息卡片 - 显示点击状态
         * 
         * 展示了如何使用状态来动态更新 UI
         */
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                /**
                 * 显示点击次数
                 * 
                 * 注意：当 clickCount 改变时，这个 Text 会自动重组（重新渲染）
                 * 这就是 Compose 的响应式特性
                 */
                Text(
                    text = "按钮点击次数: $clickCount",  // 字符串模板：$变量名
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "最后选择: $selectedButton",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        
        /**
         * Button：标准填充按钮（Filled Button）
         * 
         * Material3 的主要按钮类型，用于最重要的操作
         * 特点：
         * - 填充背景色
         * - 高对比度
         * - 视觉上最突出
         */
        Button(
            onClick = {
                clickCount++  // 增加计数
                selectedButton = "标准按钮"  // 更新选择
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("标准按钮 (Filled)")
        }
        
        /**
         * FilledTonalButton：填充色调按钮
         * 
         * Material3 新增的按钮类型
         * 特点：
         * - 使用色调容器颜色（tonalContainer）
         * - 对比度比 Filled Button 低
         * - 适合次要但仍然重要的操作
         */
        FilledTonalButton(
            onClick = {
                clickCount++
                selectedButton = "填充色调按钮"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("填充色调按钮 (Filled Tonal)")
        }
        
        /**
         * OutlinedButton：轮廓按钮
         * 
         * 特点：
         * - 只有边框，无填充
         * - 对比度更低
         * - 适合次要操作
         */
        OutlinedButton(
            onClick = {
                clickCount++
                selectedButton = "轮廓按钮"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("轮廓按钮 (Outlined)")
        }
        
        /**
         * ElevatedButton：提升按钮
         * 
         * 特点：
         * - 有阴影效果（elevation）
         * - 视觉上"浮"在界面上
         * - 在浅色背景上效果明显
         */
        ElevatedButton(
            onClick = {
                clickCount++
                selectedButton = "提升按钮"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("提升按钮 (Elevated)")
        }
        
        /**
         * TextButton：文本按钮
         * 
         * 特点：
         * - 最低对比度
         * - 无边框无填充
         * - 适合最不重要的操作或链接
         */
        TextButton(
            onClick = {
                clickCount++
                selectedButton = "文本按钮"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("文本按钮 (Text)")
        }
        
        /**
         * 带图标的按钮
         * 
         * 按钮内容是一个 Composable lambda，可以放任何内容
         * 这里放了 Icon + Spacer + Text
         */
        Button(
            onClick = {
                clickCount++
                selectedButton = "带图标按钮"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,  // 装饰性图标可以为 null
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))  // 图标和文字之间的间距
            Text("带图标的按钮")
        }
        
        /**
         * 自定义颜色的按钮
         * 
         * ButtonDefaults.buttonColors：自定义按钮颜色
         * - containerColor：背景色
         * - contentColor：内容（文字、图标）颜色
         */
        Button(
            onClick = {
                clickCount++
                selectedButton = "自定义颜色按钮"
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE),  // 紫色背景
                contentColor = Color.White  // 白色文字
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("自定义颜色按钮")
        }
        
        /**
         * 禁用状态的按钮
         * 
         * enabled = false：
         * - 按钮不可点击
         * - 视觉上变灰
         * - onClick 不会被调用
         */
        Button(
            onClick = { },
            enabled = false,  // 禁用按钮
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("禁用按钮")
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        // ==================== IconButton 示例 ====================
        
        SectionTitle("IconButton 示例")
        
        /**
         * Row 中放置多个 IconButton
         * 
         * SpaceEvenly：均匀分布，包括两端
         */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            /**
             * IconButton：标准图标按钮
             * 
             * 特点：
             * - 只有图标，无背景
             * - 有圆形的涟漪效果
             * - 适合工具栏、应用栏等
             */
            IconButton(
                onClick = {
                    clickCount++
                    selectedButton = "Home图标"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "主页",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            /**
             * FilledIconButton：填充图标按钮
             * 
             * Material3 新增
             * 特点：
             * - 有圆形背景
             * - 更突出
             */
            FilledIconButton(
                onClick = {
                    clickCount++
                    selectedButton = "设置图标"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置"
                )
            }
            
            /**
             * OutlinedIconButton：轮廓图标按钮
             * 
             * 特点：
             * - 有圆形边框
             * - 无填充
             */
            OutlinedIconButton(
                onClick = {
                    clickCount++
                    selectedButton = "收藏图标"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "收藏"
                )
            }
        }
        
        // ==================== 重置按钮 ====================
        
        Spacer(modifier = Modifier.height(16.dp))
        
        /**
         * 重置按钮 - 自定义边框颜色
         * 
         * BorderStroke：边框描边
         * - width：边框宽度
         * - color：边框颜色
         */
        OutlinedButton(
            onClick = {
                clickCount = 0  // 重置计数
                selectedButton = "None"  // 重置选择
            },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)  // 红色边框
        ) {
            Text(
                text = "重置计数器",
                color = MaterialTheme.colorScheme.error  // 红色文字
            )
        }
    }
}

// ==================== 辅助组件 ====================

/**
 * SectionTitle - 区域标题组件
 * 
 * 可重用的标题组件，用于分隔不同的示例区域
 * 
 * @param title 标题文本
 */
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

// ==================== 预览 ====================

/**
 * 预览函数
 * 在 Android Studio 中可以看到实时预览
 */
@Preview(showBackground = true)
@Composable
fun TextButtonScreenPreview() {
    ComposeTheme {
        TextButtonScreen()
    }
}

