package com.test.compose

/**
 * ==================================================================================
 * MVIActivity - MVI (Model-View-Intent) 架构示例
 * ==================================================================================
 * 
 * 本文件展示了 MVI 架构模式在 Jetpack Compose 中的实现
 * 
 * MVI 核心概念：
 * 1. Model（状态）：单一不可变的 UI 状态
 * 2. View（视图）：纯函数，根据状态渲染 UI
 * 3. Intent（意图）：用户行为或系统事件
 * 4. 单向数据流：Intent -> Model -> View
 * 
 * MVI vs MVVM：
 * - MVVM：多个可变状态，双向数据流
 * - MVI：单一不可变状态，单向数据流
 * - MVI 更可预测，更容易测试和调试
 * 
 * 学习要点：
 * - 使用密封类定义所有可能的用户意图
 * - 使用 data class 定义完整的 UI 状态
 * - 状态是不可变的，每次更新都创建新对象
 * - View 是纯函数，只依赖状态，不持有任何状态
 * 
 * ==================================================================================
 */

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.test.compose.ui.theme.ComposeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==================== MVI 架构组件 ====================

/**
 * ShoppingIntent - 用户意图（Intent）
 * 
 * 密封类（sealed class）特点：
 * - 定义所有可能的用户行为
 * - 编译器可以检查是否处理了所有情况
 * - 类型安全，避免遗漏
 * 
 * MVI 模式的核心：
 * - 所有用户交互都转换为 Intent
 * - Intent 是不可变的，描述"发生了什么"
 * - ViewModel 接收 Intent，更新 State
 */
sealed class ShoppingIntent {
    // 添加商品意图
    data class AddProduct(val name: String, val price: Double) : ShoppingIntent()
    
    // 移除商品意图
    data class RemoveProduct(val id: Int) : ShoppingIntent()
    
    // 增加商品数量意图
    data class IncreaseQuantity(val id: Int) : ShoppingIntent()
    
    // 减少商品数量意图
    data class DecreaseQuantity(val id: Int) : ShoppingIntent()
    
    // 清空购物车意图
    data object ClearCart : ShoppingIntent()
    
    // 切换优惠券意图
    data object ToggleCoupon : ShoppingIntent()
    
    // 加载示例数据意图
    data object LoadSampleData : ShoppingIntent()
    
    // 结算意图
    data object Checkout : ShoppingIntent()
    
    // 关闭结算成功提示
    data object DismissCheckout : ShoppingIntent()
}

/**
 * ShoppingState - UI 状态（State/Model）
 * 
 * MVI 的核心原则：
 * - 单一数据源（Single Source of Truth）
 * - 状态是不可变的（Immutable）
 * - 所有 UI 数据都在这个类中
 * 
 * 优点：
 * - UI 完全由状态驱动，易于测试
 * - 状态变化可追踪，便于调试
 * - 支持时间旅行调试（Time Travel Debugging）
 */
data class ShoppingState(
    val products: List<ProductItem> = emptyList(),  // 购物车商品列表
    val hasCoupon: Boolean = false,                 // 是否使用优惠券
    val couponDiscount: Double = 0.1,               // 优惠券折扣（10%）
    val isLoading: Boolean = false,                 // 加载状态
    val checkoutSuccess: Boolean = false            // 结算成功标志
) {
    /**
     * 计算属性：从状态派生的值
     * 
     * 特点：
     * - 不存储在状态中，而是实时计算
     * - 保证一致性，避免数据不同步
     * - 类似于 ViewModel 的计算属性
     */
    
    // 商品总价
    val subtotal: Double
        get() = products.sumOf { it.price * it.quantity }
    
    // 折扣金额
    val discount: Double
        get() = if (hasCoupon) subtotal * couponDiscount else 0.0
    
    // 最终总价
    val total: Double
        get() = subtotal - discount
    
    // 商品总数量
    val totalItems: Int
        get() = products.sumOf { it.quantity }
    
    // 购物车是否为空
    val isEmpty: Boolean
        get() = products.isEmpty()
}

