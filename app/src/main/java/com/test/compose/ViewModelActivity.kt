package com.test.compose

/**
 * ==================================================================================
 * ViewModelActivity - ViewModel 与 Compose 集成示例
 * ==================================================================================
 * 
 * 本文件展示了如何在 Jetpack Compose 中使用 ViewModel
 * 
 * 核心概念：
 * 1. ViewModel：Android 架构组件，用于存储和管理 UI 相关的数据
 * 2. 状态管理：使用 mutableStateOf 和 mutableStateListOf 管理可观察状态
 * 3. MVVM 模式：Model-View-ViewModel 架构模式
 * 4. 配置更改：ViewModel 在配置更改（如屏幕旋转）时保持数据
 * 
 * 学习要点：
 * - ViewModel 的生命周期比 Activity 更长
 * - State 的变化会触发 UI 重组
 * - viewModel() 函数自动处理 ViewModel 的创建和管理
 * - 业务逻辑与 UI 分离的最佳实践
 * 
 * ==================================================================================
 */

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel  // ViewModel 基类
import androidx.lifecycle.viewmodel.compose.viewModel  // 在 Compose 中获取 ViewModel 的函数
import com.test.compose.ui.theme.ComposeTheme

// ==================== ViewModel 示例 1：简单计数器 ====================

/**
 * CounterViewModel - 计数器的 ViewModel
 * 
 * ViewModel 的特点：
 * 1. 继承自 ViewModel 基类
 * 2. 生命周期比 Activity 更长，配置更改时不会销毁
 * 3. 在 Activity 销毁时自动清理（onCleared）
 * 
 * 状态管理：
 * - _count：私有可变状态，只在 ViewModel 内部修改
 * - count：公开只读状态，外部只能读取不能修改
 * 这种模式称为"单向数据流"
 */
class CounterViewModel : ViewModel() {
    
    /**
     * mutableIntStateOf：创建一个可变的 Int 类型状态
     * 
     * 为什么用下划线前缀(_count)：
     * - 表示这是私有的内部状态
     * - 只能在 ViewModel 内部修改
     * - 遵循封装原则
     */
    private val _count = mutableIntStateOf(0)
    
    /**
     * count：对外暴露的只读状态
     * 
     * State<Int>：只读状态类型
     * - 外部可以读取值
     * - 外部不能直接修改
     * - 必须通过 ViewModel 提供的方法修改
     */
    val count: State<Int> = _count
    
    /**
     * 增加计数
     * 这是修改状态的唯一入口
     */
    fun increment() {
        _count.intValue++  // 修改 Int 状态使用 intValue 属性
    }
    
    /**
     * 减少计数
     * 添加了边界检查，避免负数
     */
    fun decrement() {
        if (_count.intValue > 0) {
            _count.intValue--
        }
    }
    
    /**
     * 重置计数
     */
    fun reset() {
        _count.intValue = 0
    }
}

// ==================== 数据类定义 ====================

/**
 * TodoItem - 待办事项数据类
 * 
 * data class 特点：
 * - 自动生成 equals、hashCode、toString、copy 等方法
 * - 适合作为数据容器
 * - 不可变设计（所有属性都是 val）
 */
data class TodoItem(
    val id: Int,                    // 唯一标识
    val text: String,               // 待办内容
    val isCompleted: Boolean = false  // 是否完成，默认未完成
)

// ==================== ViewModel 示例 2：待办事项列表 ====================

/**
 * TodoViewModel - 待办事项的 ViewModel
 * 
 * 这个例子展示了如何管理列表状态
 * 
 * 关键点：
 * 1. 使用 mutableStateListOf 创建可观察的列表
 * 2. 列表的增删改会自动触发 UI 更新
 * 3. 计算属性（completedCount、activeCount）根据列表自动计算
 */
class TodoViewModel : ViewModel() {
    
