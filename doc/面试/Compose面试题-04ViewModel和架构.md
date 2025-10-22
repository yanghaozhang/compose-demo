# Compose UI 面试题 - 第4部分：ViewModel 和架构

## 19. 如何在 Compose 中使用 ViewModel？

**答案：**

ViewModel 是 Android 架构组件，用于管理 UI 相关数据，在配置更改时保留数据。

**基础使用：**

```kotlin
// 1. 定义 ViewModel
class CounterViewModel : ViewModel() {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()
    
    fun increment() {
        _count.value++
    }
    
    fun decrement() {
        _count.value--
    }
}

// 2. 在 Compose 中使用
@Composable
fun CounterScreen(viewModel: CounterViewModel = viewModel()) {
    val count by viewModel.count.collectAsState()
    
    Column {
        Text("Count: $count")
        Row {
            Button(onClick = { viewModel.increment() }) {
                Text("+")
            }
            Button(onClick = { viewModel.decrement() }) {
                Text("-")
            }
        }
    }
}
```

**使用 State 替代 Flow：**

```kotlin
class SimpleViewModel : ViewModel() {
    var count by mutableStateOf(0)
        private set
    
    fun increment() {
        count++
    }
}

@Composable
fun SimpleScreen(viewModel: SimpleViewModel = viewModel()) {
    // 直接使用，自动订阅
    Text("Count: ${viewModel.count}")
    Button(onClick = { viewModel.increment() }) {
        Text("+")
    }
}
```

**依赖注入 ViewModelFactory：**

```kotlin
class UserViewModel(
    private val userId: String,
    private val repository: UserRepository
) : ViewModel() {
    // ...
}

class UserViewModelFactory(
    private val userId: String,
    private val repository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserViewModel(userId, repository) as T
    }
}

@Composable
fun UserScreen(userId: String) {
    val factory = UserViewModelFactory(userId, UserRepository())
    val viewModel: UserViewModel = viewModel(factory = factory)
}
```

---

## 20. StateFlow 和 LiveData 在 Compose 中有什么区别？

**答案：**

两者都用于数据观察，但 StateFlow 更适合 Compose 和 Kotlin 协程。

**区别对比：**

| 特性 | StateFlow | LiveData |
|------|-----------|----------|
| 平台 | Kotlin 协程 | Android 专用 |
| 生命周期感知 | 需手动处理 | 内置 |
| 初始值 | 必须有 | 可以没有 |
| 操作符 | Flow 操作符 | Transformations |
| 性能 | 更好 | 稍慢 |
| Compose 集成 | collectAsState() | observeAsState() |

**StateFlow 使用（推荐）：**

```kotlin
class TodoViewModel : ViewModel() {
    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 可以使用 Flow 操作符
    val completedCount: StateFlow<Int> = todos
        .map { list -> list.count { it.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)
    
    fun addTodo(title: String) {
        _todos.value = _todos.value + Todo(title)
    }
}

@Composable
fun TodoScreen(viewModel: TodoViewModel = viewModel()) {
    val todos by viewModel.todos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState()
    
    // UI...
}
```

**LiveData 使用：**

```kotlin
class TodoViewModelLive : ViewModel() {
    private val _todos = MutableLiveData<List<Todo>>()
    val todos: LiveData<List<Todo>> = _todos
    
    val completedCount: LiveData<Int> = Transformations.map(todos) { list ->
        list.count { it.isCompleted }
    }
}

@Composable
fun TodoScreenLive(viewModel: TodoViewModelLive = viewModel()) {
    val todos by viewModel.todos.observeAsState(emptyList())
    val completedCount by viewModel.completedCount.observeAsState(0)
    
    // UI...
}
```

**使用建议：**
- **新项目** → 使用 StateFlow
- **已有 LiveData 项目** → 可继续使用或逐步迁移
- **需要复杂转换** → StateFlow（Flow 操作符更强大）

---

## 21. 如何处理 ViewModel 中的异步操作和错误？

**答案：**

使用协程和状态封装来管理异步操作的不同状态。

**定义 UI 状态：**

```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>()
}
```

**ViewModel 实现：**

```kotlin
class UserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val uiState: StateFlow<UiState<User>> = _uiState.asStateFlow()
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                val user = userRepository.getUser(userId)
                _uiState.value = UiState.Success(user)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = "加载失败: ${e.message}",
                    throwable = e
                )
            }
        }
    }
    
    fun retry(userId: String) {
        loadUser(userId)
    }
}
```

**Compose UI 处理：**

```kotlin
@Composable
fun UserScreen(userId: String, viewModel: UserViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }
    
    when (val state = uiState) {
        is UiState.Idle -> {
            // 初始状态
        }
        
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        
        is UiState.Success -> {
            UserContent(user = state.data)
        }
        
        is UiState.Error -> {
            ErrorView(
                message = state.message,
                onRetry = { viewModel.retry(userId) }
            )
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}
```

**多状态组合（加载中 + 数据展示）：**

