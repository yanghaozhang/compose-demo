package com.test.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.test.compose.ui.theme.ComposeTheme

class TextButtonActivity : ComponentActivity() {
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
                            title = { Text("Text & Button 测试") },
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
                    TextButtonScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun TextButtonScreen(modifier: Modifier = Modifier) {
    var clickCount by remember { mutableIntStateOf(0) }
    var selectedButton by remember { mutableStateOf("None") }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Text 示例区域
        SectionTitle("Text 组件示例")
        
        // 基础文本
        Text(
            text = "这是基础文本",
            style = MaterialTheme.typography.bodyLarge
        )
        
        // 不同样式的文本
        Text(
            text = "大标题文本",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "中等标题文本",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "斜体文本示例",
            fontStyle = FontStyle.Italic,
            fontSize = 18.sp
        )
        
        Text(
            text = "居中对齐的文本",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Text(
            text = "这是一段很长的文本，用来演示文本换行的效果。在Compose中，Text组件会自动处理文本换行，当文本超出可用宽度时会自动换到下一行。",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        // Button 示例区域
        SectionTitle("Button 组件示例")
        
        // 显示点击次数
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "按钮点击次数: $clickCount",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "最后选择: $selectedButton",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        
        // 标准 Button
        Button(
            onClick = {
                clickCount++
                selectedButton = "标准按钮"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("标准按钮 (Filled)")
        }
        
        // FilledTonalButton
        FilledTonalButton(
            onClick = {
                clickCount++
                selectedButton = "填充色调按钮"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("填充色调按钮 (Filled Tonal)")
        }
        
        // OutlinedButton
        OutlinedButton(
            onClick = {
                clickCount++
                selectedButton = "轮廓按钮"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("轮廓按钮 (Outlined)")
        }
        
        // ElevatedButton
        ElevatedButton(
            onClick = {
                clickCount++
                selectedButton = "提升按钮"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("提升按钮 (Elevated)")
        }
        
        // TextButton
        TextButton(
            onClick = {
                clickCount++
                selectedButton = "文本按钮"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("文本按钮 (Text)")
        }
        
        // 带图标的按钮
        Button(
            onClick = {
                clickCount++
                selectedButton = "带图标按钮"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("带图标的按钮")
        }
        
        // 自定义颜色的按钮
        Button(
            onClick = {
                clickCount++
                selectedButton = "自定义颜色按钮"
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE),
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("自定义颜色按钮")
        }
        
        // 禁用状态的按钮
        Button(
            onClick = { },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("禁用按钮")
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        // IconButton 示例
        SectionTitle("IconButton 示例")
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = {
                    clickCount++
                    selectedButton = "Home图标"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "主页",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            FilledIconButton(
                onClick = {
                    clickCount++
                    selectedButton = "设置图标"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置"
                )
            }
            
            OutlinedIconButton(
                onClick = {
                    clickCount++
                    selectedButton = "收藏图标"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "收藏"
                )
            }
        }
        
        // 重置按钮
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                clickCount = 0
                selectedButton = "None"
            },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        ) {
            Text(
                text = "重置计数器",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
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
fun TextButtonScreenPreview() {
    ComposeTheme {
        TextButtonScreen()
    }
}