    /**
     * mutableStateListOf：创建可观察的可变列表
     * 
     * 特点：
     * - 列表的任何修改都会触发重组
     * - 类似于 LiveData<List<T>>
     * - 支持 List 的所有操作（add、remove 等）
     */
    private val _todos = mutableStateListOf<TodoItem>()
    
    /**
     * 对外暴露为只读列表
     * List<TodoItem> 是不可变接口，外部不能修改
     */
    val todos: List<TodoItem> = _todos
    
    // 用于生成唯一 ID
    private var nextId = 1
    
    /**
     * 添加待办事项
     * 
     * 参数验证：
     * - isNotBlank() 检查文本不为空
     * - 只有有效输入才会添加
     */
    fun addTodo(text: String) {
        if (text.isNotBlank()) {
            _todos.add(TodoItem(id = nextId++, text = text))
        }
    }
    
    /**
     * 切换待办事项的完成状态
     * 
     * 注意：
     * - 不能直接修改列表中的项（因为是 data class，不可变）
     * - 需要用 copy() 创建新对象，然后替换
     * - 这是函数式编程的不可变数据模式
     */
    fun toggleTodo(id: Int) {
        val index = _todos.indexOfFirst { it.id == id }  // 查找索引
        if (index != -1) {
            // 使用 copy() 创建新对象，只改变 isCompleted 属性
            _todos[index] = _todos[index].copy(isCompleted = !_todos[index].isCompleted)
        }
    }
    
    /**
     * 删除待办事项
     */
    fun deleteTodo(id: Int) {
        _todos.removeAll { it.id == id }
    }
    
    /**
     * 清除所有已完成的待办事项
     */
    fun clearCompleted() {
        _todos.removeAll { it.isCompleted }
    }
    
    /**
     * 计算属性：已完成的数量
     * 
     * get()：计算属性的 getter
     * - 每次访问时重新计算
     * - 不存储值，而是实时计算
     * - count { } 是 Kotlin 的集合函数
     */
    val completedCount: Int
        get() = _todos.count { it.isCompleted }
    
    /**
     * 计算属性：未完成的数量
     */
    val activeCount: Int
        get() = _todos.count { !it.isCompleted }
}

// ==================== ViewModel 示例 3：表单数据 ====================

/**
 * UserFormData - 用户表单数据类
 * 
 * 将表单所有字段封装在一个数据类中
 * 优点：
 * - 统一管理
 * - 容易传递
 * - 支持 copy() 部分更新
 */
data class UserFormData(
    val name: String = "",
    val email: String = "",
    val age: String = "",
    val bio: String = ""
)

/**
 * UserFormViewModel - 用户表单的 ViewModel
 * 
 * 展示了如何处理复杂表单状态
 * 
 * 模式：
 * 1. 使用单个状态对象包含所有表单字段
 * 2. 提供每个字段的更新方法
 * 3. 提供表单验证逻辑
 * 4. 提供提交和重置功能
 */
class UserFormViewModel : ViewModel() {
    
    /**
     * mutableStateOf：创建可变状态
     * 
     * 类型：State<UserFormData>
     * 初始值：空的 UserFormData 对象
     */
    private val _formData = mutableStateOf(UserFormData())
    val formData: State<UserFormData> = _formData
    
    // 表单提交状态
    private val _isSubmitted = mutableStateOf(false)
    val isSubmitted: State<Boolean> = _isSubmitted
    
    /**
     * 更新姓名
     * 
     * copy()：data class 的方法，创建一个新对象
     * - 只改变指定的属性（name）
     * - 其他属性保持不变
     * - 这保证了不可变性
     */
    fun updateName(name: String) {
        _formData.value = _formData.value.copy(name = name)
    }
    
    fun updateEmail(email: String) {
        _formData.value = _formData.value.copy(email = email)
    }
    
    fun updateAge(age: String) {
        _formData.value = _formData.value.copy(age = age)
    }
    
    fun updateBio(bio: String) {
        _formData.value = _formData.value.copy(bio = bio)
    }
    
