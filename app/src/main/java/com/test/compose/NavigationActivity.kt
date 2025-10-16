package com.test.compose

/**
 * ==================================================================================
 * NavigationActivity - Jetpack Compose Navigation 完整示例
 * ==================================================================================
 * 
 * 本文件展示了 Compose Navigation 的完整使用：
 * 1. NavHost 和 NavController - 导航的核心组件
 * 2. 底部导航栏 - NavigationBar
 * 3. 带参数的路由 - 类型安全的参数传递
 * 4. 页面转场动画 - enterTransition, exitTransition
 * 5. 返回栈管理 - popUpTo, launchSingleTop
 * 
 * 核心概念：
 * - NavHost：导航容器，定义所有可能的目标页面
 * - NavController：导航控制器，执行导航操作
 * - Route：路由字符串，唯一标识一个目标页面
 * - Arguments：路由参数，在页面间传递数据
 * 
 * 架构特点：
 * - 类型安全的路由定义（sealed class）
 * - 声明式导航配置
 * - 自动处理返回栈
 * - 支持深度链接
 * 
 * 对比传统开发：
 * - 替代 Fragment + Navigation Component
 * - 无需 XML 导航图
 * - 代码即配置，更加灵活
 * 
 * ==================================================================================
 */

// ==================== 导入区域 ====================
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

// 动画相关
import androidx.compose.animation.*  // 动画组件
import androidx.compose.animation.core.tween  // tween：补间动画

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Navigation 导航相关
import androidx.navigation.NavHostController  // NavHostController：导航控制器
import androidx.navigation.NavType  // NavType：参数类型定义
import androidx.navigation.compose.*  // Navigation Compose 所有组件
import androidx.navigation.navArgument  // navArgument：定义路由参数

import com.test.compose.ui.theme.ComposeTheme

// ==================== 路由定义 ====================

/**
 * Screen - 路由定义 sealed class
 * 
 * sealed class 特点：
 * - 有限的子类集合，编译时已知所有子类
 * - 适合用于表示有限的状态或选项
 * - when 表达式可以做穷尽检查
 * 
 * 路由模式：
 * - "home" - 简单路由，无参数
 * - "details/{itemId}/{itemName}" - 带参数路由
 *   {参数名} 是路径参数占位符
 * 
 * 设计优势：
 * - 类型安全：编译时检查路由
 * - 集中管理：所有路由在一处定义
 * - 易于维护：修改路由只需改一处
 */
sealed class Screen(
    val route: String,        // 路由字符串
    val title: String,        // 页面标题
    val icon: ImageVector     // 导航图标
) {
    /**
     * Home - 首页路由
     * 简单路由，无参数
     */
    object Home : Screen("home", "首页", Icons.Default.Home)
    
    /**
     * Profile - 个人中心路由
     */
    object Profile : Screen("profile", "个人", Icons.Default.Person)
    
    /**
     * Settings - 设置页路由
     */
    object Settings : Screen("settings", "设置", Icons.Default.Settings)
    
    /**
     * Details - 详情页路由
     * 
     * 带参数的路由：
     * - {itemId}：商品 ID，Int 类型
     * - {itemName}：商品名称，String 类型
     * 
     * createRoute 方法：
     * - 辅助方法，创建具体的路由字符串
     * - 类型安全：参数类型明确
     * - 避免字符串拼接错误
     */
    object Details : Screen("details/{itemId}/{itemName}", "详情", Icons.Default.Info) {
        /**
         * 创建带参数的路由字符串
         * 例如：createRoute(1, "手机") -> "details/1/手机"
         */
        fun createRoute(itemId: Int, itemName: String) = "details/$itemId/$itemName"
    }
}

// ==================== Activity ====================

/**
 * NavigationActivity - Navigation 导航测试页面
 * 
 * 简单的 Activity，只负责设置 UI
 */
class NavigationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeTheme {
                NavigationApp(
                    onBack = { finish() }  // 传递返回回调
                )
            }
        }
    }
}

// ==================== 主应用组件 ====================

