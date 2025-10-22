# Compose UI 面试题 - 第5部分：Navigation 导航

## 25. Navigation Compose 的基本组成部分有哪些？

**答案：**

Navigation Compose 由三个核心组件构成。

**核心组件：**

### 1. NavController

导航控制器，管理导航状态和操作。

```kotlin
@Composable
fun MyApp() {
    val navController = rememberNavController()
    
    // 导航操作
    Button(onClick = { 
        navController.navigate("detail") 
    }) {
        Text("跳转")
    }
}
```

### 2. NavHost

导航宿主，定义导航图。

```kotlin
NavHost(
    navController = navController,
    startDestination = "home"
) {
    composable("home") { HomeScreen() }
    composable("detail") { DetailScreen() }
}
```

### 3. NavGraph

导航路由定义。

```kotlin
NavHost(navController, startDestination = "home") {
    composable("home") { HomeScreen(navController) }
    composable("profile") { ProfileScreen() }
    composable("settings") { SettingsScreen() }
}
```

**完整示例：**

```kotlin
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToDetail = { id ->
                        navController.navigate("detail/$id")
                    }
                )
            }
            
            composable(
                route = "detail/{itemId}",
                arguments = listOf(
                    navArgument("itemId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getInt("itemId")
                DetailScreen(itemId = itemId)
            }
            
            composable("profile") {
                ProfileScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("首页") }
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("我的") }
        )
    }
}
```

**关键方法：**

| 方法 | 作用 |
|------|------|
| navigate(route) | 导航到指定路由 |
| popBackStack() | 返回上一页 |
| navigateUp() | 向上导航 |
| popUpTo(route) | 弹出到指定路由 |

---

## 26. 如何在 Navigation 中传递参数？

**答案：**

Navigation Compose 支持多种参数传递方式。

### 方式1：路径参数（必需参数）

```kotlin
// 定义路由
NavHost(navController, startDestination = "home") {
    composable(
        route = "detail/{userId}/{userName}",
        arguments = listOf(
            navArgument("userId") { 
                type = NavType.IntType 
            },
            navArgument("userName") { 
                type = NavType.StringType 
            }
        )
    ) { backStackEntry ->
        val userId = backStackEntry.arguments?.getInt("userId") ?: 0
        val userName = backStackEntry.arguments?.getString("userName") ?: ""
        
        DetailScreen(userId = userId, userName = userName)
    }
}

// 导航跳转
Button(onClick = {
    navController.navigate("detail/123/张三")
}) {
    Text("查看详情")
}
```

### 方式2：查询参数（可选参数）

```kotlin
// 定义路由
composable(
    route = "search?query={query}&category={category}",
    arguments = listOf(
        navArgument("query") {
            type = NavType.StringType
            defaultValue = ""
            nullable = true
        },
        navArgument("category") {
            type = NavType.StringType
            defaultValue = "all"
        }
    )
) { backStackEntry ->
    val query = backStackEntry.arguments?.getString("query")
    val category = backStackEntry.arguments?.getString("category")
    
    SearchScreen(query = query, category = category)
}

// 跳转（可以省略参数）
navController.navigate("search?query=手机&category=电子产品")
navController.navigate("search") // 使用默认值
```

### 方式3：类型安全的导航（推荐）

```kotlin
// 定义路由类
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Profile : Screen("profile")
    
    data class Detail(val id: Int, val name: String) : Screen("detail") {
        fun createRoute() = "detail/$id/$name"
    }
}

// 注册路由
NavHost(navController, startDestination = Screen.Home.route) {
    composable(Screen.Home.route) {
        HomeScreen(
            onNavigateToDetail = { id, name ->
                navController.navigate(Screen.Detail(id, name).createRoute())
            }
        )
    }
    
    composable(
        route = "detail/{id}/{name}",
        arguments = listOf(
            navArgument("id") { type = NavType.IntType },
            navArgument("name") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val id = backStackEntry.arguments?.getInt("id") ?: 0
        val name = backStackEntry.arguments?.getString("name") ?: ""
        DetailScreen(id = id, name = name)
    }
}
```

