package com.test.compose

/**
 * ==================================================================================
 * CustomViewActivity - 自定义控件学习示例
 * ==================================================================================
 * 
 * 本文件展示了 Compose 中自定义控件的两大核心：
 * 1. Layout 测量 - 自定义组件的尺寸和位置
 * 2. Canvas 绘制 - 使用 Canvas API 绘制自定义图形
 * 
 * 学习要点：
 * - Layout 的测量和布局机制
 * - Canvas 的各种绘制 API
 * - 自定义修饰符（Modifier）
 * - 性能优化技巧
 * 
 * 核心概念：
 * - Constraints：约束条件，决定组件的最大最小尺寸
 * - Measurable：可测量的组件
 * - Placeable：可放置的组件
 * - DrawScope：绘制作用域
 * 
 * ==================================================================================
 */

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.test.compose.ui.theme.ComposeTheme
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// ==================== Activity ====================

class CustomViewActivity : ComponentActivity() {
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
                            title = { Text("自定义控件测试") },
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
                    CustomViewScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// ==================== 主屏幕 ====================

@Composable
fun CustomViewScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Canvas 绘制", "自定义测量", "实用控件")
    
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
            0 -> CanvasExamplesScreen()
            1 -> CustomLayoutScreen()
            2 -> PracticalControlsScreen()
        }
    }
}

// ==================== Canvas 绘制示例 ====================

/**
 * CanvasExamplesScreen - Canvas 绘制基础示例
 * 
 * 展示 Canvas 的各种绘制 API
 */
@Composable
fun CanvasExamplesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle("Canvas 基础绘制")
        
        // 示例 1：基础图形
        Text("1. 基础图形绘制", style = MaterialTheme.typography.titleMedium)
        BasicShapesCanvas()
        
        // 示例 2：路径绘制
        Text("2. 路径（Path）绘制", style = MaterialTheme.typography.titleMedium)
        PathDrawingCanvas()
        
        // 示例 3：渐变效果
        Text("3. 渐变效果", style = MaterialTheme.typography.titleMedium)
        GradientCanvas()
        
        // 示例 4：文字绘制
        Text("4. 文字绘制", style = MaterialTheme.typography.titleMedium)
        TextDrawingCanvas()
    }
}

/**
 * BasicShapesCanvas - 基础图形绘制示例
 * 
 * 展示如何绘制线条、圆形、矩形等基础图形
 */
@Composable
fun BasicShapesCanvas() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        /**
         * Canvas - Compose 的绘制画布
         * 
         * 参数：
         * - modifier：修饰符，通常设置大小
         * - onDraw：绘制逻辑，在 DrawScope 中执行
         * 
         * DrawScope：绘制作用域，提供各种绘制方法
         * - drawLine：绘制直线
         * - drawCircle：绘制圆形
         * - drawRect：绘制矩形
         * - drawRoundRect：绘制圆角矩形
         * - drawArc：绘制圆弧
         * - drawPath：绘制路径
         * 等等...
         */
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFF5F5F5))
        ) {
            // size：Canvas 的尺寸（Size 对象，包含 width 和 height）
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            /**
             * drawLine - 绘制直线
             * 
             * 参数：
             * - color：线条颜色
             * - start：起点坐标（Offset）
             * - end：终点坐标（Offset）
             * - strokeWidth：线宽
             */
            drawLine(
                color = Color.Red,
                start = Offset(x = 50f, y = 50f),
                end = Offset(x = canvasWidth - 50f, y = 50f),
                strokeWidth = 5f
            )
            
            /**
             * drawCircle - 绘制圆形
             * 
             * 参数：
             * - color：填充颜色
             * - radius：半径
             * - center：圆心坐标
             */
            drawCircle(
                color = Color.Blue,
                radius = 40f,
                center = Offset(x = 100f, y = 120f)
            )
            
            /**
             * drawRect - 绘制矩形
             * 
             * 参数：
             * - color：填充颜色
             * - topLeft：左上角坐标
             * - size：矩形大小
             */
            drawRect(
                color = Color.Green,
                topLeft = Offset(x = 200f, y = 80f),
                size = Size(width = 80f, height = 80f)
            )
            
            /**
             * drawRoundRect - 绘制圆角矩形
             * 
             * 参数：
             * - color：颜色
             * - topLeft：左上角坐标
             * - size：大小
             * - cornerRadius：圆角半径
             */
            drawRoundRect(
                color = Color.Magenta,
                topLeft = Offset(x = canvasWidth - 150f, y = 80f),
                size = Size(width = 100f, height = 80f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
            )
        }
    }
}

