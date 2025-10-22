package com.test.compose

/**
 * ==================================================================================
 * EffectActivity - Compose 副作用 API 对比示例
 * ==================================================================================
 * 
 * 本文件展示了 Compose 中三个核心副作用 API 的区别和使用场景：
 * 1. LaunchedEffect - 启动协程副作用
 * 2. SideEffect - 每次重组后执行
 * 3. DisposableEffect - 可清理的副作用
 * 
 * 什么是副作用（Side Effect）？
 * - Composable 函数应该是纯函数，只根据输入产生 UI
 * - 但有时需要执行"副作用"：网络请求、定时器、订阅等
 * - Effect API 提供了在 Compose 中安全执行副作用的方式
 * 
 * 三个 Effect 的核心区别：
 * ┌─────────────────────┬──────────────────┬──────────────────┬──────────────────┐
 * │       API           │   执行时机       │   协程支持       │   清理能力       │
 * ├─────────────────────┼──────────────────┼──────────────────┼──────────────────┤
 * │ LaunchedEffect      │ 首次组合/key变化 │ ✅ 支持          │ ✅ 自动取消协程  │
 * │ SideEffect          │ 每次重组后       │ ❌ 不支持        │ ❌ 无            │
 * │ DisposableEffect    │ 首次组合/key变化 │ ❌ 不支持        │ ✅ 手动清理      │
 * └─────────────────────┴──────────────────┴──────────────────┴──────────────────┘
 * 
 * 学习要点：
 * - 理解每个 Effect 的生命周期
 * - 掌握适用场景和最佳实践
 * - 避免内存泄漏和性能问题
 * 
 * ==================================================================================
 */

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.test.compose.ui.theme.ComposeTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// ==================== Activity ====================

class EffectActivity : ComponentActivity() {
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
                            title = { Text("副作用 API 对比") },
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
                    EffectScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// ==================== 主屏幕 ====================

@Composable
fun EffectScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("LaunchedEffect", "SideEffect", "DisposableEffect", "对比总结")
    
    Column(modifier = modifier.fillMaxSize()) {
        // 标签栏
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { 
                        Text(
                            text = title,
                            maxLines = 1,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }
        
        // 内容区域
        when (selectedTab) {
            0 -> LaunchedEffectDemo()
            1 -> SideEffectDemo()
            2 -> DisposableEffectDemo()
            3 -> ComparisonSummary()
        }
    }
}

// ==================== LaunchedEffect 示例 ====================

/**
 * LaunchedEffect - 启动协程副作用
 * 
 * 特点：
 * 1. 在首次组合时启动协程
 * 2. 当 key 参数改变时，取消旧协程并启动新协程
 * 3. 当离开组合时（组件移除），自动取消协程
 * 4. 适合执行异步任务：网络请求、延迟操作、Flow 收集等
 * 
 * 语法：
 * LaunchedEffect(key) {
 *     // 这里是协程作用域
 *     // 可以使用 suspend 函数
 * }
 */
@Composable
fun LaunchedEffectDemo() {
    var counter by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var timerValue by remember { mutableIntStateOf(0) }
    var logMessages by remember { mutableStateOf(listOf<String>()) }
    
    // 添加日志的辅助函数
    fun addLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        logMessages = logMessages + "[$timestamp] $message"
        Log.d("LaunchedEffect", message)
    }
    
    /**
     * 示例 1：依赖 key 的 LaunchedEffect
     * 
     * 工作原理：
     * - counter 改变时，LaunchedEffect 重新执行
     * - 旧的协程被取消，新的协程启动
     * - 每次执行都会延迟 1 秒后添加日志
     */
    LaunchedEffect(counter) {
        addLog("LaunchedEffect 启动，counter = $counter")
        delay(1000)  // 模拟异步操作
        addLog("LaunchedEffect 完成，counter = $counter")
    }
    
