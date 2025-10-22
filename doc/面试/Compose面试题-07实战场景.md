# Compose UI 面试题 - 第7部分：实战场景题

## 37. 如何实现下拉刷新功能？

**答案：**

Compose 提供了 Material 3 的 PullRefresh 组件实现下拉刷新。

**完整实现：**

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshScreen(viewModel: RefreshViewModel = viewModel()) {
    val items by viewModel.items.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    val pullRefreshState = rememberPullToRefreshState()
    
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(Unit) {
            viewModel.refresh()
        }
    }
    
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            pullRefreshState.endRefresh()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(pullRefreshState.nestedScrollConnection)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(items, key = { it.id }) { item ->
                ItemCard(item)
            }
        }
        
        if (pullRefreshState.isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }
    }
}

// ViewModel
class RefreshViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    init {
        loadData()
    }
    
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(2000) // 模拟网络请求
            
            try {
                val newData = repository.fetchItems()
                _items.value = newData
            } catch (e: Exception) {
                // 处理错误
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _items.value = repository.getItems()
        }
    }
}
```

**Accompanist 库实现（旧版本）：**

```kotlin
@Composable
fun SwipeRefreshScreen() {
    var isRefreshing by remember { mutableStateOf(false) }
    var items by remember { mutableStateOf(listOf<Item>()) }
    
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = {
            isRefreshing = true
            // 执行刷新
            loadData { newItems ->
                items = newItems
                isRefreshing = false
            }
        }
    ) {
        LazyColumn {
            items(items) { ItemCard(it) }
        }
    }
}
```

---

## 38. 如何实现搜索框带防抖功能？

**答案：**

使用 Flow 的 debounce 操作符实现防抖搜索。

**完整实现：**

```kotlin
@Composable
fun SearchScreen(viewModel: SearchViewModel = viewModel()) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 搜索框
        SearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.onSearchQueryChange(it) },
            onClear = { viewModel.onSearchQueryChange("") }
        )
        
        // 搜索结果
        when {
            isSearching -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            searchResults.isEmpty() && searchQuery.isNotEmpty() -> {
                EmptyState("没有找到结果")
            }
            
            else -> {
                LazyColumn {
                    items(searchResults, key = { it.id }) { result ->
                        SearchResultItem(result)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("搜索...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = "清除")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        )
    )
}

// ViewModel
class SearchViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    init {
        setupSearchFlow()
    }
    
    private fun setupSearchFlow() {
        viewModelScope.launch {
            searchQuery
                .debounce(500) // 500ms 防抖
                .filter { it.isNotEmpty() }
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query)
                }
        }
    }
    
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
        }
    }
    
    private suspend fun performSearch(query: String) {
        _isSearching.value = true
        
        try {
            val results = searchRepository.search(query)
            _searchResults.value = results
        } catch (e: Exception) {
            // 处理错误
            _searchResults.value = emptyList()
        } finally {
            _isSearching.value = false
        }
    }
}
```

**带历史记录的搜索：**

```kotlin
class SearchViewModel : ViewModel() {
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()
    
    init {
        loadSearchHistory()
    }
    
    fun addToHistory(query: String) {
        viewModelScope.launch {
            val newHistory = (_searchHistory.value + query)
                .distinct()
                .takeLast(10) // 只保留最近10条
            _searchHistory.value = newHistory
            saveSearchHistory(newHistory)
        }
    }
    
    fun clearHistory() {
        _searchHistory.value = emptyList()
        clearSavedHistory()
    }
}