### 方式4：传递复杂对象（通过 JSON）

```kotlin
// 定义数据类
@Serializable
data class User(val id: Int, val name: String, val email: String)

// 传递
val user = User(1, "张三", "zhang@example.com")
val userJson = Json.encodeToString(user)
val encodedJson = URLEncoder.encode(userJson, "UTF-8")
navController.navigate("profile/$encodedJson")

// 接收
composable(
    route = "profile/{userJson}",
    arguments = listOf(
        navArgument("userJson") { type = NavType.StringType }
    )
) { backStackEntry ->
    val userJson = backStackEntry.arguments?.getString("userJson") ?: ""
    val decodedJson = URLDecoder.decode(userJson, "UTF-8")
    val user = Json.decodeFromString<User>(decodedJson)
    
    ProfileScreen(user = user)
}
```

**推荐做法：**
- **简单数据** → 路径参数
- **可选数据** → 查询参数
- **复杂对象** → 通过 ID 传递，目标页面重新加载
- **避免传递大对象** → 影响性能和状态保存

---

## 27. 如何管理返回栈（Back Stack）？

**答案：**

返回栈管理对于良好的用户体验至关重要。

**基础返回栈操作：**

```kotlin
// 1. 普通跳转（压入栈）
navController.navigate("detail")

// 2. 返回上一页（弹出栈）
navController.popBackStack()

// 3. 返回到指定路由
navController.popBackStack(route = "home", inclusive = false)

// 4. 向上导航（系统返回）
navController.navigateUp()
```

**popUpTo 用法（清理返回栈）：**

```kotlin
// 场景1：登录后清除登录页
Button(onClick = {
    navController.navigate("home") {
        popUpTo("login") { inclusive = true }
        // 返回栈中移除 login
    }
}) {
    Text("登录")
}

// 场景2：Tab 切换不累积返回栈
NavigationBarItem(
    onClick = {
        navController.navigate("profile") {
            popUpTo("home") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
)

// 场景3：重置导航栈
navController.navigate("home") {
    popUpTo(0) // 清空整个返回栈
}
```

**NavOptions 详解：**

```kotlin
navController.navigate("destination") {
    // 弹出到指定路由
    popUpTo("home") {
        inclusive = true  // 是否包括目标路由本身
        saveState = true  // 保存状态以便恢复
    }
    
    // 避免多个相同实例
    launchSingleTop = true
    
    // 恢复之前保存的状态
    restoreState = true
}
```

**实战场景：**

### 场景1：底部导航栏

```kotlin
@Composable
fun BottomNavigation(navController: NavController) {
    val items = listOf("home", "search", "profile")
    
    NavigationBar {
        items.forEach { route ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        // 弹出到起始页
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // 避免重复实例
                        launchSingleTop = true
                        // 恢复状态
                        restoreState = true
                    }
                },
                icon = { Icon(...) }
            )
        }
    }
}
```

### 场景2：登录流程

```kotlin
// 登录成功后
fun onLoginSuccess() {
    navController.navigate("home") {
        // 清除所有登录相关页面
        popUpTo("welcome") { inclusive = true }
    }
}

// 退出登录
fun onLogout() {
    navController.navigate("login") {
        // 清空所有页面
        popUpTo(0)
    }
}
```

### 场景3：表单提交后返回

```kotlin
// 提交成功后返回并刷新
fun onSubmitSuccess() {
    // 设置返回结果
    navController.previousBackStackEntry
        ?.savedStateHandle
        ?.set("refresh", true)
    
    // 返回
    navController.popBackStack()
}

// 接收结果
@Composable
fun ListScreen(navController: NavController) {
    val refresh = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refresh", false)
        ?.collectAsState()
    
    LaunchedEffect(refresh?.value) {
        if (refresh?.value == true) {
            loadData()
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("refresh", false)
        }
    }
}
```

---

## 28. 如何实现导航动画？

**答案：**

Navigation Compose 支持自定义页面转场动画。

**基础动画：**