    /**
     * 示例 2：定时器
     * 
     * 工作原理：
     * - isTimerRunning 为 true 时启动定时器
     * - isTimerRunning 为 false 时取消定时器
     * - 使用无限循环 + delay 实现定时器
     */
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            addLog("定时器启动")
            timerValue = 0
            while (isTimerRunning) {
                delay(1000)
                timerValue++
                addLog("定时器: $timerValue 秒")
            }
        } else {
            addLog("定时器停止")
        }
    }
    
    // UI 布局
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 说明卡片
        InfoCard(
            title = "LaunchedEffect",
            icon = Icons.Default.Refresh,
            description = "在协程中执行副作用，当 key 改变时重新启动"
        ) {
            Text("• 支持挂起函数（suspend）", style = MaterialTheme.typography.bodyMedium)
            Text("• key 改变时自动取消旧协程", style = MaterialTheme.typography.bodyMedium)
            Text("• 离开组合时自动清理", style = MaterialTheme.typography.bodyMedium)
            Text("• 适用场景：网络请求、Flow 收集、延迟任务", style = MaterialTheme.typography.bodyMedium)
        }
        
        // 示例 1：依赖 key 的协程
        DemoCard(title = "示例 1：依赖 key 的协程") {
            Text(
                text = "点击按钮增加计数，LaunchedEffect 会重新执行",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "计数: $counter",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                Button(onClick = { counter++ }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("增加")
                }
            }
            
            Text(
                text = "每次点击都会取消上一个协程，启动新协程",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // 示例 2：定时器
        DemoCard(title = "示例 2：定时器（协程循环）") {
            Text(
                text = "使用 LaunchedEffect + while 循环实现定时器",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTimerRunning) "$timerValue 秒" else "已停止",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                if (isTimerRunning) {
                    Button(onClick = { isTimerRunning = false }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("停止")
                    }
                } else {
                    Button(onClick = { isTimerRunning = true }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("启动")
                    }
                }
            }
        }
        
        // 日志显示
        LogCard(
            logs = logMessages,
            onClear = { logMessages = emptyList() }
        )
    }
}

// ==================== SideEffect 示例 ====================

/**
 * SideEffect - 每次重组后执行
 * 
 * 特点：
 * 1. 在每次成功重组后执行
 * 2. 不支持协程（同步执行）
 * 3. 没有清理机制
 * 4. 适合将 Compose 状态同步到非 Compose 对象
 * 
 * 语法：
 * SideEffect {
 *     // 同步代码
 *     // 不能使用 suspend 函数
 * }
 * 
 * 注意：
 * - 由于每次重组都执行，性能开销较大
 * - 应该谨慎使用，避免执行耗时操作
 */
@Composable
fun SideEffectDemo() {
    var counter by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("张三") }
    var logMessages by remember { mutableStateOf(listOf<String>()) }
    var recomposeCount by remember { mutableIntStateOf(0) }
    
    fun addLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        logMessages = logMessages + "[$timestamp] $message"
        Log.d("SideEffect", message)
    }
    
    /**
     * SideEffect 示例
     * 
     * 工作原理：
     * - 每次这个 Composable 重组时都会执行
     * - 无论是 counter 还是 name 改变，都会执行
     * - 用于观察重组频率
     */
    SideEffect {
        recomposeCount++
        addLog("SideEffect 执行，重组次数: $recomposeCount")
        // 注意：这里不能使用 suspend 函数，必须是同步代码
    }
    
    /**
     * 模拟外部对象（非 Compose）
     * 
     * 实际场景：
     * - 同步到 Android View
     * - 更新分析系统
     * - 同步到传统 ViewModel
     */
    class ExternalObject {
        var currentState: String = ""
        fun updateState(state: String) {
            currentState = state
            Log.d("ExternalObject", "状态更新: $state")
        }
    }
    
    val externalObject = remember { ExternalObject() }
    
    /**
     * 使用 SideEffect 同步状态
     * 
     * 典型用例：
     * - 将 Compose 状态同步到非 Compose 系统
     * - 每次重组后都确保外部对象是最新的
     */
    SideEffect {
        externalObject.updateState("counter=$counter, name=$name")
        addLog("同步到外部对象: ${externalObject.currentState}")
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 说明卡片
        InfoCard(
            title = "SideEffect",
            icon = Icons.Default.Refresh,
            description = "在每次重组后执行同步副作用"
        ) {
            Text("• 每次重组都执行（高频率）", style = MaterialTheme.typography.bodyMedium)
            Text("• 不支持挂起函数（同步执行）", style = MaterialTheme.typography.bodyMedium)
            Text("• 没有清理机制", style = MaterialTheme.typography.bodyMedium)
            Text("• 适用场景：状态同步、日志记录、分析统计", style = MaterialTheme.typography.bodyMedium)
        }
        
        // 重组计数器
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
                    text = "重组次数",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$recomposeCount",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "SideEffect 执行了 $recomposeCount 次",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        
        // 状态修改示例
        DemoCard(title = "修改状态触发重组") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 计数器
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("计数: $counter", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { counter-- }) {
                            Text("−", style = MaterialTheme.typography.titleLarge)
                        }
                        Button(onClick = { counter++ }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }
                
                Divider()
                
                // 名称输入
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("姓名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Text(
                    text = "每次修改状态都会触发重组和 SideEffect",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // 外部对象状态
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "外部对象状态",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = externalObject.currentState,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "通过 SideEffect 自动同步",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        
        // 日志显示
        LogCard(
            logs = logMessages,
            onClear = { logMessages = emptyList() }
        )
    }
}

