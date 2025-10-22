package com.test.compose

// ==================== 导入区域 ====================
// Android 基础导入
import android.content.Intent  // Intent：用于启动其他 Activity
import android.os.Bundle

// Activity 相关
import androidx.activity.ComponentActivity  // ComponentActivity：Compose 推荐的 Activity 基类
import androidx.activity.compose.setContent  // setContent：在 Activity 中设置 Compose UI 的入口函数
import androidx.activity.enableEdgeToEdge  // enableEdgeToEdge：启用全面屏（边到边）显示

// Compose 基础组件
import androidx.compose.foundation.clickable  // clickable：为组件添加点击事件
import androidx.compose.foundation.layout.*  // 布局相关：Row, Column, Box, Spacer, padding 等

// Compose 列表组件
import androidx.compose.foundation.lazy.LazyColumn  // LazyColumn：垂直延迟加载列表，类似 RecyclerView
import androidx.compose.foundation.lazy.items  // items：LazyColumn 中渲染列表项的函数

// Material 图标
import androidx.compose.material.icons.Icons  // Icons：Material Design 图标库
import androidx.compose.material.icons.filled.KeyboardArrowRight  // 右箭头图标

// Material3 组件
import androidx.compose.material3.*  // Material3 所有组件：Card, Text, Scaffold, TopAppBar 等

// Compose 运行时
import androidx.compose.runtime.Composable  // @Composable：标记可组合函数的注解

// Compose UI 工具
import androidx.compose.ui.Alignment  // Alignment：对齐方式
import androidx.compose.ui.Modifier  // Modifier：修饰符，用于修改组件的外观和行为
import androidx.compose.ui.platform.LocalContext  // LocalContext：获取当前上下文的组合局部变量
import androidx.compose.ui.tooling.preview.Preview  // @Preview：预览注解，在 Android Studio 中显示预览
import androidx.compose.ui.unit.dp  // dp：密度无关像素单位

// 自定义主题
import com.test.compose.ui.theme.ComposeTheme

// ==================== 数据类定义 ====================
/**
 * 功能项数据类
 * 用于表示主页面每个功能卡片的信息
 * 
 * @param title 功能标题
 * @param description 功能描述
 * @param activityClass 对应的 Activity 类（用于跳转）
 */
data class FeatureItem(
    val title: String,              // 功能标题
    val description: String,         // 功能描述文本
    val activityClass: Class<*>     // 要跳转的 Activity 类
)

// ==================== 主 Activity ====================
/**
 * MainActivity - 应用的主入口页面
 * 
 * 继承自 ComponentActivity，这是 Jetpack Compose 推荐的 Activity 基类
 * 主要功能：展示所有学习功能的列表，作为导航入口
 */
class MainActivity : ComponentActivity() {
    
    /**
     * @OptIn 注解说明：
     * TopAppBar 在 Material3 中是实验性 API，使用此注解表示开发者明确知道
     * 这个 API 可能在未来版本中发生变化，但仍然选择使用它
     */
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 启用全面屏显示（边到边），让内容可以延伸到系统栏下方
        enableEdgeToEdge()
        
