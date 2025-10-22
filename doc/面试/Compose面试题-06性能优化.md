# Compose UI 面试题 - 第6部分：性能优化

## 31. Compose 的重组机制是如何工作的？如何优化？

**答案：**

Compose 通过智能重组来更新 UI，但不当使用可能导致性能问题。

**重组原理：**

```kotlin
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    
    Column {
        // ❌ 每次 count 改变，整个 Column 都会重组
        Text("计数: $count")
        Text("固定文本")
        Button(onClick = { count++ }) { Text("+") }
    }
}
```

**优化策略：**

### 1. 状态读取位置下移

```kotlin
// ❌ 不好：状态在顶层读取
@Composable
fun BadExample(viewModel: MyViewModel) {
    val state = viewModel.state.collectAsState()
    
    Column {
        Header() // 不需要 state，但会重组
        Content(state.value)
        Footer() // 不需要 state，但会重组
    }
}

// ✅ 好：状态读取下移到需要的地方
@Composable
fun GoodExample(viewModel: MyViewModel) {
    Column {
        Header() // 不会重组
        ContentWrapper(viewModel) // 只有这里重组
        Footer() // 不会重组
    }
}

@Composable
fun ContentWrapper(viewModel: MyViewModel) {
    val state = viewModel.state.collectAsState()
    Content(state.value)
}
```

### 2. 使用 key 稳定列表项

```kotlin
// ❌ 没有 key，列表项可能全部重组
LazyColumn {
    items(items) { item ->
        ItemCard(item)
    }
}

// ✅ 有 key，只重组变化的项
LazyColumn {
    items(items, key = { it.id }) { item ->
        ItemCard(item)
    }
}
```

### 3. 使用不可变数据类

```kotlin
// ❌ 可变数据，Compose 无法判断是否改变
class MutableUser(
    var name: String,
    var age: Int
)

// ✅ 不可变数据，Compose 可以优化
data class ImmutableUser(
    val name: String,
    val age: Int
)
```

### 4. 避免在 Composable 中创建对象

```kotlin
// ❌ 每次重组都创建新对象
@Composable
fun BadButton() {
    Button(
        onClick = { },
        colors = ButtonDefaults.buttonColors(...) // 每次重组都创建
    ) {
        Text("按钮")
    }
}

// ✅ 使用 remember 记忆对象
@Composable
fun GoodButton() {
    val buttonColors = remember {
        ButtonDefaults.buttonColors(...)
    }
    Button(onClick = { }, colors = buttonColors) {
        Text("按钮")
    }
}
```

### 5. 使用 derivedStateOf 避免不必要的计算

```kotlin
// ❌ 每次重组都过滤
@Composable
fun FilteredList(items: List<Item>, query: String) {
    val filtered = items.filter { it.name.contains(query) }
    LazyColumn {
        items(filtered) { ItemCard(it) }
    }
}

// ✅ 只在依赖改变时计算
@Composable
fun OptimizedFilteredList(items: List<Item>, query: String) {
    val filtered by remember(items, query) {
        derivedStateOf {
            items.filter { it.name.contains(query) }
        }
    }
    LazyColumn {
        items(filtered, key = { it.id }) { ItemCard(it) }
    }
}
```

---

## 32. 如何分析和诊断 Compose 的性能问题？

**答案：**

Compose 提供了多种工具来分析性能问题。

### 1. Layout Inspector

Android Studio 的 Layout Inspector 可以查看组合树和重组次数。

**使用步骤：**
1. 运行应用
2. View → Tool Windows → Layout Inspector
3. 查看 Recomposition Count（重组次数）

### 2. Compose Compiler Reports

启用编译器报告查看可跳过的组合函数。

**build.gradle.kts 配置：**

```kotlin
android {
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

// 在 gradle.properties 添加
org.gradle.configureondemand=true
```

**生成报告：**

```bash
./gradlew assembleRelease -PcomposeCompilerReports=true
```

**查看报告：**
- 报告位置：`app/build/compose_compiler/`
- 查看 `*-composables.txt` 文件

**报告解读：**

```
restartable skippable scheme("[androidx.compose.ui.UiComposable]") 
fun MyComponent(text: String)
  // 可跳过的组合函数

unstable fun MyBadComponent(mutableData: MutableData)
  // 不稳定，无法跳过
```

### 3. 使用 Systrace / Perfetto

**代码插桩：**

```kotlin
@Composable
fun TracedComposable() {
    Trace.beginSection("MyComposable")
    try {
        // 组合内容
        ExpensiveContent()
    } finally {
        Trace.endSection()
    }
}
```

### 4. 自定义重组计数器