// ==================== DisposableEffect 示例 ====================

/**
 * DisposableEffect - 可清理的副作用
 * 
 * 特点：
 * 1. 在首次组合时执行
 * 2. 当 key 改变时，先执行清理（onDispose），再执行新的副作用
 * 3. 当离开组合时，执行清理（onDispose）
 * 4. 必须返回 onDispose 清理块
 * 5. 不支持协程（同步执行）
 * 6. 适合需要清理资源的场景：监听器、订阅、传感器等
 * 
 * 语法：
 * DisposableEffect(key) {
 *     // 设置副作用
 *     onDispose {
 *         // 清理副作用
 *     }
 * }
 */
@Composable
fun DisposableEffectDemo() {
    var isListening by remember { mutableStateOf(false) }
    var eventCount by remember { mutableIntStateOf(0) }
    var logMessages by remember { mutableStateOf(listOf<String>()) }
    var isComponentVisible by remember { mutableStateOf(true) }
    
    fun addLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        logMessages = logMessages + "[$timestamp] $message"
        Log.d("DisposableEffect", message)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 说明卡片
        InfoCard(
            title = "DisposableEffect",
            icon = Icons.Default.Delete,
            description = "执行需要清理的副作用，提供 onDispose 清理机制"
        ) {
            Text("• 首次组合/key 变化时执行", style = MaterialTheme.typography.bodyMedium)
            Text("• 必须返回 onDispose 清理块", style = MaterialTheme.typography.bodyMedium)
            Text("• 不支持挂起函数（同步执行）", style = MaterialTheme.typography.bodyMedium)
            Text("• 适用场景：监听器、订阅、资源管理", style = MaterialTheme.typography.bodyMedium)
        }
        
        // 控制按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { isListening = !isListening },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    if (isListening) Icons.Default.Close else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isListening) "停止监听" else "开始监听")
            }
            
            Button(
                onClick = { isComponentVisible = !isComponentVisible },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    if (isComponentVisible) Icons.Default.Delete else Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isComponentVisible) "移除组件" else "添加组件")
            }
        }
        
        // 条件显示组件
        AnimatedVisibility(visible = isComponentVisible) {
            DisposableEffectComponent(
                isListening = isListening,
                onEvent = {
                    eventCount++
                    addLog("收到事件 #$eventCount")
                },
                onLog = { addLog(it) }
            )
        }
        
        if (!isComponentVisible) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "组件已移除\n（onDispose 已执行）",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        
        // 事件计数
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "事件计数",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$eventCount",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // 日志显示
        LogCard(
            logs = logMessages,
            onClear = { logMessages = emptyList() }
        )
    }
}

/**
 * DisposableEffectComponent - 演示 DisposableEffect 的组件
 */
@Composable
fun DisposableEffectComponent(
    isListening: Boolean,
    onEvent: () -> Unit,
    onLog: (String) -> Unit
) {
    /**
     * 模拟事件监听器
     */
    class EventListener {
        private var callback: (() -> Unit)? = null
        private var isActive = false
        
        fun start(callback: () -> Unit) {
            this.callback = callback
            isActive = true
            Log.d("EventListener", "监听器启动")
        }
        
        fun stop() {
            callback = null
            isActive = false
            Log.d("EventListener", "监听器停止")
        }
        
        fun triggerEvent() {
            if (isActive) {
                callback?.invoke()
            }
        }
    }
    
    val eventListener = remember { EventListener() }
    
    /**
     * DisposableEffect 核心示例
     * 
     * 生命周期：
     * 1. isListening 变为 true：执行 start()
     * 2. isListening 变为 false：先执行 onDispose { stop() }，再执行新的副作用
     * 3. 组件移除：执行 onDispose { stop() }
     */
    DisposableEffect(isListening) {
        if (isListening) {
            onLog("DisposableEffect: 设置监听器")
            eventListener.start {
                onEvent()
            }
        } else {
            onLog("DisposableEffect: 监听未启动")
        }
        
        // onDispose 是必须的！
        // 在 key 改变或组件移除时执行
        onDispose {
            onLog("DisposableEffect: onDispose 清理")
            eventListener.stop()
        }
    }
    
    // UI 显示
    DemoCard(title = "DisposableEffect 组件") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = if (isListening) "🟢 监听中" else "🔴 未监听",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "当组件移除或 isListening 改变时，会调用 onDispose 清理资源",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (isListening) {
                Button(
                    onClick = { eventListener.triggerEvent() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("触发事件")
                }
            }
        }
    }
}

