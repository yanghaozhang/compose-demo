# Compose UI 面试题 - 第2部分：组件和布局

## 7. Scaffold 组件的作用是什么？如何使用？

**答案：**

Scaffold 是 Material Design 的页面脚手架，提供标准的应用结构。

**核心功能：**
- TopAppBar（顶部栏）
- BottomBar（底部栏）
- FloatingActionButton（悬浮按钮）
- Drawer（侧滑抽屉）
- Snackbar（提示条）

**基本使用：**

```kotlin
@Composable
fun MyScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的应用") },
                navigationIcon = {
                    IconButton(onClick = { /* 返回 */ }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("首页") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* 添加 */ }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { paddingValues ->
        // 内容区域，注意使用 paddingValues
        Column(modifier = Modifier.padding(paddingValues)) {
            Text("主要内容")
        }
    }
}
```

**重要：必须使用 paddingValues**

```kotlin
// ❌ 错误：内容会被 TopBar 遮挡
Scaffold(topBar = { TopAppBar(...) }) {
    Text("被遮挡了")
}

// ✅ 正确
Scaffold(topBar = { TopAppBar(...) }) { padding ->
    Text("正确显示", modifier = Modifier.padding(padding))
}
```

---

## 8. Card 组件有哪些常用属性？如何实现卡片点击效果？

**答案：**

Card 是 Material Design 的卡片容器，用于展示相关内容。

**常用属性：**

```kotlin
@Composable
fun CardExample() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        
        // 形状
        shape = RoundedCornerShape(12.dp),
        
        // 颜色
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        
        // 阴影
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            hoveredElevation = 6.dp
        ),
        
        // 边框
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        // 卡片内容
        Column(modifier = Modifier.padding(16.dp)) {
            Text("卡片标题", style = MaterialTheme.typography.titleMedium)
            Text("卡片内容")
        }
    }
}
```

**点击效果实现：**

```kotlin
@Composable
fun ClickableCard() {
    var isSelected by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isSelected = !isSelected }, // 方式1：使用 clickable
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Text("可点击卡片", modifier = Modifier.padding(16.dp))
    }
}

// 方式2：使用 ElevatedCard 或 OutlinedCard
@Composable
fun ElevatedClickableCard(onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick, // 内置点击处理
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("带点击效果的卡片", modifier = Modifier.padding(16.dp))
    }
}
```

---

## 9. TextField 和 OutlinedTextField 有什么区别？如何实现输入验证？

**答案：**

两者都是文本输入组件，区别在于外观样式。

**区别对比：**

| 特性 | TextField | OutlinedTextField |
|------|-----------|-------------------|
| 外观 | 填充样式 | 边框样式 |
| 背景 | 有背景色 | 透明背景 |
| 适用场景 | 表单填写 | 需要突出边框时 |

**基本使用：**

```kotlin
@Composable
fun InputExample() {
    var text by remember { mutableStateOf("") }
    
    // TextField - 填充样式
    TextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("用户名") },
        placeholder = { Text("请输入用户名") },
        leadingIcon = { Icon(Icons.Default.Person, null) },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = { text = "" }) {
                    Icon(Icons.Default.Clear, null)
                }
            }
        }
    )
    
    // OutlinedTextField - 边框样式
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("邮箱") }
    )
}
```

**输入验证实现：**

```kotlin
@Composable
fun ValidatedInput() {
    var email by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    
    fun validateEmail(input: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()
    }
    
    OutlinedTextField(
        value = email,
        onValueChange = { 
            email = it
            isError = it.isNotEmpty() && !validateEmail(it)
        },
        label = { Text("邮箱") },
        isError = isError,
        supportingText = {
            if (isError) {
                Text(
                    text = "邮箱格式不正确",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        trailingIcon = {
            if (isError) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        )
    )
}
```

---

## 10. 如何在 Compose 中实现不同类型的 Button？

**答案：**

Compose 提供了多种 Button 变体，适用于不同场景。

**Button 类型：**

```kotlin
@Composable
fun ButtonVariants() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. 标准填充按钮（最常用）
        Button(onClick = { }) {
            Text("Filled Button")
        }
        
        // 2. 填充色调按钮
        FilledTonalButton(onClick = { }) {
            Text("Filled Tonal Button")
        }
        
        // 3. 轮廓按钮
        OutlinedButton(onClick = { }) {
            Text("Outlined Button")
        }
        
        // 4. 提升按钮（带阴影）
        ElevatedButton(onClick = { }) {
            Text("Elevated Button")
        }
        
        // 5. 文本按钮（无背景）
        TextButton(onClick = { }) {
            Text("Text Button")
        }
        
        // 6. 图标按钮
        IconButton(onClick = { }) {
            Icon(Icons.Default.Favorite, contentDescription = null)
        }
        
        // 7. 带图标的按钮
        Button(onClick = { }) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text("添加")
        }
    }
}
```

