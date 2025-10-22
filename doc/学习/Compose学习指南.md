# Jetpack Compose UI 完整学习指南

> 通过实战代码学习现代 Android UI 开发

## 📚 目录

- [项目概述](#项目概述)
- [快速开始](#快速开始)
- [核心概念](#核心概念)
- [代码导航](#代码导航)
- [学习路径](#学习路径)
- [概念详解](#概念详解)
- [最佳实践](#最佳实践)
- [常见问题](#常见问题)

---

## 项目概述

这是一个专门为学习 Jetpack Compose 设计的完整项目，包含 5 个独立的学习模块，每个模块都有详细的中文注释。

### 🎯 学习目标

- ✅ 掌握 Compose 声明式 UI 编程
- ✅ 理解状态管理和响应式编程
- ✅ 学会使用 Material3 组件库
- ✅ 掌握 ViewModel 架构模式
- ✅ 学会使用 Navigation 导航框架

### 📂 项目结构

```
app/src/main/java/com/test/compose/
├── MainActivity.kt              # 主入口页面（功能列表）
├── TextButtonActivity.kt        # Text & Button 组件示例
├── ListActivity.kt              # 列表组件示例
├── ViewModelActivity.kt         # ViewModel 绑定示例
└── NavigationActivity.kt        # Navigation 导航示例
```

---

## 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 35
- Gradle 8.9

### 运行项目

1. **用 Android Studio 打开项目**
   ```
   File -> Open -> 选择项目目录
   ```

2. **等待 Gradle 同步完成**
   - 首次打开会自动下载依赖
   - 请确保网络连接正常

3. **运行应用**
   - 连接 Android 设备或启动模拟器
   - 点击运行按钮 ▶️
   - 最低支持 Android 7.0 (API 25)

### 查看代码预览

在 Android Studio 中打开任意 `.kt` 文件，在代码右侧会看到预览面板，展示 UI 效果（无需运行应用）。

---

## 核心概念

### 1. 声明式 UI

**传统方式（命令式）：**
```kotlin
// 需要手动操作视图
val textView = findViewById<TextView>(R.id.textView)
textView.text = "Hello"
textView.textSize = 16f
textView.setTextColor(Color.BLUE)
```

**Compose 方式（声明式）：**
```kotlin
// 直接描述 UI 应该是什么样子
Text(
    text = "Hello",
    fontSize = 16.sp,
    color = Color.Blue
)
```

**学习位置：** `MainActivity.kt` 第 267-276 行

---

### 2. Composable 函数

**什么是 @Composable？**

```kotlin
@Composable
fun Greeting(name: String) {
    Text("Hello $name!")
}
```

- 使用 `@Composable` 注解标记
- 可以调用其他 Composable 函数
- 会在状态改变时自动重组（重新执行）
- 无返回值（返回类型是 Unit）

**学习位置：** `MainActivity.kt` 第 128-220 行

---

### 3. 状态（State）

**为什么需要状态？**

UI 需要响应数据变化。在 Compose 中，当状态改变时，UI 会自动更新。

**基本用法：**

```kotlin
@Composable
fun Counter() {
    // remember: 在重组时保持状态
    // mutableStateOf: 创建可观察的状态
    var count by remember { mutableStateOf(0) }
    
    Button(onClick = { count++ }) {
        Text("点击次数: $count")
    }
}
```

**关键点：**
- `remember`：在重组时记住值
- `mutableStateOf`：创建可变状态
- `by` 关键字：属性委托，简化读写

**学习位置：** 
- `TextButtonActivity.kt` 第 62-63 行（本地状态）
- `ViewModelActivity.kt` 第 45-57 行（ViewModel 状态）

---

### 4. Modifier（修饰符）

**什么是 Modifier？**

Modifier 用于修改组件的外观和行为，类似于传统 View 的属性设置。

**常用修饰符：**

```kotlin
Text(
    text = "Hello",
    modifier = Modifier
        .fillMaxWidth()        // 填充宽度
        .padding(16.dp)        // 内边距
        .background(Color.Red) // 背景色
        .clickable { }         // 点击事件
)
```

**顺序很重要：**

```kotlin
// 先 padding 后 background - padding 外有背景
Modifier.padding(16.dp).background(Color.Red)

// 先 background 后 padding - padding 内有背景
Modifier.background(Color.Red).padding(16.dp)
```

**学习位置：** `MainActivity.kt` 第 242-245 行

---

### 5. 布局（Layout）

**三大基础布局：**

```kotlin
// Column: 垂直布局（从上到下）
Column {
    Text("第一行")
    Text("第二行")
}

// Row: 水平布局（从左到右）
Row {
    Text("左边")
    Text("右边")
}

// Box: 层叠布局（重叠）
Box {
    Image(...)        // 底层
    Text("覆盖文字") // 上层
}
```

**学习位置：** `MainActivity.kt` 第 247-303 行

---

### 6. 列表（List）

**LazyColumn vs Column：**

```kotlin
// Column: 渲染所有子元素（适合少量元素）
Column {
    items.forEach { item ->
        ItemCard(item)
    }
}

// LazyColumn: 延迟加载（适合大量元素）
LazyColumn {
    items(items) { item ->
        ItemCard(item)
    }
}
```

**为什么用 Lazy？**
- 只渲染可见的项
- 类似 RecyclerView
- 性能更好

**学习位置：** `ListActivity.kt` 第 188-220 行

---

## 代码导航

### 📱 模块 1: MainActivity.kt

**位置：** `app/src/main/java/com/test/compose/MainActivity.kt`

**学习内容：**
- ✅ Compose 基础结构（第 63-124 行）
- ✅ LazyColumn 列表（第 192-220 行）
- ✅ Card 卡片组件（第 236-305 行）
- ✅ 数据类使用（第 42-54 行）

**核心代码片段：**

```kotlin
// LazyColumn 基本用法
LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    items(features) { feature ->
        FeatureCard(feature = feature, onClick = { /* ... */ })
    }
}
```

**💡 学习重点：**
- 理解 Scaffold 脚手架的作用
- 掌握列表渲染的基本方法
- 学会创建可重用组件

---

### 📝 模块 2: TextButtonActivity.kt

**位置：** `app/src/main/java/com/test/compose/TextButtonActivity.kt`

**学习内容：**
- ✅ Text 组件样式（第 76-106 行）
- ✅ Button 所有变体（第 130-235 行）
- ✅ IconButton 使用（第 251-295 行）
- ✅ 状态管理（第 62-63 行）

**Text 组件示例：**

```kotlin
// 基础文本
Text(
    text = "这是基础文本",
    style = MaterialTheme.typography.bodyLarge
)

// 自定义样式
Text(
    text = "大标题文本",
    style = MaterialTheme.typography.headlineLarge,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary
)

// 对齐方式
Text(
    text = "居中对齐",
    modifier = Modifier.fillMaxWidth(),
    textAlign = TextAlign.Center
)
```

**Button 变体：**

| 类型 | 用途 | 代码位置 |
|------|------|----------|
| Button | 主要操作 | 第 130 行 |
| FilledTonalButton | 次要操作 | 第 143 行 |
| OutlinedButton | 轮廓按钮 | 第 156 行 |
| ElevatedButton | 提升按钮 | 第 169 行 |
| TextButton | 文本按钮 | 第 182 行 |
| IconButton | 图标按钮 | 第 261 行 |

**💡 学习重点：**
- Material3 的字体和颜色系统
- 不同按钮类型的使用场景
- 如何用状态实现交互

---

### 📋 模块 3: ListActivity.kt

**位置：** `app/src/main/java/com/test/compose/ListActivity.kt`

**学习内容：**
- ✅ LazyColumn 垂直列表（第 203-257 行）
- ✅ LazyRow 横向列表（第 286-340 行）
- ✅ LazyVerticalGrid 网格（第 485-520 行）
- ✅ 列表项交互（第 259-284 行）

**四种列表类型：**

**1. 垂直列表（LazyColumn）：**
```kotlin
LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(users) { user ->
        UserCard(user)
    }
}
```

**2. 横向列表（LazyRow）：**
```kotlin
LazyRow(
    horizontalArrangement = Arrangement.spacedBy(12.dp)
) {
    items(products) { product ->
        ProductCard(product)
    }
}
```

**3. 网格列表（LazyVerticalGrid）：**
```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(2)  // 2列
) {
    items(colors) { color ->
        ColorCard(color)
    }
}
```

**4. 带索引的列表：**
```kotlin
LazyColumn {
    itemsIndexed(items) { index, item ->
        Text("第 ${index + 1} 项: ${item.name}")
    }
}
```

**💡 学习重点：**
- Lazy 组件的性能优势
- items() vs itemsIndexed() 的区别
- 如何实现列表项状态管理

---

### 🎯 模块 4: ViewModelActivity.kt

**位置：** `app/src/main/java/com/test/compose/ViewModelActivity.kt`

**学习内容：**
- ✅ ViewModel 基础（第 45-87 行）
- ✅ 状态管理模式（第 45-57 行）
- ✅ 列表状态（第 107-158 行）
- ✅ 表单处理（第 178-229 行）

**三个完整示例：**

**1. 计数器 ViewModel：**
```kotlin
class CounterViewModel : ViewModel() {
    // 私有可变状态
    private val _count = mutableIntStateOf(0)
    
    // 公开只读状态
    val count: State<Int> = _count
    
    // 修改状态的方法
    fun increment() {
        _count.intValue++
    }
}

// 在 Composable 中使用
@Composable
fun Counter(viewModel: CounterViewModel = viewModel()) {
    Text("计数: ${viewModel.count.value}")
    Button(onClick = { viewModel.increment() }) {
        Text("增加")
    }
}
```

**2. 待办事项 ViewModel：**
```kotlin
class TodoViewModel : ViewModel() {
    // 可观察列表
    private val _todos = mutableStateListOf<TodoItem>()
    val todos: List<TodoItem> = _todos
    
    fun addTodo(text: String) {
        _todos.add(TodoItem(id = nextId++, text = text))
    }
    
    fun toggleTodo(id: Int) {
        val index = _todos.indexOfFirst { it.id == id }
        if (index != -1) {
            _todos[index] = _todos[index].copy(
                isCompleted = !_todos[index].isCompleted
            )
        }
    }
}
```

**3. 表单 ViewModel：**
```kotlin
class UserFormViewModel : ViewModel() {
    private val _formData = mutableStateOf(UserFormData())
    val formData: State<UserFormData> = _formData
    
    fun updateName(name: String) {
        _formData.value = _formData.value.copy(name = name)
    }
    
    fun isValid(): Boolean {
        return _formData.value.name.isNotBlank()
    }
}
```

**💡 学习重点：**
- ViewModel 生命周期优势
- 单向数据流模式
- 状态提升（State Hoisting）
- 业务逻辑与 UI 分离

**ViewModel 优势：**
| 特性 | 说明 |
|------|------|
| 生命周期 | 比 Activity 更长，配置更改时不会销毁 |
| 状态保持 | 屏幕旋转时自动保持数据 |
| 业务逻辑 | 分离 UI 和业务逻辑 |
| 可测试性 | 独立于 UI，易于单元测试 |

---

### 🧭 模块 5: NavigationActivity.kt

**位置：** `app/src/main/java/com/test/compose/NavigationActivity.kt`

**学习内容：**
- ✅ Navigation 架构（第 154-212 行）
- ✅ 路由定义（第 73-131 行）
- ✅ 带参数导航（第 238-263 行）
- ✅ 底部导航栏（第 265-294 行）

**完整导航架构：**

**1. 路由定义（Sealed Class）：**
```kotlin
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "首页", Icons.Default.Home)
    object Profile : Screen("profile", "个人", Icons.Default.Person)
    
    // 带参数的路由
    object Details : Screen("details/{itemId}/{itemName}", "详情", Icons.Default.Info) {
        fun createRoute(itemId: Int, itemName: String) = "details/$itemId/$itemName"
    }
}
```

**2. NavHost 配置：**
```kotlin
@Composable
fun NavigationApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // 简单路由
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        
        // 带参数的路由
        composable(
            route = Screen.Details.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.IntType },
                navArgument("itemName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
            val itemName = backStackEntry.arguments?.getString("itemName") ?: ""
            DetailsScreen(itemId, itemName)
        }
    }
}
```

**3. 导航操作：**
```kotlin
// 简单导航
navController.navigate(Screen.Home.route)

// 带参数导航
navController.navigate(Screen.Details.createRoute(1, "手机"))

// 返回
navController.navigateUp()

// 返回栈管理
navController.navigate(Screen.Home.route) {
    popUpTo(navController.graph.startDestinationId) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
```

**4. 底部导航栏：**
```kotlin
@Composable
fun NavigationBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
```

**💡 学习重点：**
- NavController 的创建和使用
- 类型安全的路由定义
- 参数传递机制
- 返回栈管理策略
- 页面转场动画

**Navigation 特性对比：**

| 特性 | Compose Navigation | Fragment Navigation |
|------|-------------------|-------------------|
| 配置方式 | 代码（声明式） | XML 导航图 |
| 类型安全 | Sealed Class | Safe Args 插件 |
| 参数传递 | 路径参数 + Arguments | Bundle |
| 动画 | enterTransition/exitTransition | XML animations |
| 深度链接 | 原生支持 | 需要配置 |

---

## 学习路径

### 🌱 初级阶段（1-2 周）

**目标：** 理解 Compose 基础，能写简单 UI

**学习顺序：**

1. **Day 1-2: 基础概念**
   - 阅读 `MainActivity.kt` 全部注释
   - 理解 @Composable、Modifier、State
   - 运行应用，查看效果

2. **Day 3-4: Text & Button**
   - 学习 `TextButtonActivity.kt`
   - 练习：自己创建不同样式的按钮
   - 实现一个简单的计数器

3. **Day 5-7: 布局系统**
   - 重点学习 Column、Row、Box
   - 练习：仿写微信聊天界面
   - 理解 weight、padding、arrangement

**练习项目：**
- 制作一个登录页面（包含 Text、TextField、Button）
- 制作一个商品卡片（使用 Column、Row、Image）

---

### 🌿 中级阶段（2-3 周）

**目标：** 掌握列表和状态管理

**学习顺序：**

1. **Week 1: 列表组件**
   - 学习 `ListActivity.kt`
   - 理解 LazyColumn vs Column
   - 掌握 items() 和 itemsIndexed()
   - 练习：制作通讯录列表

2. **Week 2: ViewModel**
   - 学习 `ViewModelActivity.kt`
   - 理解 ViewModel 生命周期
   - 掌握状态提升模式
   - 练习：制作待办事项应用

3. **Week 3: 综合练习**
   - 结合列表 + ViewModel
   - 练习：制作新闻列表应用
   - 实现下拉刷新、加载更多

**练习项目：**
- 待办事项应用（添加、删除、标记完成）
- 购物车应用（商品列表、数量增减、价格计算）

---

### 🌳 高级阶段（3-4 周）

**目标：** 掌握完整应用架构

**学习顺序：**

1. **Week 1-2: Navigation**
   - 学习 `NavigationActivity.kt`
   - 理解路由和参数传递
   - 掌握底部导航栏
   - 练习：制作多页面应用

2. **Week 3: 架构整合**
   - ViewModel + Navigation 结合
   - 理解数据流向
   - 实现页面间数据传递

3. **Week 4: 实战项目**
   - 制作完整应用
   - 集成网络请求（Retrofit）
   - 本地存储（Room）

**练习项目：**
- 新闻应用（列表、详情、分类、搜索）
- 电商应用（首页、分类、购物车、我的）
- 社交应用（动态、评论、个人主页）

---

## 概念详解

### 重组（Recomposition）

**什么是重组？**

当状态改变时，Compose 会重新执行相关的 Composable 函数，更新 UI。

**示例：**

```kotlin
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    
    // 点击按钮 -> count 改变 -> 这个函数重新执行 -> UI 更新
    Button(onClick = { count++ }) {
        Text("Count: $count")  // 这里会显示新的值
    }
}
```

**重组特点：**
- 只重组需要更新的部分（智能重组）
- 可能被跳过（如果输入没变）
- 可能按任意顺序执行
- 可能并行执行

**最佳实践：**
```kotlin
// ✅ 好：Composable 函数无副作用
@Composable
fun Good(count: Int) {
    Text("Count: $count")
}

// ❌ 坏：有副作用，每次重组都执行
@Composable
fun Bad(count: Int) {
    Log.d("TAG", "Count: $count")  // 不要这样！
    Text("Count: $count")
}

// ✅ 好：使用 LaunchedEffect 处理副作用
@Composable
fun Better(count: Int) {
    LaunchedEffect(count) {
        Log.d("TAG", "Count: $count")  // 只在 count 改变时执行
    }
    Text("Count: $count")
}
```

---

### 状态提升（State Hoisting）

**什么是状态提升？**

将状态移到调用者，让组件变得无状态（Stateless），更容易重用和测试。

**有状态组件（Stateful）：**
```kotlin
@Composable
fun StatefulCounter() {
    var count by remember { mutableStateOf(0) }
    
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}
```

**无状态组件（Stateless）：**
```kotlin
@Composable
fun StatelessCounter(
    count: Int,
    onIncrement: () -> Unit
) {
    Button(onClick = onIncrement) {
        Text("Count: $count")
    }
}

// 使用
@Composable
fun Parent() {
    var count by remember { mutableStateOf(0) }
    
    StatelessCounter(
        count = count,
        onIncrement = { count++ }
    )
}
```

**优势：**
- 组件更容易重用
- 更容易测试
- 状态集中管理
- 符合单向数据流

**学习位置：** `ViewModelActivity.kt` 第 231-353 行

---

### 副作用（Side Effects）

**什么是副作用？**

在 Composable 函数之外产生的效果，比如网络请求、数据库操作、日志记录等。

**常用副作用 API：**

**1. LaunchedEffect - 协程副作用：**
```kotlin
@Composable
fun LoadDataExample(userId: String) {
    var userData by remember { mutableStateOf<User?>(null) }
    
    LaunchedEffect(userId) {
        // 当 userId 改变时执行
        userData = loadUserData(userId)  // 挂起函数
    }
    
    userData?.let { user ->
        UserCard(user)
    }
}
```

**2. DisposableEffect - 清理副作用：**
```kotlin
@Composable
fun ObserveLifecycle(lifecycle: Lifecycle) {
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            // 处理生命周期事件
        }
        lifecycle.addObserver(observer)
        
        onDispose {
            // 组件离开组合树时清理
            lifecycle.removeObserver(observer)
        }
    }
}
```

**3. SideEffect - 发布状态：**
```kotlin
@Composable
fun Analytics(screenName: String) {
    SideEffect {
        // 每次成功重组后执行
        analytics.logScreenView(screenName)
    }
}
```

---

### Material3 主题系统

**颜色方案（ColorScheme）：**

```kotlin
MaterialTheme.colorScheme.primary           // 主色
MaterialTheme.colorScheme.onPrimary         // 主色上的文字颜色
MaterialTheme.colorScheme.primaryContainer  // 主色容器
MaterialTheme.colorScheme.onPrimaryContainer

MaterialTheme.colorScheme.secondary         // 辅色
MaterialTheme.colorScheme.tertiary          // 第三色
MaterialTheme.colorScheme.error             // 错误色
MaterialTheme.colorScheme.surface           // 表面色
MaterialTheme.colorScheme.background        // 背景色
```

**字体系统（Typography）：**

```kotlin
MaterialTheme.typography.displayLarge       // 最大显示文字
MaterialTheme.typography.displayMedium
MaterialTheme.typography.displaySmall

MaterialTheme.typography.headlineLarge      // 大标题
MaterialTheme.typography.headlineMedium
MaterialTheme.typography.headlineSmall

MaterialTheme.typography.titleLarge         // 标题
MaterialTheme.typography.titleMedium
MaterialTheme.typography.titleSmall

MaterialTheme.typography.bodyLarge          // 正文
MaterialTheme.typography.bodyMedium
MaterialTheme.typography.bodySmall

MaterialTheme.typography.labelLarge         // 标签
MaterialTheme.typography.labelMedium
MaterialTheme.typography.labelSmall
```

**学习位置：** `TextButtonActivity.kt` 第 76-106 行

---

## 最佳实践

### 1. 组件设计原则

**✅ 单一职责：**
```kotlin
// 好：职责单一
@Composable
fun UserAvatar(imageUrl: String, size: Dp) { }

@Composable
fun UserName(name: String) { }

@Composable
fun UserCard(user: User) {
    Row {
        UserAvatar(user.imageUrl, 48.dp)
        UserName(user.name)
    }
}
```

**✅ 可重用：**
```kotlin
// 好：可配置、可重用
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(onClick = onClick, modifier = modifier, enabled = enabled) {
        Text(text)
    }
}
```

---

### 2. 性能优化

**✅ 使用 key：**
```kotlin
LazyColumn {
    items(
        items = users,
        key = { user -> user.id }  // 提供稳定的 key
    ) { user ->
        UserCard(user)
    }
}
```

**✅ 避免在 Composable 中创建对象：**
```kotlin
// ❌ 坏：每次重组都创建新对象
@Composable
fun Bad() {
    val viewModel = MyViewModel()  // 不要这样！
}

// ✅ 好：使用 remember
@Composable
fun Good() {
    val viewModel = remember { MyViewModel() }
}

// ✅ 更好：使用 viewModel()
@Composable
fun Better(viewModel: MyViewModel = viewModel()) {
    // ...
}
```

**✅ 使用 derivedStateOf：**
```kotlin
@Composable
fun TodoList(todos: List<Todo>) {
    // 只在 todos 改变且结果不同时重组
    val completedCount by remember(todos) {
        derivedStateOf { todos.count { it.isCompleted } }
    }
    
    Text("已完成: $completedCount")
}
```

---

### 3. 状态管理

**✅ 状态最小化：**
```kotlin
// ❌ 坏：存储可计算的状态
class BadViewModel : ViewModel() {
    val items = mutableStateListOf<Item>()
    val itemCount = mutableIntStateOf(0)  // 冗余
    
    fun addItem(item: Item) {
        items.add(item)
        itemCount.intValue = items.size  // 需要手动同步
    }
}

// ✅ 好：只存储必要状态
class GoodViewModel : ViewModel() {
    val items = mutableStateListOf<Item>()
    
    val itemCount: Int
        get() = items.size  // 自动计算
}
```

**✅ 状态归属明确：**
```kotlin
// UI 状态 -> remember
@Composable
fun SearchBar() {
    var searchText by remember { mutableStateOf("") }
    // ...
}

// 业务状态 -> ViewModel
class SearchViewModel : ViewModel() {
    val searchResults = mutableStateListOf<Result>()
    // ...
}
```

---

### 4. 命名规范

```kotlin
// Composable 函数：大驼峰，名词
@Composable
fun UserCard() { }

// 普通函数：小驼峰，动词
fun loadUser() { }

// 状态变量
var isLoading by remember { mutableStateOf(false) }
var userName by remember { mutableStateOf("") }

// 回调参数
onClick: () -> Unit
onValueChange: (String) -> Unit
```

---

## 常见问题

### Q1: remember 和 ViewModel 有什么区别？

**remember：**
- 用于 UI 临时状态
- 配置更改（屏幕旋转）时丢失
- 组件离开组合树时清除

**ViewModel：**
- 用于业务数据
- 配置更改时保持
- Activity 销毁时才清除

**示例：**
```kotlin
@Composable
fun SearchScreen(viewModel: SearchViewModel = viewModel()) {
    // UI 状态：用 remember
    var isSearchBarExpanded by remember { mutableStateOf(false) }
    
    // 业务数据：用 ViewModel
    val searchResults = viewModel.searchResults
}
```

---

### Q2: 什么时候用 LazyColumn，什么时候用 Column？

**Column：**
- 少量固定元素（< 20 个）
- 所有元素都需要渲染
- 元素高度不确定

**LazyColumn：**
- 大量或无限元素
- 需要滚动
- 性能要求高

**示例：**
```kotlin
// ✅ 用 Column：固定几个元素
Column {
    Header()
    Content()
    Footer()
}

// ✅ 用 LazyColumn：列表数据
LazyColumn {
    items(users) { user ->
        UserCard(user)
    }
}
```

---

### Q3: 如何在 Composable 中执行网络请求？

使用 `LaunchedEffect`：

```kotlin
@Composable
fun UserProfile(userId: String, viewModel: UserViewModel = viewModel()) {
    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }
    
    val user = viewModel.user.value
    user?.let {
        UserCard(it)
    } ?: LoadingIndicator()
}
```

---

### Q4: 如何传递数据给 Navigation 目标页面？

**方式 1：路径参数（推荐简单数据）：**
```kotlin
// 定义
composable("user/{userId}") { backStackEntry ->
    val userId = backStackEntry.arguments?.getString("userId")
    UserScreen(userId)
}

// 导航
navController.navigate("user/$userId")
```

**方式 2：通过 ViewModel（推荐复杂数据）：**
```kotlin
// 共享 ViewModel
@Composable
fun NavGraph(sharedViewModel: SharedViewModel = viewModel()) {
    NavHost(...) {
        composable("list") {
            ListScreen(
                onItemClick = { item ->
                    sharedViewModel.selectItem(item)
                    navController.navigate("detail")
                }
            )
        }
        composable("detail") {
            DetailScreen(sharedViewModel)
        }
    }
}
```

---

### Q5: 为什么我的 UI 没有更新？

**常见原因：**

1. **忘记使用 State：**
```kotlin
// ❌ 坏
var count = 0  // 改变不会触发重组

// ✅ 好
var count by remember { mutableStateOf(0) }
```

2. **修改了不可观察的对象：**
```kotlin
// ❌ 坏
data class User(var name: String)
val user = remember { User("Tom") }
user.name = "Jerry"  // 不会触发重组

// ✅ 好
data class User(val name: String)
var user by remember { mutableStateOf(User("Tom")) }
user = user.copy(name = "Jerry")  // 会触发重组
```

3. **在列表中直接修改元素：**
```kotlin
// ❌ 坏
val list = remember { mutableStateListOf(1, 2, 3) }
list[0] = 10  // 可能不会触发重组

// ✅ 好
val list = remember { mutableStateListOf(1, 2, 3) }
list.removeAt(0)
list.add(0, 10)
```

---

## 进阶学习资源

### 官方文档
- [Jetpack Compose 官方文档](https://developer.android.com/jetpack/compose)
- [Compose 路线图](https://developer.android.com/jetpack/compose/roadmap)

### 推荐教程
- [Compose Pathway](https://developer.android.com/courses/pathways/compose)
- [Compose Camp](https://developer.android.com/courses/compose-camp)

### 示例项目
- [Now in Android](https://github.com/android/nowinandroid) - Google 官方示例
- [Jetpack Compose Samples](https://github.com/android/compose-samples)

### 社区资源
- [Compose 中文社区](https://compose.net.cn/)
- [掘金 Compose 专栏](https://juejin.cn/tag/Jetpack%20Compose)

---

## 总结

### 你已经学到了

✅ Compose 声明式 UI 编程
✅ 状态管理和响应式更新
✅ Material3 组件库
✅ ViewModel 架构模式  
✅ Navigation 导航框架
✅ 列表性能优化
✅ 最佳实践和设计模式

### 下一步

🎯 实践项目：使用这些知识构建真实应用
🎯 深入学习：动画、手势、自定义组件
🎯 架构进阶：Hilt、Room、Retrofit 集成
🎯 性能优化：使用 Baseline Profiles
🎯 测试：UI 测试、快照测试

---

## 项目信息

- **版本：** 1.0.0
- **最后更新：** 2024-10
- **编译SDK：** 35
- **最低SDK：** 25 (Android 7.0)
- **Compose BOM：** 2024.04.01

---

## 贡献与反馈

如果你在学习过程中：
- 发现代码错误或注释不清楚
- 有更好的示例建议
- 遇到无法解决的问题

欢迎提出 Issue 或贡献代码！

---

**祝学习愉快！Happy Composing! 🎉**