/**
 * ProductItem - 商品数据类
 */
data class ProductItem(
    val id: Int,
    val name: String,
    val price: Double,
    val quantity: Int = 1
)

// ==================== MVI ViewModel ====================

/**
 * ShoppingViewModel - MVI 的 ViewModel
 * 
 * 职责：
 * 1. 持有 UI 状态（State）
 * 2. 接收用户意图（Intent）
 * 3. 处理业务逻辑
 * 4. 更新状态（通过创建新的不可变对象）
 * 
 * MVI 流程：
 * View -> Intent -> ViewModel.processIntent() -> 更新 State -> View 重组
 */
class ShoppingViewModel : ViewModel() {
    
    /**
     * _state：私有可变状态
     * state：对外暴露的只读状态
     * 
     * 这是 MVI 的关键：
     * - 外部只能读取状态，不能直接修改
     * - 必须通过发送 Intent 来修改状态
     * - 保证了单向数据流
     */
    private val _state = mutableStateOf(ShoppingState())
    val state: State<ShoppingState> = _state
    
    // 用于生成唯一 ID
    private var nextId = 1
    
    /**
     * processIntent - 处理用户意图的唯一入口
     * 
     * MVI 核心方法：
     * - 接收 Intent
     * - 根据 Intent 类型执行相应逻辑
     * - 更新 State（创建新对象）
     * 
     * 优点：
     * - 所有状态变化都经过这个方法
     * - 便于日志记录和调试
     * - 易于添加中间件（如日志、分析）
     */
    fun processIntent(intent: ShoppingIntent) {
        when (intent) {
            is ShoppingIntent.AddProduct -> addProduct(intent.name, intent.price)
            is ShoppingIntent.RemoveProduct -> removeProduct(intent.id)
            is ShoppingIntent.IncreaseQuantity -> increaseQuantity(intent.id)
            is ShoppingIntent.DecreaseQuantity -> decreaseQuantity(intent.id)
            is ShoppingIntent.ClearCart -> clearCart()
            is ShoppingIntent.ToggleCoupon -> toggleCoupon()
            is ShoppingIntent.LoadSampleData -> loadSampleData()
            is ShoppingIntent.Checkout -> checkout()
            is ShoppingIntent.DismissCheckout -> dismissCheckout()
        }
    }
    
    /**
     * 添加商品
     * 
     * MVI 状态更新模式：
     * 1. 读取当前状态
     * 2. 计算新数据
     * 3. 使用 copy() 创建新状态对象
     * 4. 替换旧状态
     */
    private fun addProduct(name: String, price: Double) {
        if (name.isBlank() || price <= 0) return
        
        val currentProducts = _state.value.products
        val newProduct = ProductItem(
            id = nextId++,
            name = name,
            price = price
        )
        
        // 使用 copy() 创建新状态，只修改 products 字段
        _state.value = _state.value.copy(
            products = currentProducts + newProduct
        )
    }
    
    /**
     * 移除商品
     */
    private fun removeProduct(id: Int) {
        _state.value = _state.value.copy(
            products = _state.value.products.filter { it.id != id }
        )
    }
    
    /**
     * 增加商品数量
     */
    private fun increaseQuantity(id: Int) {
        _state.value = _state.value.copy(
            products = _state.value.products.map { product ->
                if (product.id == id) {
                    // 使用 data class 的 copy() 方法创建新对象
                    product.copy(quantity = product.quantity + 1)
                } else {
                    product
                }
            }
        )
    }
    
    /**
     * 减少商品数量
     * 如果数量为 1，则移除商品
     */
    private fun decreaseQuantity(id: Int) {
        _state.value = _state.value.copy(
            products = _state.value.products.mapNotNull { product ->
                when {
                    product.id != id -> product
                    product.quantity > 1 -> product.copy(quantity = product.quantity - 1)
                    else -> null  // 数量为 1 时移除
                }
            }
        )
    }
    
