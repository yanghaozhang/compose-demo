# Compose UI 面试题 - 第3部分：状态管理

## 13. remember 和 mutableStateOf 的作用是什么？

**答案：**

这是 Compose 状态管理的核心 API。

**mutableStateOf** - 创建可观察的状态：
```kotlin
val count = mutableStateOf(0) // State<Int>
count.value = 1 // 修改值会触发重组
```

**remember** - 在重组之间保存状态：
```kotlin
@Composable
fun Counter() {
    // ❌ 错误：每次重组都会重置为0
    var count = mutableStateOf(0)
    
    // ✅ 正确：remember 保存状态
    var count by remember { mutableStateOf(0) }
    
    Button(onClick = { count++ }) {
        Text("点击次数: $count")
    }
}
```

**关键区别：**

| API | 作用 | 使用场景 |
|-----|------|---------|
| mutableStateOf | 创建可观察状态 | 需要触发UI更新的数据 |
| remember | 记忆值避免重组重置 | 保存UI状态 |

**委托属性语法：**

```kotlin
// 完整写法
val count: MutableState<Int> = remember { mutableStateOf(0) }
count.value = 1

// 委托写法（推荐）
var count by remember { mutableStateOf(0) }
count = 1 // 更简洁

// 只读状态
val count: State<Int> = remember { mutableStateOf(0) }
// count.value = 1 // 编译错误，只读
```

---

## 14. 什么是状态提升（State Hoisting）？为什么需要它？

**答案：**

状态提升是将状态从子组件移到父组件的模式，使组件变得无状态和可重用。

**为什么需要：**
1. **单一数据源** - 避免状态重复
2. **可重用性** - 组件不依赖内部状态
3. **可测试性** - 更容易测试
4. **共享状态** - 多个组件共享同一状态

**示例对比：**

```kotlin
// ❌ 有状态组件（不推荐）
@Composable
fun StatefulCounter() {
    var count by remember { mutableStateOf(0) }
    
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}
// 问题：状态被封装，外部无法控制

// ✅ 无状态组件（推荐）
@Composable
fun StatelessCounter(
    count: Int,
    onIncrement: () -> Unit
) {
    Button(onClick = onIncrement) {
        Text("Count: $count")
    }
}

// 使用方
@Composable
fun ParentComponent() {
    var count by remember { mutableStateOf(0) }
    
    StatelessCounter(
        count = count,
        onIncrement = { count++ }
    )
}
```

**实战案例 - 搜索框：**

```kotlin
// 无状态搜索框组件
@Composable
fun SearchBar(
    query: String,          // 状态由父组件控制
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("搜索...") },
        modifier = modifier
    )
}

// 父组件管理状态
@Composable
fun SearchScreen() {
    var searchQuery by remember { mutableStateOf("") }
    
    Column {
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )
        
        // 可以在多处使用 searchQuery
        Text("搜索: $searchQuery")
        FilteredList(query = searchQuery)
    }
}
```

**状态提升的层级：**
- 提升到最低公共祖先
- 提升到需要读取或修改的最低层级

---

## 15. rememberSaveable 和 remember 有什么区别？

**答案：**

两者都用于保存状态，但在配置更改（如屏幕旋转）时表现不同。

**区别对比：**

| 特性 | remember | rememberSaveable |
|------|----------|------------------|
| 重组时保存 | ✅ | ✅ |
| 配置更改时保存 | ❌ | ✅ |
| 进程死亡后保存 | ❌ | ✅ |
| 性能 | 更快 | 稍慢（序列化） |
| 支持类型 | 任意类型 | 可序列化类型 |

**示例：**

```kotlin
@Composable
fun CounterComparison() {
    // 屏幕旋转后会丢失
    var count1 by remember { mutableStateOf(0) }
    
    // 屏幕旋转后保留
    var count2 by rememberSaveable { mutableStateOf(0) }
    
    Column {
        Text("remember: $count1")
        Text("rememberSaveable: $count2")
        
        Button(onClick = { 
            count1++
            count2++
        }) {
            Text("增加")
        }
    }
}
```

**自定义类型保存：**