/**
 * PathDrawingCanvas - 路径绘制示例
 * 
 * Path 可以绘制复杂的自定义形状
 */
@Composable
fun PathDrawingCanvas() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFF5F5F5))
        ) {
            /**
             * Path - 路径对象
             * 
             * 可以创建复杂的图形路径
             * 常用方法：
             * - moveTo：移动到某点（不绘制）
             * - lineTo：画线到某点
             * - quadraticBezierTo：二次贝塞尔曲线
             * - cubicTo：三次贝塞尔曲线
             * - arcTo：圆弧
             * - close：闭合路径
             */
            
            // 示例 1：绘制三角形
            val trianglePath = Path().apply {
                moveTo(100f, 50f)  // 移动到顶点
                lineTo(50f, 150f)  // 画线到左下角
                lineTo(150f, 150f) // 画线到右下角
                close()            // 闭合路径（连接到起点）
            }
            
            drawPath(
                path = trianglePath,
                color = Color.Red,
                style = Stroke(width = 5f)  // Stroke：描边样式
            )
            
            // 示例 2：绘制波浪线
            val wavePath = Path().apply {
                moveTo(200f, 100f)
                // 二次贝塞尔曲线
                quadraticBezierTo(
                    x1 = 250f, y1 = 50f,   // 控制点
                    x2 = 300f, y2 = 100f   // 终点
                )
                quadraticBezierTo(
                    x1 = 350f, y1 = 150f,
                    x2 = 400f, y2 = 100f
                )
            }
            
            drawPath(
                path = wavePath,
                color = Color.Blue,
                style = Stroke(width = 5f)
            )
            
            // 示例 3：绘制心形
            val heartPath = Path().apply {
                val width = 80f
                val height = 80f
                val startX = size.width - 150f
                val startY = 80f
                
                moveTo(startX + width / 2, startY + height / 4)
                
                cubicTo(
                    startX + width / 2, startY,
                    startX, startY,
                    startX, startY + height / 4
                )
                
                cubicTo(
                    startX, startY + height / 2,
                    startX + width / 2, startY + height * 3 / 4,
                    startX + width / 2, startY + height
                )
                
                cubicTo(
                    startX + width / 2, startY + height * 3 / 4,
                    startX + width, startY + height / 2,
                    startX + width, startY + height / 4
                )
                
                cubicTo(
                    startX + width, startY,
                    startX + width / 2, startY,
                    startX + width / 2, startY + height / 4
                )
            }
            
            drawPath(
                path = heartPath,
                color = Color(0xFFFF6B9D)
            )
        }
    }
}

/**
 * GradientCanvas - 渐变效果示例
 */
@Composable
fun GradientCanvas() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.White)
        ) {
            /**
             * Brush - 画刷，用于创建渐变效果
             * 
             * 类型：
             * - linearGradient：线性渐变
             * - radialGradient：径向渐变（放射状）
             * - sweepGradient：扫描渐变（圆形扫描）
             */
            
            // 线性渐变
            val linearBrush = Brush.linearGradient(
                colors = listOf(Color.Blue, Color.Cyan, Color.Green),
                start = Offset(50f, 50f),
                end = Offset(200f, 50f)
            )
            
            drawRect(
                brush = linearBrush,
                topLeft = Offset(50f, 30f),
                size = Size(150f, 50f)
            )
            
            // 径向渐变（从中心向外）
            val radialBrush = Brush.radialGradient(
                colors = listOf(Color.Yellow, Color.Red),
                center = Offset(150f, 130f),
                radius = 50f
            )
            
            drawCircle(
                brush = radialBrush,
                radius = 50f,
                center = Offset(150f, 130f)
            )
            
            // 扫描渐变（彩虹圆环）
            val sweepBrush = Brush.sweepGradient(
                colors = listOf(
                    Color.Red, Color.Yellow, Color.Green,
                    Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                ),
                center = Offset(size.width - 150f, 130f)
            )
            
            drawCircle(
                brush = sweepBrush,
                radius = 50f,
                center = Offset(size.width - 150f, 130f)
            )
        }
    }
}