```kotlin
class RefreshableViewModel : ViewModel() {
    private val _data = MutableStateFlow<List<Item>>(emptyList())
    val data: StateFlow<List<Item>> = _data.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            
            try {
                val newData = repository.fetchData()
                _data.value = newData
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}

@Composable
fun RefreshableScreen(viewModel: RefreshableViewModel = viewModel()) {
    val data by viewModel.data.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() }
    )
    
    Box(Modifier.pullRefresh(pullRefreshState)) {
        LazyColumn {
            items(data) { item ->
                ItemCard(item)
            }
        }
        
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        error?.let { errorMessage ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Text(errorMessage)
            }
        }
    }
}
```

---

## 22. 什么是单向数据流（UDF）？如何在 Compose 中实现？

**答案：**

单向数据流是一种架构模式，数据从 ViewModel 流向 UI，事件从 UI 流向 ViewModel。

**UDF 核心原则：**

1. **State Down（状态向下）** - ViewModel 向 UI 传递状态
2. **Events Up（事件向上）** - UI 向 ViewModel 发送事件
3. **Single Source of Truth（单一数据源）** - 状态只在一处定义

**架构图：**
```
ViewModel (State) 
     ↓
UI (Display)
     ↓
User Action (Event)
     ↓
ViewModel (Update State)
     ↓
(循环)
```

**完整实现示例：**

```kotlin
// 1. 定义 UI 状态
data class TodoUiState(
    val todos: List<Todo> = emptyList(),
    val filter: FilterType = FilterType.ALL,
    val isLoading: Boolean = false
)

// 2. 定义 UI 事件
sealed class TodoEvent {
    data class AddTodo(val title: String) : TodoEvent()
    data class DeleteTodo(val id: String) : TodoEvent()
    data class ToggleTodo(val id: String) : TodoEvent()
    data class ChangeFilter(val filter: FilterType) : TodoEvent()
}

// 3. ViewModel 处理事件和状态
class TodoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()
    
    // 单一事件处理入口
    fun onEvent(event: TodoEvent) {
        when (event) {
            is TodoEvent.AddTodo -> addTodo(event.title)
            is TodoEvent.DeleteTodo -> deleteTodo(event.id)
            is TodoEvent.ToggleTodo -> toggleTodo(event.id)
            is TodoEvent.ChangeFilter -> changeFilter(event.filter)
        }
    }
    
    private fun addTodo(title: String) {
        val newTodo = Todo(id = UUID.randomUUID().toString(), title = title)
        _uiState.update { it.copy(
            todos = it.todos + newTodo
        )}
    }
    
    private fun deleteTodo(id: String) {
        _uiState.update { it.copy(
            todos = it.todos.filterNot { todo -> todo.id == id }
        )}
    }
    
    private fun toggleTodo(id: String) {
        _uiState.update { currentState ->
            currentState.copy(
                todos = currentState.todos.map { todo ->
                    if (todo.id == id) {
                        todo.copy(isCompleted = !todo.isCompleted)
                    } else {
                        todo
                    }
                }
            )
        }
    }
    
    private fun changeFilter(filter: FilterType) {
        _uiState.update { it.copy(filter = filter) }
    }
}

// 4. UI 层消费状态、发送事件
@Composable
fun TodoScreen(viewModel: TodoViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    TodoContent(
        uiState = uiState,
        onEvent = viewModel::onEvent // 传递事件处理器
    )
}

@Composable
fun TodoContent(
    uiState: TodoUiState,
    onEvent: (TodoEvent) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    
    Column {
        // 输入框
        Row {
            TextField(
                value = inputText,
                onValueChange = { inputText = it }
            )
            Button(onClick = {
                if (inputText.isNotBlank()) {
                    onEvent(TodoEvent.AddTodo(inputText))
                    inputText = ""
                }
            }) {
                Text("添加")
            }
        }
        
        // 过滤器
        FilterTabs(
            selectedFilter = uiState.filter,
            onFilterChange = { onEvent(TodoEvent.ChangeFilter(it)) }
        )
        
        // 待办列表
        LazyColumn {
            items(uiState.todos, key = { it.id }) { todo ->
                TodoItem(
                    todo = todo,
                    onToggle = { onEvent(TodoEvent.ToggleTodo(todo.id)) },
                    onDelete = { onEvent(TodoEvent.DeleteTodo(todo.id)) }
                )
            }
        }
    }
}
```

**优势：**
- ✅ 状态可预测
- ✅ 易于测试
- ✅ 便于调试
- ✅ 支持状态恢复

---

## 23. 如何在 ViewModel 中使用 Repository 模式？

**答案：**

Repository 模式抽象数据源，ViewModel 不需要知道数据来自网络还是本地。

**完整架构：**