    /**
     * 提交表单
     */
    fun submitForm() {
        _isSubmitted.value = true
    }
    
    /**
     * 重置表单
     * 恢复到初始状态
     */
    fun resetForm() {
        _formData.value = UserFormData()
        _isSubmitted.value = false
    }
    
    /**
     * 表单验证
     * 
     * 返回：表单是否有效
     * 规则：姓名、邮箱、年龄都不能为空
     */
    fun isValid(): Boolean {
        return _formData.value.name.isNotBlank() &&
                _formData.value.email.isNotBlank() &&
                _formData.value.age.isNotBlank()
    }
}

// ==================== Activity ====================

/**
 * ViewModelActivity
 * 
 * 这是使用 ViewModel 的 Activity
 * 本身非常简单，只负责设置 UI
 */
class ViewModelActivity : ComponentActivity() {
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
                            title = { Text("ViewModel 绑定测试") },
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
                    ViewModelScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// ==================== UI 组件 ====================

/**
 * ViewModelScreen - 主屏幕
 * 
 * 使用 Tab 切换不同的 ViewModel 示例
 */
@Composable
fun ViewModelScreen(modifier: Modifier = Modifier) {
    // remember：在重组时记住状态
    // mutableIntStateOf：可变的 Int 状态
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("计数器", "待办事项", "表单")
    
    Column(modifier = modifier.fillMaxSize()) {
        // TabRow：Material3 的标签栏组件
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // 根据选中的 Tab 显示不同内容
        when (selectedTab) {
            0 -> CounterSection()
            1 -> TodoSection()
            2 -> FormSection()
        }
    }
}

/**
 * CounterSection - 计数器示例
 * 
 * @param viewModel 使用 viewModel() 函数获取 ViewModel
 * 
 * viewModel() 函数：
 * - Compose 提供的函数，自动创建和管理 ViewModel
 * - 同一个 Composable 多次调用会返回同一个实例
 * - Activity 销毁时自动清理
 * - 配置更改（屏幕旋转）时不会重新创建
 */
@Composable
fun CounterSection(
    viewModel: CounterViewModel = viewModel()  // 默认参数，自动创建 ViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "计数器 ViewModel 示例",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        // 显示计数的卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "当前计数",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                /**
                 * viewModel.count.value：读取 State 的值
                 * 
                 * 关键点：
                 * - .value 获取 State 的当前值
                 * - Compose 会自动订阅这个 State
                 * - 当 count 变化时，这个 Text 会自动重组（重新渲染）
                 */
                Text(
                    text = "${viewModel.count.value}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Text(
            text = "ViewModel 会在配置更改（如屏幕旋转）时保持状态",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // 控制按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.decrement() },  // 调用 ViewModel 的方法
                modifier = Modifier.weight(1f)
            ) {
                Text("减少 -1")
            }
            
            FilledTonalButton(
                onClick = { viewModel.increment() },
                modifier = Modifier.weight(1f)
            ) {
                Text("增加 +1")
            }
        }
        
        OutlinedButton(
            onClick = { viewModel.reset() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("重置")
        }
    }
}

/**
 * TodoSection - 待办事项示例
 * 
 * 展示了 ViewModel 管理列表的能力
 */