/**
 * TextDrawingCanvas - 文字绘制示例
 */
@Composable
fun TextDrawingCanvas() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0xFFF5F5F5))
        ) {
            /**
             * drawIntoCanvas - 访问原生 Canvas
             * 
             * 某些功能需要使用原生 Canvas API
             * 比如绘制文字
             */
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    textSize = 60f
                    color = android.graphics.Color.BLUE
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                
                canvas.nativeCanvas.drawText(
                    "Canvas 绘制文字",
                    size.width / 2,
                    100f,
                    paint
                )
            }
        }
    }
}

// ==================== 自定义测量示例 ====================

/**
 * CustomLayoutScreen - 自定义测量和布局示例
 */
@Composable
fun CustomLayoutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle("自定义 Layout")
        
        Text("1. 自定义列布局", style = MaterialTheme.typography.titleMedium)
        Text(
            "子元素垂直排列，间距自定义",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        CustomColumn(spacing = 8.dp) {
            Box(
                modifier = Modifier
                    .size(100.dp, 50.dp)
                    .background(Color.Red)
            )
            Box(
                modifier = Modifier
                    .size(150.dp, 50.dp)
                    .background(Color.Green)
            )
            Box(
                modifier = Modifier
                    .size(80.dp, 50.dp)
                    .background(Color.Blue)
            )
        }
        
        Divider()
        
        Text("2. 流式布局", style = MaterialTheme.typography.titleMedium)
        Text(
            "自动换行，类似网页的 flex-wrap",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        FlowLayout(spacing = 8.dp) {
            repeat(10) { index ->
                Box(
                    modifier = Modifier
                        .size((50 + index * 10).dp, 40.dp)
                        .background(
                            Color(
                                red = (index * 25) % 256,
                                green = (index * 50) % 256,
                                blue = (index * 75) % 256
                            )
                        )
                )
            }
        }
    }
}

/**
 * CustomColumn - 自定义列布局
 * 
 * 演示如何使用 Layout 创建自定义布局
 * 
 * @param spacing 子元素之间的间距
 * @param content 子元素内容
 */
@Composable
fun CustomColumn(
    spacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    /**
     * Layout - 自定义布局的核心组件
     * 
     * 参数：
     * - content：子元素
     * - modifier：修饰符
     * - measurePolicy：测量策略（定义如何测量和放置子元素）
     * 
     * MeasurePolicy 是一个函数，接收：
     * - measurables：可测量的子元素列表
     * - constraints：约束条件
     * 返回 MeasureResult
     */
    Layout(
        content = content,
        modifier = Modifier.fillMaxWidth()
    ) { measurables, constraints ->
        /**
         * measurables：List<Measurable>
         * - 所有子元素的可测量对象
         * 
         * constraints：Constraints
         * - 父容器给定的约束条件
         * - minWidth, maxWidth, minHeight, maxHeight
         */
        
        // 将 dp 转换为 px
        val spacingPx = spacing.toPx().toInt()
        
        /**
         * 第一步：测量所有子元素
         * 
         * measure() 方法：
         * - 接收约束条件
         * - 返回 Placeable 对象（可放置的）
         */
        val placeables = measurables.map { measurable ->
            // 测量每个子元素，使用父容器的宽度约束
            measurable.measure(constraints)
        }
        
        /**
         * 第二步：计算总高度
         * 
         * 高度 = 所有子元素高度之和 + 间距
         */
        val height = placeables.sumOf { it.height } + 
                     spacingPx * (placeables.size - 1).coerceAtLeast(0)
        
        /**
         * 第三步：布局（放置子元素）
         * 
         * layout() 方法：
         * - 指定这个布局的最终宽高
         * - 在 lambda 中放置子元素
         */
        layout(
            width = constraints.maxWidth,
            height = height
        ) {
            /**
             * 第四步：放置所有子元素
             * 
             * placeable.place() 方法：
             * - 指定子元素的位置（x, y）
             */
            var yPosition = 0
            
            placeables.forEach { placeable ->
                // 将子元素放置在 (0, yPosition) 位置
                placeable.place(x = 0, y = yPosition)
                
                // 更新下一个元素的 y 坐标
                yPosition += placeable.height + spacingPx
            }
        }
    }
}