/**
 * NavigationApp - 导航应用的主组件
 * 
 * 这是整个导航架构的核心，包含：
 * - NavController：导航控制器
 * - NavHost：导航宿主
 * - 底部导航栏
 * - 所有页面的路由配置
 * 
 * @param onBack 返回回调，用于关闭 Activity
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationApp(onBack: () -> Unit) {
    /**
     * rememberNavController：创建并记住 NavController
     * 
     * NavController：
     * - 导航的核心控制器
     * - 管理返回栈
     * - 执行导航操作（navigate, popBackStack 等）
     * - 保存和恢复状态
     */
    val navController = rememberNavController()
    
    /**
     * 控制底部导航栏的显示/隐藏
     * 
     * 设计：
     * - 主要页面（Home、Profile、Settings）显示底部栏
     * - 详情页隐藏底部栏
     */
    var showBottomBar by remember { mutableStateOf(true) }
    
    /**
     * 监听导航变化
     * 
     * currentBackStackEntryAsState()：
     * - 将当前返回栈顶部条目转为 State
     * - 导航发生时会触发重组
     * - 可以获取当前路由、参数等信息
     */
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    /**
     * 根据当前路由决定是否显示底部导航栏
     * 
     * in 操作符：检查元素是否在集合中
     * 如果当前路由是主要页面之一，显示底部栏
     */
    showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Profile.route,
        Screen.Settings.route
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when (currentRoute) {
                            Screen.Home.route -> "Navigation 测试"
                            Screen.Profile.route -> "个人中心"
                            Screen.Settings.route -> "设置"
                            else -> "详情页"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (navController.previousBackStackEntry != null) {
                            navController.navigateUp()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -1000 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -1000 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 1000 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController)
            }
            composable(
                route = Screen.Details.route,
                arguments = listOf(
                    navArgument("itemId") { type = NavType.IntType },
                    navArgument("itemName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
                val itemName = backStackEntry.arguments?.getString("itemName") ?: ""
                DetailsScreen(itemId = itemId, itemName = itemName, navController = navController)
            }
        }
    }
}

@Composable
fun NavigationBottomBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Profile,
        Screen.Settings
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
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

@Composable
fun HomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Compose Navigation 示例",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "点击下方卡片查看带参数的路由导航",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // 功能说明卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🎯 Navigation 特性",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("✓ 底部导航栏切换", style = MaterialTheme.typography.bodyMedium)
                Text("✓ 带参数的路由", style = MaterialTheme.typography.bodyMedium)
                Text("✓ 页面转场动画", style = MaterialTheme.typography.bodyMedium)
                Text("✓ 状态保存与恢复", style = MaterialTheme.typography.bodyMedium)
                Text("✓ 返回栈管理", style = MaterialTheme.typography.bodyMedium)
            }
        }
        
        Text(
            text = "商品列表",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        // 示例商品列表
        val items = listOf(
            "智能手机" to Color(0xFF2196F3),
            "笔记本电脑" to Color(0xFF4CAF50),
            "无线耳机" to Color(0xFFFF9800),
            "智能手表" to Color(0xFF9C27B0),
            "平板电脑" to Color(0xFFF44336),
            "数码相机" to Color(0xFF00BCD4)
        )
        
        items.forEachIndexed { index, (name, color) ->
            ItemCard(
                id = index + 1,
                name = name,
                color = color,
                onClick = {
                    navController.navigate(Screen.Details.createRoute(index + 1, name))
                }
            )
        }
    }
}

@Composable
fun ItemCard(
    id: Int,
    name: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$id",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "点击查看详情",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProfileScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // 头像
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Text(
            text = "张三",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "zhangsan@example.com",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 用户信息卡片
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow(icon = Icons.Default.Phone, label = "手机", value = "138 1234 5678")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                InfoRow(icon = Icons.Default.LocationOn, label = "地址", value = "北京市朝阳区")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                InfoRow(icon = Icons.Default.DateRange, label = "注册时间", value = "2024-01-01")
            }
        }
        
        // 统计卡片
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "订单",
                value = "23",
                icon = Icons.Default.ShoppingCart
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "收藏",
                value = "156",
                icon = Icons.Default.Favorite
            )
        }
        
        Button(
            onClick = { /* 编辑资料 */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("编辑资料")
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun SettingsScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "常规设置",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        var notificationsEnabled by remember { mutableStateOf(true) }
        var darkModeEnabled by remember { mutableStateOf(false) }
        
        SettingItem(
            icon = Icons.Default.Notifications,
            title = "通知",
            subtitle = "接收应用通知",
            trailing = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }
        )
        
        Divider()
        
        SettingItem(
            icon = Icons.Default.Star,
            title = "深色模式",
            subtitle = "使用深色主题",
            trailing = {
                Switch(
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
            }
        )
        
        Divider()
        
        Text(
            text = "其他",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        SettingItem(
            icon = Icons.Default.Info,
            title = "关于",
            subtitle = "版本 1.0.0"
        )
        
        Divider()
        
        SettingItem(
            icon = Icons.Default.Share,
            title = "分享应用",
            subtitle = "分享给朋友"
        )
        
        Divider()
        
        SettingItem(
            icon = Icons.Default.Email,
            title = "反馈",
            subtitle = "发送反馈和建议"
        )
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = { }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DetailsScreen(
    itemId: Int,
    itemName: String,
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "商品 #$itemId",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = itemName,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        
        Text(
            text = "路由参数展示",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "参数 itemId: $itemId",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "参数 itemName: $itemName",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        Text(
            text = "商品详情",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "这是商品 $itemName 的详细信息页面。",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "通过 Navigation Compose，你可以轻松实现带参数的页面跳转。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("返回")
            }
            
            Button(
                onClick = { /* 加入购物车 */ },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("购买")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavigationAppPreview() {
    ComposeTheme {
        NavigationApp(onBack = {})
    }
}

