# Compose UI 学习项目

这是一个全面的 Jetpack Compose UI 学习项目，展示了现代 Android 开发的最佳实践。

## 📱 项目结构

### 主页面 (MainActivity.kt)
- **功能**: 展示所有测试功能的入口列表
- **特点**:
  - 使用 `LazyColumn` 实现列表
  - Material3 设计风格
  - 卡片式布局，清晰的导航

### 1️⃣ Text & Button 测试 (TextButtonActivity.kt)

#### Text 组件示例
- 基础文本
- 不同样式的标题（大、中、小）
- 斜体、粗体文本
- 文本对齐（居中、左对齐等）
- 自动换行处理
- 颜色和字体大小自定义

#### Button 组件示例
- **Filled Button** - 标准填充按钮
- **Filled Tonal Button** - 填充色调按钮
- **Outlined Button** - 轮廓按钮
- **Elevated Button** - 提升按钮（带阴影）
- **Text Button** - 文本按钮
- **Icon Button** - 图标按钮
- 带图标的按钮组合
- 自定义颜色按钮
- 禁用状态按钮

#### 交互功能
- 实时点击计数器
- 显示最后点击的按钮
- 重置功能

### 2️⃣ 列表加载测试 (ListActivity.kt)

#### Tab 切换展示
使用 `TabRow` 实现多种列表类型切换

#### 垂直列表 (LazyColumn)
- 用户列表展示
- 可展开/收起的卡片
- 支持动态加载
- 圆形头像设计

#### 横向列表 (LazyRow)
- 分类筛选器横向滚动
- 商品卡片横向展示
- 组合布局（横向 + 竖向）

#### 网格列表 (LazyVerticalGrid)
- 2列网格布局
- 颜色卡片展示
- 自适应布局

#### 消息列表
- 仿聊天界面
- 已读/未读状态管理
- 圆形头像设计
- 时间戳显示
- 未读标记

### 3️⃣ ViewModel 绑定测试 (ViewModelActivity.kt)

#### 架构特点
- ✅ 业务逻辑与UI分离
- ✅ 配置更改时保持数据（屏幕旋转等）
- ✅ 生命周期感知
- ✅ 与 Compose State 无缝集成

#### 三个实用示例

##### 1. 计数器 ViewModel
```kotlin
class CounterViewModel : ViewModel()
```
- 简单的状态管理
- 增加/减少/重置功能
- 展示 ViewModel 基本用法

##### 2. 待办事项 ViewModel
```kotlin
class TodoViewModel : ViewModel()
```
- 列表数据管理
- 添加、删除、切换完成状态
- 统计功能（总数、已完成、进行中）
- `mutableStateListOf` 的使用

##### 3. 用户表单 ViewModel
```kotlin
class UserFormViewModel : ViewModel()
```
- 表单状态管理
- 多字段数据绑定
- 表单验证
- 提交和重置功能
- Data Class 数据模型

### 4️⃣ Navigation 导航测试 (NavigationActivity.kt)

#### 完整的 Navigation 架构

##### 导航组件
- **NavHost** - 导航宿主容器
- **NavController** - 导航控制器
- **NavigationBar** - 底部导航栏
- **Sealed Class** - 类型安全的路由定义

##### 四个导航页面

###### 1. 首页 (HomeScreen)
- 展示商品列表
- 点击跳转到详情页（带参数）
- Navigation 特性说明卡片

###### 2. 个人中心 (ProfileScreen)
- 用户信息展示
- 头像、联系方式
- 统计卡片（订单、收藏）
- 编辑资料按钮

###### 3. 设置页 (SettingsScreen)
- 开关设置（通知、深色模式）
- 关于、分享、反馈入口
- Material3 设置项布局

###### 4. 详情页 (DetailsScreen)
- 接收路由参数（itemId、itemName）
- 参数展示
- 商品详情信息
- 返回和购买操作

##### Navigation 特性
- ✅ **底部导航栏** - 三个主要Tab切换
- ✅ **带参数路由** - 类型安全的参数传递
- ✅ **页面转场动画** - 滑动 + 淡入淡出效果
- ✅ **状态保存与恢复** - Tab切换保持状态
- ✅ **返回栈管理** - 智能管理导航历史
- ✅ **条件显示** - 详情页隐藏底部导航栏

#### 动画效果
```kotlin
enterTransition = slideInHorizontally + fadeIn
exitTransition = slideOutHorizontally + fadeOut
popEnterTransition = slideInHorizontally + fadeIn
popExitTransition = slideOutHorizontally + fadeOut
```

## 🎨 技术栈

### 核心技术
- **Jetpack Compose** - 声明式UI框架
- **Material 3** - 最新的 Material Design
- **Kotlin** - 现代编程语言
- **ViewModel** - MVVM 架构组件
- **Navigation Compose** - Compose 导航库