```kotlin
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedNavHost() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable("home") { HomeScreen() }
        composable("detail") { DetailScreen() }
    }
}
```

**单个路由自定义动画：**

```kotlin
composable(
    route = "detail",
    enterTransition = {
        // 从下往上滑入
        slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    },
    exitTransition = {
        // 淡出
        fadeOut(animationSpec = tween(300))
    },
    popEnterTransition = {
        // 淡入
        fadeIn(animationSpec = tween(300))
    },
    popExitTransition = {
        // 从上往下滑出
        slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        )
    }
) {
    DetailScreen()
}
```

**常见动画效果：**

```kotlin
// 1. 水平滑动（默认）
slideInHorizontally { fullWidth -> fullWidth } + fadeIn()
slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut()

// 2. 垂直滑动
slideInVertically { fullHeight -> fullHeight } + fadeIn()
slideOutVertically { fullHeight -> fullHeight } + fadeOut()

// 3. 缩放效果
scaleIn(
    initialScale = 0.8f,
    animationSpec = tween(300)
) + fadeIn()

scaleOut(
    targetScale = 0.8f,
    animationSpec = tween(300)
) + fadeOut()

// 4. 淡入淡出
fadeIn(animationSpec = tween(500))
fadeOut(animationSpec = tween(500))

// 5. 组合动画
slideInHorizontally { it } + scaleIn(initialScale = 0.9f) + fadeIn()
```

**根据导航方向不同动画：**

```kotlin
composable(
    route = "detail",
    enterTransition = {
        when (initialState.destination.route) {
            "home" -> slideInHorizontally { it } + fadeIn()
            "list" -> slideInVertically { it } + fadeIn()
            else -> fadeIn()
        }
    }
) {
    DetailScreen()
}
```

**Material Motion 风格：**

```kotlin
val slideDistance = 30.dp

composable(
    route = "detail",
    enterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(300, easing = EaseOut)
        )
    },
    exitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(300, easing = EaseIn)
        )
    }
) {
    DetailScreen()
}
```

---

## 29. 如何实现嵌套导航（Nested Navigation）？

**答案：**

嵌套导航用于组织复杂的导航结构，如 Tab 内的子页面。

**基础嵌套导航：**

```kotlin
@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    
    NavHost(navController, startDestination = "main") {
        // 主页面（包含底部导航）
        composable("main") {
            MainScreen()
        }
        
        // 嵌套导航图
        navigation(
            route = "home_graph",
            startDestination = "home_main"
        ) {
            composable("home_main") { HomeMainScreen() }
            composable("home_detail/{id}") { HomeDetailScreen() }
        }
    }
}
```

**完整实战示例（底部导航 + 子页面）：**

```kotlin
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            // 首页导航图
            homeGraph(navController)
            
            // 购物车导航图
            cartGraph(navController)
            
            // 我的导航图
            profileGraph(navController)
        }
    }
}

// 首页导航图
fun NavGraphBuilder.homeGraph(navController: NavController) {
    navigation(
        route = "home",
        startDestination = "home_main"
    ) {
        composable("home_main") {
            HomeScreen(
                onProductClick = { id ->
                    navController.navigate("home_detail/$id")
                }
            )
        }
        
        composable("home_detail/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductDetailScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("home_category/{category}") {
            CategoryScreen()
        }
    }
}

// 购物车导航图
fun NavGraphBuilder.cartGraph(navController: NavController) {
    navigation(
        route = "cart",
        startDestination = "cart_main"
    ) {
        composable("cart_main") {
            CartScreen(
                onCheckout = { navController.navigate("cart_checkout") }
            )
        }
        
        composable("cart_checkout") {
            CheckoutScreen(
                onSuccess = {
                    navController.navigate("cart_success") {
                        popUpTo("cart_main")
                    }
                }
            )
        }
        
        composable("cart_success") {
            OrderSuccessScreen()
        }
    }
}

// 我的导航图
fun NavGraphBuilder.profileGraph(navController: NavController) {
    navigation(
        route = "profile",
        startDestination = "profile_main"
    ) {
        composable("profile_main") {
            ProfileScreen(
                onSettingsClick = { navController.navigate("profile_settings") },
                onOrdersClick = { navController.navigate("profile_orders") }
            )
        }
        
        composable("profile_settings") {
            SettingsScreen()
        }
        
        composable("profile_orders") {
            OrdersScreen()
        }
    }
}

// 底部导航栏
@Composable
fun BottomBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState()
        .value?.destination?.route
    
    NavigationBar {
        // 首页 Tab
        NavigationBarItem(
            selected = currentRoute?.startsWith("home") == true,
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("首页") }
        )
        
        // 购物车 Tab
        NavigationBarItem(
            selected = currentRoute?.startsWith("cart") == true,
            onClick = {
                navController.navigate("cart") {
                    popUpTo("cart") { inclusive = true }
                }
            },
            icon = { Icon(Icons.Default.ShoppingCart, null) },
            label = { Text("购物车") }
        )
        
        // 我的 Tab
        NavigationBarItem(
            selected = currentRoute?.startsWith("profile") == true,
            onClick = {
                navController.navigate("profile") {
                    popUpTo("profile") { inclusive = true }
                }
            },
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("我的") }
        )
    }
}
```

