# Compose UI 面试题 - 第1部分：基础概念

## 1. 什么是 Jetpack Compose？它与传统 XML 布局的主要区别是什么？

**答案：**

Jetpack Compose 是 Android 的现代声明式 UI 工具包，用于构建原生界面。

**主要区别：**

| 特性 | Compose (声明式) | XML (命令式) |
|------|-----------------|--------------|
| UI定义 | Kotlin 代码 | XML 文件 |
| 更新方式 | 自动重组 | 手动调用 `findViewById` |
| 类型安全 | 编译时检查 | 运行时检查 |
| 预览 | `@Preview` 注解 | 需要运行应用 |
| 代码量 | 更少 | 更多模板代码 |

**示例对比：**

```kotlin
// Compose 方式
@Composable
fun Greeting(name: String) {
    Text(text = "Hello, $name!")
}

// XML 方式需要：
// 1. layout.xml 文件定义
// 2. Activity/Fragment 中 findViewById
// 3. 手动设置文本
```

---

## 2. 什么是 @Composable 注解？为什么需要它？

**答案：**

`@Composable` 注解标记一个函数可以发出 UI 并参与 Compose 的重组机制。

**作用：**
- 告诉 Compose 编译器这是一个可组合函数
- 启用特殊的编译器优化
- 允许调用其他 @Composable 函数
- 支持状态管理和重组

**规则：**
```kotlin
// ✅ 正确
@Composable
fun MyButton() {
    Button(onClick = {}) {
        Text("点击")
    }
}

// ❌ 错误：非 Composable 函数不能调用 Composable 函数
fun regularFunction() {
    Text("错误") // 编译错误
}

// ✅ 正确：从非 Composable 调用需要使用 ComposeView
fun useCompose() {
    val composeView = ComposeView(context)
    composeView.setContent {
        Text("正确") // 这里可以调用
    }
}
```

---

## 3. 什么是重组（Recomposition）？

**答案：**

重组是 Compose 在数据变化时重新执行可组合函数以更新 UI 的过程。

**核心特点：**

1. **智能更新**：只重组需要更新的部分
2. **自动触发**：状态改变时自动发生
3. **可跳过**：输入未改变的函数会被跳过

**示例：**

```kotlin
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    
    Column {
        // 只有这个 Text 会在 count 改变时重组
        Text("计数: $count")
        
        // 这个 Button 不会重组（输入没变）
        Button(onClick = { count++ }) {
            Text("增加")
        }
    }
}
```

**注意事项：**
- 重组可能随时发生
- 重组可能被跳过
- 可组合函数应该是无副作用的
- 不要依赖重组的执行顺序

---

## 4. Modifier 是什么？常用的 Modifier 有哪些？

**答案：**

Modifier 用于修饰 Composable 的外观、布局和行为，类似于 XML 中的属性，但更强大和灵活。

**特点：**
- 链式调用
- 顺序很重要
- 可重用

**常用 Modifier：**

```kotlin
@Composable
fun ModifierExample() {
    Text(
        text = "示例",
        modifier = Modifier
            // 尺寸相关
            .size(100.dp)           // 固定大小
            .width(100.dp)          // 宽度
            .height(50.dp)          // 高度
            .fillMaxWidth()         // 填充宽度
            .fillMaxSize()          // 填充全部
            
            // 间距相关
            .padding(16.dp)         // 内边距
            .padding(horizontal = 8.dp, vertical = 4.dp)
            
            // 背景和边框
            .background(Color.Blue) // 背景色
            .border(2.dp, Color.Red) // 边框
            .clip(RoundedCornerShape(8.dp)) // 圆角裁剪
            
            // 交互相关
            .clickable { /* 点击 */ }
            .selectable(selected = true) { /* 选择 */ }
            
            // 布局相关
            .align(Alignment.Center) // 对齐
            .weight(1f)              // 权重
    )
}
```

**顺序的重要性：**

```kotlin
// 不同的顺序产生不同效果
Modifier
    .padding(16.dp)
    .background(Color.Blue)  // 背景不包括 padding

Modifier
    .background(Color.Blue)
    .padding(16.dp)          // 背景包括 padding
```

---

## 5. Row、Column 和 Box 的区别和使用场景？

**答案：**

这是 Compose 的三个基础布局容器。

### Row - 水平布局

```kotlin
@Composable
fun RowExample() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween, // 水平排列
        verticalAlignment = Alignment.CenterVertically    // 垂直对齐
    ) {
        Text("左")
        Text("中")
        Text("右")
    }
}
```

**使用场景：**
- 按钮组合
- 图标+文字
- 标签栏

### Column - 垂直布局

```kotlin
@Composable
fun ColumnExample() {
    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly,   // 垂直排列
        horizontalAlignment = Alignment.CenterHorizontally // 水平对齐
    ) {
        Text("上")
        Text("中")
        Text("下")
    }
}
```

**使用场景：**
- 表单列表
- 垂直滚动内容
- 卡片内容

### Box - 层叠布局

```kotlin
@Composable
fun BoxExample() {
    Box(
        modifier = Modifier.size(100.dp),
        contentAlignment = Alignment.Center // 内容对齐
    ) {
        // 子元素会层叠显示
        Image(...)           // 底层
        Text("覆盖文字")      // 上层
    }
}
```

**使用场景：**
- 徽章（Badge）
- 图片上的文字
- 加载遮罩

**对比表格：**

| 布局 | 排列方向 | 典型用途 |
|------|---------|---------|
| Row | 水平 | 工具栏、按钮组 |
| Column | 垂直 | 列表、表单 |
| Box | 层叠 | 覆盖层、徽章 |

---

## 6. LazyColumn 和 Column 有什么区别？什么时候使用 LazyColumn？

**答案：**

**Column** 会一次性加载所有子项，而 **LazyColumn** 是懒加载列表，只渲染可见的项。

**区别对比：**

| 特性 | Column | LazyColumn |
|------|--------|------------|
| 加载方式 | 全部加载 | 按需加载（懒加载） |
| 性能 | 少量项时好 | 大量数据时优 |
| 滚动 | 需要 `verticalScroll()` | 内置滚动 |
| 适用场景 | < 10 项固定内容 | 大量或动态数据 |
| 内存占用 | 与项数成正比 | 只占用可见项内存 |

**Column 示例：**

```kotlin
@Composable
fun FixedList() {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        // 所有 20 项都会立即创建
        repeat(20) { index ->
            Text("Item $index")
        }
    }
}
```

**LazyColumn 示例：**

```kotlin
@Composable
fun DynamicList() {
    LazyColumn {
        // 只创建可见的项 + 少量缓冲
        items(1000) { index ->
            Text("Item $index")
        }
    }
}

// 使用列表数据
@Composable
fun UserList(users: List<User>) {
    LazyColumn {
        items(users) { user ->
            UserCard(user)
        }
        
        // 或者使用 key 优化
        items(users, key = { it.id }) { user ->
            UserCard(user)
        }
    }
}
```

**使用建议：**
- **< 10 项且固定** → 使用 Column
- **动态数据或 > 10 项** → 使用 LazyColumn
- **网络加载的列表** → 使用 LazyColumn
- **需要分页加载** → 使用 LazyColumn

---

**第1部分完成！共 6 题**

