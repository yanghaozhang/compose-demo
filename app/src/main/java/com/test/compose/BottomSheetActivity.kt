package com.test.compose

/**
 * ==================================================================================
 * BottomSheetActivity - 底部抽屉测试
 * ==================================================================================
 * 
 * 展示 Material3 底部抽屉的各种实现：
 * 1. ModalBottomSheet - 模态底部抽屉
 * 2. BottomSheetScaffold - 标准底部抽屉
 * 3. 自定义底部抽屉
 * 
 * 学习要点：
 * - rememberModalBottomSheetState
 * - rememberStandardBottomSheetState
 * - SheetValue (Expanded, PartiallyExpanded, Hidden)
 * - 手势交互
 * 
 * ==================================================================================
 */

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.test.compose.ui.theme.ComposeTheme
import kotlinx.coroutines.launch

class BottomSheetActivity : ComponentActivity() {
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
                            title = { Text("底部抽屉测试") },
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
                    BottomSheetScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Modal 抽屉", "Standard 抽屉", "自定义样式")
    
    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        when (selectedTab) {
            0 -> ModalBottomSheetExample()
            1 -> StandardBottomSheetExample()
            2 -> CustomBottomSheetExample()
        }
    }
}

// ==================== Modal Bottom Sheet ====================

/**
 * ModalBottomSheetExample - 模态底部抽屉示例
 * 
 * 特点：
 * - 覆盖整个屏幕
 * - 背景半透明遮罩
 * - 点击外部关闭
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetExample() {
    // 控制抽屉显示/隐藏
    var showBottomSheet by remember { mutableStateOf(false) }
    
    // 抽屉状态
    val sheetState = rememberModalBottomSheetState()
    
    // 协程作用域
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Modal Bottom Sheet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            "模态底部抽屉会覆盖整个屏幕，背景有半透明遮罩",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // 示例按钮
        Button(
            onClick = { showBottomSheet = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.List, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("打开菜单列表")
        }
        
        Button(
            onClick = { showBottomSheet = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("打开分享选项")
        }
        
        // 状态信息
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "抽屉状态",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text("显示状态: ${if (showBottomSheet) "显示" else "隐藏"}")
                Text("当前值: ${sheetState.currentValue}")
            }
        }
    }
    
    /**
     * ModalBottomSheet - 模态底部抽屉组件
     * 
     * 参数：
     * - onDismissRequest: 关闭时回调
     * - sheetState: 抽屉状态
     * - content: 抽屉内容
     */
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            // 抽屉内容
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "选择操作",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(16.dp))
                
                // 菜单项列表
                val menuItems = listOf(
                    "编辑" to Icons.Default.Edit,
                    "分享" to Icons.Default.Share,
                    "删除" to Icons.Default.Delete,
                    "收藏" to Icons.Default.Favorite,
                    "下载" to Icons.Default.Add,
                    "设置" to Icons.Default.Settings
                )
                
                menuItems.forEach { (title, icon) ->
                    MenuItem(
                        title = title,
                        icon = icon,
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                showBottomSheet = false
                            }
                        }
                    )
                }
                
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun MenuItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp)
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
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// ==================== Standard Bottom Sheet ====================

/**
 * StandardBottomSheetExample - 标准底部抽屉示例
 * 
 * 使用 BottomSheetScaffold 实现
 * 特点：
 * - 可以部分展开
 * - 不会遮挡整个屏幕
 * - 适合持久性内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardBottomSheetExample() {
    // 抽屉状态
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    
    /**
     * BottomSheetScaffold - 带底部抽屉的脚手架
     * 
     * 参数：
     * - scaffoldState: 脚手架状态
     * - sheetContent: 抽屉内容
     * - sheetPeekHeight: 折叠时显示的高度
     * - content: 主内容
     */
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 80.dp,  // 折叠时显示的高度
        sheetContent = {
            // 抽屉内容
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(16.dp)
            ) {
                // 拖动手柄
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        .align(Alignment.CenterHorizontally)
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "播放列表",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(16.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(20) { index ->
                        SongItem(
                            title = "歌曲 ${index + 1}",
                            artist = "艺术家 ${index + 1}",
                            isPlaying = index == 0
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // 主内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Standard Bottom Sheet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "标准底部抽屉可以部分展开，适合显示持久性内容",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 控制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("展开")
                }
                
                Button(
                    onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.partialExpand()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("部分展开")
                }
            }
            
            // 状态信息
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "抽屉状态",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("当前状态: ${scaffoldState.bottomSheetState.currentValue}")
                    Text("是否展开: ${scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded}")
                }
            }
        }
    }
}

@Composable
private fun SongItem(
    title: String,
    artist: String,
    isPlaying: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isPlaying) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Star else Icons.Default.Menu,
            contentDescription = null,
            tint = if (isPlaying) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (isPlaying) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ==================== Custom Bottom Sheet ====================

/**
 * CustomBottomSheetExample - 自定义样式底部抽屉
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheetExample() {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false  // 允许部分展开
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "自定义样式底部抽屉",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            "展示自定义颜色、形状、内容的底部抽屉",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Button(
            onClick = { showBottomSheet = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("打开购物车")
        }
        
        Button(
            onClick = { showBottomSheet = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.MoreVert, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("打开筛选器")
        }
    }
    
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 顶部标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "购物车",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = { showBottomSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // 商品列表
                val cartItems = listOf(
                    CartItem("无线耳机", 299.00, 1),
                    CartItem("手机壳", 39.00, 2),
                    CartItem("充电器", 79.00, 1)
                )
                
                cartItems.forEach { item ->
                    CartItemRow(item)
                    Spacer(Modifier.height(8.dp))
                }
                
                Divider(Modifier.padding(vertical = 16.dp))
                
                // 总价
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "总计",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "¥${cartItems.sumOf { it.price * it.quantity }}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("结算", modifier = Modifier.padding(8.dp))
                }
                
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

data class CartItem(
    val name: String,
    val price: Double,
    val quantity: Int
)

@Composable
private fun CartItemRow(item: CartItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 图片占位
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "¥${item.price}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // 数量
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = "×${item.quantity}",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

