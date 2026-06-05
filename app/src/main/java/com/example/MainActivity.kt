package com.example


import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.model.FoodItem
import com.example.model.MenuData
import com.example.model.PoutineCategory
import com.example.ui.components.PotatoCharacterAvatar
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.CanadianRed
import com.example.ui.theme.GoldenPotato
import com.example.ui.theme.PotatoBeige
import com.example.ui.theme.PrimaryBlack
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.White
import com.example.viewmodel.AdminProduct
import com.example.viewmodel.AdminMetrics
import com.example.viewmodel.AdminOrder
import com.example.viewmodel.Coupon
import com.example.viewmodel.MetricPoint
import com.example.viewmodel.AuthState
import com.example.viewmodel.AuthUser
import com.example.viewmodel.CheckoutState
import com.example.viewmodel.HomeSettings
import com.example.viewmodel.HomeSection
import com.example.viewmodel.PoutineViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.PaymentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PoutineHouseApp()
            }
        }
    }
}

// Sealed hierarchy for Screens
sealed interface Screen {
    object Auth : Screen
    object Splash : Screen
    object Home : Screen
    object Menu : Screen
    object Cart : Screen
    object MyOrders : Screen
    object Story : Screen
    object HouseTour : Screen
    object AdminDashboard : Screen
    object Contact : Screen
}