```kotlin
// 方式1：使用 Parcelable
@Parcelize
data class User(val name: String, val age: Int) : Parcelable

@Composable
fun UserScreen() {
    var user by rememberSaveable {
        mutableStateOf(User("张三", 25))
    }
}

// 方式2：使用自定义 Saver
data class Counter(var count: Int)

val CounterSaver = Saver<Counter, Int>(
    save = { it.count },
    restore = { Counter(it) }
)

@Composable
fun CustomSaverExample() {
    var counter by rememberSaveable(stateSaver = CounterSaver) {
        mutableStateOf(Counter(0))
    }
}
```

**使用建议：**
- **表单输入** → rememberSaveable
- **临时UI状态**（如展开/折叠）→ remember
- **用户数据** → rememberSaveable
- **计算结果** → remember

---

## 16. derivedStateOf 的作用是什么？什么时候使用？

**答案：**

`derivedStateOf` 用于创建派生状态，只在依赖的状态改变时才重新计算，避免不必要的重组。

**使用场景：**
- 从其他状态计算得出的值
- 避免每次重组都重新计算
- 优化性能

**示例对比：**

```kotlin
@Composable
fun WithoutDerivedState() {
    var firstName by remember { mutableStateOf("张") }
    var lastName by remember { mutableStateOf("三") }
    
    // ❌ 每次重组都会创建新的字符串
    val fullName = "$firstName$lastName"
    
    Text(fullName)
}

@Composable
fun WithDerivedState() {
    var firstName by remember { mutableStateOf("张") }
    var lastName by remember { mutableStateOf("") }
    
    // ✅ 只在 firstName 或 lastName 改变时计算
    val fullName by remember {
        derivedStateOf { "$firstName$lastName" }
    }
    
    Text(fullName)
}
```

**复杂计算的优化：**

```kotlin
@Composable
fun ListExample() {
    var items by remember { mutableStateOf(listOf<Item>()) }
    var filterQuery by remember { mutableStateOf("") }
    
    // ✅ 只在 items 或 filterQuery 改变时过滤
    val filteredItems by remember {
        derivedStateOf {
            if (filterQuery.isEmpty()) {
                items
            } else {
                items.filter { it.name.contains(filterQuery, ignoreCase = true) }
            }
        }
    }
    
    LazyColumn {
        items(filteredItems) { item ->
            ItemCard(item)
        }
    }
}
```

**LazyList 滚动状态示例：**

```kotlin
@Composable
fun ScrollExample() {
    val listState = rememberLazyListState()
    
    // ✅ 只在滚动位置改变时重新计算
    val showButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }
    
    Box {
        LazyColumn(state = listState) {
            items(100) { Text("Item $it") }
        }
        
        // 滚动到顶部按钮
        if (showButton) {
            FloatingActionButton(
                onClick = { /* 滚动到顶部 */ },
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Icon(Icons.Default.ArrowUpward, null)
            }
        }
    }
}
```

**性能对比：**

```kotlin
// ❌ 性能差：每次重组都计算
val expensiveValue = items.filter { ... }.map { ... }.sortedBy { ... }

// ✅ 性能好：只在 items 改变时计算
val expensiveValue by remember {
    derivedStateOf {
        items.filter { ... }.map { ... }.sortedBy { ... }
    }
}
```

---

## 17. snapshotFlow 是什么？如何使用？

**答案：**

`snapshotFlow` 将 Compose 的 State 转换为 Kotlin Flow，用于与协程集成。

**作用：**
- 将状态变化转为 Flow
- 可使用 Flow 操作符（debounce、filter等）
- 与挂起函数配合使用

**基础用法：**

```kotlin
@Composable
fun SearchWithDebounce() {
    var searchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        snapshotFlow { searchQuery }
            .debounce(300) // 防抖：等待300ms无输入后执行
            .filter { it.isNotEmpty() }
            .collect { query ->
                // 执行搜索
                performSearch(query)
            }
    }
    
    TextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        label = { Text("搜索") }
    )
}
```

**滚动监听示例：**

```kotlin
@Composable
fun ScrollMonitor() {
    val listState = rememberLazyListState()
    
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                Log.d("Scroll", "Current index: $index")
                
                // 接近底部时加载更多
                if (index > listState.layoutInfo.totalItemsCount - 5) {
                    loadMoreData()
                }
            }
    }
    
    LazyColumn(state = listState) {
        items(100) { Text("Item $it") }
    }
}
```