@Composable
fun TodoSection(
    viewModel: TodoViewModel = viewModel()
) {
    // 本地 UI 状态（不需要在配置更改时保持）
    // 使用 remember + mutableStateOf
    var newTodoText by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部统计卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "待办事项 ViewModel",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 这些值来自 ViewModel 的计算属性
                    Text("总计: ${viewModel.todos.size}")
                    Text("已完成: ${viewModel.completedCount}")
                    Text("进行中: ${viewModel.activeCount}")
                }
            }
        }
        
        // 输入框和添加按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            /**
             * OutlinedTextField：Material3 的输入框
             * 
             * 参数：
             * - value：当前值（本地状态）
             * - onValueChange：值改变时的回调
             * - 这是双向绑定的典型模式
             */
            OutlinedTextField(
                value = newTodoText,
                onValueChange = { newTodoText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("输入待办事项...") },
                singleLine = true
            )
            
            FilledIconButton(
                onClick = {
                    viewModel.addTodo(newTodoText)  // 调用 ViewModel 方法
                    newTodoText = ""  // 清空输入框
                },
                enabled = newTodoText.isNotBlank()  // 按钮启用状态
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        }
        
        // 清除已完成按钮（条件显示）
        if (viewModel.completedCount > 0) {
            TextButton(
                onClick = { viewModel.clearCompleted() },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("清除已完成")
            }
        }
        
        // 待办列表
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            /**
             * forEach：遍历 ViewModel 的待办列表
             * 
             * 注意：
             * - viewModel.todos 是可观察列表
             * - 列表变化时会触发重组
             * - 这里的所有 TodoItemCard 都会重新渲染
             */
            viewModel.todos.forEach { todo ->
                TodoItemCard(
                    todo = todo,
                    onToggle = { viewModel.toggleTodo(todo.id) },
                    onDelete = { viewModel.deleteTodo(todo.id) }
                )
            }
            
            if (viewModel.todos.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Text(
                        text = "暂无待办事项",
                        modifier = Modifier.padding(32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * TodoItemCard - 待办事项卡片
 * 
 * 可重用组件，展示单个待办事项
 */
@Composable
fun TodoItemCard(
    todo: TodoItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            /**
             * Checkbox：复选框组件
             * 
             * 参数：
             * - checked：当前选中状态
             * - onCheckedChange：状态改变回调
             */
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggle() }
            )
            
            Text(
                text = todo.text,
                modifier = Modifier.weight(1f),
                style = if (todo.isCompleted) {
                    // 已完成：使用不同的样式
                    MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    MaterialTheme.typography.bodyLarge
                }
            )
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * FormSection - 表单示例
 * 
 * 展示了 ViewModel 管理复杂表单状态
 */
@Composable
fun FormSection(
    viewModel: UserFormViewModel = viewModel()
) {
    // 从 ViewModel 读取状态
    val formData = viewModel.formData.value
    val isSubmitted = viewModel.isSubmitted.value
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "用户表单 ViewModel",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "ViewModel 管理表单状态，保持数据一致性",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // 根据提交状态显示不同内容
        if (!isSubmitted) {
            // 未提交：显示表单
            OutlinedTextField(
                value = formData.name,
                onValueChange = { viewModel.updateName(it) },  // 调用 ViewModel 更新
                label = { Text("姓名 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = formData.email,
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text("邮箱 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = formData.age,
                onValueChange = { viewModel.updateAge(it) },
                label = { Text("年龄 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = formData.bio,
                onValueChange = { viewModel.updateBio(it) },
                label = { Text("个人简介") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            Button(
                onClick = { viewModel.submitForm() },
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.isValid()  // 根据验证结果启用/禁用按钮
            ) {
                Text("提交表单")
            }
        } else {
            // 已提交：显示结果
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✓ 表单提交成功！",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(
                        text = "姓名: ${formData.name}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "邮箱: ${formData.email}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "年龄: ${formData.age}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (formData.bio.isNotBlank()) {
                        Text(
                            text = "简介: ${formData.bio}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            OutlinedButton(
                onClick = { viewModel.resetForm() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("重新填写")
            }
        }
        
        // 说明卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ViewModel 特性",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• 配置更改时保持数据", style = MaterialTheme.typography.bodyMedium)
                Text("• 与 Compose 状态无缝集成", style = MaterialTheme.typography.bodyMedium)
                Text("• 业务逻辑与UI分离", style = MaterialTheme.typography.bodyMedium)
                Text("• 生命周期感知", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ViewModelScreenPreview() {
    ComposeTheme {
        ViewModelScreen()
    }
}