```kotlin
@Composable
fun RecompositionCounter(label: String) {
    val count = remember { mutableStateOf(0) }
    
    SideEffect {
        count.value++
        Log.d("Recomposition", "$label: ${count.value}")
    }
}

// 使用
@Composable
fun MyScreen() {
    RecompositionCounter("MyScreen")
    
    // 内容...
}
```

### 5. 性能基准测试

```kotlin
@RunWith(AndroidJUnit4::class)
class ComposeBenchmark {
    @get:Rule
    val benchmarkRule = BenchmarkRule()
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun benchmarkRecomposition() {
        var count by mutableStateOf(0)
        
        composeTestRule.setContent {
            MyComponent(count)
        }
        
        benchmarkRule.measureRepeated {
            count++
        }
    }
}
```

**常见性能问题标志：**
- 📊 重组次数过高（> 5次/秒）
- 🐌 帧率低于 60fps
- 🔄 不必要的重组
- 💾 内存泄漏
- ⚡ 阻塞主线程

---

## 33. LazyColumn/LazyRow 的性能优化技巧有哪些？

**答案：**

LazyList 是 Compose 中最常用的组件，优化它至关重要。

### 1. 使用 key 参数（最重要）

```kotlin
// ❌ 没有 key，项的顺序改变会导致全部重组
LazyColumn {
    items(users) { user ->
        UserCard(user)
    }
}

// ✅ 有 key，只重组变化的项
LazyColumn {
    items(users, key = { it.id }) { user ->
        UserCard(user)
    }
}
```

### 2. 使用 contentType 优化布局缓存

```kotlin
LazyColumn {
    items(
        items = mixedContent,
        key = { it.id },
        contentType = { item ->
            when (item) {
                is Header -> "header"
                is Product -> "product"
                is Ad -> "ad"
                else -> null
            }
        }
    ) { item ->
        when (item) {
            is Header -> HeaderView(item)
            is Product -> ProductView(item)
            is Ad -> AdView(item)
        }
    }
}
```

### 3. 优化 Item 高度计算

```kotlin
// ❌ 不固定高度，每次都要测量
@Composable
fun DynamicHeightItem(text: String) {
    Text(
        text = text,
        maxLines = Int.MAX_VALUE
    )
}

// ✅ 固定高度或限制高度
@Composable
fun FixedHeightItem(text: String) {
    Text(
        text = text,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.height(72.dp)
    )
}
```

### 4. 避免在 Item 中使用复杂计算

```kotlin
// ❌ 每次滚动都计算
LazyColumn {
    items(items) { item ->
        val complexData = computeExpensiveData(item) // 每次都计算
        ItemView(complexData)
    }
}

// ✅ 提前计算或使用 remember
LazyColumn {
    items(items) { item ->
        val complexData = remember(item) {
            computeExpensiveData(item)
        }
        ItemView(complexData)
    }
}

// 更好：在 ViewModel 中预处理
class ListViewModel : ViewModel() {
    val processedItems = items.map { processItem(it) }
}
```

### 5. 使用合适的间距

```kotlin
// ❌ 每个 item 都加 padding
LazyColumn {
    items(items) { item ->
        Card(modifier = Modifier.padding(8.dp)) {
            ItemContent(item)
        }
    }
}

// ✅ 使用 Arrangement.spacedBy
LazyColumn(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(8.dp)
) {
    items(items) { item ->
        Card {
            ItemContent(item)
        }
    }
}
```

### 6. 预加载更多数据

```kotlin
@Composable
fun InfiniteList(viewModel: ListViewModel) {
    val items by viewModel.items.collectAsState()
    val listState = rememberLazyListState()
    
    LazyColumn(state = listState) {
        items(items, key = { it.id }) { item ->
            ItemCard(item)
        }
    }
    
    // 监听滚动，接近底部时加载更多
    LaunchedEffect(listState) {
        snapshotFlow { 
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index 
        }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && 
                    lastVisibleIndex >= items.size - 5) {
                    viewModel.loadMore()
                }
            }
    }
}
```

### 7. 使用 itemsIndexed 时注意性能

```kotlin
// ❌ 索引作为 key（不稳定）
LazyColumn {
    itemsIndexed(items) { index, item ->
        ItemCard(item)
    }
}

// ✅ 使用稳定的 ID
LazyColumn {
    itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
        ItemCard(item, index)
    }
}
```

---

## 34. Modifier 的顺序为什么重要？如何优化？

**答案：**

Modifier 的顺序直接影响布局和渲染性能。

**顺序的影响：**