    /**
     * 清空购物车
     */
    private fun clearCart() {
        _state.value = _state.value.copy(
            products = emptyList(),
            hasCoupon = false
        )
    }
    
    /**
     * 切换优惠券状态
     */
    private fun toggleCoupon() {
        _state.value = _state.value.copy(
            hasCoupon = !_state.value.hasCoupon
        )
    }
    
    /**
     * 加载示例数据
     * 演示异步操作在 MVI 中的处理
     */
    private fun loadSampleData() {
        // 1. 设置加载状态
        _state.value = _state.value.copy(isLoading = true)
        
        // 2. 模拟网络请求（使用协程）
        viewModelScope.launch {
            delay(1000)  // 模拟延迟
            
            // 3. 更新数据和状态
            val sampleProducts = listOf(
                ProductItem(nextId++, "MacBook Pro", 12999.0),
                ProductItem(nextId++, "iPhone 15 Pro", 7999.0),
                ProductItem(nextId++, "AirPods Pro", 1999.0),
                ProductItem(nextId++, "iPad Air", 4799.0)
            )
            
            _state.value = _state.value.copy(
                products = _state.value.products + sampleProducts,
                isLoading = false
            )
        }
    }
    
    /**
     * 结算
     */
    private fun checkout() {
        if (_state.value.isEmpty) return
        
        _state.value = _state.value.copy(
            checkoutSuccess = true,
            products = emptyList(),
            hasCoupon = false
        )
    }
    
    /**
     * 关闭结算成功提示
     */
    private fun dismissCheckout() {
        _state.value = _state.value.copy(
            checkoutSuccess = false
        )
    }
}

// ==================== Activity ====================

class MVIActivity : ComponentActivity() {
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
                            title = { Text("MVI 架构测试") },
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
                    MVIScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// ==================== UI 组件（View） ====================

/**
 * MVIScreen - MVI 的 View 层
 * 
 * MVI View 的特点：
 * 1. 纯函数：只依赖传入的状态
 * 2. 无状态：不持有任何本地状态（除了临时 UI 状态）
 * 3. 声明式：描述 UI 应该是什么样子，而不是如何变化
 * 4. 单向数据流：State -> UI，用户操作 -> Intent
 */
@Composable
fun MVIScreen(
    modifier: Modifier = Modifier,
    viewModel: ShoppingViewModel = viewModel()
) {
    // 读取状态
    val state = viewModel.state.value
    
    // 本地 UI 状态（输入框的值，不需要在 ViewModel 中管理）
    var productName by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部统计卡片
            ShoppingHeader(
                state = state,
                onIntent = viewModel::processIntent
            )
            
            // 添加商品表单
            AddProductForm(
                productName = productName,
                onNameChange = { productName = it },
                productPrice = productPrice,
                onPriceChange = { productPrice = it },
                onAddClick = {
                    // 发送 Intent
                    val price = productPrice.toDoubleOrNull() ?: 0.0
                    viewModel.processIntent(
                        ShoppingIntent.AddProduct(productName, price)
                    )
                    // 清空输入框
                    productName = ""
                    productPrice = ""
                }
            )
            
            // 商品列表
            ProductList(
                modifier = Modifier.weight(1f),
                products = state.products,
                onIntent = viewModel::processIntent
            )
            
            // 底部总价和结算按钮
            if (!state.isEmpty) {
                ShoppingFooter(
                    state = state,
                    onIntent = viewModel::processIntent
                )
            }
        }
        
        // 加载指示器
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // 结算成功提示
        if (state.checkoutSuccess) {
            AlertDialog(
                onDismissRequest = { 
                    viewModel.processIntent(ShoppingIntent.DismissCheckout)
                },
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                title = { Text("结算成功！") },
                text = { Text("订单已提交，感谢购买！") },
                confirmButton = {
                    TextButton(
                        onClick = { 
                            viewModel.processIntent(ShoppingIntent.DismissCheckout)
                        }
                    ) {
                        Text("确定")
                    }
                }
            )
        }
    }
}