@Composable
fun SearchWithHistory(viewModel: SearchViewModel) {
    val history by viewModel.searchHistory.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    
    Column {
        SearchBar(...)
        
        if (query.isEmpty() && history.isNotEmpty()) {
            Text("搜索历史", style = MaterialTheme.typography.titleMedium)
            
            history.forEach { historyItem ->
                TextButton(onClick = {
                    viewModel.onSearchQueryChange(historyItem)
                }) {
                    Text(historyItem)
                }
            }
        }
    }
}
```

---

## 39. 如何实现图片预览（点击放大）功能？

**答案：**

使用对话框和手势实现图片预览。

**完整实现：**

```kotlin
@Composable
fun ImagePreviewExample() {
    var selectedImage by remember { mutableStateOf<String?>(null) }
    
    // 图片列表
    LazyVerticalGrid(columns = GridCells.Fixed(3)) {
        items(imageUrls) { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { selectedImage = imageUrl },
                contentScale = ContentScale.Crop
            )
        }
    }
    
    // 预览对话框
    selectedImage?.let { imageUrl ->
        ImagePreviewDialog(
            imageUrl = imageUrl,
            onDismiss = { selectedImage = null }
        )
    }
}

@Composable
fun ImagePreviewDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() }
        ) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offsetX by remember { mutableFloatStateOf(0f) }
            var offsetY by remember { mutableFloatStateOf(0f) }
            
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    },
                contentScale = ContentScale.Fit
            )
            
            // 关闭按钮
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = Color.White
                )
            }
        }
    }
}
```

**使用 Pager 实现图片画廊：**

```kotlin
@Composable
fun ImageGallery(
    images: List<String>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { images.size }
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                ZoomableImage(imageUrl = images[page])
            }
            
            // 页码指示器
            Text(
                text = "${pagerState.currentPage + 1} / ${images.size}",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
            
            // 关闭按钮
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun ZoomableImage(imageUrl: String) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 3f)
                    offset = if (scale > 1f) {
                        offset + pan
                    } else {
                        Offset.Zero
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale > 1f) 1f else 2f
                        offset = Offset.Zero
                    }
                )
            },
        contentScale = ContentScale.Fit
    )
}
```

---

## 40. 如何实现无限滚动加载更多？

**答案：**

监听滚动位置，接近底部时触发加载。

**完整实现：**

```kotlin
@Composable
fun InfiniteScrollList(viewModel: ListViewModel = viewModel()) {
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val listState = rememberLazyListState()
    
    LaunchedEffect(listState) {
        snapshotFlow { 
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index 
        }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null &&
                    lastVisibleIndex >= items.size - 3 && // 距底部还有3项时触发
                    !isLoading &&
                    hasMore
                ) {
                    viewModel.loadMore()
                }
            }
    }
    
    LazyColumn(state = listState) {
        items(items, key = { it.id }) { item ->
            ItemCard(item)
        }
        
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        if (!hasMore && items.isNotEmpty()) {
            item {
                Text(
                    "没有更多了",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }
    }
}

// ViewModel
class ListViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()
    
    private var currentPage = 0
    private val pageSize = 20
    
    init {
        loadMore()
    }
    
    fun loadMore() {
        if (_isLoading.value || !_hasMore.value) return
        
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val newItems = repository.loadItems(
                    page = currentPage,
                    pageSize = pageSize
                )
                
                if (newItems.isEmpty()) {
                    _hasMore.value = false
                } else {
                    _items.value = _items.value + newItems
                    currentPage++
                }
            } catch (e: Exception) {
                // 处理错误
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refresh() {
        currentPage = 0
        _items.value = emptyList()
        _hasMore.value = true
        loadMore()
    }
}
```

**结合下拉刷新：**

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshAndLoadMoreList(viewModel: ListViewModel = viewModel()) {
    val items by viewModel.items.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val listState = rememberLazyListState()
    
    val pullRefreshState = rememberPullToRefreshState()
    
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(Unit) {
            viewModel.refresh()
        }
    }
    
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            pullRefreshState.endRefresh()
        }
    }
    
    // 监听滚动加载更多
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= items.size - 3) {
                    viewModel.loadMore()
                }
            }
    }
    
    Box(Modifier.nestedScroll(pullRefreshState.nestedScrollConnection)) {
        LazyColumn(state = listState) {
            items(items, key = { it.id }) {
                ItemCard(it)
            }
            
            if (isLoadingMore) {
                item {
                    LoadingIndicator()
                }
            }
        }
        
        if (pullRefreshState.isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

---

## 41. 如何实现动态主题切换（深色/浅色模式）？

**答案：**

通过状态管理和 MaterialTheme 实现主题切换。

**完整实现：**

```kotlin
// 1. 定义主题偏好
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

// 2. ViewModel 管理主题状态
class ThemeViewModel : ViewModel() {
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        // 保存到 DataStore
        saveThemePreference(mode)
    }
}