@Composable
fun PoutineHouseApp(viewModel: PoutineViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var selectedProduct by remember { mutableStateOf<FoodItem?>(null) }
    val appScope = rememberCoroutineScope()
    
    // Auto-advance Splash Screen after 2.8 seconds
    LaunchedEffect(key1 = currentScreen) {
        if (currentScreen is Screen.Splash) {
            delay(2800)
            currentScreen = Screen.Home
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main Navigation Layout
        when (currentScreen) {
            Screen.Auth -> {
                AuthScreen(
                    viewModel = viewModel,
                    onAuthenticated = { user ->
                        currentScreen = if (user.role == "admin") Screen.AdminDashboard else Screen.Home
                    },
                    onBack = { currentScreen = Screen.Home }
                )
            }
            Screen.Splash -> {
                SplashScreen(onEnter = { currentScreen = Screen.Home })
            }
            else -> {
                Scaffold(
                    bottomBar = {
                        if (currentScreen != Screen.AdminDashboard) {
                            PoutineBottomNavBar(
                                currentScreen = currentScreen,
                                onNavigate = { screen ->
                                    currentScreen = screen
                                    selectedProduct = null // close details on nav change
                                }
                            )
                        }
                    },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                if (initialState == Screen.AdminDashboard || targetState == Screen.AdminDashboard) {
                                    fadeIn(animationSpec = tween(90)) togetherWith fadeOut(animationSpec = tween(90))
                                } else {
                                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                                }
                            },
                            label = "MainScreenTransition"
                        ) { screen ->
                            when (screen) {
                                Screen.Home -> HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToMenu = { category ->
                                        viewModel.setSelectedCategory(category)
                                        currentScreen = Screen.Menu
                                    },
                                    onNavigateToProduct = { item ->
                                        selectedProduct = item
                                    },
                                    onNavigateToAuth = {
                                        currentScreen = Screen.Auth
                                    },
                                    onNavigateToStory = {
                                        currentScreen = Screen.Story
                                    },
                                    onNavigateToHouseTour = {
                                        currentScreen = Screen.HouseTour
                                    },
                                    onNavigateToDashboard = {
                                        currentScreen = Screen.AdminDashboard
                                    }
                                )
                                Screen.Menu -> MenuScreen(
                                    viewModel = viewModel,
                                    onSelectProduct = { item ->
                                        selectedProduct = item
                                    },
                                    onViewFamily = {
                                        currentScreen = Screen.Story
                                    }
                                )
                                Screen.Cart -> CartScreen(
                                    viewModel = viewModel,
                                    onNavigateToProducts = {
                                        viewModel.setSelectedCategory(null)
                                        currentScreen = Screen.Menu
                                    },
                                    onNavigateToOrders = {
                                        currentScreen = Screen.MyOrders
                                    }
                                )
                                Screen.MyOrders -> MyOrdersScreen(viewModel = viewModel)
                                Screen.Story -> StoryScreen()
                                Screen.HouseTour -> HouseTourScreen()
                                Screen.AdminDashboard -> AdminDashboardScreen(
                                    viewModel = viewModel,
                                    onLogout = {
                                        currentScreen = Screen.Home
                                        selectedProduct = null
                                        appScope.launch {
                                            delay(180)
                                            viewModel.logout()
                                        }
                                    }
                                )
                                Screen.Contact -> ContactScreen()
                                Screen.Auth -> { /* Auth screen is outside main navigation */ }
                                Screen.Splash -> { /* Fallback */ }
                            }
                        }

                        // Product Details Overlay
                        AnimatedVisibility(
                            visible = selectedProduct != null,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                            ) + fadeIn(),
                            exit = slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = tween(250)
                            ) + fadeOut(),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            selectedProduct?.let { product ->
                                ProductDetailsScreen(
                                    product = product,
                                    viewModel = viewModel,
                                    onDismiss = { selectedProduct = null }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthScreen(
    viewModel: PoutineViewModel,
    onAuthenticated: (AuthUser) -> Unit,
    onBack: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    var isRegisterMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var registerAsAdmin by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var alert by remember { mutableStateOf<SweetAlertInfo?>(null) }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                val isAdmin = state.user.role == "admin"
                if (!isAdmin) {
                    alert = SweetAlertInfo(
                        title = "Welcome, ${state.user.name.substringBefore(" ")}",
                        message = "You are signed in and ready to order.",
                        type = SweetAlertType.Success
                    )
                    delay(650)
                }
                onAuthenticated(state.user)
            }
            is AuthState.Error -> {
                alert = SweetAlertInfo(
                    title = "Check your details",
                    message = state.message,
                    type = SweetAlertType.Error
                )
            }
            else -> Unit
        }
    }

    alert?.let { info ->
        SweetAlertDialog(
            info = info,
            onDismiss = {
                alert = null
                viewModel.clearAuthError()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .width(172.dp)
                        .height(132.dp)
                        .shadow(10.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, GoldenPotato.copy(alpha = 0.7f))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_poutine_family_logo),
                        contentDescription = "Le Poutine House",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Le Poutine House",
                    color = PrimaryBlack,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isRegisterMode) "Create your family account" else "Welcome back to the house",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 22.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isRegisterMode) {
                            AuthTextField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    viewModel.clearAuthError()
                                },
                                label = "Name",
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GoldenPotato) },
                                imeAction = ImeAction.Next
                            )
                        }

                        AuthTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                viewModel.clearAuthError()
                            },
                            label = "Email",
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GoldenPotato) },
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )

                        AuthTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                viewModel.clearAuthError()
                            },
                            label = "Password",
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldenPotato) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = Color.Gray
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )

                        if (authState is AuthState.Error) {
                            Text(
                                text = (authState as AuthState.Error).message,
                                color = CanadianRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        Button(
                            onClick = {
                                val validationError = validateAuthFields(
                                    isRegisterMode = isRegisterMode,
                                    name = name,
                                    email = email,
                                    password = password
                                )

                                if (validationError != null) {
                                    alert = SweetAlertInfo(
                                        title = "Almost there",
                                        message = validationError,
                                        type = SweetAlertType.Warning
                                    )
                                } else if (isRegisterMode) {
                                    viewModel.register(name.trim(), email.trim(), password, registerAsAdmin)
                                } else {
                                    viewModel.login(email.trim(), password)
                                }
                            },
                            enabled = authState !is AuthState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CanadianRed),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isRegisterMode) "Create Account" else "Login",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        TextButton(
                            onClick = {
                                isRegisterMode = !isRegisterMode
                                viewModel.clearAuthError()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isRegisterMode) "I already have an account" else "Create a new account",
                                color = PrimaryBlack,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isRegisterMode) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { registerAsAdmin = !registerAsAdmin }
                                    .background(CreamBackground)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = registerAsAdmin,
                                    onCheckedChange = { registerAsAdmin = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = CanadianRed,
                                        uncheckedColor = Color.Gray
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Register as administrator",
                                        color = PrimaryBlack,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Admins can add content to the home page.",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        TextButton(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Continue browsing",
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class SweetAlertType {
    Success,
    Error,
    Warning
}

data class SweetAlertInfo(
    val title: String,
    val message: String,
    val type: SweetAlertType
)

data class FamilyVisual(
    val category: PoutineCategory,
    val title: String,
    val subtitle: String,
    val imageName: String
)

data class FamilyStoryVisual(
    val category: PoutineCategory,
    val title: String,
    val subtitle: String,
    val description: String,
    val imageName: String
)

data class ProductFamilyCategory(
    val value: String,
    val label: String,
    val description: String
)

val ProductFamilyCategories = listOf(
    ProductFamilyCategory(
        value = "Papá - Platos fuertes",
        label = "Papá - Platos fuertes",
        description = "Poutines grandes, carnes, brisket, bacon y recetas contundentes."
    ),
    ProductFamilyCategory(
        value = "Mamá - Poutines suaves y dulces",
        label = "Mamá - Suaves y dulces",
        description = "Poutines creativas, gourmet, toque dulce/salado y postres inspirados en poutine."
    ),
    ProductFamilyCategory(
        value = "Niños - Menú infantil",
        label = "Niños - Menú infantil",
        description = "Porciones pequeñas, sabores suaves y combos para los Little Spuds."
    ),
    ProductFamilyCategory(
        value = "Abuelos - Entradas tradicionales",
        label = "Abuelos - Entradas",
        description = "Entradas, clásicos de casa, recetas tradicionales y gravy de la familia."
    ),
    ProductFamilyCategory(
        value = "Familia - Combos",
        label = "Familia - Combos",
        description = "Combos para compartir, packs familiares y ofertas de la casa."
    )
)

fun PoutineCategory.menuTabLabel(): String = when (this) {
    PoutineCategory.MR_POUTINE -> "Papá"
    PoutineCategory.MRS_POUTINE -> "Mamá"
    PoutineCategory.KIDS -> "Niños"
    PoutineCategory.GRANDPA, PoutineCategory.GRANDMA -> "Abuelos"
    PoutineCategory.COMBOS -> "Familia"
}

fun PoutineCategory.menuFamilyDescription(): String = when (this) {
    PoutineCategory.MR_POUTINE -> "Platos fuertes de la casa: poutines grandes, carnes, bacon y recetas con carácter."
    PoutineCategory.MRS_POUTINE -> "Poutines suaves, gourmet y dulce/saladas pensadas con el toque de Mamá."
    PoutineCategory.KIDS -> "Menú infantil con porciones pequeñas, gravy suave y sabores divertidos."
    PoutineCategory.GRANDPA, PoutineCategory.GRANDMA -> "Entradas, clásicos tradicionales y recetas de los abuelos con gravy de familia."
    PoutineCategory.COMBOS -> "Combos para compartir entre todos: packs familiares, sides y favoritos de la casa."
}

@Composable
fun DrawableImageByName(
    resourceName: String,
    fallbackResId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val remoteUrl = remember(resourceName) {
        when {
            resourceName.startsWith("http://") || resourceName.startsWith("https://") -> resourceName
            resourceName.startsWith("/uploads/") -> "http://192.168.3.140:3000$resourceName"
            else -> null
        }
    }
    val drawableId = remember(resourceName) {
        context.resources.getIdentifier(resourceName, "drawable", context.packageName)
    }

    if (remoteUrl != null) {
        AsyncImage(
            model = remoteUrl,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Image(
            painter = painterResource(id = if (drawableId != 0) drawableId else fallbackResId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
fun SweetAlertDialog(
    info: SweetAlertInfo,
    onDismiss: () -> Unit
) {
    val accentColor = when (info.type) {
        SweetAlertType.Success -> Color(0xFF2E7D32)
        SweetAlertType.Error -> CanadianRed
        SweetAlertType.Warning -> GoldenPotato
    }

    val icon = when (info.type) {
        SweetAlertType.Success -> Icons.Default.CheckCircle
        SweetAlertType.Error -> Icons.Default.Error
        SweetAlertType.Warning -> Icons.Default.Warning
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(accentColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(38.dp))
            }
        },
        title = {
            Text(
                text = info.title,
                color = PrimaryBlack,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = info.message,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}

fun validateAuthFields(
    isRegisterMode: Boolean,
    name: String,
    email: String,
    password: String
): String? {
    if (isRegisterMode && name.trim().length < 2) {
        return "Please enter your full name."
    }

    val trimmedEmail = email.trim()
    val emailIsValid = trimmedEmail.contains("@") &&
            trimmedEmail.substringAfter("@").contains(".") &&
            trimmedEmail.length >= 6

    if (!emailIsValid) {
        return "Please enter a valid email address."
    }

    if (password.length < 5) {
        return "Password must have at least 5 characters."
    }

    return null
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldenPotato,
            unfocusedBorderColor = PotatoBeige.copy(alpha = 0.5f),
            focusedLabelColor = GoldenPotato,
            cursorColor = CanadianRed,
            focusedTextColor = PrimaryBlack,
            unfocusedTextColor = PrimaryBlack
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ProductCategoryDropdown(
    selectedValue: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = ProductFamilyCategories.firstOrNull { it.value == selectedValue }
        ?: ProductFamilyCategories.first()

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = GoldenPotato) },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Open categories",
                        tint = PrimaryBlack
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldenPotato,
                unfocusedBorderColor = PotatoBeige.copy(alpha = 0.5f),
                focusedLabelColor = GoldenPotato,
                cursorColor = CanadianRed,
                focusedTextColor = PrimaryBlack,
                unfocusedTextColor = PrimaryBlack
            ),
            shape = RoundedCornerShape(16.dp)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .background(Color.White)
        ) {
            ProductFamilyCategories.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(option.label, color = PrimaryBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(option.description, color = Color.Gray, fontSize = 11.sp, lineHeight = 14.sp)
                        }
                    },
                    onClick = {
                        onSelected(option.value)
                        expanded = false
                    }
                )
            }
        }
    }
}

// 1. Splash Screen
@Composable
fun SplashScreen(onEnter: () -> Unit) {
    var alphaVal by remember { mutableStateOf(0f) }
    var scaleVal by remember { mutableStateOf(0.85f) }

    LaunchedEffect(key1 = true) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(1200, easing = LinearOutSlowInEasing)
        ) { value, _ ->
            alphaVal = value
        }
        animate(
            initialValue = 0.85f,
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) { value, _ ->
            scaleVal = value
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onEnter)
            .testTag("splash_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        // Decorative Maple Leafs floating subtly
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .alpha(alphaVal)
                .scale(scaleVal)
        ) {
            Text(
                text = "BIENVENUE",
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Dynamic Framed Potato Family Logo Container
            Card(
                modifier = Modifier
                    .size(240.dp)
                    .shadow(16.dp, RoundedCornerShape(120.dp)),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(4.dp, MaterialTheme.colorScheme.secondary)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_poutine_family),
                    contentDescription = "Le Poutine House Potato Family",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Le Poutine House",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("app_title")
            )

            Text(
                text = "Poutine • Family • Love",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// Bottom Navigation Bar
@Composable
fun PoutineBottomNavBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier
            .shadow(4.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBarItem(
            selected = currentScreen is Screen.Home,
            onClick = { onNavigate(Screen.Home) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CanadianRed,
                unselectedIconColor = Color.Gray,
                selectedTextColor = PrimaryBlack,
                unselectedTextColor = Color.Gray,
                indicatorColor = CreamBackground
            ),
            modifier = Modifier.testTag("nav_home")
        )
        NavigationBarItem(
            selected = currentScreen is Screen.Menu,
            onClick = { onNavigate(Screen.Menu) },
            icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Menu") },
            label = { Text("Menu", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CanadianRed,
                unselectedIconColor = Color.Gray,
                selectedTextColor = PrimaryBlack,
                unselectedTextColor = Color.Gray,
                indicatorColor = CreamBackground
            ),
            modifier = Modifier.testTag("nav_menu")
        )
        NavigationBarItem(
            selected = currentScreen is Screen.Cart,
            onClick = { onNavigate(Screen.Cart) },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
            label = { Text("Cart", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CanadianRed,
                unselectedIconColor = Color.Gray,
                selectedTextColor = PrimaryBlack,
                unselectedTextColor = Color.Gray,
                indicatorColor = CreamBackground
            ),
            modifier = Modifier.testTag("nav_cart")
        )
        NavigationBarItem(
            selected = currentScreen is Screen.Story,
            onClick = { onNavigate(Screen.Story) },
            icon = { Icon(Icons.Default.HistoryEdu, contentDescription = "Our Story") },
            label = { Text("Story", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CanadianRed,
                unselectedIconColor = Color.Gray,
                selectedTextColor = PrimaryBlack,
                unselectedTextColor = Color.Gray,
                indicatorColor = CreamBackground
            ),
            modifier = Modifier.testTag("nav_story")
        )
        NavigationBarItem(
            selected = currentScreen is Screen.HouseTour,
            onClick = { onNavigate(Screen.HouseTour) },
            icon = { Icon(Icons.Default.Home, contentDescription = "House Tour") },
            label = { Text("Tour", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CanadianRed,
                unselectedIconColor = Color.Gray,
                selectedTextColor = PrimaryBlack,
                unselectedTextColor = Color.Gray,
                indicatorColor = CreamBackground
            ),
            modifier = Modifier.testTag("nav_house_tour")
        )
        NavigationBarItem(
            selected = currentScreen is Screen.Contact,
            onClick = { onNavigate(Screen.Contact) },
            icon = { Icon(Icons.Default.LocationOn, contentDescription = "Location") },
            label = { Text("Contact", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CanadianRed,
                unselectedIconColor = Color.Gray,
                selectedTextColor = PrimaryBlack,
                unselectedTextColor = Color.Gray,
                indicatorColor = CreamBackground
            ),
            modifier = Modifier.testTag("nav_contact")
        )
    }
}

// 2. Home Page
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: PoutineViewModel,
    onNavigateToMenu: (PoutineCategory?) -> Unit,
    onNavigateToProduct: (FoodItem) -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToStory: () -> Unit,
    onNavigateToHouseTour: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val homeSections by viewModel.homeSections.collectAsState()
    val homeSettings by viewModel.homeSettings.collectAsState()
    val activeSections = homeSections.filter { it.active }
    val featuredSection = activeSections.firstOrNull { it.sectionType == "featured" }
        ?: activeSections.firstOrNull()
    var searchVal by remember { mutableStateOf("") }
    val featuredList by remember {
        derivedStateOf { MenuData.items.filter { it.isFeatured } }
    }
    val familyCards = remember {
        listOf(
            FamilyVisual(PoutineCategory.MR_POUTINE, "Father", "Mr. Poutine", "papa"),
            FamilyVisual(PoutineCategory.MRS_POUTINE, "Mother", "Mrs. Poutine", "mama"),
            FamilyVisual(PoutineCategory.KIDS, "Kids", "Little Spuds", "ninos"),
            FamilyVisual(PoutineCategory.GRANDPA, "Grandparents", "Grandpa & Grandma", "abuelos"),
            FamilyVisual(PoutineCategory.COMBOS, "Full Family", "Le Poutine House", "img_poutine_family_logo")
        )
    }

    // Promotions Static Data
    val promotions = listOf(
        "BOGO Mrs. Poutine Favorites! Double the love." to "Buy 1 Get 1 free on Mrs. Poutine category this Sunday.",
        "Free Canadian Delivery! 🍟" to "Get free premium shipping on all orders over $30 across Montreal.",
        "Grandma's Thanksgiving Special: 15% off!" to "Use code GRANNYLOVE at checkout. Warm slices of heaven.",
        "Grandpa's Smoked Woodfire Deal: $5 off" to "Order the cherrywood Pork and enjoy discounts instantly."
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_root"),
        contentPadding = PaddingValues(bottom = 112.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                DrawableImageByName(
                    resourceName = homeSettings.heroLogo,
                    fallbackResId = R.drawable.img_poutine_family,
                    contentDescription = "Le Poutine House family logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(176.dp)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // 1. Header Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = homeSettings.eyebrowText,
                        color = CanadianRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = homeSettings.brandTitle.substringBeforeLast(" ", homeSettings.brandTitle),
                            color = GoldenPotato,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = homeSettings.brandTitle.substringAfterLast(" ", ""),
                            color = PrimaryBlack,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                when (val state = authState) {
                    is AuthState.Authenticated -> {
                        Card(
                            modifier = Modifier
                                .height(48.dp)
                                .clickable {
                                    Toast.makeText(context, "Signed in as ${state.user.name}", Toast.LENGTH_SHORT).show()
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.3f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp).fillMaxHeight(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = "Account", tint = GoldenPotato)
                                Text(
                                    text = state.user.name.substringBefore(" ").take(10),
                                    color = PrimaryBlack,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { viewModel.logout() }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Logout, contentDescription = "Logout", tint = CanadianRed)
                                }
                            }
                        }
                    }
                    else -> {
                        Button(
                            onClick = onNavigateToAuth,
                            modifier = Modifier.height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CanadianRed),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Sign in",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sign in",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        if ((authState as? AuthState.Authenticated)?.user?.role == "admin") {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .clickable(onClick = onNavigateToDashboard),
                    colors = CardDefaults.cardColors(containerColor = GoldenPotato.copy(alpha = 0.16f)),
                    border = BorderStroke(1.dp, GoldenPotato.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = CanadianRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Admin dashboard", color = PrimaryBlack, fontWeight = FontWeight.Black)
                            Text("Add announcements to the main page.", color = Color.Gray, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CanadianRed)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlack),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = homeSettings.familyTitle,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            Text(
                                text = homeSettings.familyDescription,
                                color = Color.White.copy(alpha = 0.72f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        TextButton(onClick = onNavigateToStory) {
                            Text("Ver mas", color = GoldenPotato, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(familyCards) { family ->
                            Card(
                                modifier = Modifier
                                    .width(126.dp)
                                    .height(150.dp)
                                    .clickable {
                                        viewModel.setSelectedCategory(family.category)
                                        onNavigateToMenu(family.category)
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    DrawableImageByName(
                                        resourceName = family.imageName,
                                        fallbackResId = R.drawable.img_poutine_family,
                                        contentDescription = family.title,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(82.dp)
                                            .clip(RoundedCornerShape(14.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = family.title,
                                        color = PrimaryBlack,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = family.subtitle,
                                        color = Color.Gray,
                                        fontSize = 9.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .clickable(onClick = onNavigateToHouseTour),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(CanadianRed.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = CanadianRed)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = homeSettings.houseTourTitle,
                            color = PrimaryBlack,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Text(
                            text = homeSettings.houseTourDescription,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GoldenPotato)
                }
            }
        }

        // 2. Search Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchVal,
                    onValueChange = { searchVal = it },
                    placeholder = { Text("Find your favorite potato...", color = Color.Gray.copy(alpha = 0.7f), fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = GoldenPotato) },
                    trailingIcon = {
                        if (searchVal.isNotEmpty()) {
                            IconButton(onClick = { searchVal = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Color.Gray)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_bar"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = GoldenPotato,
                        unfocusedBorderColor = PotatoBeige.copy(alpha = 0.3f),
                        focusedTextColor = PrimaryBlack,
                        unfocusedTextColor = PrimaryBlack
                    ),
                    shape = RoundedCornerShape(28.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    singleLine = true
                )

                // Quick trigger: Navigate with search parameter
                if (searchVal.isNotEmpty()) {
                    Button(
                        onClick = {
                            viewModel.setSearchQuery(searchVal)
                            onNavigateToMenu(null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CanadianRed),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Text("Search in Family Menu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // 3. Hero Banner
        item {
            val mainAnnouncement = featuredSection ?: HomeSection(
                id = 0,
                title = homeSettings.featuredTitle,
                description = homeSettings.featuredDescription,
                sectionType = "featured",
                imageName = homeSettings.featuredImage,
                active = true,
                sortOrder = 1
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .height(144.dp)
                    .background(PrimaryBlack, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .clickable {
                        val grandpaItem = MenuData.items.find { it.category == PoutineCategory.GRANDPA }
                        if (grandpaItem != null) {
                            onNavigateToProduct(grandpaItem)
                        } else {
                            onNavigateToMenu(PoutineCategory.GRANDPA)
                        }
                    }
            ) {
                // Abstract decorative background circle on the right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 32.dp, y = 32.dp)
                        .size(160.dp)
                        .background(GoldenPotato.copy(alpha = 0.2f), CircleShape)
                )

                // 🍟 Emoji / asset decoration on top-right
                Text(
                    text = "🍟",
                    fontSize = 64.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 24.dp, top = 16.dp)
                        .alpha(0.9f)
                )

                // Content Column
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(220.dp)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "FEATURED TODAY",
                        color = PotatoBeige,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = mainAnnouncement.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = mainAnnouncement.description,
                        color = Color.White.copy(alpha = 0.74f),
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .background(CanadianRed, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ORDER NOW",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        val extraSections = activeSections.filter { it.id != featuredSection?.id }
        if (extraSections.isNotEmpty()) {
            item {
                Text(
                    text = "FROM THE HOUSE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = PrimaryBlack,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 10.dp, bottom = 8.dp)
                )
            }

            items(extraSections) { section ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                        .clickable {
                            if (section.sectionType == "tour") {
                                onNavigateToHouseTour()
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DrawableImageByName(
                            resourceName = section.imageName ?: "img_poutine_family_logo",
                            fallbackResId = R.drawable.img_poutine_family_logo,
                            contentDescription = section.title,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = section.title,
                                color = PrimaryBlack,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = section.description,
                                color = Color.Gray,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 3.dp)
                            )
                        }
                        if (section.sectionType == "tour") {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CanadianRed)
                        }
                    }
                }
            }
        }

        // 4. Meet the Potato Family Navigation Categories Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "THE FAMILY MENU",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = PrimaryBlack
                )
                TextButton(
                    onClick = { onNavigateToMenu(null) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "View All",
                        color = CanadianRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 5. Grid scroll of Potato Families
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                familyCards.forEach { family ->
                    Card(
                        modifier = Modifier
                            .width(132.dp)
                            .height(160.dp)
                            .clickable { onNavigateToMenu(family.category) }
                            .testTag("family_cat_${family.category.id}"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            DrawableImageByName(
                                resourceName = family.imageName,
                                fallbackResId = R.drawable.img_poutine_family,
                                contentDescription = family.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(78.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = family.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = PrimaryBlack,
                                lineHeight = 13.sp
                            )
                            Text(
                                text = family.subtitle,
                                fontSize = 9.sp,
                                color = CanadianRed,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 6. Promotions Carousel
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                Text(
                    text = "Weekly Family Promotions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
                    color = PrimaryBlack
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(promotions) { promo ->
                        Card(
                            modifier = Modifier
                                .width(280.dp)
                                .height(104.dp)
                                .clickable {
                                    Toast.makeText(context, "Promotion applied: ${promo.first}", Toast.LENGTH_SHORT).show()
                                },
                            colors = CardDefaults.cardColors(containerColor = PrimaryBlack),
                            shape = RoundedCornerShape(24.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = "Promo Fire Icon",
                                    tint = GoldenPotato,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = promo.first,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = promo.second,
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Featured Offers Heading
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .clickable(onClick = onNavigateToHouseTour),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_poutine_hero),
                        contentDescription = "House tour preview",
                        modifier = Modifier
                            .size(78.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "House Tour",
                            color = PrimaryBlack,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Walk through the kitchen, dining room, family wall and pickup counter.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CanadianRed)
                }
            }
        }

        item {
            Text(
                text = "Featured Family Favorites",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
                color = PrimaryBlack
            )
        }

        items(featuredList) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp)
                    .clickable { onNavigateToProduct(item) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(GoldenPotato.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        PotatoCharacterAvatar(
                            category = item.category,
                            modifier = Modifier.size(46.dp),
                            backgroundColor = Color.Transparent
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PrimaryBlack
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.description,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = GoldenPotato,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = " ${item.rating}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlack
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "⏱️ ${item.prepTime}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$${item.price}",
                            fontWeight = FontWeight.Black,
                            color = CanadianRed,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Details navigation",
                            tint = GoldenPotato,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// 4. Product Details Screen / Overlay sheet
@Composable
fun ProductDetailsScreen(
    product: FoodItem,
    viewModel: PoutineViewModel,
    onDismiss: () -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    var quantity by remember { mutableStateOf(1) }
    val isFavorited = favorites.contains(product.id)
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("product_details_overlay"),
        color = CreamBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                DrawableImageByName(
                    resourceName = product.imageUrl ?: "img_poutine_hero",
                    fallbackResId = R.drawable.img_poutine_hero,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent),
                                startY = 0f, endY = 200f
                            )
                        )
                )

                // Header Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilledIconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White.copy(alpha = 0.9f))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryBlack)
                    }

                    FilledIconButton(
                        onClick = { viewModel.toggleFavorite(product.id) },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White.copy(alpha = 0.9f))
                    ) {
                        Icon(
                            imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            tint = if (isFavorited) Color.Red else PrimaryBlack,
                            contentDescription = "Favorite product"
                        )
                    }
                }

                // Family chef badge
                Card(
                    shape = RoundedCornerShape(topStart = 16.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(Color.Transparent),
                    colors = CardDefaults.cardColors(containerColor = CanadianRed),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${product.category.characterName}'s Chef Choice",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Core Details Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Category Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    PotatoCharacterAvatar(
                        category = product.category,
                        modifier = Modifier.size(28.dp),
                        backgroundColor = Color.Transparent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = product.category.title,
                        color = CanadianRed,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = PrimaryBlack,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Ratings and timers
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Icon(Icons.Default.Star, "Rating star", tint = GoldenPotato, modifier = Modifier.size(18.dp))
                    Text(text = " ${product.rating} (120+ reviews)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlack)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "⏱️ ${product.prepTime}", color = Color.Gray, fontSize = 13.sp)
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = if (product.stock > 5) Color(0xFFE8F5E9) else GoldenPotato.copy(alpha = 0.16f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val stockColor = if (product.stock > 5) Color(0xFF2E7D32) else CanadianRed
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = stockColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (product.available && product.stock > 0) "${product.stock} available today" else "Sold out today",
                            color = stockColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Divider(color = PotatoBeige.copy(alpha = 0.2f), thickness = 1.dp)

                // Description
                Text(
                    text = "Description Story",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PrimaryBlack,
                    modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
                )
                Text(
                    text = product.description,
                    color = Color.Gray,
                    lineHeight = 18.sp,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Ingredients List
                if (product.ingredients.isNotEmpty()) {
                    Text(
                        text = "Real Family Farm Ingredients:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryBlack,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        product.ingredients.forEach { ing ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = ing,
                                    fontSize = 12.sp,
                                    color = PrimaryBlack,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Quantity & Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Quantity", fontWeight = FontWeight.Black, fontSize = 16.sp, color = PrimaryBlack)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .border(BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.3f)), RoundedCornerShape(20.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(onClick = { if (quantity > 1) quantity-- }) {
                            Icon(Icons.Default.Remove, "Decrease quantity", tint = PrimaryBlack)
                        }
                        Text(
                            text = quantity.toString(),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = PrimaryBlack,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(onClick = { quantity++ }) {
                            Icon(Icons.Default.Add, "Increase quantity", tint = PrimaryBlack)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Purchase Action Button
                val totalCost = product.price * quantity
                Button(
                    onClick = {
                        viewModel.addToCart(product, quantity)
                        Toast.makeText(context, "${product.name} x$quantity added to cart!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    enabled = product.available && product.stock > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("add_to_cart_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = CanadianRed),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = if (product.available && product.stock > 0) "Add to Cart" else "Sold Out", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Text(
                            text = String.format("$%.2f", totalCost),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// 3. Family Menu Screen
@Composable
fun MenuScreen(
    viewModel: PoutineViewModel,
    onSelectProduct: (FoodItem) -> Unit,
    onViewFamily: () -> Unit
) {
    val items by viewModel.filteredItems.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var showOnlyFavorites by remember { mutableStateOf(false) }
    val visibleFamilyTabs = listOf(
        PoutineCategory.MR_POUTINE,
        PoutineCategory.MRS_POUTINE,
        PoutineCategory.KIDS,
        PoutineCategory.GRANDPA,
        PoutineCategory.COMBOS
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("menu_screen_root")
    ) {
        // Search Bar Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CreamBackground)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Filter gourmet poutine catalog...", color = Color.Gray.copy(alpha = 0.7f), fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldenPotato) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search filter", tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("menu_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = GoldenPotato,
                    unfocusedBorderColor = PotatoBeige.copy(alpha = 0.3f),
                    focusedTextColor = PrimaryBlack,
                    unfocusedTextColor = PrimaryBlack
                ),
                shape = RoundedCornerShape(28.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable Pill Categories Selectors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" Pill
                FilterChip(
                    selected = selectedCategory == null && !showOnlyFavorites,
                    onClick = {
                        viewModel.setSelectedCategory(null)
                        showOnlyFavorites = false
                    },
                    label = { Text("All Recipes", fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlack,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = PrimaryBlack
                    ),
                    shape = RoundedCornerShape(18.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedCategory == null && !showOnlyFavorites,
                        borderColor = PotatoBeige.copy(alpha = 0.3f),
                        selectedBorderColor = PrimaryBlack
                    )
                )

                // "Favorites" Pill
                FilterChip(
                    selected = showOnlyFavorites,
                    onClick = {
                        showOnlyFavorites = true
                        viewModel.setSelectedCategory(null)
                    },
                    label = { Text("Favorites ❤️", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CanadianRed,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = PrimaryBlack
                    ),
                    shape = RoundedCornerShape(18.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = showOnlyFavorites,
                        borderColor = PotatoBeige.copy(alpha = 0.3f),
                        selectedBorderColor = CanadianRed
                    )
                )

                // Specific families
                visibleFamilyTabs.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat && !showOnlyFavorites,
                        onClick = {
                            viewModel.setSelectedCategory(cat)
                            showOnlyFavorites = false
                        },
                        label = { Text(cat.menuTabLabel(), fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlack,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = PrimaryBlack
                        ),
                        shape = RoundedCornerShape(18.dp),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == cat && !showOnlyFavorites,
                            borderColor = PotatoBeige.copy(alpha = 0.3f),
                            selectedBorderColor = PrimaryBlack
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val categoryDescriptionTitle = selectedCategory?.menuTabLabel() ?: "Toda la familia"
            val categoryDescriptionText = selectedCategory?.menuFamilyDescription()
                ?: "Explora las recetas de Papá, Mamá, Niños, Abuelos y los combos Familia. Todo se actualiza desde el dashboard."

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlack),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DrawableImageByName(
                        resourceName = "img_poutine_family_logo",
                        fallbackResId = R.drawable.img_poutine_family,
                        contentDescription = "Poutine family",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = categoryDescriptionTitle,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Text(
                            text = categoryDescriptionText,
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                    TextButton(onClick = onViewFamily) {
                        Text("Ver mas", color = GoldenPotato, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active List display
        val filteredList = if (showOnlyFavorites) {
            items.filter { favorites.contains(it.id) }
        } else {
            items
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "🥔🤷",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "No Delicacies Found",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = PrimaryBlack
                    )
                    Text(
                        text = "Try clearing search keywords or selecting another potato family member's specialty.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredList) { fItem ->
                    MenuProductCard(
                        item = fItem,
                        isBookmarked = favorites.contains(fItem.id),
                        onToggleFav = { viewModel.toggleFavorite(fItem.id) },
                        onSelect = { onSelectProduct(fItem) },
                        onQuickAdd = {
                            viewModel.addToCart(fItem, 1)
                        }
                    )
                }
            }
        }
    }
}

// Custom Menu Product Card Item
@Composable
fun MenuProductCard(
    item: FoodItem,
    isBookmarked: Boolean,
    onToggleFav: () -> Unit,
    onSelect: () -> Unit,
    onQuickAdd: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable(onClick = onSelect)
            .testTag("menu_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                if (item.imageUrl != null) {
                    DrawableImageByName(
                        resourceName = item.imageUrl,
                        fallbackResId = R.drawable.img_poutine_hero,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.15f))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        GoldenPotato.copy(alpha = 0.22f),
                                        CreamBackground,
                                        Color(item.category.colorHex).copy(alpha = 0.20f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        PotatoCharacterAvatar(
                            category = item.category,
                            modifier = Modifier.size(76.dp),
                            backgroundColor = Color.White.copy(alpha = 0.78f)
                        )
                    }
                }

                // Top absolute pills inside cover
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item.tag?.let {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CanadianRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = " $it ",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } ?: Spacer(modifier = Modifier.width(4.dp))

                    FilledIconButton(
                        onClick = onToggleFav,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            tint = if (isBookmarked) Color.Red else PrimaryBlack,
                            contentDescription = "Add bookmark",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Body Area
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = PrimaryBlack
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PotatoCharacterAvatar(item.category, modifier = Modifier.size(24.dp), backgroundColor = Color.Transparent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.category.characterName,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Text(
                        text = String.format("$%.2f", item.price),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = CanadianRed
                    )
                }

                Text(
                    text = item.description,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    lineHeight = 15.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                Text(
                    text = if (item.available && item.stock > 0) "Available today: ${item.stock}" else "Sold out today",
                    color = if (item.available && item.stock > 0) Color(0xFF2E7D32) else CanadianRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = GoldenPotato, modifier = Modifier.size(15.dp))
                        Text(text = " ${item.rating}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlack)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "⏱️ ${item.prepTime}", color = Color.Gray, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onQuickAdd()
                            Toast.makeText(context, "Added ${item.name} to Cart!", Toast.LENGTH_SHORT).show()
                        },
                        enabled = item.available && item.stock > 0,
                        modifier = Modifier.height(34.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack),
                        shape = RoundedCornerShape(17.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Quick add", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (item.available && item.stock > 0) "Add" else "Out", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// 5. Shopping Cart Page
@Composable
fun CartScreen(
    viewModel: PoutineViewModel,
    onNavigateToProducts: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val cartList by viewModel.cart.collectAsState()
    val checkoutState by viewModel.checkoutState.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val couponMessage by viewModel.couponMessage.collectAsState()
    var promoCode by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(checkoutState) {
        val error = checkoutState as? CheckoutState.Error
        if (error != null) {
            Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            viewModel.resetCheckout()
        }
    }

    if (checkoutState is CheckoutState.Processing) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 4.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Contacting Canadian Bank...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "A Potato Family driver is warming up their sled!",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
        return
    }

    if (checkoutState is CheckoutState.Success) {
        val oState = checkoutState as CheckoutState.Success
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🍟🎉", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Poutine is on the Way!",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Order confirmation: ${oState.orderId}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = "We have received your order with warmth and are preparing the squeakiest curds in Quebec! Estimated delivery 25-35 minutes.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.resetCheckout() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Order More Poutine", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        viewModel.resetCheckout()
                        viewModel.refreshCustomerOrders()
                        onNavigateToOrders()
                    },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View My Orders", fontWeight = FontWeight.Bold, color = PrimaryBlack)
                }
            }
        }
        return
    }

    if (cartList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🛒💨", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your Cart is Empty",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = PrimaryBlack
                )
                Text(
                    text = "Mr. Poutine's gravy cooker is ready. Let's fill this cart with delicious local formulas!",
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onNavigateToProducts,
                    colors = ButtonDefaults.buttonColors(containerColor = CanadianRed),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Explore Family Menus", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("cart_screen_root"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "My Poutine Feast Cart",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = PrimaryBlack,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = {
                        viewModel.refreshCustomerOrders()
                        onNavigateToOrders()
                    },
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = CanadianRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Orders", color = PrimaryBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        items(cartList) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp)
                    .testTag("cart_item_${item.foodItem.id}"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(GoldenPotato.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        PotatoCharacterAvatar(
                            category = item.foodItem.category,
                            modifier = Modifier.size(44.dp),
                            backgroundColor = Color.Transparent
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.foodItem.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = PrimaryBlack
                        )
                        Text(
                            text = item.foodItem.category.characterName,
                            fontSize = 11.sp,
                            color = CanadianRed,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = String.format("$%.2f each", item.foodItem.price),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    // Quantity buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(CreamBackground, RoundedCornerShape(20.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.updateCartQuantity(item.foodItem.id, item.quantity - 1) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Reduce", modifier = Modifier.size(14.dp), tint = PrimaryBlack)
                        }
                        Text(
                            text = item.quantity.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PrimaryBlack
                        )
                        IconButton(
                            onClick = { viewModel.updateCartQuantity(item.foodItem.id, item.quantity + 1) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp), tint = PrimaryBlack)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.removeFromCart(item.foodItem.id) }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                    }
                }
            }
        }

        // Coupon Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text("Delivery address") },
                    placeholder = { Text("Street, apartment, city", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoldenPotato) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = GoldenPotato,
                        unfocusedBorderColor = PotatoBeige.copy(alpha = 0.3f),
                        focusedTextColor = PrimaryBlack,
                        unfocusedTextColor = PrimaryBlack
                    ),
                    shape = RoundedCornerShape(18.dp),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = promoCode,
                        onValueChange = { promoCode = it.uppercase() },
                        placeholder = { Text("Coupon (e.g. GRANNYLOVE)", fontSize = 12.sp, color = Color.Gray.copy(alpha = 0.7f)) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = GoldenPotato,
                            unfocusedBorderColor = PotatoBeige.copy(alpha = 0.3f),
                            focusedTextColor = PrimaryBlack,
                            unfocusedTextColor = PrimaryBlack
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            viewModel.applyCoupon(promoCode)
                        },
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                couponMessage?.let { message ->
                    Text(
                        text = message,
                        color = if (appliedCoupon != null) Color(0xFF2E7D32) else CanadianRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp, top = 6.dp)
                    )
                }
            }
        }

        // Bill Card Summary
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Receipt Summary",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = PrimaryBlack,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", color = Color.Gray, fontSize = 13.sp)
                        Text(String.format("$%.2f", viewModel.cartSubtotal), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PrimaryBlack)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Canadian GST / QST (15%)", color = Color.Gray, fontSize = 13.sp)
                        Text(String.format("$%.2f", viewModel.cartTax), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PrimaryBlack)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Family Delivery Charge", color = Color.Gray, fontSize = 13.sp)
                        val del = viewModel.cartDelivery
                        if (del == 0.0) {
                            Text("FREE", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Text(String.format("$%.2f", del), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PrimaryBlack)
                        }
                    }

                    if (viewModel.couponDiscount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Coupon ${appliedCoupon?.code.orEmpty()}", color = Color(0xFF2E7D32), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(String.format("-$%.2f", viewModel.couponDiscount), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                        }
                    }

                    if (viewModel.cartDelivery > 0) {
                        val gap = 30.0 - viewModel.cartSubtotal
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GoldenPotato.copy(alpha = 0.15f)),
                            modifier = Modifier.padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = String.format("🎉 Add $%.2f more to unlock FREE Delivery!", gap),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CanadianRed,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Divider(
                        color = PotatoBeige.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount", fontWeight = FontWeight.Black, fontSize = 16.sp, color = PrimaryBlack)
                        Text(
                            text = String.format("$%.2f", viewModel.cartTotal),
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge,
                            color = CanadianRed
                        )
                    }
                }
            }
        }

        // Place Order button
        item {
            var isPayPalProcessing by remember { mutableStateOf(false) }
            
            val payPalLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                isPayPalProcessing = false
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    val orderId = result.data?.getStringExtra("order_id")
                    viewModel.capturePayPalOrder(orderId ?: "") { success ->
                        if (success) {
                            Toast.makeText(context, "¡Pago completado!", Toast.LENGTH_LONG).show()
                        }
                    }
                } else if (result.resultCode == android.app.Activity.RESULT_CANCELED) {
                    Toast.makeText(context, "Pago cancelado", Toast.LENGTH_SHORT).show()
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón de pago normal (efectivo/tarjeta en persona)
                Button(
                    onClick = {
                        if (deliveryAddress.trim().length < 8) {
                            Toast.makeText(context, "Add a valid delivery address", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.checkout(deliveryAddress)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("checkout_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = CanadianRed),
                    shape = RoundedCornerShape(27.dp)
                ) {
                    Text(
                        text = "Pagar en efectivo / tarjeta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
                
                // Botón de PayPal
                Button(
                    onClick = {
                        if (deliveryAddress.trim().length < 8) {
                            Toast.makeText(context, "Add a valid delivery address", Toast.LENGTH_SHORT).show()
                        } else {
                            isPayPalProcessing = true
                            viewModel.createPayPalOrder(viewModel.cartTotal) { paypalUrl ->
                                isPayPalProcessing = false
                                if (paypalUrl != null) {
                                    val intent = Intent(context, PaymentActivity::class.java).apply {
                                        putExtra("paypal_url", paypalUrl)
                                        putExtra("order_id", viewModel.currentPayPalOrderId.value)
                                    }
                                    payPalLauncher.launch(intent)
                                } else {
                                    Toast.makeText(context, "Error al crear orden PayPal", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    enabled = !isPayPalProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070BA)),
                    shape = RoundedCornerShape(27.dp)
                ) {
                    if (isPayPalProcessing) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Procesando...", color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pagar con PayPal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// 6. About Us / Story Screen
@Composable
fun StoryScreen() {
    val stories = listOf(
        FamilyStoryVisual(PoutineCategory.MR_POUTINE, "Mr. Poutine", "The Visionary Patriarch", "Mr. Poutine believes the core of a thriving household is home-cooked, giant portions. He is responsible for original secret starch twice-fry methods that create the crisp crust essential to surviving winters.", "papa"),
        FamilyStoryVisual(PoutineCategory.MRS_POUTINE, "Mrs. Poutine", "The Fine Dining Artisan", "Mrs. Poutine studied French culinary methods before falling in love with Father. She brought elegant lavender herb sauces, wild mushrooms, and decadent black truffle drizzles to our traditional menu.", "mama"),
        FamilyStoryVisual(PoutineCategory.KIDS, "Little Spuds", "The Playful Joyful Sprouts", "Little Potato Boy and Girl represent the sweet energy of youth. From demanding sausage cuts to sweet potato maple pairings, they ensure our kitchen remains youthful and incredibly adventurous.", "ninos"),
        FamilyStoryVisual(PoutineCategory.GRANDPA, "Grandpa & Grandma", "The Traditional Heart", "Grandpa keeps original woodfired Quebec methods alive while Grandma adds warm comforting love to everything. Together they are the soul of the house.", "abuelos"),
        FamilyStoryVisual(PoutineCategory.COMBOS, "The Whole Family", "Poutine, Family, Love", "Every character contributes a recipe, a story, and a reason to gather around the table.", "img_poutine_family_logo")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("story_screen_root"),
        contentPadding = PaddingValues(24.dp)
    ) {
        item {
            Text(
                text = "The Potato Family Story",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = PrimaryBlack,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "Established in Montreal in 1991, Le Poutine House is a traditional French-Canadian family-centric dining experience. Every member of our cartoon potato mascot family contributes their own secret recipe to keep the fireplace of Montreal warm and alive.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }

        items(stories) { info ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(GoldenPotato.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        DrawableImageByName(
                            resourceName = info.imageName,
                            fallbackResId = R.drawable.img_poutine_family,
                            contentDescription = info.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = info.title,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = PrimaryBlack
                        )
                        Text(
                            text = info.subtitle,
                            fontWeight = FontWeight.Bold,
                            color = CanadianRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = info.description,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier
            .width(132.dp)
            .height(92.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = CanadianRed, modifier = Modifier.size(22.dp))
            Column {
                Text(value, color = PrimaryBlack, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text(label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminMetricsPanel(metrics: AdminMetrics) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlack),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Analytics, contentDescription = null, tint = GoldenPotato)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Live metrics", color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
            }
            MiniBarChart("Sales by day", metrics.dailySales, valuePrefix = "$")
            MiniBarChart("Top products", metrics.topProducts)
            MiniBarChart("Order status", metrics.orderStatus)
            MiniBarChart("New users", metrics.newUsers)
        }
    }
}

@Composable
fun MiniBarChart(
    title: String,
    points: List<MetricPoint>,
    valuePrefix: String = ""
) {
    val maxValue = points.maxOfOrNull { it.value }?.takeIf { it > 0.0 } ?: 1.0
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        if (points.isEmpty()) {
            Text("No data yet", color = Color.White.copy(alpha = 0.62f), fontSize = 11.sp)
        } else {
            points.take(5).forEach { point ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = point.label.takeLast(10),
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 10.sp,
                        modifier = Modifier.width(82.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((point.value / maxValue).toFloat().coerceIn(0.05f, 1f))
                                .background(GoldenPotato, RoundedCornerShape(10.dp))
                        )
                    }
                    Text(
                        text = if (valuePrefix.isBlank()) point.value.toInt().toString() else "$valuePrefix${String.format("%.0f", point.value)}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(42.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun ImageUploadField(
    label: String,
    value: String,
    isUploading: Boolean,
    onPickImage: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, GoldenPotato.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(GoldenPotato.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = GoldenPotato, modifier = Modifier.size(21.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, color = PrimaryBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        text = if (value.isBlank()) "PNG, JPG, JPEG or WEBP. Optimized to 1200px / max 2MB." else "Ready: ${value.substringAfterLast("/")}",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (isUploading) {
                    CircularProgressIndicator(color = CanadianRed, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                }
            }
            if (value.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(138.dp)
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    DrawableImageByName(
                        resourceName = value,
                        fallbackResId = R.drawable.img_poutine_family_logo,
                        contentDescription = "$label preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlack.copy(alpha = 0.86f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = GoldenPotato, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Optimized", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .background(CreamBackground, RoundedCornerShape(18.dp))
                        .border(BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.35f)), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = GoldenPotato, modifier = Modifier.size(28.dp))
                        Text("Preview appears here", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onPickImage,
                    enabled = !isUploading,
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isUploading) {
                        Text("Optimizing...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Upload, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(
                    onClick = onClear,
                    enabled = value.isNotBlank() && !isUploading,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Clear", color = PrimaryBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminDashboardHeader(onLogout: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlack),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Card(
                    modifier = Modifier.size(width = 96.dp, height = 72.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, GoldenPotato.copy(alpha = 0.55f))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_poutine_family_logo),
                        contentDescription = "Le Poutine House logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Admin Dashboard",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Manage home sectors, menu, users, and orders.",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = GoldenPotato.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GoldenPotato.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = GoldenPotato, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Quick controls", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = CanadianRed),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Logout", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AdminDashboardScreen(
    viewModel: PoutineViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sections by viewModel.homeSections.collectAsState()
    val stats by viewModel.adminStats.collectAsState()
    val users by viewModel.adminUsers.collectAsState()
    val products by viewModel.adminProducts.collectAsState()
    val orders by viewModel.adminOrders.collectAsState()
    val coupons by viewModel.adminCoupons.collectAsState()
    val metrics by viewModel.adminMetrics.collectAsState()
    val adminUiState by viewModel.adminUiState.collectAsState()
    val savedHomeSettings by viewModel.homeSettings.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var sectionType by remember { mutableStateOf("featured") }
    var imageName by remember { mutableStateOf("img_poutine_family_logo") }
    var sortOrder by remember { mutableStateOf("1") }
    var active by remember { mutableStateOf(true) }
    var editingSection by remember { mutableStateOf<HomeSection?>(null) }
    var productName by remember { mutableStateOf("") }
    var productCategory by remember { mutableStateOf(ProductFamilyCategories.first().value) }
    var productDescription by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var productStock by remember { mutableStateOf("25") }
    var productAvailable by remember { mutableStateOf(true) }
    var productImage by remember { mutableStateOf("") }
    var editingProduct by remember { mutableStateOf<AdminProduct?>(null) }
    var couponCode by remember { mutableStateOf("") }
    var couponDescription by remember { mutableStateOf("") }
    var couponType by remember { mutableStateOf("percent") }
    var couponValue by remember { mutableStateOf("") }
    var couponActive by remember { mutableStateOf(true) }
    var editingCoupon by remember { mutableStateOf<Coupon?>(null) }
    var uploadingImageFor by remember { mutableStateOf<String?>(null) }
    var alert by remember { mutableStateOf<SweetAlertInfo?>(null) }
    var adminTab by remember { mutableStateOf("Home Content") }
    var heroLogo by remember { mutableStateOf(savedHomeSettings.heroLogo) }
    var eyebrowText by remember { mutableStateOf(savedHomeSettings.eyebrowText) }
    var brandTitle by remember { mutableStateOf(savedHomeSettings.brandTitle) }
    var familyTitle by remember { mutableStateOf(savedHomeSettings.familyTitle) }
    var familyDescription by remember { mutableStateOf(savedHomeSettings.familyDescription) }
    var featuredTitle by remember { mutableStateOf(savedHomeSettings.featuredTitle) }
    var featuredDescription by remember { mutableStateOf(savedHomeSettings.featuredDescription) }
    var featuredImage by remember { mutableStateOf(savedHomeSettings.featuredImage) }
    var houseTourTitle by remember { mutableStateOf(savedHomeSettings.houseTourTitle) }
    var houseTourDescription by remember { mutableStateOf(savedHomeSettings.houseTourDescription) }
    var expandedUserId by remember { mutableStateOf<Int?>(null) }
    var expandedOrderId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(savedHomeSettings) {
        heroLogo = savedHomeSettings.heroLogo
        eyebrowText = savedHomeSettings.eyebrowText
        brandTitle = savedHomeSettings.brandTitle
        familyTitle = savedHomeSettings.familyTitle
        familyDescription = savedHomeSettings.familyDescription
        featuredTitle = savedHomeSettings.featuredTitle
        featuredDescription = savedHomeSettings.featuredDescription
        featuredImage = savedHomeSettings.featuredImage
        houseTourTitle = savedHomeSettings.houseTourTitle
        houseTourDescription = savedHomeSettings.houseTourDescription
    }

    val sectionImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            uploadingImageFor = "section"
            viewModel.uploadImage(context.contentResolver, uri) { imagePath, error ->
                uploadingImageFor = null
                if (imagePath != null) {
                    imageName = imagePath
                    alert = SweetAlertInfo("Image uploaded", "The sector image is ready to use.", SweetAlertType.Success)
                } else {
                    alert = SweetAlertInfo("Invalid image", error ?: "Only PNG, JPG, JPEG, and WEBP images are allowed.", SweetAlertType.Error)
                }
            }
        } else {
            alert = SweetAlertInfo("No image selected", "Choose a PNG, JPG, JPEG, or WEBP image.", SweetAlertType.Warning)
        }
    }

    val productImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            uploadingImageFor = "product"
            viewModel.uploadImage(context.contentResolver, uri) { imagePath, error ->
                uploadingImageFor = null
                if (imagePath != null) {
                    productImage = imagePath
                    alert = SweetAlertInfo("Image uploaded", "The product image is ready to use.", SweetAlertType.Success)
                } else {
                    alert = SweetAlertInfo("Invalid image", error ?: "Only PNG, JPG, JPEG, and WEBP images are allowed.", SweetAlertType.Error)
                }
            }
        } else {
            alert = SweetAlertInfo("No image selected", "Choose a PNG, JPG, JPEG, or WEBP image.", SweetAlertType.Warning)
        }
    }

    val heroLogoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            uploadingImageFor = "hero"
            viewModel.uploadImage(context.contentResolver, uri) { imagePath, error ->
                uploadingImageFor = null
                if (imagePath != null) {
                    heroLogo = imagePath
                    alert = SweetAlertInfo("Logo uploaded", "Preview it, then save the home blocks.", SweetAlertType.Success)
                } else {
                    alert = SweetAlertInfo("Invalid image", error ?: "Only PNG, JPG, JPEG, and WEBP images are allowed.", SweetAlertType.Error)
                }
            }
        }
    }

    val featuredImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            uploadingImageFor = "featured"
            viewModel.uploadImage(context.contentResolver, uri) { imagePath, error ->
                uploadingImageFor = null
                if (imagePath != null) {
                    featuredImage = imagePath
                    alert = SweetAlertInfo("Featured image uploaded", "Preview it, then save the home blocks.", SweetAlertType.Success)
                } else {
                    alert = SweetAlertInfo("Invalid image", error ?: "Only PNG, JPG, JPEG, and WEBP images are allowed.", SweetAlertType.Error)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAdminData()
        alert = SweetAlertInfo(
            title = "Admin dashboard ready",
            message = "Manage home sectors, products, users, and orders from here.",
            type = SweetAlertType.Success
        )
    }

    alert?.let { info ->
        SweetAlertDialog(info = info, onDismiss = { alert = null })
    }

    fun resetForm() {
        title = ""
        description = ""
        sectionType = "featured"
        imageName = "img_poutine_family_logo"
        sortOrder = "1"
        active = true
        editingSection = null
    }

    fun resetProductForm() {
        productName = ""
        productCategory = ProductFamilyCategories.first().value
        productDescription = ""
        productPrice = ""
        productStock = "25"
        productAvailable = true
        productImage = ""
        editingProduct = null
    }

    fun resetCouponForm() {
        couponCode = ""
        couponDescription = ""
        couponType = "percent"
        couponValue = ""
        couponActive = true
        editingCoupon = null
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_dashboard_root"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 40.dp)
    ) {
        item {
            AdminDashboardHeader(onLogout = onLogout)
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item { AdminStatCard("Users", stats.totalUsers.toString(), Icons.Default.Person) }
                item { AdminStatCard("Admins", stats.adminUsers.toString(), Icons.Default.Settings) }
                item { AdminStatCard("New 7d", stats.newUsers7d.toString(), Icons.Default.TrendingUp) }
                item { AdminStatCard("Active", stats.activeUsers.toString(), Icons.Default.VerifiedUser) }
                item { AdminStatCard("Products", stats.totalProducts.toString(), Icons.Default.RestaurantMenu) }
                item { AdminStatCard("Orders", stats.totalOrders.toString(), Icons.Default.Receipt) }
                item { AdminStatCard("Revenue", String.format("$%.2f", stats.totalRevenue), Icons.Default.AttachMoney) }
            }
        }

        item {
            AdminMetricsPanel(metrics = metrics)
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(listOf("Home Content", "Menu Products", "Coupons", "Orders", "Users", "Settings")) { tab ->
                    FilterChip(
                        selected = adminTab == tab,
                        onClick = { adminTab = tab },
                        label = { Text(tab, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            val icon = when (tab) {
                                "Home Content" -> Icons.Default.Home
                                "Menu Products" -> Icons.Default.RestaurantMenu
                                "Coupons" -> Icons.Default.LocalOffer
                                "Users" -> Icons.Default.People
                                "Settings" -> Icons.Default.Settings
                                else -> Icons.Default.Receipt
                            }
                            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CanadianRed,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }
        }

        if (adminTab == "Home Content") {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Edit main page blocks", color = PrimaryBlack, fontWeight = FontWeight.Black)
                    ImageUploadField(
                        label = "Main logo",
                        value = heroLogo,
                        isUploading = uploadingImageFor == "hero",
                        onPickImage = {
                            heroLogoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onClear = { heroLogo = "" }
                    )
                    AuthTextField(value = eyebrowText, onValueChange = { eyebrowText = it }, label = "Premium text", leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = GoldenPotato) })
                    AuthTextField(value = brandTitle, onValueChange = { brandTitle = it }, label = "Brand title", leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = GoldenPotato) })
                    AuthTextField(value = familyTitle, onValueChange = { familyTitle = it }, label = "Family section title", leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null, tint = GoldenPotato) })
                    OutlinedTextField(
                        value = familyDescription,
                        onValueChange = { familyDescription = it },
                        label = { Text("Family section description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldenPotato,
                            unfocusedBorderColor = PotatoBeige.copy(alpha = 0.5f),
                            focusedLabelColor = GoldenPotato,
                            cursorColor = CanadianRed,
                            focusedTextColor = PrimaryBlack,
                            unfocusedTextColor = PrimaryBlack
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    AuthTextField(value = featuredTitle, onValueChange = { featuredTitle = it }, label = "Featured title", leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = GoldenPotato) })
                    OutlinedTextField(
                        value = featuredDescription,
                        onValueChange = { featuredDescription = it },
                        label = { Text("Featured description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldenPotato,
                            unfocusedBorderColor = PotatoBeige.copy(alpha = 0.5f),
                            focusedLabelColor = GoldenPotato,
                            cursorColor = CanadianRed,
                            focusedTextColor = PrimaryBlack,
                            unfocusedTextColor = PrimaryBlack
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    ImageUploadField(
                        label = "Featured image",
                        value = featuredImage,
                        isUploading = uploadingImageFor == "featured",
                        onPickImage = {
                            featuredImagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onClear = { featuredImage = "" }
                    )
                    AuthTextField(value = houseTourTitle, onValueChange = { houseTourTitle = it }, label = "House tour title", leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = GoldenPotato) })
                    OutlinedTextField(
                        value = houseTourDescription,
                        onValueChange = { houseTourDescription = it },
                        label = { Text("House tour text") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldenPotato,
                            unfocusedBorderColor = PotatoBeige.copy(alpha = 0.5f),
                            focusedLabelColor = GoldenPotato,
                            cursorColor = CanadianRed,
                            focusedTextColor = PrimaryBlack,
                            unfocusedTextColor = PrimaryBlack
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Button(
                        onClick = {
                            viewModel.saveHomeSettings(
                                HomeSettings(
                                    heroLogo = heroLogo.ifBlank { "img_poutine_family_logo" },
                                    eyebrowText = eyebrowText,
                                    brandTitle = brandTitle,
                                    familyTitle = familyTitle,
                                    familyDescription = familyDescription,
                                    featuredTitle = featuredTitle,
                                    featuredDescription = featuredDescription,
                                    featuredImage = featuredImage.ifBlank { "abuelos" },
                                    houseTourTitle = houseTourTitle,
                                    houseTourDescription = houseTourDescription
                                )
                            )
                            alert = SweetAlertInfo("Saving", "Home blocks are being updated.", SweetAlertType.Success)
                        },
                        enabled = !adminUiState.savingSettings,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CanadianRed),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        if (adminUiState.savingSettings) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving...", color = Color.White, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Home Blocks", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (editingSection == null) "Create home sector" else "Edit home sector",
                        color = PrimaryBlack,
                        fontWeight = FontWeight.Black
                    )
                    AuthTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Title",
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = GoldenPotato) }
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = GoldenPotato) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldenPotato,
                            unfocusedBorderColor = PotatoBeige.copy(alpha = 0.5f),
                            focusedLabelColor = GoldenPotato,
                            cursorColor = CanadianRed,
                            focusedTextColor = PrimaryBlack,
                            unfocusedTextColor = PrimaryBlack
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    AuthTextField(
                        value = sectionType,
                        onValueChange = { sectionType = it.lowercase().replace(" ", "_") },
                        label = "Sector type",
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = GoldenPotato) }
                    )
                    ImageUploadField(
                        label = "Sector image",
                        value = imageName,
                        isUploading = uploadingImageFor == "section",
                        onPickImage = {
                            sectionImagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onClear = { imageName = "" }
                    )
                    AuthTextField(
                        value = sortOrder,
                        onValueChange = { sortOrder = it.filter { char -> char.isDigit() } },
                        label = "Order",
                        leadingIcon = { Icon(Icons.Default.Sort, contentDescription = null, tint = GoldenPotato) },
                        keyboardType = KeyboardType.Number
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { active = !active }
                            .background(CreamBackground)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = active,
                            onCheckedChange = { active = it },
                            colors = CheckboxDefaults.colors(checkedColor = CanadianRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Active on home page", color = PrimaryBlack, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val order = sortOrder.toIntOrNull() ?: 0
                            if (title.trim().length < 3 || description.trim().length < 8 || sectionType.isBlank()) {
                                alert = SweetAlertInfo(
                                    title = "Missing details",
                                    message = "Add a title, sector type and a short description before publishing.",
                                    type = SweetAlertType.Warning
                                )
                            } else {
                                val current = editingSection
                                if (current == null) {
                                    viewModel.addHomeSection(
                                        title = title,
                                        description = description,
                                        sectionType = sectionType,
                                        imageName = imageName,
                                        sortOrder = order,
                                        active = active
                                    )
                                } else {
                                    viewModel.updateHomeSection(
                                        current.copy(
                                            title = title.trim(),
                                            description = description.trim(),
                                            sectionType = sectionType.trim(),
                                            imageName = imageName.trim().ifBlank { null },
                                            active = active,
                                            sortOrder = order
                                        )
                                    )
                                }
                                resetForm()
                                alert = SweetAlertInfo(
                                    title = if (current == null) "Published" else "Updated",
                                    message = "The home page sector was saved.",
                                    type = SweetAlertType.Success
                                )
                            }
                        },
                        enabled = !adminUiState.savingHome,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CanadianRed),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        if (adminUiState.savingHome) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving...", color = Color.White, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Publish, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (editingSection == null) "Create Sector" else "Save Changes",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (editingSection != null) {
                        OutlinedButton(
                            onClick = { resetForm() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text("Cancel edit", color = PrimaryBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Current sectors",
                color = PrimaryBlack,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                modifier = Modifier.padding(top = 22.dp, bottom = 8.dp)
            )
        }

        items(sections) { section ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = if (section.active) CreamBackground else Color.White),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DrawableImageByName(
                            resourceName = section.imageName ?: "img_poutine_family_logo",
                            fallbackResId = R.drawable.img_poutine_family_logo,
                            contentDescription = section.title,
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(section.title, color = PrimaryBlack, fontWeight = FontWeight.Bold)
                            Text(
                                "${section.sectionType} - order ${section.sortOrder} - ${if (section.active) "active" else "hidden"}",
                                color = CanadianRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                section.description,
                                color = Color.Gray,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 3.dp)
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                editingSection = section
                                title = section.title
                                description = section.description
                                sectionType = section.sectionType
                                imageName = section.imageName.orEmpty()
                                sortOrder = section.sortOrder.toString()
                                active = section.active
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryBlack, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit", color = PrimaryBlack, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                viewModel.deleteHomeSection(section.id)
                                if (editingSection?.id == section.id) resetForm()
                                alert = SweetAlertInfo(
                                    title = "Deleted",
                                    message = "The sector was removed from the home page.",
                                    type = SweetAlertType.Success
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CanadianRed),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        }

        if (adminTab == "Menu Products") {
        item {
            Text(
                text = "Product management",
                color = PrimaryBlack,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                modifier = Modifier.padding(top = 22.dp, bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (editingProduct == null) "Create product" else "Edit product",
                        color = PrimaryBlack,
                        fontWeight = FontWeight.Black
                    )
                    AuthTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = "Product name",
                        leadingIcon = { Icon(Icons.Default.Fastfood, contentDescription = null, tint = GoldenPotato) }
                    )
                    ProductCategoryDropdown(
                        selectedValue = productCategory,
                        onSelected = { productCategory = it }
                    )
                    AuthTextField(
                        value = productPrice,
                        onValueChange = { productPrice = it.filter { char -> char.isDigit() || char == '.' } },
                        label = "Price",
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = GoldenPotato) },
                        keyboardType = KeyboardType.Decimal
                    )
                    AuthTextField(
                        value = productStock,
                        onValueChange = { productStock = it.filter { char -> char.isDigit() } },
                        label = "Stock available",
                        leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null, tint = GoldenPotato) },
                        keyboardType = KeyboardType.Number
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CreamBackground, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Available for customers", color = PrimaryBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Turn off when the item is sold out.", color = Color.Gray, fontSize = 11.sp)
                        }
                        Switch(checked = productAvailable, onCheckedChange = { productAvailable = it })
                    }
                    ImageUploadField(
                        label = "Product image",
                        value = productImage,
                        isUploading = uploadingImageFor == "product",
                        onPickImage = {
                            productImagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onClear = { productImage = "" }
                    )
                    OutlinedTextField(
                        value = productDescription,
                        onValueChange = { productDescription = it },
                        label = { Text("Description") },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = GoldenPotato) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldenPotato,
                            unfocusedBorderColor = PotatoBeige.copy(alpha = 0.5f),
                            focusedLabelColor = GoldenPotato,
                            cursorColor = CanadianRed,
                            focusedTextColor = PrimaryBlack,
                            unfocusedTextColor = PrimaryBlack
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Button(
                        onClick = {
                            val price = productPrice.toDoubleOrNull()
                            val stock = productStock.toIntOrNull() ?: 0
                            if (productName.trim().length < 3 || productCategory.isBlank() || price == null || price <= 0) {
                                alert = SweetAlertInfo(
                                    title = "Product incomplete",
                                    message = "Add name, category and a valid price.",
                                    type = SweetAlertType.Warning
                                )
                            } else {
                                viewModel.saveAdminProduct(
                                    AdminProduct(
                                        id = editingProduct?.id ?: 0,
                                        name = productName.trim(),
                                        category = productCategory.trim(),
                                        description = productDescription.trim(),
                                        price = price,
                                        stock = stock,
                                        available = productAvailable,
                                        imageUrl = productImage.trim().ifBlank { null }
                                    )
                                )
                                resetProductForm()
                                alert = SweetAlertInfo(
                                    title = "Saved",
                                    message = "Product changes were saved.",
                                    type = SweetAlertType.Success
                                )
                            }
                        },
                        enabled = !adminUiState.savingProduct,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CanadianRed),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        if (adminUiState.savingProduct) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving...", color = Color.White, fontWeight = FontWeight.Bold)
                        } else {
                            Text(
                                text = if (editingProduct == null) "Create Product" else "Save Product",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (editingProduct != null) {
                        OutlinedButton(
                            onClick = { resetProductForm() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text("Cancel product edit", color = PrimaryBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        items(products) { product ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = CreamBackground),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, color = PrimaryBlack, fontWeight = FontWeight.Black)
                            Text(
                                "${product.category} - ${String.format("$%.2f", product.price)}",
                                color = CanadianRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Stock: ${product.stock} - ${if (product.available) "Available" else "Hidden"}",
                                color = if (product.available && product.stock > 0) Color(0xFF2E7D32) else CanadianRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                product.description,
                                color = Color.Gray,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                editingProduct = product
                                productName = product.name
                                productCategory = product.category
                                productDescription = product.description
                                productPrice = product.price.toString()
                                productStock = product.stock.toString()
                                productAvailable = product.available
                                productImage = product.imageUrl.orEmpty()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Edit", color = PrimaryBlack, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                viewModel.deleteAdminProduct(product.id)
                                if (editingProduct?.id == product.id) resetProductForm()
                                alert = SweetAlertInfo("Deleted", "Product was removed.", SweetAlertType.Success)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CanadianRed),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        }

        if (adminTab == "Coupons") {
        item {
            Text(
                text = "Coupon management",
                color = PrimaryBlack,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                modifier = Modifier.padding(top = 22.dp, bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (editingCoupon == null) "Create coupon" else "Edit coupon",
                        color = PrimaryBlack,
                        fontWeight = FontWeight.Black
                    )
                    AuthTextField(
                        value = couponCode,
                        onValueChange = { couponCode = it.uppercase().filter { char -> char.isLetterOrDigit() || char == '_' || char == '-' } },
                        label = "Code",
                        leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null, tint = GoldenPotato) }
                    )
                    AuthTextField(
                        value = couponDescription,
                        onValueChange = { couponDescription = it },
                        label = "Description",
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = GoldenPotato) }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = couponType == "percent",
                            onClick = { couponType = "percent" },
                            label = { Text("Percent") },
                            leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        FilterChip(
                            selected = couponType == "fixed",
                            onClick = { couponType = "fixed" },
                            label = { Text("Fixed") },
                            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    AuthTextField(
                        value = couponValue,
                        onValueChange = { couponValue = it.filter { char -> char.isDigit() || char == '.' } },
                        label = if (couponType == "percent") "Discount percent" else "Discount amount",
                        leadingIcon = { Icon(Icons.Default.Sell, contentDescription = null, tint = GoldenPotato) },
                        keyboardType = KeyboardType.Decimal
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CreamBackground, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Coupon active", color = PrimaryBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Switch(checked = couponActive, onCheckedChange = { couponActive = it })
                    }
                    Button(
                        onClick = {
                            val value = couponValue.toDoubleOrNull()
                            if (couponCode.trim().length < 3 || value == null || value <= 0) {
                                alert = SweetAlertInfo("Coupon incomplete", "Add a code and valid discount.", SweetAlertType.Warning)
                            } else {
                                viewModel.saveAdminCoupon(
                                    Coupon(
                                        id = editingCoupon?.id ?: 0,
                                        code = couponCode.trim(),
                                        description = couponDescription.trim(),
                                        discountType = couponType,
                                        discountValue = value,
                                        active = couponActive
                                    )
                                )
                                resetCouponForm()
                                alert = SweetAlertInfo("Saved", "Coupon changes were saved.", SweetAlertType.Success)
                            }
                        },
                        enabled = !adminUiState.savingCoupon,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CanadianRed),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        if (adminUiState.savingCoupon) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving...", color = Color.White, fontWeight = FontWeight.Bold)
                        } else {
                            Text(if (editingCoupon == null) "Create Coupon" else "Save Coupon", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (editingCoupon != null) {
                        OutlinedButton(
                            onClick = { resetCouponForm() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text("Cancel coupon edit", color = PrimaryBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        items(coupons) { coupon ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = CreamBackground),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalOffer, contentDescription = null, tint = if (coupon.active) CanadianRed else Color.Gray)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(coupon.code, color = PrimaryBlack, fontWeight = FontWeight.Black)
                            Text(
                                if (coupon.discountType == "percent") "${coupon.discountValue.toInt()}% off" else String.format("$%.2f off", coupon.discountValue),
                                color = CanadianRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(coupon.description.ifBlank { "No description" }, color = Color.Gray, fontSize = 12.sp)
                        }
                        Text(if (coupon.active) "ACTIVE" else "OFF", color = if (coupon.active) Color(0xFF2E7D32) else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                editingCoupon = coupon
                                couponCode = coupon.code
                                couponDescription = coupon.description
                                couponType = coupon.discountType
                                couponValue = coupon.discountValue.toString()
                                couponActive = coupon.active
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Edit", color = PrimaryBlack, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                viewModel.deleteAdminCoupon(coupon.id)
                                if (editingCoupon?.id == coupon.id) resetCouponForm()
                                alert = SweetAlertInfo("Deleted", "Coupon was removed.", SweetAlertType.Success)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CanadianRed),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        }

        if (adminTab == "Users") {
        item {
            Text(
                text = if (adminUiState.loading) "Loading users..." else "Registered users",
                color = PrimaryBlack,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                modifier = Modifier.padding(top = 22.dp, bottom = 8.dp)
            )
        }

        items(users) { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable { expandedUserId = if (expandedUserId == user.id) null else user.id },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = if (user.active) CanadianRed else Color.Gray)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.name, color = PrimaryBlack, fontWeight = FontWeight.Bold)
                            Text(user.email, color = Color.Gray, fontSize = 12.sp)
                            Text("Joined ${user.createdAt.take(10)}", color = Color.Gray, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(user.role.uppercase(), color = GoldenPotato, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Text(if (user.active) "ACTIVE" else "DISABLED", color = if (user.active) Color(0xFF2E7D32) else CanadianRed, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    if (expandedUserId == user.id) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            FilterChip(
                                selected = user.role == "admin",
                                onClick = { viewModel.updateAdminUser(user, role = if (user.role == "admin") "customer" else "admin") },
                                enabled = adminUiState.savingUserId != user.id,
                                label = { Text(if (user.role == "admin") "Make customer" else "Make admin", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                            FilterChip(
                                selected = user.active,
                                onClick = { viewModel.updateAdminUser(user, active = !user.active) },
                                enabled = adminUiState.savingUserId != user.id,
                                label = { Text(if (user.active) "Disable" else "Enable", fontSize = 11.sp) },
                                leadingIcon = {
                                    if (adminUiState.savingUserId == user.id) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        }

        if (adminTab == "Orders") {
        item {
            Text(
                text = "Orders",
                color = PrimaryBlack,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                modifier = Modifier.padding(top = 22.dp, bottom = 8.dp)
            )
        }

        items(orders) { order ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable { expandedOrderId = if (expandedOrderId == order.id) null else order.id },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("#${order.id} ${order.customerName}", color = PrimaryBlack, fontWeight = FontWeight.Bold)
                        Text(String.format("$%.2f", order.total), color = CanadianRed, fontWeight = FontWeight.Black)
                    }
                    Text("${order.customerPhone} - ${order.status}", color = Color.Gray, fontSize = 12.sp)
                    if (order.deliveryAddress.isNotBlank()) {
                        Text("Deliver to: ${order.deliveryAddress}", color = PrimaryBlack, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (!order.couponCode.isNullOrBlank() && order.discount > 0) {
                        Text("Coupon ${order.couponCode}: -${String.format("$%.2f", order.discount)}", color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    if (expandedOrderId == order.id) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CreamBackground, RoundedCornerShape(14.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Order details", color = PrimaryBlack, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            if (order.items.isEmpty()) {
                                Text("No item details saved for this order.", color = Color.Gray, fontSize = 12.sp)
                            } else {
                                order.items.forEach { item ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${item.quantity}x ${item.productName}", color = PrimaryBlack, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                        Text(String.format("$%.2f", item.price * item.quantity), color = CanadianRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        listOf("pending", "preparing", "ready", "delivered", "cancelled").forEach { status ->
                            FilterChip(
                                selected = order.status == status,
                                enabled = adminUiState.savingOrderId != order.id,
                                onClick = { viewModel.updateOrderStatus(order.id, status) },
                                label = { Text(status.replaceFirstChar { it.uppercase() }, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }
        }

        if (adminTab == "Settings") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Settings", color = PrimaryBlack, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("Backend URL: http://192.168.3.140:3000", color = Color.Gray, fontSize = 12.sp)
                        Text("Allowed image formats: PNG, JPG, JPEG, WEBP.", color = Color.Gray, fontSize = 12.sp)
                        Text("New users this week: ${stats.newUsers7d}", color = PrimaryBlack, fontWeight = FontWeight.Bold)
                        Text("Active users: ${stats.activeUsers}", color = PrimaryBlack, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { viewModel.loadAdminData() },
                            enabled = !adminUiState.loading,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (adminUiState.loading) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Loading...", color = Color.White, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Refresh dashboard", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyOrdersScreen(viewModel: PoutineViewModel) {
    val orders by viewModel.customerOrders.collectAsState()
    val authState by viewModel.authState.collectAsState()
    var expandedOrderId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            viewModel.refreshCustomerOrders()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("my_orders_screen_root"),
        contentPadding = PaddingValues(24.dp)
    ) {
        item {
            Text(
                text = "My Orders",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = PrimaryBlack
            )
            Text(
                text = "Track each feast from pending to delivered.",
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )
        }

        if (authState !is AuthState.Authenticated) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = CanadianRed, modifier = Modifier.size(34.dp))
                        Text("Login required", color = PrimaryBlack, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp))
                        Text(
                            "Sign in so we can show your saved cart and order history.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else if (orders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = GoldenPotato, modifier = Modifier.size(42.dp))
                        Text("No orders yet", color = PrimaryBlack, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp))
                        Text("Your next poutine order will appear here.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(orders) { order ->
                CustomerOrderCard(
                    order = order,
                    expanded = expandedOrderId == order.id,
                    onToggle = { expandedOrderId = if (expandedOrderId == order.id) null else order.id }
                )
            }
        }
    }
}

@Composable
fun CustomerOrderCard(order: AdminOrder, expanded: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(CanadianRed.copy(alpha = 0.10f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = CanadianRed)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Order #${order.id}", color = PrimaryBlack, fontWeight = FontWeight.Black)
                    Text(order.createdAt.take(10), color = Color.Gray, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(String.format("$%.2f", order.total), color = CanadianRed, fontWeight = FontWeight.Black)
                    Text(order.status.uppercase(), color = GoldenPotato, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }

            OrderStatusTimeline(order.status)

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CreamBackground, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    if (order.deliveryAddress.isNotBlank()) {
                        Text("Delivery address", color = PrimaryBlack, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        Text(order.deliveryAddress, color = Color.Gray, fontSize = 12.sp)
                    }
                    if (!order.couponCode.isNullOrBlank() && order.discount > 0) {
                        Text("Coupon ${order.couponCode}: -${String.format("$%.2f", order.discount)}", color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Items", color = PrimaryBlack, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    order.items.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${item.quantity}x ${item.productName}", color = PrimaryBlack, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text(String.format("$%.2f", item.price * item.quantity), color = CanadianRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusTimeline(status: String) {
    val steps = listOf(
        "pending" to Icons.Default.ReceiptLong,
        "preparing" to Icons.Default.Restaurant,
        "ready" to Icons.Default.TaskAlt,
        "delivered" to Icons.Default.Home
    )
    val activeIndex = steps.indexOfFirst { it.first == status }.coerceAtLeast(0)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            steps.forEachIndexed { index, step ->
                val active = index <= activeIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(7.dp)
                        .background(if (active) CanadianRed else PotatoBeige.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            steps.forEachIndexed { index, step ->
                val active = index <= activeIndex
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Icon(step.second, contentDescription = null, tint = if (active) CanadianRed else Color.Gray, modifier = Modifier.size(16.dp))
                    Text(step.first.replaceFirstChar { it.uppercase() }, color = if (active) PrimaryBlack else Color.Gray, fontSize = 9.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun LegacyOrderStatusTimeline(status: String) {
    val steps = listOf("pending", "preparing", "ready", "delivered")
    val activeIndex = steps.indexOf(status).coerceAtLeast(0)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        steps.forEachIndexed { index, step ->
            val active = index <= activeIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(7.dp)
                    .background(if (active) CanadianRed else PotatoBeige.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
            )
        }
    }
}

// 7. House Tour Screen
@Composable
fun HouseTourScreen() {
    val sections = listOf(
        Triple("Front Door", "A warm Canadian welcome with the family logo, maple details, and the smell of gravy at the door.", Icons.Default.Home),
        Triple("Kitchen", "Fresh fries, cheese curds, and house gravy prepared where the full family recipes come alive.", Icons.Default.Restaurant),
        Triple("Dining Room", "Comfortable tables for family combos, kids poutines, and long Sunday meals.", Icons.Default.RestaurantMenu),
        Triple("Family Wall", "Portraits of Mr. Poutine, Mrs. Poutine, Grandpa, Grandma, and the Little Spuds.", Icons.Default.HistoryEdu),
        Triple("Pickup Counter", "Quick orders, delivery bags, and hot bowls ready for the road.", Icons.Default.ShoppingCart)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("house_tour_screen_root"),
        contentPadding = PaddingValues(24.dp)
    ) {
        item {
            Text(
                text = "House Tour",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = PrimaryBlack
            )
            Text(
                text = "Explore every warm corner of Le Poutine House before you order.",
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.35f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_poutine_hero),
                        contentDescription = "Le Poutine House tour hero",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, PrimaryBlack.copy(alpha = 0.72f))
                                )
                            )
                    )
                    Text(
                        text = "Poutine made right, room by room.",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    )
                }
            }
        }

        items(sections) { section ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 7.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(22.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(GoldenPotato.copy(alpha = 0.14f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(section.third, contentDescription = null, tint = CanadianRed, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = section.first,
                            color = PrimaryBlack,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Text(
                            text = section.second,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

// 7. Contact & Location Screen
@Composable
fun ContactScreen() {
    val context = LocalContext.current
    val address = "4242 Rue St-Denis, Montreal, QC H2J 2K8"
    val phoneNum = "+1 (514) 555-0199"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("contact_screen_root"),
        contentPadding = PaddingValues(24.dp)
    ) {
        item {
            Text(
                text = "Find Us & Contact",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = PrimaryBlack,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Stop by our Montreal store or give us a buzz for family catering!",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Map mockup made completely with Compose Canvas with Theme Colors!
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.3f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Background map coloring (CreamBackground)
                        drawRect(color = CreamBackground)

                        // Saint Lawrence River (Blue Ribbon curving at bottom)
                        val riverPath = Path().apply {
                            moveTo(0f, h * 0.75f)
                            cubicTo(w * 0.3f, h * 0.65f, w * 0.6f, h * 0.9f, w, h * 0.8f)
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(riverPath, color = Color(0xFFA2D2FF))

                        // Grid roads lines
                        drawLine(Color.White, Offset(w * 0.15f, 0f), Offset(w * 0.15f, h), strokeWidth = 14f)
                        drawLine(Color.White, Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), strokeWidth = 16f) // Rue St-Denis
                        drawLine(Color.White, Offset(w * 0.82f, 0f), Offset(w * 0.82f, h), strokeWidth = 12f)

                        drawLine(Color.White, Offset(0f, h * 0.25f), Offset(w, h * 0.25f), strokeWidth = 14f)
                        drawLine(Color.White, Offset(0f, h * 0.52f), Offset(w, h * 0.52f), strokeWidth = 15f)

                        // Central Red Destination Pin Location
                        val pinCx = w * 0.5f
                        val pinCy = h * 0.52f

                        // Draw golden pulse circle
                        drawCircle(
                            color = GoldenPotato.copy(alpha = 0.4f),
                            radius = 35f,
                            center = Offset(pinCx, pinCy)
                        )

                        // Draw Pin Head
                        drawCircle(
                            color = CanadianRed,
                            radius = 12f,
                            center = Offset(pinCx, pinCy - 15f)
                        )
                        // Pin sharp bottom
                        val pinTail = Path().apply {
                            moveTo(pinCx - 11f, pinCy - 13f)
                            lineTo(pinCx + 11f, pinCy - 13f)
                            lineTo(pinCx, pinCy)
                            close()
                        }
                        drawPath(pinTail, color = CanadianRed)

                        // Inner dot center pin of White
                        drawCircle(
                            color = Color.White,
                            radius = 4.5f,
                            center = Offset(pinCx, pinCy - 15f)
                        )
                    }

                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "📍 Le Poutine House Montreal Pin",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlack,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Contact particulars
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        try {
                            val msg = "Geo navigation is a visual simulation"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {}
                    },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, "Location pin tag", tint = CanadianRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Our House Address", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlack)
                        Text(address, color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNum"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Call simulated to $phoneNum!", Toast.LENGTH_SHORT).show()
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, "Telephone receiver icon", tint = CanadianRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Phone Hot-Line (Tap to Dial)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlack)
                        Text(phoneNum, color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PotatoBeige.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, "Working calendar hours", tint = CanadianRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Family Opening Hours", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlack)
                        Text("Mon - Thu: 11:00 AM - 10:00 PM\nFri - Sat: 11:00 AM - Midnight\nSunday: Noon - 9:00 PM", color = Color.Gray, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

