package com.test.compose

/**
 * ==================================================================================
 * InteropActivity - Compose 与传统 View 互操作示例
 * ==================================================================================
 * 
 * 本文件展示了 Jetpack Compose 与传统 Android View 系统的混合使用
 * 
 * 核心概念：
 * 1. AndroidView - 在 Compose 中使用传统 View
 * 2. ComposeView - 在传统 View 中使用 Compose
 * 3. 双向数据传递和事件处理
 * 4. 生命周期管理
 * 
 * 互操作场景：
 * ┌─────────────────────────────────────────────────────────────┐
 * │  Compose 中嵌入 View     │  AndroidView { }                 │
 * │  View 中嵌入 Compose     │  ComposeView().setContent { }   │
 * │  混合布局                │  同时使用两种系统                │
 * └─────────────────────────────────────────────────────────────┘
 * 
 * 学习要点：
 * - 渐进式迁移策略（逐步从 View 迁移到 Compose）
 * - 重用现有的自定义 View
 * - 使用第三方库（可能还不支持 Compose）
 * - 性能优化和注意事项
 * 
 * ==================================================================================
 */

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.test.compose.ui.theme.ComposeTheme
import com.test.compose.ui.BulletPoint
import com.test.compose.ui.InfoCard
import com.test.compose.ui.DemoCard
import com.test.compose.ui.CodeCard

// ==================== Activity ====================

class InteropActivity : ComponentActivity() {
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
                            title = { Text("Compose & View 互操作") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
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
                    InteropScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// ==================== 主屏幕 ====================

@Composable
fun InteropScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Compose→View", "View→Compose", "双向交互", "实用案例")
    
    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        when (selectedTab) {
            0 -> ComposeToViewDemo()
            1 -> ViewToComposeDemo()
            2 -> BidirectionalDemo()
            3 -> PracticalExamples()
        }
    }
}

// ==================== Tab 1: Compose 中使用 View ====================

/**
 * AndroidView - 在 Compose 中嵌入传统 View
 * 
 * 语法：
 * AndroidView(
 *     factory = { context -> 
 *         // 创建 View 实例
 *         TextView(context)
 *     },
 *     update = { view ->
 *         // 更新 View（当状态改变时）
 *         view.text = "新文本"
 *     }
 * )
 * 
 * 参数说明：
 * - factory: 创建 View 的工厂函数（只调用一次）
 * - update: 更新 View 的回调（状态改变时调用）
 * - modifier: Compose 修饰符
 */