### Compose 组件
- `LazyColumn` / `LazyRow` - 延迟加载列表
- `LazyVerticalGrid` - 网格布局
- `Scaffold` - 页面脚手架
- `TopAppBar` - 顶部导航栏
- `NavigationBar` - 底部导航栏
- `TabRow` - Tab 切换
- `Card` - 卡片容器
- `TextField` / `OutlinedTextField` - 输入框
- `Button` 系列 - 各种按钮
- `Icon` - 图标组件
- `Checkbox` / `Switch` - 选择组件

### State 管理
- `remember` - 组合记忆
- `mutableStateOf` - 可变状态
- `mutableStateListOf` - 可变列表状态
- `mutableIntStateOf` - 可变Int状态
- `State<T>` - 只读状态

### 依赖配置
```toml
[versions]
navigationCompose = "2.8.5"
lifecycleViewmodelCompose = "2.9.2"

[libraries]
androidx-navigation-compose = "..."
androidx-lifecycle-viewmodel-compose = "..."
```

## 📋 学习要点

### 1. Compose 基础
- 声明式UI vs 命令式UI
- 组合函数 (`@Composable`)
- 修饰符 (`Modifier`)
- 布局系统 (`Row`, `Column`, `Box`)

### 2. 状态管理
- 单向数据流
- State hoisting（状态提升）
- 记忆与重组
- 副作用处理

### 3. ViewModel 集成
- `viewModel()` 函数
- 状态观察
- 生命周期管理
- 业务逻辑分离

### 4. Navigation
- 路由定义
- 参数传递
- 返回栈管理
- 深度链接（可扩展）

### 5. Material 3
- 主题系统
- 颜色方案
- 动态颜色
- 组件变体

## 🚀 运行项目

### 环境要求
- Android Studio Hedgehog 或更高版本
- JDK 17
- Android SDK 35
- Gradle 8.9

### 运行步骤
1. 在 Android Studio 中打开项目
2. 等待 Gradle 同步完成
3. 连接 Android 设备或启动模拟器
4. 点击运行按钮

### 编译命令
```bash
# Windows
.\gradlew assembleDebug

# Mac/Linux
./gradlew assembleDebug
```

## 📚 学习路径建议

### 初级
1. ✅ Text & Button 测试 - 了解基础组件
2. ✅ 列表加载测试 - 掌握列表渲染

### 中级
3. ✅ ViewModel 绑定测试 - 学习架构模式
4. ✅ Navigation 导航测试 - 掌握页面导航

### 高级（可扩展）
- 网络请求与数据加载
- Room 数据库集成
- 图片加载（Coil）
- 动画与过渡效果
- 自定义组件开发

## 🎯 最佳实践

### 代码组织
- 每个 Activity 独立文件
- ViewModel 与 UI 分离
- 可组合函数单一职责
- 合理的文件命名

### 性能优化
- 使用 `remember` 避免重组
- LazyColumn 替代 Column + scroll
- 避免在组合函数中创建对象
- 合理使用 `key` 参数

### UI 设计
- 遵循 Material Design 规范
- 合理的间距和布局
- 使用主题色彩
- 提供良好的交互反馈

## 📝 代码亮点

### 1. 类型安全的路由
```kotlin
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Details : Screen("details/{itemId}/{itemName}", "详情", Icons.Default.Info) {
        fun createRoute(itemId: Int, itemName: String) = "details/$itemId/$itemName"
    }
}
```

### 2. 优雅的状态管理
```kotlin
class TodoViewModel : ViewModel() {
    private val _todos = mutableStateListOf<TodoItem>()
    val todos: List<TodoItem> = _todos
    
    val completedCount: Int
        get() = _todos.count { it.isCompleted }
}
```

### 3. 可重用的组件
```kotlin
@Composable
fun FeatureCard(feature: FeatureItem, onClick: () -> Unit) {
    // 清晰的组件设计
}
```

## 🔧 扩展建议

### 功能扩展
- [ ] 添加深色模式切换
- [ ] 集成网络请求（Retrofit）
- [ ] 添加本地数据库（Room）
- [ ] 实现下拉刷新
- [ ] 添加骨架屏加载
- [ ] 实现搜索功能

### 技术扩展
- [ ] Hilt 依赖注入
- [ ] Flow 响应式编程
- [ ] Paging 3 分页加载
- [ ] WorkManager 后台任务
- [ ] Coil 图片加载
- [ ] 单元测试

## 📄 许可证

本项目仅用于学习目的，可自由使用和修改。

## 🤝 贡献

欢迎提出问题和改进建议！

---

**Happy Coding! 🎉**