        /**
         * setContent：Compose 的入口函数
         * 在传统 Android 开发中使用 setContentView(R.layout.xxx)
         * 在 Compose 中使用 setContent { } 来设置 UI
         */
        setContent {
            // ComposeTheme：应用自定义主题，包含颜色、字体、形状等配置
            ComposeTheme {
                /**
                 * Scaffold：脚手架组件，Material Design 的标准布局结构
                 * 提供了 topBar、bottomBar、floatingActionButton 等插槽
                 * 是构建完整页面的基础容器
                 */
                Scaffold(
                    // modifier：修饰符，fillMaxSize() 让组件填充整个可用空间
                    modifier = Modifier.fillMaxSize(),
                    
                    // topBar：顶部应用栏插槽
                    topBar = {
                        /**
                         * TopAppBar：顶部导航栏
                         * Material3 的标准顶部栏组件
                         */
                        TopAppBar(
                            // title：顶部栏标题，是一个 Composable lambda
                            title = { Text("Compose UI 学习") },
                            
                            // colors：自定义 TopAppBar 的颜色
                            colors = TopAppBarDefaults.topAppBarColors(
                                // containerColor：容器背景色，使用主题的 primaryContainer 颜色
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                // titleContentColor：标题文字颜色
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                ) { innerPadding ->
                    /**
                     * innerPadding：Scaffold 提供的内边距
                     * 自动计算 topBar、bottomBar 等占用的空间
                     * 需要传递给内容区域，避免内容被系统栏遮挡
                     */
                    FeatureListScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// ==================== Composable 函数区域 ====================

/**
 * FeatureListScreen - 功能列表屏幕
 * 
 * @Composable 注解：
 * - 标记这是一个可组合函数，可以被 Compose 运行时调用
 * - 可组合函数可以调用其他可组合函数
 * - 在重组（Recomposition）时会重新执行
 * 
 * @param modifier Modifier 参数，遵循 Compose 的最佳实践
 *                 - 默认值为 Modifier，表示无修饰
 *                 - 允许调用者自定义外观和行为
 */
@Composable
fun FeatureListScreen(modifier: Modifier = Modifier) {
    /**
     * LocalContext.current：获取当前的 Android Context
     * - LocalContext 是 Compose 提供的 CompositionLocal
     * - CompositionLocal 允许在组合树中隐式传递数据
     * - 这里用于获取 Context 以便启动其他 Activity
     */
    val context = LocalContext.current
    
    /**
     * 功能列表数据
     * 使用 listOf 创建不可变列表
     * 每个 FeatureItem 对应主页面的一个功能卡片
     */
    val features = listOf(
        FeatureItem(
            title = "Text & Button 测试",
            description = "学习Text和Button组件的各种用法",
            activityClass = TextButtonActivity::class.java
        ),
        FeatureItem(
            title = "列表加载测试",
            description = "学习LazyColumn列表的使用",
            activityClass = ListActivity::class.java
        ),
        FeatureItem(
            title = "ViewModel 绑定测试",
            description = "学习ViewModel与Compose的集成",
            activityClass = ViewModelActivity::class.java
        ),
        FeatureItem(
            title = "Navigation 导航测试",
            description = "Material3 + Compose Navigation 完整架构",
            activityClass = NavigationActivity::class.java
        ),
        FeatureItem(
            title = "自定义控件测试",
            description = "Canvas 绘制和自定义测量布局",
            activityClass = CustomViewActivity::class.java
        ),
        FeatureItem(
            title = "底部抽屉测试",
            description = "Modal 和 Standard 底部抽屉的实现",
            activityClass = BottomSheetActivity::class.java
        ),
        FeatureItem(
            title = "MVI 架构测试",
            description = "Model-View-Intent 单向数据流架构",
            activityClass = MVIActivity::class.java
        ),
        FeatureItem(
            title = "副作用 API 测试",
            description = "LaunchedEffect、SideEffect、DisposableEffect",
            activityClass = EffectActivity::class.java
        )
    )
    
    /**
     * LazyColumn：延迟加载的垂直列表
     * 
     * 特点：
     * - 类似于传统的 RecyclerView
     * - 只渲染可见的项，提高性能
     * - 自动处理滚动和回收
     * - 比普通 Column 更适合大量数据
     * 
     * 参数说明：
     * - modifier：应用传入的修饰符，并添加 fillMaxSize() 填充整个空间
     * - contentPadding：内容内边距，四周各 16dp
     * - verticalArrangement：垂直排列方式，spacedBy(12dp) 表示项之间间距 12dp
     */
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),  // 列表内容的内边距
        verticalArrangement = Arrangement.spacedBy(12.dp)  // 列表项之间的间距
    ) {
        /**
         * items：LazyColumn 的 DSL 函数，用于渲染列表项
         * 
         * 参数：
         * - features：要渲染的数据列表
         * - lambda：对每个项执行的渲染逻辑
         * 
         * 工作原理：
         * - 遍历 features 列表
         * - 对每个 feature 调用 lambda 生成对应的 UI
         * - 自动管理列表项的键值和重组
         */
        items(features) { feature ->
            FeatureCard(
                feature = feature,
                onClick = {
                    // 点击卡片时启动对应的 Activity
                    // Intent 用于跨组件通信，这里用于启动新的 Activity
                    context.startActivity(Intent(context, feature.activityClass))
                }
            )
        }
    }
}

/**
 * FeatureCard - 功能卡片组件
 * 
 * 可重用的 UI 组件，展示单个功能项
 * 这是 Compose 组件化开发的典型例子
 * 
 * @param feature 功能数据对象
 * @param onClick 点击事件回调，() -> Unit 表示无参数无返回值的函数
 */
@Composable
fun FeatureCard(
    feature: FeatureItem,
    onClick: () -> Unit
) {
    /**
     * Card：Material3 的卡片组件
     * - 提供阴影、圆角等视觉效果
     * - 符合 Material Design 规范
     */
    Card(
        modifier = Modifier
            .fillMaxWidth()  // 填充父容器的宽度
            .clickable(onClick = onClick),  // 添加点击事件
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)  // 设置卡片阴影高度
    ) {
        /**
         * Row：水平布局容器
         * - 子元素从左到右排列
         * - 类似于传统的 LinearLayout (horizontal)
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()  // 填充卡片宽度
                .padding(16.dp),  // 内边距 16dp
            horizontalArrangement = Arrangement.SpaceBetween,  // 子元素两端对齐
            verticalAlignment = Alignment.CenterVertically  // 子元素垂直居中对齐
        ) {
            /**
             * Column：垂直布局容器
             * - 子元素从上到下排列
             * - 类似于传统的 LinearLayout (vertical)
             */
            Column(
                modifier = Modifier.weight(1f)  // weight(1f) 占据剩余空间
            ) {
                /**
                 * Text：文本组件
                 * - Compose 中显示文本的基础组件
                 * - 替代传统的 TextView
                 */
                Text(
                    text = feature.title,  // 显示的文本内容
                    style = MaterialTheme.typography.titleMedium,  // 使用主题定义的标题样式
                    color = MaterialTheme.colorScheme.onSurface  // 使用主题定义的颜色
                )
                
                /**
                 * Spacer：空白间隔组件
                 * - 用于在组件之间添加空白
                 * - 比使用 padding 更语义化
                 */
                Spacer(modifier = Modifier.height(4.dp))  // 4dp 高度的垂直间距
                
                Text(
                    text = feature.description,  // 描述文本
                    style = MaterialTheme.typography.bodyMedium,  // 正文样式
                    color = MaterialTheme.colorScheme.onSurfaceVariant  // 次要文本颜色
                )
            }
            
            /**
             * Icon：图标组件
             * - 显示矢量图标
             * - imageVector 使用 Material Icons
             * - contentDescription 用于无障碍支持
             */
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,  // 右箭头图标
                contentDescription = "进入",  // 无障碍描述
                tint = MaterialTheme.colorScheme.onSurfaceVariant  // 图标颜色
            )
        }
    }
}

// ==================== 预览区域 ====================

/**
 * @Preview：预览注解
 * - 在 Android Studio 中显示 Compose UI 预览
 * - 无需运行应用即可查看 UI 效果
 * - showBackground = true 显示背景
 * 
 * 预览函数的要求：
 * - 必须是 @Composable 函数
 * - 不能有参数
 * - 用于开发时快速查看 UI
 */
@Preview(showBackground = true)
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FeatureListPreview() {
    // 在预览中也要包裹主题，保证样式正确
    ComposeTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Compose UI 学习") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { innerPadding ->
            FeatureListScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}