```kotlin
// 示例1：padding 和 background 的顺序
Box(
    Modifier
        .padding(16.dp)      // 先 padding
        .background(Color.Blue) // 背景不包括 padding 区域
)

Box(
    Modifier
        .background(Color.Blue) // 先背景
        .padding(16.dp)         // 背景包括 padding 区域
)

// 示例2：size 和 padding 的顺序
Box(
    Modifier
        .size(100.dp)     // 总大小 100dp
        .padding(16.dp)   // 内容区域 68dp (100 - 16*2)
)

Box(
    Modifier
        .padding(16.dp)   // 先 padding
        .size(100.dp)     // 内容大小 100dp，总大小 132dp
)

// 示例3：clickable 的位置
Box(
    Modifier
        .padding(16.dp)
        .clickable { }    // 点击区域不包括 padding
)

Box(
    Modifier
        .clickable { }
        .padding(16.dp)   // 点击区域包括 padding
)
```

**性能优化建议：**

### 1. 减少 Modifier 链长度

```kotlin
// ❌ 过长的 Modifier 链
Text(
    "Hello",
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .background(Color.White)
        .border(1.dp, Color.Gray)
        .padding(8.dp)
        .clip(RoundedCornerShape(4.dp))
        .clickable { }
        .padding(4.dp)
)

// ✅ 拆分为多个组件
@Composable
fun StyledText(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        onClick = onClick
    ) {
        Text(
            text,
            modifier = Modifier.padding(12.dp)
        )
    }
}
```

### 2. 重用 Modifier

```kotlin
// ❌ 每次都创建
@Composable
fun RepeatedModifier() {
    Column {
        Text("Text 1", modifier = Modifier.padding(16.dp).fillMaxWidth())
        Text("Text 2", modifier = Modifier.padding(16.dp).fillMaxWidth())
        Text("Text 3", modifier = Modifier.padding(16.dp).fillMaxWidth())
    }
}

// ✅ 重用 Modifier
@Composable
fun ReusedModifier() {
    val commonModifier = Modifier.padding(16.dp).fillMaxWidth()
    
    Column {
        Text("Text 1", modifier = commonModifier)
        Text("Text 2", modifier = commonModifier)
        Text("Text 3", modifier = commonModifier)
    }
}
```

### 3. 条件 Modifier 优化

```kotlin
// ❌ 创建多个 Modifier 对象
@Composable
fun ConditionalModifier(isHighlighted: Boolean) {
    Text(
        "Text",
        modifier = if (isHighlighted) {
            Modifier.background(Color.Yellow).padding(8.dp)
        } else {
            Modifier.padding(8.dp)
        }
    )
}

// ✅ 使用 then 条件添加
@Composable
fun OptimizedConditionalModifier(isHighlighted: Boolean) {
    Text(
        "Text",
        modifier = Modifier
            .then(
                if (isHighlighted) {
                    Modifier.background(Color.Yellow)
                } else {
                    Modifier
                }
            )
            .padding(8.dp)
    )
}
```

**推荐的 Modifier 顺序：**

```kotlin
Modifier
    // 1. 尺寸和布局
    .size(100.dp)
    .fillMaxWidth()
    .weight(1f)
    
    // 2. 位置和对齐
    .align(Alignment.Center)
    .offset(x = 10.dp)
    
    // 3. 装饰效果
    .background(Color.Blue)
    .border(1.dp, Color.Gray)
    .clip(RoundedCornerShape(8.dp))
    
    // 4. 交互
    .clickable { }
    .pointerInput(Unit) { }
    
    // 5. 内边距（通常放在最后）
    .padding(16.dp)
```

---

## 35. 如何处理大图片和图片列表的性能问题？

**答案：**

图片加载是常见的性能瓶颈，需要配合 Coil 等库优化。

### 1. 使用 Coil 加载图片

**添加依赖：**

```kotlin
implementation("io.coil-kt:coil-compose:2.5.0")
```

**基础使用：**

```kotlin
@Composable
fun ImageExample(imageUrl: String) {
    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        modifier = Modifier.size(200.dp),
        contentScale = ContentScale.Crop
    )
}
```

### 2. 优化图片列表

```kotlin
@Composable
fun ImageGrid(images: List<String>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(images, key = { it }) { imageUrl ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)           // 淡入动画
                    .size(400)                  // 限制尺寸
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.placeholder),
                error = painterResource(R.drawable.error)
            )
        }
    }
}
```

### 3. 预加载图片

```kotlin
@Composable
fun PreloadImages(viewModel: ImageViewModel) {
    val context = LocalContext.current
    val imageLoader = ImageLoader(context)
    val images by viewModel.images.collectAsState()
    
    // 预加载即将显示的图片
    LaunchedEffect(images) {
        images.take(10).forEach { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .build()
            imageLoader.enqueue(request)
        }
    }
    
    ImageList(images)
}
```

### 4. 缓存策略