```kotlin
// 1. 数据模型
data class Article(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long
)

// 2. 数据源接口
interface ArticleRepository {
    suspend fun getArticles(): List<Article>
    suspend fun getArticle(id: String): Article
    suspend fun saveArticle(article: Article)
    suspend fun deleteArticle(id: String)
}

// 3. Repository 实现
class ArticleRepositoryImpl(
    private val remoteDataSource: ArticleRemoteDataSource,
    private val localDataSource: ArticleLocalDataSource
) : ArticleRepository {
    
    override suspend fun getArticles(): List<Article> {
        return try {
            // 先从网络获取
            val articles = remoteDataSource.fetchArticles()
            // 缓存到本地
            localDataSource.saveArticles(articles)
            articles
        } catch (e: Exception) {
            // 网络失败，从本地获取
            localDataSource.getArticles()
        }
    }
    
    override suspend fun getArticle(id: String): Article {
        // 先查本地
        return localDataSource.getArticle(id) 
            ?: remoteDataSource.fetchArticle(id)
    }
    
    override suspend fun saveArticle(article: Article) {
        localDataSource.saveArticle(article)
        remoteDataSource.uploadArticle(article)
    }
    
    override suspend fun deleteArticle(id: String) {
        localDataSource.deleteArticle(id)
        remoteDataSource.deleteArticle(id)
    }
}

// 4. ViewModel 使用 Repository
class ArticleViewModel(
    private val repository: ArticleRepository
) : ViewModel() {
    
    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadArticles()
    }
    
    fun loadArticles() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _articles.value = repository.getArticles()
            } catch (e: Exception) {
                // 处理错误
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteArticle(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteArticle(id)
                _articles.value = _articles.value.filterNot { it.id == id }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
}

// 5. Compose UI
@Composable
fun ArticleScreen(viewModel: ArticleViewModel = viewModel()) {
    val articles by viewModel.articles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    if (isLoading) {
        LoadingIndicator()
    } else {
        ArticleList(
            articles = articles,
            onDelete = { viewModel.deleteArticle(it.id) }
        )
    }
}
```

**使用 Hilt 依赖注入：**

```kotlin
// Repository 模块
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideArticleRepository(
        remoteDataSource: ArticleRemoteDataSource,
        localDataSource: ArticleLocalDataSource
    ): ArticleRepository {
        return ArticleRepositoryImpl(remoteDataSource, localDataSource)
    }
}

// ViewModel 注入
@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val repository: ArticleRepository
) : ViewModel() {
    // ...
}

// Compose 中使用
@Composable
fun ArticleScreen(viewModel: ArticleViewModel = hiltViewModel()) {
    // ...
}
```

---

## 24. ViewModel 之间如何通信？

**答案：**

ViewModel 之间通常不应该直接通信，但有几种推荐的方式实现数据共享。

**方案对比：**

### 方案1：共享 Repository（推荐）

```kotlin
// 共享同一个 Repository 实例
class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    val user = userRepository.currentUser
}

class SettingsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    val user = userRepository.currentUser
    
    fun updateSettings() {
        userRepository.updateUser(...)
    }
}

// Repository 持有共享状态
class UserRepository {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
}
```

### 方案2：共享 ViewModel（作用域提升）

```kotlin
// Activity 级别的共享 ViewModel
@Composable
fun MainScreen() {
    val sharedViewModel: SharedViewModel = viewModel()
    
    NavHost(...) {
        composable("screen1") {
            Screen1(sharedViewModel)
        }
        composable("screen2") {
            Screen2(sharedViewModel)
        }
    }
}

@Composable
fun Screen1(sharedViewModel: SharedViewModel) {
    val data by sharedViewModel.data.collectAsState()
    // 使用共享数据
}

@Composable
fun Screen2(sharedViewModel: SharedViewModel) {
    val data by sharedViewModel.data.collectAsState()
    Button(onClick = { sharedViewModel.updateData() }) {
        Text("更新")
    }
}
```

### 方案3：事件总线（不推荐，但可用于解耦场景）

```kotlin
// 定义事件
sealed class AppEvent {
    data class UserLoggedIn(val user: User) : AppEvent()
    object UserLoggedOut : AppEvent()
}

// 事件总线
object EventBus {
    private val _events = MutableSharedFlow<AppEvent>()
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()
    
    suspend fun emit(event: AppEvent) {
        _events.emit(event)
    }
}

// ViewModel 1 发送事件
class LoginViewModel : ViewModel() {
    fun login(user: User) {
        viewModelScope.launch {
            EventBus.emit(AppEvent.UserLoggedIn(user))
        }
    }
}

// ViewModel 2 接收事件
class HomeViewModel : ViewModel() {
    init {
        viewModelScope.launch {
            EventBus.events.collect { event ->
                when (event) {
                    is AppEvent.UserLoggedIn -> handleLogin(event.user)
                    is AppEvent.UserLoggedOut -> handleLogout()
                }
            }
        }
    }
}
```

### 方案4：Navigation 参数传递

```kotlin
// 通过导航传递数据
navController.navigate("detail/${item.id}")

// 目标页面接收
@Composable
fun DetailScreen(itemId: String) {
    val viewModel: DetailViewModel = viewModel()
    
    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }
}
```

**最佳实践：**
1. **优先使用共享 Repository** - 最清晰
2. **简单情况使用共享 ViewModel** - 同一导航图内
3. **避免事件总线** - 容易导致代码难以追踪
4. **通过导航传递数据** - 页面间通信

---

**第4部分完成！共 6 题**

