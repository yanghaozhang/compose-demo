package com.test.compose

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.test.compose.ui.theme.ComposeTheme

// 计数器 ViewModel
class CounterViewModel : ViewModel() {
    private val _count = mutableIntStateOf(0)
    val count: State<Int> = _count
    
    fun increment() {
        _count.intValue++
    }
    
    fun decrement() {
        if (_count.intValue > 0) {
            _count.intValue--
        }
    }
    
    fun reset() {
        _count.intValue = 0
    }
}

// 待办事项数据类
data class TodoItem(
    val id: Int,
    val text: String,
    val isCompleted: Boolean = false
)

// 待办事项 ViewModel
class TodoViewModel : ViewModel() {
    private val _todos = mutableStateListOf<TodoItem>()
    val todos: List<TodoItem> = _todos
    
    private var nextId = 1
    
    fun addTodo(text: String) {
        if (text.isNotBlank()) {
            _todos.add(TodoItem(id = nextId++, text = text))
        }
    }
    
    fun toggleTodo(id: Int) {
        val index = _todos.indexOfFirst { it.id == id }
        if (index != -1) {
            _todos[index] = _todos[index].copy(isCompleted = !_todos[index].isCompleted)
        }
    }
    
    fun deleteTodo(id: Int) {
        _todos.removeAll { it.id == id }
    }
    
    fun clearCompleted() {
        _todos.removeAll { it.isCompleted }
    }
    
    val completedCount: Int
        get() = _todos.count { it.isCompleted }
    
    val activeCount: Int
        get() = _todos.count { !it.isCompleted }
}

// 用户表单数据类
data class UserFormData(
    val name: String = "",
    val email: String = "",
    val age: String = "",
    val bio: String = ""
)

// 用户表单 ViewModel
class UserFormViewModel : ViewModel() {
    private val _formData = mutableStateOf(UserFormData())
    val formData: State<UserFormData> = _formData
    
    private val _isSubmitted = mutableStateOf(false)
    val isSubmitted: State<Boolean> = _isSubmitted
    
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
    
    fun submitForm() {
        _isSubmitted.value = true
    }
    
    fun resetForm() {
        _formData.value = UserFormData()
        _isSubmitted.value = false
    }
    
    fun isValid(): Boolean {
        return _formData.value.name.isNotBlank() &&
                _formData.value.email.isNotBlank() &&
                _formData.value.age.isNotBlank()
    }
}

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

@Composable
fun ViewModelScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("计数器", "待办事项", "表单")
    
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
            0 -> CounterSection()
            1 -> TodoSection()
            2 -> FormSection()
        }
    }
}

// 计数器示例
@Composable
fun CounterSection(
    viewModel: CounterViewModel = viewModel()
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
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.decrement() },
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

// 待办事项示例
@Composable
fun TodoSection(
    viewModel: TodoViewModel = viewModel()
) {
    var newTodoText by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 顶部输入区
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
                    Text("总计: ${viewModel.todos.size}")
                    Text("已完成: ${viewModel.completedCount}")
                    Text("进行中: ${viewModel.activeCount}")
                }
            }
        }
        
        // 输入框
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTodoText,
                onValueChange = { newTodoText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("输入待办事项...") },
                singleLine = true
            )
            
            FilledIconButton(
                onClick = {
                    viewModel.addTodo(newTodoText)
                    newTodoText = ""
                },
                enabled = newTodoText.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        }
        
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
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggle() }
            )
            
            Text(
                text = todo.text,
                modifier = Modifier.weight(1f),
                style = if (todo.isCompleted) {
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

// 表单示例
@Composable
fun FormSection(
    viewModel: UserFormViewModel = viewModel()
) {
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
        
        if (!isSubmitted) {
            // 表单输入
            OutlinedTextField(
                value = formData.name,
                onValueChange = { viewModel.updateName(it) },
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
                enabled = viewModel.isValid()
            ) {
                Text("提交表单")
            }
        } else {
            // 提交成功显示
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