/**
 * FlowLayout - 流式布局
 * 
 * 子元素从左到右排列，空间不足时自动换行
 */
@Composable
fun FlowLayout(
    spacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = Modifier.fillMaxWidth()
    ) { measurables, constraints ->
        val spacingPx = spacing.toPx().toInt()
        
        // 测量所有子元素
        val placeables = measurables.map { it.measure(constraints) }
        
        // 行信息：每行包含的 placeable 索引
        val rows = mutableListOf<List<Int>>()
        var currentRow = mutableListOf<Int>()
        var currentRowWidth = 0
        
        /**
         * 计算每行应该放哪些元素
         */
        placeables.forEachIndexed { index, placeable ->
            if (currentRowWidth + placeable.width > constraints.maxWidth) {
                // 当前行放不下了，开始新的一行
                if (currentRow.isNotEmpty()) {
                    rows.add(currentRow)
                    currentRow = mutableListOf()
                    currentRowWidth = 0
                }
            }
            
            currentRow.add(index)
            currentRowWidth += placeable.width + spacingPx
        }
        
        // 添加最后一行
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }
        
        // 计算总高度
        val rowHeights = rows.map { row ->
            row.maxOfOrNull { index -> placeables[index].height } ?: 0
        }
        val totalHeight = rowHeights.sum() + spacingPx * (rows.size - 1).coerceAtLeast(0)
        
        // 布局
        layout(
            width = constraints.maxWidth,
            height = totalHeight
        ) {
            var yPosition = 0
            
            rows.forEachIndexed { rowIndex, row ->
                var xPosition = 0
                val rowHeight = rowHeights[rowIndex]
                
                row.forEach { index ->
                    val placeable = placeables[index]
                    placeable.place(x = xPosition, y = yPosition)
                    xPosition += placeable.width + spacingPx
                }
                
                yPosition += rowHeight + spacingPx
            }
        }
    }
}

// ==================== 实用控件示例 ====================

/**
 * PracticalControlsScreen - 实用自定义控件示例
 */