**自定义按钮样式：**

```kotlin
@Composable
fun CustomButton() {
    Button(
        onClick = { },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Red,
            contentColor = Color.White,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.LightGray
        ),
        shape = RoundedCornerShape(50), // 圆角
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        contentPadding = PaddingValues(
            horizontal = 24.dp,
            vertical = 12.dp
        ),
        enabled = true
    ) {
        Text("自定义按钮")
    }
}
```

**使用场景建议：**
- **Button** - 主要操作（提交、确认）
- **OutlinedButton** - 次要操作（取消）
- **TextButton** - 最低优先级操作
- **IconButton** - 工具栏、导航

---

## 11. TabRow 和 ScrollableTabRow 有什么区别？如何使用？

**答案：**

两者都用于实现 Tab 切换，区别在于 Tab 数量较多时的表现。

**区别：**

| 特性 | TabRow | ScrollableTabRow |
|------|--------|------------------|
| Tab 布局 | 平均分配宽度 | 根据内容宽度 |
| 溢出处理 | Tab 会被压缩 | 可以横向滚动 |
| 适用场景 | 2-4 个 Tab | 5+ 个 Tab |

**TabRow 使用：**

```kotlin
@Composable
fun TabExample() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("首页", "消息", "我的")
    
    Column {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) },
                    icon = {
                        Icon(
                            when (index) {
                                0 -> Icons.Default.Home
                                1 -> Icons.Default.Notifications
                                else -> Icons.Default.Person
                            },
                            contentDescription = null
                        )
                    }
                )
            }
        }
        
        // 根据选中的 Tab 显示内容
        when (selectedTabIndex) {
            0 -> HomeContent()
            1 -> MessageContent()
            2 -> ProfileContent()
        }
    }
}
```

**ScrollableTabRow 使用：**

```kotlin
@Composable
fun ScrollableTabExample() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("推荐", "热榜", "视频", "科技", "体育", "财经", "娱乐")
    
    Column {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 16.dp // 边缘留白
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        
        // 内容区域
        Box(modifier = Modifier.fillMaxSize()) {
            Text("${tabs[selectedTabIndex]} 的内容")
        }
    }
}
```

**结合 HorizontalPager 实现滑动切换：**

```kotlin
@Composable
fun TabWithPager() {
    val tabs = listOf("Tab 1", "Tab 2", "Tab 3")
    val pagerState = rememberPagerState { tabs.size }
    val coroutineScope = rememberCoroutineScope()
    
    Column {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(title) }
                )
            }
        }
        
        HorizontalPager(state = pagerState) { page ->
            Text("Page $page", modifier = Modifier.fillMaxSize())
        }
    }
}
```

---

## 12. 如何实现网格布局（Grid）？

**答案：**

Compose 提供了 `LazyVerticalGrid` 和 `LazyHorizontalGrid` 实现网格布局。

**基础用法：**

```kotlin
@Composable
fun GridExample() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 固定2列
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(20) { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // 保持正方形
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Item $index")
                }
            }
        }
    }
}
```

**不同的列数设置方式：**

```kotlin
// 1. 固定列数
GridCells.Fixed(3) // 固定3列

// 2. 自适应（根据最小宽度）
GridCells.Adaptive(minSize = 100.dp) // 每列最小100dp，自动计算列数

// 3. 使用数据列表
@Composable
fun ProductGrid(products: List<Product>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp)
    ) {
        items(products, key = { it.id }) { product ->
            ProductCard(product)
        }
    }
}
```

**混合内容的网格（Span）：**

```kotlin
@Composable
fun MixedGrid() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3)
    ) {
        item(span = { GridItemSpan(3) }) {
            // 占满整行
            Text("标题", style = MaterialTheme.typography.headlineMedium)
        }
        
        items(9) { index ->
            Card {
                Text("Item $index")
            }
        }
        
        item(span = { GridItemSpan(2) }) {
            // 占2列
            Button(onClick = { }) {
                Text("加载更多")
            }
        }
    }
}
```

**实战案例 - 图片网格：**

```kotlin
@Composable
fun ImageGrid(images: List<String>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(images) { imageUrl ->
            Card(
                modifier = Modifier.aspectRatio(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                // 使用 Coil 加载图片
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
```

---

**第2部分完成！共 6 题**