@Composable
fun ComposeToViewDemo() {
    var textContent by remember { mutableStateOf("Hello from Compose!") }
    var sliderValue by remember { mutableFloatStateOf(50f) }
    var isChecked by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableIntStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 说明卡片
        InfoCard(
            title = "AndroidView",
            description = "在 Compose 中使用传统的 Android View"
        ) {
            BulletPoint("factory 参数：创建 View 实例（只执行一次）")
            BulletPoint("update 参数：更新 View（状态改变时执行）")
            BulletPoint("可以使用任何传统 View：TextView、Button、WebView 等")
            BulletPoint("适用场景：重用现有 View、使用第三方 View 库")
        }
        
        // 示例 1: 传统 TextView
        DemoCard(title = "示例 1: 传统 TextView") {
            Text(
                text = "Compose 控制传统 TextView 的内容",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Compose TextField
            OutlinedTextField(
                value = textContent,
                onValueChange = { textContent = it },
                label = { Text("修改文本") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 传统 TextView（通过 AndroidView 嵌入）
            AndroidView(
                factory = { context ->
                    /**
                     * factory: 创建 View 的地方
                     * - 参数 context: Android Context
                     * - 返回值: View 实例
                     * - 只会执行一次
                     */
                    TextView(context).apply {
                        // 初始化配置
                        textSize = 18f
                        setPadding(32, 32, 32, 32)
                        gravity = Gravity.CENTER
                        setBackgroundColor(Color.parseColor("#E3F2FD"))
                        setTextColor(Color.parseColor("#1976D2"))
                    }
                },
                update = { textView ->
                    /**
                     * update: 更新 View 的地方
                     * - 参数 textView: factory 创建的 View 实例
                     * - 当 textContent 改变时，这个 lambda 会重新执行
                     */
                    textView.text = textContent
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // 示例 2: SeekBar (滑块)
        DemoCard(title = "示例 2: 传统 SeekBar") {
            Text(
                text = "当前值: ${sliderValue.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            AndroidView(
                factory = { context ->
                    SeekBar(context).apply {
                        max = 100
                        // 设置监听器，当用户拖动时更新 Compose 状态
                        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                                if (fromUser) {
                                    sliderValue = progress.toFloat()
                                }
                            }
                            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                        })
                    }
                },
                update = { seekBar ->
                    // 从 Compose 更新 SeekBar 的进度
                    seekBar.progress = sliderValue.toInt()
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Compose Slider 对比
            Text(
                text = "对比：Compose Slider",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // 示例 3: CheckBox 和 RadioButton
        DemoCard(title = "示例 3: 传统 CheckBox & RadioGroup") {
            // CheckBox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("传统 CheckBox:", modifier = Modifier.weight(1f))
                
                AndroidView(
                    factory = { context ->
                        CheckBox(context).apply {
                            setOnCheckedChangeListener { _, checked ->
                                isChecked = checked
                            }
                        }
                    },
                    update = { checkBox ->
                        checkBox.isChecked = isChecked
                    }
                )
                
                Text(if (isChecked) "已选中" else "未选中")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // RadioGroup
            Text("传统 RadioGroup:")
            
            AndroidView(
                factory = { context ->
                    RadioGroup(context).apply {
                        orientation = RadioGroup.VERTICAL
                        setPadding(16, 16, 16, 16)
                        
                        // 添加选项
                        val options = listOf("选项 A", "选项 B", "选项 C")
                        options.forEachIndexed { index, text ->
                            val radioButton = RadioButton(context).apply {
                                id = index
                                this.text = text
                                textSize = 16f
                            }
                            addView(radioButton)
                        }
                        
                        // 设置监听器
                        setOnCheckedChangeListener { _, checkedId ->
                            selectedOption = checkedId
                        }
                    }
                },
                update = { radioGroup ->
                    radioGroup.check(selectedOption)
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "当前选择: 选项 ${('A'.code + selectedOption).toChar()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // 代码示例
        CodeCard(
            title = "AndroidView 代码示例",
            code = """
AndroidView(
    factory = { context ->
        // 1. 创建 View（只执行一次）
        TextView(context).apply {
            textSize = 18f
            setPadding(32, 32, 32, 32)
        }
    },
    update = { view ->
        // 2. 更新 View（状态改变时执行）
        view.text = composeState
    },
    modifier = Modifier.fillMaxWidth()
)
            """.trimIndent()
        )
    }
}

// ==================== Tab 2: View 中使用 Compose ====================

/**
 * ComposeView - 在传统 View 中嵌入 Compose
 * 
 * 使用方式：
 * 1. 在 XML 中声明 ComposeView
 * 2. 在代码中调用 setContent { } 设置 Compose UI
 * 
 * 这对于渐进式迁移非常有用！
 */
@Composable
fun ViewToComposeDemo() {
    var counter by remember { mutableIntStateOf(0) }
    var viewMessage by remember { mutableStateOf("来自 View 的消息") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoCard(
            title = "ComposeView",
            description = "在传统 View 中使用 Compose UI"
        ) {
            BulletPoint("在 XML 布局中添加 <ComposeView> 标签")
            BulletPoint("在代码中调用 composeView.setContent { }")
            BulletPoint("适用场景：渐进式迁移、部分页面使用 Compose")
            BulletPoint("可以在 Fragment、RecyclerView 中使用")
        }
        
        // 示例: 模拟 View 布局中嵌入 Compose
        DemoCard(title = "示例: LinearLayout 中嵌入 Compose") {
            Text(
                text = "下面的内容在传统 LinearLayout 中",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 使用 AndroidView 创建传统布局，其中包含 ComposeView
            AndroidView(
                factory = { context ->
                    LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(32, 32, 32, 32)
                        setBackgroundColor(Color.parseColor("#FFF3E0"))
                        
                        // 1. 传统 TextView
                        addView(TextView(context).apply {
                            text = "⬇️ 这是传统 TextView"
                            textSize = 16f
                            setTextColor(Color.parseColor("#E65100"))
                            gravity = Gravity.CENTER
                        })
                        
                        // 2. ComposeView（在 View 中嵌入 Compose）
                        addView(ComposeView(context).apply {
                            setContent {
                                ComposeTheme {
                                    // 这里是 Compose UI！
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "✨ 这是 Compose UI",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "嵌入在 LinearLayout 中",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            Button(onClick = { counter++ }) {
                                                Text("点击次数: $counter")
                                            }
                                        }
                                    }
                                }
                            }
                        })
                        
                        // 3. 又是传统 TextView
                        addView(TextView(context).apply {
                            text = "⬆️ 上面是 Compose Card"
                            textSize = 16f
                            setTextColor(Color.parseColor("#E65100"))
                            gravity = Gravity.CENTER
                        })
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // XML 示例
        CodeCard(
            title = "XML 布局示例",
            code = """
<!-- activity_main.xml -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">
    
    <!-- 传统 TextView -->
    <TextView
        android:text="传统 View"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
    
    <!-- ComposeView -->
    <androidx.compose.ui.platform.ComposeView
        android:id="@+id/compose_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
    
    <!-- 传统 Button -->
    <Button
        android:text="传统按钮"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</LinearLayout>
            """.trimIndent()
        )
        
        CodeCard(
            title = "Kotlin 代码示例",
            code = """
// Activity 或 Fragment 中
val composeView = findViewById<ComposeView>(R.id.compose_view)
composeView.setContent {
    ComposeTheme {
        // Compose UI
        Text("Hello from Compose!")
        Button(onClick = { }) {
            Text("Compose Button")
        }
    }
}
            """.trimIndent()
        )
    }
}

// ==================== Tab 3: 双向交互 ====================

@Composable
fun BidirectionalDemo() {
    // 共享状态
    var sharedCounter by remember { mutableIntStateOf(0) }
    var sharedMessage by remember { mutableStateOf("双向数据绑定") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoCard(
            title = "双向数据交互",
            description = "Compose 和 View 之间的数据和事件传递"
        ) {
            BulletPoint("使用 remember 和 mutableStateOf 管理共享状态")
            BulletPoint("View 通过回调更新 Compose 状态")
            BulletPoint("Compose 通过 update lambda 更新 View")
            BulletPoint("实现真正的双向数据流")
        }
        
        // 状态显示
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
                Text(
                    text = "共享状态",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "计数: $sharedCounter",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "消息: $sharedMessage",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        // Compose 控制区
        DemoCard(title = "Compose 控制区") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { sharedCounter++ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compose: 增加计数")
                }
                
                OutlinedTextField(
                    value = sharedMessage,
                    onValueChange = { sharedMessage = it },
                    label = { Text("Compose: 修改消息") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // View 控制区
        DemoCard(title = "传统 View 控制区") {
            AndroidView(
                factory = { context ->
                    LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(0, 0, 0, 0)
                        
                        // Button: 修改计数
                        val button = android.widget.Button(context).apply {
                            text = "View: 减少计数"
                            textSize = 14f
                            // View 通过回调更新 Compose 状态
                            setOnClickListener {
                                sharedCounter--
                            }
                        }
                        addView(button, LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ))
                        
                        // EditText: 修改消息
                        val editText = EditText(context).apply {
                            hint = "View: 输入消息"
                            textSize = 16f
                            setPadding(32, 32, 32, 32)
                            // View 通过回调更新 Compose 状态
                            addTextChangedListener(object : android.text.TextWatcher {
                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                    sharedMessage = s.toString()
                                }
                                override fun afterTextChanged(s: android.text.Editable?) {}
                            })
                        }
                        addView(editText, LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = 16
                        })
                    }
                },
                update = { layout ->
                    // Compose 状态改变时更新 View
                    val editText = layout.getChildAt(1) as? EditText
                    if (editText?.text.toString() != sharedMessage) {
                        editText?.setText(sharedMessage)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // 数据流图示
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "数据流向",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("🔄 Compose State ↔️ AndroidView", style = MaterialTheme.typography.bodyMedium)
                Text("   ├─ State 改变 → update lambda 执行", style = MaterialTheme.typography.bodySmall)
                Text("   └─ View 事件 → 回调更新 State", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ==================== Tab 4: 实用案例 ====================

@Composable
fun PracticalExamples() {
    var webUrl by remember { mutableStateOf("https://www.example.com") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoCard(
            title = "实用案例",
            description = "常见的 Compose 与 View 混合使用场景"
        ) {
            BulletPoint("WebView: 显示网页内容")
            BulletPoint("第三方库: 地图、视频播放器等")
            BulletPoint("自定义 View: 重用现有的复杂自定义控件")
            BulletPoint("RecyclerView: 在列表项中使用 Compose")
        }
        
        // 案例 1: WebView
        DemoCard(title = "案例 1: WebView") {
            OutlinedTextField(
                value = webUrl,
                onValueChange = { webUrl = it },
                label = { Text("输入网址") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            AndroidView(
                factory = { context ->
                    android.webkit.WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                    }
                },
                update = { webView ->
                    webView.loadUrl(webUrl)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
            
            Text(
                text = "💡 WebView 是典型的需要使用 AndroidView 的场景",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // 案例 2: 自定义进度条
        DemoCard(title = "案例 2: 自定义 ProgressBar") {
            var progress by remember { mutableIntStateOf(50) }
            
            Text("进度: $progress%", style = MaterialTheme.typography.titleMedium)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 传统 ProgressBar
            AndroidView(
                factory = { context ->
                    ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
                        max = 100
                        scaleY = 3f // 增加高度
                    }
                },
                update = { progressBar ->
                    progressBar.progress = progress
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { if (progress > 0) progress -= 10 },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("− 10")
                }
                Button(
                    onClick = { if (progress < 100) progress += 10 },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+ 10")
                }
            }
        }
        
        // 最佳实践
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "最佳实践",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                BulletPoint("优先使用 Compose: 新功能优先考虑 Compose")
                BulletPoint("渐进式迁移: 逐步将 View 替换为 Compose")
                BulletPoint("性能考虑: 避免频繁重组导致 View 重建")
                BulletPoint("生命周期: 注意 View 和 Compose 的生命周期差异")
                BulletPoint("测试: 两种系统的测试方式不同，需要分别处理")
            }
        }
        
        // 迁移策略
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "迁移策略",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                MigrationStep("1", "新屏幕", "使用 Compose 开发新功能")
                MigrationStep("2", "局部替换", "在现有屏幕中用 ComposeView 替换部分 View")
                MigrationStep("3", "整屏迁移", "将整个 Activity/Fragment 迁移到 Compose")
                MigrationStep("4", "完全迁移", "移除所有传统 View 代码")
            }
        }
    }
}

// ==================== UI 辅助组件 ====================
// 已移至 com.test.compose.ui.CommonComponents

@Composable
fun MigrationStep(step: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = step,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InteropScreenPreview() {
    ComposeTheme {
        InteropScreen()
    }
}