**表单自动保存：**

```kotlin
@Composable
fun AutoSaveForm() {
    var formData by remember { mutableStateOf(FormData()) }
    
    LaunchedEffect(Unit) {
        snapshotFlow { formData }
            .debounce(1000) // 1秒后自动保存
            .collect { data ->
                saveToDatabase(data)
                Log.d("AutoSave", "表单已保存")
            }
    }
    
    // 表单输入组件...
}
```

**结合多个状态：**

```kotlin
@Composable
fun CombinedStates() {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        snapshotFlow { firstName to lastName }
            .filter { (first, last) -> 
                first.isNotEmpty() && last.isNotEmpty() 
            }
            .collect { (first, last) ->
                Log.d("Name", "完整姓名: $first $last")
            }
    }
}
```

---

## 18. 什么是副作用（Side Effects）？Compose 提供了哪些副作用 API？

**答案：**

副作用是在可组合函数之外执行的操作，如网络请求、数据库操作、订阅等。

**常用副作用 API：**

### 1. LaunchedEffect

在组合期间启动协程，key 改变时重启。

```kotlin
@Composable
fun DataLoader(userId: String) {
    var userData by remember { mutableStateOf<User?>(null) }
    
    LaunchedEffect(userId) { // userId 改变时重新执行
        userData = loadUserData(userId)
    }
    
    userData?.let { UserCard(it) }
}
```

### 2. DisposableEffect

需要清理的副作用（如注册监听器）。

```kotlin
@Composable
fun LocationTracker() {
    val context = LocalContext.current
    
    DisposableEffect(context) {
        val listener = LocationListener { location ->
            // 处理位置更新
        }
        
        // 注册监听
        locationManager.requestLocationUpdates(listener)
        
        // 清理
        onDispose {
            locationManager.removeUpdates(listener)
        }
    }
}
```

### 3. SideEffect

在每次成功重组后执行。

```kotlin
@Composable
fun Analytics(screen: String) {
    SideEffect {
        // 每次重组后发送分析事件
        analytics.logScreenView(screen)
    }
}
```

### 4. rememberCoroutineScope

获取协程作用域，用于事件处理。

```kotlin
@Composable
fun MyScreen() {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    Button(onClick = {
        scope.launch {
            snackbarHostState.showSnackbar("操作成功")
        }
    }) {
        Text("点击")
    }
}
```

### 5. rememberUpdatedState

捕获最新值，避免闭包陈旧问题。

```kotlin
@Composable
fun Timer(onTimeout: () -> Unit) {
    val currentOnTimeout by rememberUpdatedState(onTimeout)
    
    LaunchedEffect(Unit) {
        delay(5000)
        currentOnTimeout() // 确保调用最新的 onTimeout
    }
}
```

**使用场景对比：**

| API | 使用场景 | 执行时机 |
|-----|---------|---------|
| LaunchedEffect | 协程操作 | key改变或首次组合 |
| DisposableEffect | 需要清理的操作 | key改变或首次组合 |
| SideEffect | 非挂起副作用 | 每次成功重组后 |
| rememberCoroutineScope | 事件回调中启动协程 | 按需 |
| rememberUpdatedState | 捕获最新值 | 每次重组 |

**完整示例：**

```kotlin
@Composable
fun CompleteExample(userId: String, onUserLoaded: (User) -> Unit) {
    var user by remember { mutableStateOf<User?>(null) }
    val scope = rememberCoroutineScope()
    val currentOnUserLoaded by rememberUpdatedState(onUserLoaded)
    
    // 加载数据
    LaunchedEffect(userId) {
        user = loadUser(userId)
    }
    
    // 数据加载后回调
    user?.let { loadedUser ->
        SideEffect {
            currentOnUserLoaded(loadedUser)
        }
    }
    
    // 注册监听
    DisposableEffect(userId) {
        val listener = createListener(userId)
        registerListener(listener)
        
        onDispose {
            unregisterListener(listener)
        }
    }
    
    // UI
    Button(onClick = {
        scope.launch {
            refreshUser(userId)
        }
    }) {
        Text("刷新")
    }
}
```

---

**第3部分完成！共 6 题**