@Composable
fun PracticalControlsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle("实用自定义控件")
        
        // 圆形进度条
        Text("1. 圆形进度条", style = MaterialTheme.typography.titleMedium)
        var progress1 by remember { mutableFloatStateOf(0.7f) }
        CircularProgressIndicator(
            progress = progress1,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Slider(
            value = progress1,
            onValueChange = { progress1 = it },
            modifier = Modifier.fillMaxWidth()
        )
        
        Divider()
        
        // 时钟
        Text("2. 模拟时钟", style = MaterialTheme.typography.titleMedium)
        AnimatedClock(modifier = Modifier.align(Alignment.CenterHorizontally))
        
        Divider()
        
        // 自定义图表
        Text("3. 简单柱状图", style = MaterialTheme.typography.titleMedium)
        SimpleBarChart(
            values = listOf(30f, 60f, 45f, 80f, 55f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * CircularProgressIndicator - 自定义圆形进度条
 * 
 * @param progress 进度值（0.0 - 1.0）
 */
@Composable
fun CircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(120.dp)
    ) {
        val strokeWidth = 12f
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        
        // 绘制背景圆环
        drawCircle(
            color = Color.LightGray,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        
        /**
         * drawArc - 绘制圆弧
         * 
         * 参数：
         * - color：颜色
         * - startAngle：起始角度（0度在3点钟方向）
         * - sweepAngle：扫过的角度
         * - useCenter：是否使用中心点（false 为圆弧，true 为扇形）
         * - topLeft：外接矩形的左上角
         * - size：外接矩形的大小
         * - style：样式（填充或描边）
         */
        drawArc(
            color = Color(0xFF2196F3),
            startAngle = -90f,  // 从12点钟方向开始
            sweepAngle = 360f * progress,  // 根据进度计算角度
            useCenter = false,
            topLeft = Offset(
                x = center.x - radius,
                y = center.y - radius
            ),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // 绘制进度文字
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                textSize = 40f
                color = android.graphics.Color.BLACK
                textAlign = android.graphics.Paint.Align.CENTER
            }
            
            canvas.nativeCanvas.drawText(
                "${(progress * 100).toInt()}%",
                center.x,
                center.y + 15f,
                paint
            )
        }
    }
}

/**
 * AnimatedClock - 动画时钟
 * 
 * 展示如何结合动画和 Canvas 绘制
 */
@Composable
fun AnimatedClock(modifier: Modifier = Modifier) {
    /**
     * rememberInfiniteTransition - 创建无限循环的动画
     */
    val infiniteTransition = rememberInfiniteTransition(label = "clock")
    
    /**
     * animateFloat - 创建浮点数动画
     * 
     * 参数：
     * - initialValue：初始值
     * - targetValue：目标值
     * - animationSpec：动画规格
     */
    val seconds by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "seconds"
    )
    
    Canvas(
        modifier = modifier.size(150.dp)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 2 - 20f
        
        // 绘制表盘背景
        drawCircle(
            color = Color.White,
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 4f)
        )
        
        // 绘制刻度
        for (i in 0 until 12) {
            val angle = (i * 30 - 90) * (Math.PI / 180).toFloat()
            val startX = centerX + (radius - 15f) * cos(angle)
            val startY = centerY + (radius - 15f) * sin(angle)
            val endX = centerX + radius * cos(angle)
            val endY = centerY + radius * sin(angle)
            
            drawLine(
                color = Color.Black,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3f
            )
        }
        
        /**
         * rotate - 旋转绘制作用域
         * 
         * 参数：
         * - degrees：旋转角度
         * - pivot：旋转中心点
         */
        
        // 绘制秒针
        rotate(seconds * 6f, pivot = Offset(centerX, centerY)) {
            drawLine(
                color = Color.Red,
                start = Offset(centerX, centerY),
                end = Offset(centerX, centerY - radius * 0.8f),
                strokeWidth = 2f
            )
        }
        
        // 绘制分针（假设从0分开始）
        rotate((seconds / 60f) * 360f, pivot = Offset(centerX, centerY)) {
            drawLine(
                color = Color.Black,
                start = Offset(centerX, centerY),
                end = Offset(centerX, centerY - radius * 0.6f),
                strokeWidth = 4f
            )
        }
        
        // 绘制中心点
        drawCircle(
            color = Color.Red,
            radius = 8f,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * SimpleBarChart - 简单柱状图
 * 
 * @param values 数据值列表
 */
@Composable
fun SimpleBarChart(
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .height(200.dp)
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        if (values.isEmpty()) return@Canvas
        
        val barWidth = size.width / values.size * 0.7f
        val spacing = size.width / values.size * 0.3f
        val maxValue = values.maxOrNull() ?: 100f
        
        values.forEachIndexed { index, value ->
            val barHeight = (value / maxValue) * size.height
            val x = index * (barWidth + spacing) + spacing / 2
            
            // 绘制柱子
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2196F3),
                        Color(0xFF64B5F6)
                    )
                ),
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight)
            )
            
            // 绘制数值
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    textSize = 30f
                    color = android.graphics.Color.BLACK
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                
                canvas.nativeCanvas.drawText(
                    value.toInt().toString(),
                    x + barWidth / 2,
                    size.height - barHeight - 10f,
                    paint
                )
            }
        }
    }
}

// ==================== 辅助组件 ====================

/**
 * SectionTitle - 区域标题组件（私有）
 * 
 * 私有函数，只在本文件内使用
 */
@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun CustomViewScreenPreview() {
    ComposeTheme {
        CustomViewScreen()
    }
}