/**
 * ShoppingHeader - 顶部统计卡片
 * 
 * 纯 View 组件：
 * - 只接收状态和回调
 * - 不持有任何状态
 * - 可以单独测试
 */
@Composable
fun ShoppingHeader(
    state: ShoppingState,
    onIntent: (ShoppingIntent) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "购物车 MVI 示例",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // 加载示例数据按钮
                FilledTonalButton(
                    onClick = { onIntent(ShoppingIntent.LoadSampleData) },
                    enabled = !state.isLoading
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("示例数据")
                }
            }
            
            // 统计信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("商品", "${state.totalItems} 件")
                StatItem("小计", "¥${String.format("%.2f", state.subtotal)}")
                if (state.hasCoupon) {
                    StatItem("优惠", "-¥${String.format("%.2f", state.discount)}")
                }
            }
            
            // MVI 说明
            Text(
                text = "MVI：单向数据流架构，所有操作都是 Intent",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * StatItem - 统计项
 */
@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * AddProductForm - 添加商品表单
 */
@Composable
fun AddProductForm(
    productName: String,
    onNameChange: (String) -> Unit,
    productPrice: String,
    onPriceChange: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "添加商品",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = productName,
                    onValueChange = onNameChange,
                    label = { Text("商品名称") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = productPrice,
                    onValueChange = onPriceChange,
                    label = { Text("价格") },
                    modifier = Modifier.weight(0.6f),
                    singleLine = true,
                    prefix = { Text("¥") }
                )
                
                FilledIconButton(
                    onClick = onAddClick,
                    enabled = productName.isNotBlank() && 
                             (productPrice.toDoubleOrNull() ?: 0.0) > 0,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加")
                }
            }
        }
    }
}

/**
 * ProductList - 商品列表
 */
@Composable
fun ProductList(
    modifier: Modifier = Modifier,
    products: List<ProductItem>,
    onIntent: (ShoppingIntent) -> Unit
) {
    if (products.isEmpty()) {
        // 空状态
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "购物车为空",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        // 商品列表
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products, key = { it.id }) { product ->
                ProductItemCard(
                    product = product,
                    onIncrease = { onIntent(ShoppingIntent.IncreaseQuantity(product.id)) },
                    onDecrease = { onIntent(ShoppingIntent.DecreaseQuantity(product.id)) },
                    onRemove = { onIntent(ShoppingIntent.RemoveProduct(product.id)) }
                )
            }
        }
    }
}

/**
 * ProductItemCard - 商品卡片
 */
@Composable
fun ProductItemCard(
    product: ProductItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 商品信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "¥${String.format("%.2f", product.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // 数量控制
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onDecrease,
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "−",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "${product.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                FilledTonalButton(
                    onClick = onIncrease,
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * ShoppingFooter - 底部结算栏
 */
@Composable
fun ShoppingFooter(
    state: ShoppingState,
    onIntent: (ShoppingIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large.copy(
            bottomStart = androidx.compose.foundation.shape.CornerSize(0.dp),
            bottomEnd = androidx.compose.foundation.shape.CornerSize(0.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 优惠券切换
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("使用优惠券 (-10%)")
                }
                
                Switch(
                    checked = state.hasCoupon,
                    onCheckedChange = { onIntent(ShoppingIntent.ToggleCoupon) }
                )
            }
            
            Divider()
            
            // 总价显示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "总计",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "¥${String.format("%.2f", state.total)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onIntent(ShoppingIntent.ClearCart) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("清空")
                }
                
                Button(
                    onClick = { onIntent(ShoppingIntent.Checkout) },
                    modifier = Modifier.weight(2f)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("结算")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MVIScreenPreview() {
    ComposeTheme {
        MVIScreen()
    }
}