// ==================== 对比总结 ====================

@Composable
fun ComparisonSummary() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Effect API 对比总结",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // LaunchedEffect 总结
        ComparisonCard(
            title = "LaunchedEffect",
            color = MaterialTheme.colorScheme.primaryContainer,
            icon = Icons.Default.Refresh
        ) {
            ComparisonItem("执行时机", "首次组合 + key 改变")
            ComparisonItem("协程支持", "✅ 支持挂起函数")
            ComparisonItem("清理机制", "✅ 自动取消协程")
            ComparisonItem("典型场景", "网络请求、Flow 收集、定时器")
            
            Spacer(modifier = Modifier.height(8.dp))
            CodeExample("""
                LaunchedEffect(userId) {
                    val user = api.getUser(userId)
                    updateUI(user)
                }
            """.trimIndent())
        }
        
        // SideEffect 总结
        ComparisonCard(
            title = "SideEffect",
            color = MaterialTheme.colorScheme.secondaryContainer,
            icon = Icons.Default.Refresh
        ) {
            ComparisonItem("执行时机", "每次重组后（高频）")
            ComparisonItem("协程支持", "❌ 仅同步代码")
            ComparisonItem("清理机制", "❌ 无清理")
            ComparisonItem("典型场景", "状态同步、分析统计")
            
            Spacer(modifier = Modifier.height(8.dp))
            CodeExample("""
                SideEffect {
                    analytics.trackScreen(screenName)
                    externalObject.sync(state)
                }
            """.trimIndent())
        }
        
        // DisposableEffect 总结
        ComparisonCard(
            title = "DisposableEffect",
            color = MaterialTheme.colorScheme.tertiaryContainer,
            icon = Icons.Default.Delete
        ) {
            ComparisonItem("执行时机", "首次组合 + key 改变")
            ComparisonItem("协程支持", "❌ 仅同步代码")
            ComparisonItem("清理机制", "✅ onDispose 手动清理")
            ComparisonItem("典型场景", "监听器、订阅、资源管理")
            
            Spacer(modifier = Modifier.height(8.dp))
            CodeExample("""
                DisposableEffect(key) {
                    val listener = setupListener()
                    onDispose {
                        listener.remove()
                    }
                }
            """.trimIndent())
        }
        
        // 决策树
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "如何选择？",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                DecisionItem("需要调用挂起函数？", "→ LaunchedEffect")
                DecisionItem("需要每次重组都执行？", "→ SideEffect")
                DecisionItem("需要手动清理资源？", "→ DisposableEffect")
                DecisionItem("不需要协程也不需要清理？", "→ 可能不需要 Effect")
            }
        }
        
        // 最佳实践
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "注意事项",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                BulletPoint("避免在 Effect 中直接修改状态（可能导致无限循环）")
                BulletPoint("SideEffect 每次重组都执行，避免执行耗时操作")
                BulletPoint("DisposableEffect 必须返回 onDispose 块")
                BulletPoint("正确设置 key 参数，避免不必要的重启")
                BulletPoint("LaunchedEffect 的协程会自动取消，无需手动清理")
            }
        }
    }
}

// ==================== UI 辅助组件 ====================

@Composable
fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            content()
        }
    }
}

@Composable
fun DemoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun LogCard(
    logs: List<String>,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "执行日志",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onClear) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("清空")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (logs.isEmpty()) {
                Text(
                    text = "暂无日志",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                        .background(
                            Color.Black.copy(alpha = 0.05f),
                            MaterialTheme.shapes.small
                        )
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    logs.forEach { log ->
                        Text(
                            text = log,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonCard(
    title: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun ComparisonItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun CodeExample(code: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = code,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

@Composable
fun DecisionItem(question: String, answer: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text("• ", fontWeight = FontWeight.Bold)
        Column {
            Text(
                text = question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = answer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text("• ", fontWeight = FontWeight.Bold)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EffectScreenPreview() {
    ComposeTheme {
        EffectScreen()
    }
}