// 3. 自定义主题
@Composable
fun MyAppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = Color(0xFF BB86FC),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6),
            background = Color.White,
            surface = Color(0xFFF5F5F5)
        )
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// 4. 主应用
@Composable
fun MyApp(viewModel: ThemeViewModel = viewModel()) {
    val themeMode by viewModel.themeMode.collectAsState()
    
    MyAppTheme(themeMode = themeMode) {
        MainScreen()
    }
}

// 5. 设置页面
@Composable
fun ThemeSettings(viewModel: ThemeViewModel = viewModel()) {
    val currentTheme by viewModel.themeMode.collectAsState()
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text("主题设置", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(Modifier.height(16.dp))
        
        ThemeOption(
            title = "跟随系统",
            selected = currentTheme == ThemeMode.SYSTEM,
            onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
        )
        
        ThemeOption(
            title = "浅色模式",
            selected = currentTheme == ThemeMode.LIGHT,
            onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
        )
        
        ThemeOption(
            title = "深色模式",
            selected = currentTheme == ThemeMode.DARK,
            onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
        )
    }
}

@Composable
fun ThemeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title)
            RadioButton(
                selected = selected,
                onClick = onClick
            )
        }
    }
}
```

**使用 DataStore 持久化：**

```kotlin
class ThemePreferenceManager(context: Context) {
    private val dataStore = context.createDataStore(name = "theme_preferences")
    
    companion object {
        private val THEME_MODE_KEY = intPreferencesKey("theme_mode")
    }
    
    val themeMode: Flow<ThemeMode> = dataStore.data
        .map { preferences ->
            when (preferences[THEME_MODE_KEY]) {
                0 -> ThemeMode.LIGHT
                1 -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }
    
    suspend fun saveThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = when (mode) {
                ThemeMode.LIGHT -> 0
                ThemeMode.DARK -> 1
                ThemeMode.SYSTEM -> 2
            }
        }
    }
}

// 在 ViewModel 中使用
class ThemeViewModel(
    private val preferenceManager: ThemePreferenceManager
) : ViewModel() {
    
    val themeMode = preferenceManager.themeMode
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ThemeMode.SYSTEM
        )
    
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferenceManager.saveThemeMode(mode)
        }
    }
}
```

**动画过渡：**

```kotlin
@Composable
fun AnimatedTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    val animatedColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF121212) else Color.White,
        animationSpec = tween(durationMillis = 300)
    )
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = animatedColor
    ) {
        MyAppTheme(themeMode = themeMode, content = content)
    }
}
```

---

## 42. 如何实现表单验证？

**答案：**

创建表单状态管理和验证逻辑。

**完整实现：**

```kotlin
// 1. 表单字段状态
data class FormFieldState(
    val value: String = "",
    val error: String? = null,
    val isValid: Boolean = true
)

// 2. 表单数据类
data class RegisterFormState(
    val email: FormFieldState = FormFieldState(),
    val password: FormFieldState = FormFieldState(),
    val confirmPassword: FormFieldState = FormFieldState(),
    val username: FormFieldState = FormFieldState()
) {
    val isValid: Boolean
        get() = email.isValid && 
                password.isValid && 
                confirmPassword.isValid && 
                username.isValid &&
                email.value.isNotEmpty() &&
                password.value.isNotEmpty()
}

// 3. ViewModel
class RegisterViewModel : ViewModel() {
    private val _formState = MutableStateFlow(RegisterFormState())
    val formState: StateFlow<RegisterFormState> = _formState.asStateFlow()
    
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()
    
    fun onEmailChange(email: String) {
        val error = validateEmail(email)
        _formState.update { 
            it.copy(
                email = FormFieldState(
                    value = email,
                    error = error,
                    isValid = error == null
                )
            )
        }
    }
    
