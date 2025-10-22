# Compose 自定义控件完整学习指南

> 掌握 Canvas 绘制和自定义测量，打造独特的 UI 控件

## 📚 目录

- [概述](#概述)
- [Canvas 绘制基础](#canvas-绘制基础)
- [自定义测量与布局](#自定义测量与布局)
- [实用控件示例](#实用控件示例)
- [性能优化](#性能优化)
- [常见问题](#常见问题)

---

## 概述

### 为什么需要自定义控件？

虽然 Compose 提供了丰富的组件，但有时我们需要：
- 🎨 创建独特的视觉效果
- 📊 绘制自定义图表
- ⚡ 优化性能（避免组件嵌套）
- 🎮 实现复杂的交互效果

### 两大核心技术

| 技术 | 用途 | 难度 |
|------|------|------|
| **Canvas 绘制** | 自定义图形、动画、图表 | ⭐⭐⭐ |
| **Layout 测量** | 自定义布局逻辑、尺寸计算 | ⭐⭐⭐⭐ |

---

## Canvas 绘制基础

### Canvas 是什么？

Canvas 就像一块画布，你可以在上面绘制任何图形。

### 基本用法

```kotlin
@Composable
fun MyCanvas() {
    Canvas(
        modifier = Modifier.size(200.dp)
    ) {
        // 在这里绘制
        // size: Canvas 的尺寸
        // center: Canvas 的中心点
    }
}
```

**代码位置：** `CustomViewActivity.kt` 第 144-160 行

---

### 绘制基础图形

#### 1. 绘制直线

```kotlin
drawLine(
    color = Color.Red,
    start = Offset(x = 50f, y = 50f),    // 起点
    end = Offset(x = 200f, y = 50f),     // 终点
    strokeWidth = 5f                      // 线宽
)
```

**Offset：** 坐标点，(0, 0) 在左上角

#### 2. 绘制圆形

```kotlin
drawCircle(
    color = Color.Blue,
    radius = 40f,                         // 半径
    center = Offset(x = 100f, y = 100f)  // 圆心
)
```

#### 3. 绘制矩形

```kotlin
drawRect(
    color = Color.Green,
    topLeft = Offset(x = 50f, y = 50f),  // 左上角
    size = Size(width = 100f, height = 80f)
)
```

#### 4. 绘制圆角矩形

```kotlin
drawRoundRect(
    color = Color.Magenta,
    topLeft = Offset(x = 50f, y = 50f),
    size = Size(100f, 80f),
    cornerRadius = CornerRadius(20f, 20f)  // 圆角半径
)
```

**代码位置：** `CustomViewActivity.kt` 第 144-224 行

---

### Path（路径）绘制

Path 可以创建复杂的自定义形状。

#### Path 常用方法

```kotlin
val path = Path().apply {
    moveTo(x, y)           // 移动到某点（不绘制）
    lineTo(x, y)           // 画直线到某点
    quadraticBezierTo(     // 二次贝塞尔曲线
        x1, y1,            // 控制点
        x2, y2             // 终点
    )
    cubicTo(               // 三次贝塞尔曲线
        x1, y1, x2, y2,    // 两个控制点
        x3, y3             // 终点
    )
    close()                // 闭合路径
}

// 绘制路径
drawPath(
    path = path,
    color = Color.Red,
    style = Stroke(width = 5f)  // 描边样式
)
```

#### 示例：绘制三角形

```kotlin
val trianglePath = Path().apply {
    moveTo(100f, 50f)   // 顶点
    lineTo(50f, 150f)   // 左下角
    lineTo(150f, 150f)  // 右下角
    close()             // 闭合（回到起点）
}

drawPath(
    path = trianglePath,
    color = Color.Red,
    style = Stroke(width = 5f)
)
```

**代码位置：** `CustomViewActivity.kt` 第 232-324 行

---

### 渐变效果

Brush（画刷）可以创建各种渐变效果。

#### 1. 线性渐变

```kotlin
val linearBrush = Brush.linearGradient(
    colors = listOf(Color.Blue, Color.Cyan, Color.Green),
    start = Offset(0f, 0f),
    end = Offset(200f, 0f)
)

drawRect(brush = linearBrush, /* ... */)
```

#### 2. 径向渐变（放射状）

```kotlin
val radialBrush = Brush.radialGradient(
    colors = listOf(Color.Yellow, Color.Red),
    center = Offset(100f, 100f),
    radius = 50f
)

drawCircle(brush = radialBrush, /* ... */)
```

#### 3. 扫描渐变（圆形扫描）

```kotlin
val sweepBrush = Brush.sweepGradient(
    colors = listOf(
        Color.Red, Color.Yellow, Color.Green,
        Color.Cyan, Color.Blue, Color.Magenta
    ),
    center = Offset(100f, 100f)
)

drawCircle(brush = sweepBrush, /* ... */)
```

**代码位置：** `CustomViewActivity.kt` 第 332-391 行

---

### 绘制文字

```kotlin
drawIntoCanvas { canvas ->
    val paint = android.graphics.Paint().apply {
        textSize = 60f
        color = android.graphics.Color.BLUE
        textAlign = android.graphics.Paint.Align.CENTER
    }
    
    canvas.nativeCanvas.drawText(
        "Hello Canvas",
        size.width / 2,  // x 坐标
        100f,            // y 坐标
        paint
    )
}
```

**注意：** 绘制文字需要使用原生 Canvas API。

**代码位置：** `CustomViewActivity.kt` 第 399-422 行

---

## 自定义测量与布局

### Layout 组件

Layout 是创建自定义布局的核心组件。

### 基本结构

```kotlin
@Composable
fun CustomLayout(content: @Composable () -> Unit) {
    Layout(
        content = content,
        modifier = Modifier.fillMaxWidth()
    ) { measurables, constraints ->
        // 1. 测量所有子元素
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }
        
        // 2. 计算布局的总大小
        val width = /* 计算宽度 */
        val height = /* 计算高度 */
        
        // 3. 布局（放置子元素）
        layout(width, height) {
            // 4. 放置每个子元素
            placeables.forEach { placeable ->
                placeable.place(x = 0, y = 0)
            }
        }
    }
}
```

---

### 核心概念

#### 1. Constraints（约束）

```kotlin
constraints.minWidth   // 最小宽度
constraints.maxWidth   // 最大宽度
constraints.minHeight  // 最小高度
constraints.maxHeight  // 最大高度
```

#### 2. Measurable（可测量的）

```kotlin
measurable.measure(constraints)  // 测量，返回 Placeable
```

#### 3. Placeable（可放置的）

```kotlin
placeable.width   // 测量后的宽度
placeable.height  // 测量后的高度
placeable.place(x, y)  // 放置到指定位置
```

---

### 示例 1：自定义列布局

```kotlin
@Composable
fun CustomColumn(
    spacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = Modifier.fillMaxWidth()
    ) { measurables, constraints ->
        val spacingPx = spacing.toPx().toInt()
        
        // 测量所有子元素
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }
        
        // 计算总高度 = 所有子元素高度之和 + 间距
        val height = placeables.sumOf { it.height } + 
                     spacingPx * (placeables.size - 1).coerceAtLeast(0)
        
        // 布局
        layout(
            width = constraints.maxWidth,
            height = height
        ) {
            var yPosition = 0
            
            placeables.forEach { placeable ->
                placeable.place(x = 0, y = yPosition)
                yPosition += placeable.height + spacingPx
            }
        }
    }
}
```

**使用：**

```kotlin
CustomColumn(spacing = 8.dp) {
    Box(Modifier.size(100.dp, 50.dp).background(Color.Red))
    Box(Modifier.size(150.dp, 50.dp).background(Color.Green))
    Box(Modifier.size(80.dp, 50.dp).background(Color.Blue))
}
```

**代码位置：** `CustomViewActivity.kt` 第 496-578 行

---

### 示例 2：流式布局

流式布局会自动换行，类似网页的 flex-wrap。

```kotlin
@Composable
fun FlowLayout(
    spacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = Modifier.fillMaxWidth()
    ) { measurables, constraints ->
        val spacingPx = spacing.toPx().toInt()
        
        // 测量所有子元素
        val placeables = measurables.map { it.measure(constraints) }
        
        // 计算每行应该放哪些元素
        val rows = mutableListOf<List<Int>>()
        var currentRow = mutableListOf<Int>()
        var currentRowWidth = 0
        
        placeables.forEachIndexed { index, placeable ->
            if (currentRowWidth + placeable.width > constraints.maxWidth) {
                // 当前行放不下，开始新的一行
                if (currentRow.isNotEmpty()) {
                    rows.add(currentRow)
                    currentRow = mutableListOf()
                    currentRowWidth = 0
                }
            }
            
            currentRow.add(index)
            currentRowWidth += placeable.width + spacingPx
        }
        
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }
        
        // 计算总高度
        val rowHeights = rows.map { row ->
            row.maxOfOrNull { index -> placeables[index].height } ?: 0
        }
        val totalHeight = rowHeights.sum() + 
                         spacingPx * (rows.size - 1).coerceAtLeast(0)
        
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
```

**代码位置：** `CustomViewActivity.kt` 第 587-651 行

---

## 实用控件示例

### 1. 圆形进度条

```kotlin
@Composable
fun CircularProgressIndicator(
    progress: Float,  // 0.0 - 1.0
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(120.dp)) {
        val strokeWidth = 12f
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        
        // 背景圆环
        drawCircle(
            color = Color.LightGray,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        
        // 进度圆弧
        drawArc(
            color = Color(0xFF2196F3),
            startAngle = -90f,  // 从12点钟方向开始
            sweepAngle = 360f * progress,
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
```

**使用：**

```kotlin
var progress by remember { mutableFloatStateOf(0.7f) }

CircularProgressIndicator(progress = progress)

Slider(
    value = progress,
    onValueChange = { progress = it }
)
```

**代码位置：** `CustomViewActivity.kt` 第 708-772 行

---

### 2. 动画时钟

```kotlin
@Composable
fun AnimatedClock(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val seconds by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Canvas(modifier = modifier.size(150.dp)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 2 - 20f
        
        // 表盘背景
        drawCircle(
            color = Color.White,
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 4f)
        )
        
        // 绘制12个刻度
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
        
        // 秒针（旋转绘制）
        rotate(seconds * 6f, pivot = Offset(centerX, centerY)) {
            drawLine(
                color = Color.Red,
                start = Offset(centerX, centerY),
                end = Offset(centerX, centerY - radius * 0.8f),
                strokeWidth = 2f
            )
        }
        
        // 中心点
        drawCircle(
            color = Color.Red,
            radius = 8f,
            center = Offset(centerX, centerY)
        )
    }
}
```

**代码位置：** `CustomViewActivity.kt` 第 780-864 行

---

### 3. 简单柱状图

```kotlin
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
            
            // 绘制柱子（带渐变）
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
```

**使用：**

```kotlin
SimpleBarChart(
    values = listOf(30f, 60f, 45f, 80f, 55f)
)
```

**代码位置：** `CustomViewActivity.kt` 第 875-929 行

---

## 性能优化

### 1. 避免在 onDraw 中创建对象

❌ **不好：**
```kotlin
Canvas(modifier) {
    val paint = Paint()  // 每次重绘都创建新对象
    // ...
}
```

✅ **好：**
```kotlin
val paint = remember { Paint() }  // 只创建一次

Canvas(modifier) {
    // 使用 paint
}
```

---

### 2. 使用 remember 缓存计算结果

```kotlin
@Composable
fun MyCanvas(data: List<Float>) {
    // 只在 data 改变时重新计算
    val processedData = remember(data) {
        data.map { /* 复杂计算 */ }
    }
    
    Canvas(modifier) {
        // 使用 processedData
    }
}
```

---

### 3. 限制重组范围

```kotlin
@Composable
fun AnimatedView() {
    var value by remember { mutableStateOf(0f) }
    
    // 只有 Canvas 会重组，外层 Column 不会
    Column {
        Text("标题")  // 不受 value 影响
        
        Canvas(modifier) {
            // 使用 value 绘制
        }
    }
}
```

---

## 常见问题

### Q1: Canvas 坐标系是怎样的？

**答：** 
- 原点 (0, 0) 在**左上角**
- X 轴向右递增
- Y 轴向下递增

```
(0,0) -----> X
  |
  |
  v
  Y
```

---

### Q2: 如何让 Canvas 响应点击？

使用 `Modifier.pointerInput`：

```kotlin
Canvas(
    modifier = Modifier
        .size(200.dp)
        .pointerInput(Unit) {
            detectTapGestures { offset ->
                // offset.x, offset.y 是点击位置
                println("点击位置: ${offset.x}, ${offset.y}")
            }
        }
) {
    // 绘制
}
```

---

### Q3: drawArc 的角度是怎么计算的？

- `startAngle`: 起始角度
  - 0° 在 3 点钟方向（右侧）
  - -90° 在 12 点钟方向（顶部）
  - 90° 在 6 点钟方向（底部）
  - 180° 在 9 点钟方向（左侧）

- `sweepAngle`: 扫过的角度
  - 正值：顺时针
  - 负值：逆时针

```kotlin
drawArc(
    color = Color.Blue,
    startAngle = -90f,   // 从顶部开始
    sweepAngle = 90f,    // 顺时针画 90°（1/4 圆）
    useCenter = false,   // false=圆弧, true=扇形
    // ...
)
```

---

### Q4: 如何实现动画？

使用 `rememberInfiniteTransition` 或 `animateFloatAsState`：

```kotlin
@Composable
fun AnimatedCanvas() {
    // 无限循环动画
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Canvas(modifier) {
        rotate(rotation) {
            // 绘制会旋转的内容
        }
    }
}
```

---

### Q5: Layout 的测量顺序是什么？

1. **父容器传递约束给子元素**
   ```kotlin
   measurable.measure(constraints)
   ```

2. **子元素返回测量结果（Placeable）**
   ```kotlin
   val placeable = measurable.measure(constraints)
   placeable.width  // 测量后的宽度
   placeable.height // 测量后的高度
   ```

3. **父容器决定自己的大小**
   ```kotlin
   layout(width = ..., height = ...) { }
   ```

4. **父容器放置子元素**
   ```kotlin
   placeable.place(x = ..., y = ...)
   ```

---

## 学习路径建议

### 🌱 初级（Canvas 基础）

1. **绘制基础图形**
   - 线、圆、矩形
   - 练习：画一个笑脸

2. **使用 Path**
   - 三角形、五角星
   - 练习：画一个心形

3. **渐变效果**
   - 线性、径向、扫描渐变
   - 练习：彩虹圆环

### 🌿 中级（自定义布局）

1. **理解测量流程**
   - Constraints、Measurable、Placeable
   - 练习：自定义 Row

2. **实现流式布局**
   - 自动换行逻辑
   - 练习：标签云

3. **组合使用**
   - Layout + Canvas
   - 练习：带刻度的进度条

### 🌳 高级（实用控件）

1. **动画控件**
   - 结合 Animation API
   - 练习：加载动画

2. **图表组件**
   - 柱状图、折线图、饼图
   - 练习：股票走势图

3. **复杂交互**
   - 手势处理
   - 练习：可拖动的组件

---

## 代码索引

### Canvas 绘制

| 功能 | 代码位置 | 行数 |
|------|----------|------|
| 基础图形 | CustomViewActivity.kt | 144-224 |
| Path 绘制 | CustomViewActivity.kt | 232-324 |
| 渐变效果 | CustomViewActivity.kt | 332-391 |
| 文字绘制 | CustomViewActivity.kt | 399-422 |

### 自定义布局

| 功能 | 代码位置 | 行数 |
|------|----------|------|
| 自定义列布局 | CustomViewActivity.kt | 496-578 |
| 流式布局 | CustomViewActivity.kt | 587-651 |

### 实用控件

| 功能 | 代码位置 | 行数 |
|------|----------|------|
| 圆形进度条 | CustomViewActivity.kt | 708-772 |
| 动画时钟 | CustomViewActivity.kt | 780-864 |
| 柱状图 | CustomViewActivity.kt | 875-929 |

---

## 参考资源

### 官方文档
- [Canvas and drawing modifiers](https://developer.android.com/jetpack/compose/graphics/draw/overview)
- [Custom layouts](https://developer.android.com/jetpack/compose/layouts/custom)

### 推荐阅读
- [Jetpack Compose 自定义绘图](https://developer.android.com/jetpack/compose/graphics)
- [Understanding Compose Layout](https://developer.android.com/jetpack/compose/layouts/basics)

---

## 总结

### 你学到了

✅ Canvas 的各种绘制 API  
✅ Path 创建复杂形状  
✅ 渐变效果的使用  
✅ Layout 的测量和布局机制  
✅ 如何创建自定义布局  
✅ 实用控件的实现思路  

### 下一步

🎯 练习绘制更复杂的图形  
🎯 实现自己的图表库  
🎯 学习手势处理  
🎯 优化绘制性能  
🎯 创建可重用的自定义组件库  

---

**Happy Drawing! 🎨**


