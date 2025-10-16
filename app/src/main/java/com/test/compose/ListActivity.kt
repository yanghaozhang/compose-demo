package com.test.compose

/**
 * ==================================================================================
 * ListActivity - Compose 列表组件学习示例
 * ==================================================================================
 * 
 * 本文件全面展示了 Compose 中列表的使用：
 * 1. LazyColumn - 垂直延迟加载列表
 * 2. LazyRow - 横向延迟加载列表
 * 3. LazyVerticalGrid - 网格列表
 * 4. 列表项的交互和状态管理
 * 
 * 核心概念：
 * - Lazy 组件的性能优势（类似 RecyclerView）
 * - items() 和 itemsIndexed() 的使用
 * - 列表数据的创建和管理
 * - 动态状态在列表中的应用
 * 
 * 对比传统开发：
 * - LazyColumn ≈ RecyclerView (vertical)
 * - LazyRow ≈ RecyclerView (horizontal)
 * - LazyVerticalGrid ≈ GridLayoutManager
 * 
 * 优势：
 * - 无需适配器（Adapter）
 * - 无需 ViewHolder
 * - 声明式编写，代码更简洁
 * 
 * ==================================================================================
 */

// ==================== 导入区域 ====================
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background  // background：背景修饰符
import androidx.compose.foundation.layout.*

// Lazy 列表组件
import androidx.compose.foundation.lazy.LazyColumn  // 垂直延迟列表
import androidx.compose.foundation.lazy.LazyRow  // 横向延迟列表
import androidx.compose.foundation.lazy.items  // 渲染列表项
import androidx.compose.foundation.lazy.itemsIndexed  // 带索引的列表项渲染

// Grid 网格组件
import androidx.compose.foundation.lazy.grid.GridCells  // 网格单元格配置
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid  // 垂直网格
import androidx.compose.foundation.lazy.grid.items  // 网格项渲染

// 形状
import androidx.compose.foundation.shape.CircleShape  // 圆形
import androidx.compose.foundation.shape.RoundedCornerShape  // 圆角矩形

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*  // 所有 filled 图标
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip  // clip：裁剪修饰符
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.test.compose.ui.theme.ComposeTheme

// ==================== 数据类定义 ====================

/**
 * User - 用户数据类
 * 用于垂直列表示例
 */
data class User(
    val id: Int,              // 用户唯一标识
    val name: String,         // 用户名
    val email: String,        // 邮箱
    val avatar: ImageVector   // 头像图标
)

/**
 * Product - 商品数据类
 * 用于横向列表示例
 */
data class Product(
    val id: Int,          // 商品 ID
    val name: String,     // 商品名称
    val price: Double,    // 价格
    val category: String, // 分类
    val color: Color      // 展示颜色
)

/**
 * Message - 消息数据类
 * 用于消息列表示例
 */
data class Message(
    val id: Int,          // 消息 ID
    val sender: String,   // 发送者
    val content: String,  // 消息内容
    val time: String,     // 时间
    val isRead: Boolean   // 是否已读
)

// ==================== Activity ====================

/**
 * ListActivity - 列表加载测试页面
 */