    fun onPasswordChange(password: String) {
        val error = validatePassword(password)
        _formState.update { 
            it.copy(
                password = FormFieldState(
                    value = password,
                    error = error,
                    isValid = error == null
                )
            )
        }
        // 同时验证确认密码
        if (_formState.value.confirmPassword.value.isNotEmpty()) {
            onConfirmPasswordChange(_formState.value.confirmPassword.value)
        }
    }
    
    fun onConfirmPasswordChange(confirmPassword: String) {
        val error = if (confirmPassword != _formState.value.password.value) {
            "两次密码不一致"
        } else {
            null
        }
        _formState.update { 
            it.copy(
                confirmPassword = FormFieldState(
                    value = confirmPassword,
                    error = error,
                    isValid = error == null
                )
            )
        }
    }
    
    fun onUsernameChange(username: String) {
        val error = validateUsername(username)
        _formState.update { 
            it.copy(
                username = FormFieldState(
                    value = username,
                    error = error,
                    isValid = error == null
                )
            )
        }
    }
    
    fun submit() {
        if (!_formState.value.isValid) return
        
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val form = _formState.value
                authRepository.register(
                    email = form.email.value,
                    password = form.password.value,
                    username = form.username.value
                )
                // 注册成功
            } catch (e: Exception) {
                // 处理错误
            } finally {
                _isSubmitting.value = false
            }
        }
    }
    
    private fun validateEmail(email: String): String? {
        return when {
            email.isEmpty() -> "邮箱不能为空"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                "邮箱格式不正确"
            else -> null
        }
    }
    
    private fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "密码不能为空"
            password.length < 6 -> "密码至少6位"
            !password.any { it.isDigit() } -> "密码必须包含数字"
            !password.any { it.isLetter() } -> "密码必须包含字母"
            else -> null
        }
    }
    
    private fun validateUsername(username: String): String? {
        return when {
            username.isEmpty() -> "用户名不能为空"
            username.length < 3 -> "用户名至少3位"
            username.length > 20 -> "用户名最多20位"
            !username.all { it.isLetterOrDigit() || it == '_' } -> 
                "用户名只能包含字母、数字和下划线"
            else -> null
        }
    }
}

// 4. UI 组件
@Composable
fun RegisterScreen(viewModel: RegisterViewModel = viewModel()) {
    val formState by viewModel.formState.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "注册账号",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // 用户名
        FormTextField(
            label = "用户名",
            value = formState.username.value,
            error = formState.username.error,
            onValueChange = { viewModel.onUsernameChange(it) },
            leadingIcon = Icons.Default.Person
        )
        
        // 邮箱
        FormTextField(
            label = "邮箱",
            value = formState.email.value,
            error = formState.email.error,
            onValueChange = { viewModel.onEmailChange(it) },
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )
        
        // 密码
        PasswordTextField(
            label = "密码",
            value = formState.password.value,
            error = formState.password.error,
            onValueChange = { viewModel.onPasswordChange(it) }
        )
        
        // 确认密码
        PasswordTextField(
            label = "确认密码",
            value = formState.confirmPassword.value,
            error = formState.confirmPassword.error,
            onValueChange = { viewModel.onConfirmPasswordChange(it) }
        )
        
        Spacer(Modifier.height(24.dp))
        
        // 提交按钮
        Button(
            onClick = { viewModel.submit() },
            modifier = Modifier.fillMaxWidth(),
            enabled = formState.isValid && !isSubmitting
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("注册")
            }
        }
    }
}

@Composable
fun FormTextField(
    label: String,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(leadingIcon, contentDescription = null)
        },
        isError = error != null,
        supportingText = {
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        )
    )
}

@Composable
fun PasswordTextField(
    label: String,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null)
        },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    if (passwordVisible) Icons.Default.VisibilityOff 
                    else Icons.Default.Visibility,
                    contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                )
            }
        },
        visualTransformation = if (passwordVisible) 
            VisualTransformation.None 
        else 
            PasswordVisualTransformation(),
        isError = error != null,
        supportingText = {
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        )
    )
}
```

---

**第7部分完成！共 6 题**

**🎉 全部 7 个部分、42 道题目已完成！**