```kotlin
val imageLoader = ImageLoader.Builder(context)
    .memoryCache {
        MemoryCache.Builder(context)
            .maxSizePercent(0.25) // 使用 25% 内存
            .build()
    }
    .diskCache {
        DiskCache.Builder()
            .directory(context.cacheDir.resolve("image_cache"))
            .maxSizeBytes(512L * 1024 * 1024) // 512MB
            .build()
    }
    .build()

CompositionLocalProvider(LocalImageLoader provides imageLoader) {
    MyApp()
}
```

### 5. 按需加载高分辨率图片

```kotlin
@Composable
fun AdaptiveImage(imageUrl: String) {
    var showHighRes by remember { mutableStateOf(false) }
    
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(if (showHighRes) imageUrl else "${imageUrl}_thumb")
            .size(if (showHighRes) Size.ORIGINAL else Size(400, 400))
            .build(),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .clickable { showHighRes = !showHighRes }
    )
}
```

---

## 36. 如何避免内存泄漏？

**答案：**

Compose 虽然简化了 UI 开发，但仍需注意内存管理。

**常见内存泄漏场景和解决方案：**

### 1. 协程泄漏

```kotlin
// ❌ 使用 GlobalScope（永不取消）
@Composable
fun BadCoroutine() {
    Button(onClick = {
        GlobalScope.launch {
            // 永远不会取消，导致泄漏
            delay(10000)
        }
    }) {
        Text("点击")
    }
}

// ✅ 使用 rememberCoroutineScope
@Composable
fun GoodCoroutine() {
    val scope = rememberCoroutineScope()
    
    Button(onClick = {
        scope.launch {
            // 组件销毁时自动取消
            delay(10000)
        }
    }) {
        Text("点击")
    }
}

// ✅ 使用 LaunchedEffect
@Composable
fun LaunchedEffectExample(userId: String) {
    LaunchedEffect(userId) {
        // 组件离开组合时自动取消
        loadUserData(userId)
    }
}
```

### 2. 监听器泄漏

```kotlin
// ❌ 注册监听器但不释放
@Composable
fun BadListener() {
    val context = LocalContext.current
    
    SideEffect {
        val listener = createListener()
        registerListener(listener)
        // 没有清理！
    }
}

// ✅ 使用 DisposableEffect
@Composable
fun GoodListener() {
    val context = LocalContext.current
    
    DisposableEffect(Unit) {
        val listener = createListener()
        registerListener(listener)
        
        onDispose {
            unregisterListener(listener)
        }
    }
}
```

### 3. ViewModel 中的泄漏

```kotlin
// ❌ ViewModel 持有 Context 引用
class BadViewModel(private val context: Context) : ViewModel() {
    // 可能导致 Activity 泄漏
}

// ✅ 使用 Application Context
class GoodViewModel(
    private val application: Application
) : ViewModel() {
    // Application 不会泄漏
}

// ✅ 或使用 AndroidViewModel
class BetterViewModel(application: Application) : AndroidViewModel(application) {
    fun getString(resId: Int): String {
        return getApplication<Application>().getString(resId)
    }
}
```

### 4. Flow 和 LiveData 收集

```kotlin
// ❌ 在 Composable 外收集
class MyScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel.stateFlow.collect { state ->
            // Activity 销毁后仍在收集
        }
    }
}

// ✅ 使用 collectAsState
@Composable
fun GoodCollection(viewModel: MyViewModel) {
    val state by viewModel.stateFlow.collectAsState()
    // 组件销毁时自动取消收集
}

// ✅ 或使用 lifecycleScope
class MyScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow.collect { state ->
                    // 生命周期感知
                }
            }
        }
    }
}
```

### 5. Bitmap 泄漏

```kotlin
// ❌ 手动创建 Bitmap 不释放
@Composable
fun BadBitmap() {
    val bitmap = BitmapFactory.decodeResource(...)
    Image(bitmap = bitmap.asImageBitmap(), contentDescription = null)
    // Bitmap 没有回收
}

// ✅ 使用 Coil 自动管理
@Composable
fun GoodBitmap(imageUrl: String) {
    AsyncImage(
        model = imageUrl,
        contentDescription = null
    )
    // Coil 自动管理内存
}

// ✅ 如果必须手动管理
@Composable
fun ManualBitmap() {
    val bitmap = remember {
        BitmapFactory.decodeResource(...)
    }
    
    DisposableEffect(Unit) {
        onDispose {
            bitmap.recycle()
        }
    }
    
    Image(bitmap = bitmap.asImageBitmap(), contentDescription = null)
}
```

**检测内存泄漏工具：**
- **LeakCanary** - 自动检测
- **Android Profiler** - 手动分析
- **Layout Inspector** - 查看组件生命周期

---

**第6部分完成！共 6 题**