class ListActivity : ComponentActivity() {
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
                            title = { Text("列表加载测试") },
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
                    ListScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// ==================== 主屏幕 ====================

/**
 * ListScreen - 列表示例主屏幕
 * 
 * 使用 TabRow 切换不同类型的列表示例：
 * - 垂直列表（LazyColumn）
 * - 横向列表（LazyRow）
 * - 网格列表（LazyVerticalGrid）
 * - 消息列表（带状态交互）
 */
@Composable
fun ListScreen(modifier: Modifier = Modifier) {
    // 选中的 Tab 索引
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("垂直列表", "横向列表", "网格列表", "消息列表")
    
    Column(modifier = modifier.fillMaxSize()) {
        /**
         * TabRow：Material3 的标签页组件
         * 常用于在多个视图之间切换
         */
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        /**
         * 根据选中的 Tab 显示不同的列表示例
         * when 表达式：Kotlin 的模式匹配
         */
        when (selectedTab) {
            0 -> VerticalListExample()
            1 -> HorizontalListExample()
            2 -> GridListExample()
            3 -> MessageListExample()
        }
    }
}

// ==================== 垂直列表示例 ====================

/**
 * VerticalListExample - 垂直列表示例
 * 
 * 演示 LazyColumn 的基本用法：
 * - 创建模拟数据
 * - 使用 itemsIndexed 渲染列表项
 * - 列表项支持展开/收起交互
 * 
 * LazyColumn 特点：
 * - 只渲染可见项，性能优秀
 * - 自动处理滚动和回收
 * - 类似 RecyclerView 但更简单
 */
@Composable
fun VerticalListExample() {
    /**
     * remember：在重组时保持数据
     * 
     * 这里创建 20 个模拟用户数据
     * List(20) { }：创建包含 20 个元素的列表
     */
    val users = remember {
        List(20) { index ->
            User(
                id = index,
                name = "用户 ${index + 1}",
                email = "user${index + 1}@example.com",
                avatar = when (index % 4) {
                    0 -> Icons.Default.Person
                    1 -> Icons.Default.AccountCircle
                    2 -> Icons.Default.Face
                    else -> Icons.Default.Person
                }
            )
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "用户列表 (${users.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        itemsIndexed(users) { index, user ->
            UserCard(user = user, index = index)
        }
        
        item {
            Text(
                text = "已加载全部用户",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun UserCard(user: User, index: Int) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { isExpanded = !isExpanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = user.avatar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "这是第 ${index + 1} 个用户的详细信息区域",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 横向列表示例
@Composable
fun HorizontalListExample() {
    val categories = remember {
        listOf("全部", "电子产品", "服装", "食品", "图书", "运动", "家居", "美妆")
    }
    
    val products = remember {
        List(15) { index ->
            Product(
                id = index,
                name = "商品 ${index + 1}",
                price = (10..1000).random().toDouble(),
                category = categories.random(),
                color = Color(
                    red = (0..255).random(),
                    green = (0..255).random(),
                    blue = (0..255).random()
                )
            )
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 横向分类列表
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text(category) }
                )
            }
        }
        
        Divider()
        
        // 横向商品列表
        Text(
            text = "热门商品",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                ProductCard(product = product)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 竖向商品列表
        Text(
            text = "推荐商品",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products.take(5)) { product ->
                ProductListItem(product = product)
            }
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(product.color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¥${product.price}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProductListItem(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(product.color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "¥${product.price}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 网格列表示例
@Composable
fun GridListExample() {
    val colors = remember {
        List(50) { index ->
            Color(
                red = (0..255).random(),
                green = (0..255).random(),
                blue = (0..255).random()
            )
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "颜色网格 (2列)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(colors) { color ->
                ColorGridItem(color = color)
            }
        }
    }
}

@Composable
fun ColorGridItem(color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#${color.value.toString(16).take(6).uppercase()}",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 消息列表示例
@Composable
fun MessageListExample() {
    val messages = remember {
        List(25) { index ->
            Message(
                id = index,
                sender = "联系人 ${index + 1}",
                content = "这是第 ${index + 1} 条消息的内容。点击查看详情...",
                time = "${(8..23).random()}:${(10..59).random()}",
                isRead = index % 3 != 0
            )
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        item {
            Text(
                text = "消息列表 (${messages.count { !it.isRead }} 条未读)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
        }
        
        items(messages) { message ->
            MessageItem(message = message)
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    var isRead by remember { mutableStateOf(message.isRead) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRead) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        onClick = { isRead = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.sender.first().toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = message.sender,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isRead) FontWeight.Normal else FontWeight.Bold
                    )
                    Text(
                        text = message.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            
            if (!isRead) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListScreenPreview() {
    ComposeTheme {
        ListScreen()
    }
}