**嵌套导航的优势：**
- ✅ 逻辑分组
- ✅ 返回栈独立管理
- ✅ 代码更清晰
- ✅ 便于维护

---

## 30. 如何测试 Navigation？

**答案：**

Navigation Compose 提供了测试工具来验证导航行为。

**添加依赖：**

```kotlin
androidTestImplementation "androidx.navigation:navigation-testing:2.7.5"
androidTestImplementation "androidx.compose.ui:ui-test-junit4:1.5.4"
```

**基础导航测试：**

```kotlin
@Test
fun testNavigationToDetailScreen() {
    val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    )
    
    composeTestRule.setContent {
        navController.navigatorProvider.addNavigator(
            ComposeNavigator()
        )
        NavHost(navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    onNavigateToDetail = {
                        navController.navigate("detail")
                    }
                )
            }
            composable("detail") {
                DetailScreen()
            }
        }
    }
    
    // 验证初始路由
    assertThat(navController.currentDestination?.route).isEqualTo("home")
    
    // 执行导航
    composeTestRule.onNodeWithText("查看详情").performClick()
    
    // 验证导航成功
    assertThat(navController.currentDestination?.route).isEqualTo("detail")
}
```

**测试参数传递：**

```kotlin
@Test
fun testNavigationWithArguments() {
    val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    )
    
    composeTestRule.setContent {
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        
        NavHost(navController, startDestination = "list") {
            composable("list") {
                ListScreen(
                    onItemClick = { id ->
                        navController.navigate("detail/$id")
                    }
                )
            }
            
            composable(
                route = "detail/{itemId}",
                arguments = listOf(
                    navArgument("itemId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getInt("itemId")
                DetailScreen(itemId = itemId)
            }
        }
    }
    
    // 点击第一项
    composeTestRule.onNodeWithText("Item 1").performClick()
    
    // 验证参数
    val itemId = navController.currentBackStackEntry
        ?.arguments
        ?.getInt("itemId")
    assertThat(itemId).isEqualTo(1)
}
```

**测试返回栈：**

```kotlin
@Test
fun testBackStackBehavior() {
    val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    )
    
    // 导航到多个页面
    navController.navigate("screen1")
    navController.navigate("screen2")
    navController.navigate("screen3")
    
    // 验证返回栈
    assertThat(navController.currentDestination?.route).isEqualTo("screen3")
    
    // 返回
    navController.popBackStack()
    assertThat(navController.currentDestination?.route).isEqualTo("screen2")
    
    // popUpTo 测试
    navController.navigate("screen4") {
        popUpTo("screen1") { inclusive = true }
    }
    
    // 验证 screen1 已被移除
    navController.popBackStack()
    assertThat(navController.currentDestination?.route).isEqualTo(null)
}
```

---

**第5部分完成！共 6 题**

