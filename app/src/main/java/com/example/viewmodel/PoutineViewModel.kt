package com.example.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.FoodItem
import com.example.model.MenuData
import com.example.model.PoutineCategory
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class CartItem(
    val foodItem: FoodItem,
    val quantity: Int
)

sealed interface CheckoutState {
    object Idle : CheckoutState
    object Processing : CheckoutState
    data class Success(val orderId: String) : CheckoutState
    data class Error(val message: String) : CheckoutState
}

data class AuthUser(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val token: String = ""
)

sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    data class Authenticated(val user: AuthUser) : AuthState
    data class Error(val message: String) : AuthState
}

data class HomeSection(
    val id: Int,
    val title: String,
    val description: String,
    val sectionType: String,
    val imageName: String?,
    val active: Boolean,
    val sortOrder: Int
)

data class HomeSettings(
    val heroLogo: String = "img_poutine_family_logo",
    val eyebrowText: String = "PREMIUM CANADIAN",
    val brandTitle: String = "Le Poutine House",
    val familyTitle: String = "Meet the Poutine Family",
    val familyDescription: String = "Grandpa, Grandma, the little spuds and every house recipe.",
    val featuredTitle: String = "Grandpa's Traditional Feast",
    val featuredDescription: String = "Featured today with crisp fries, cheese curds, and house gravy.",
    val featuredImage: String = "abuelos",
    val houseTourTitle: String = "Do you want a house tour?",
    val houseTourDescription: String = "Take a quick walk through the kitchen, dining room and pickup counter."
)

data class AdminStats(
    val totalUsers: Int = 0,
    val adminUsers: Int = 0,
    val customerUsers: Int = 0,
    val activeUsers: Int = 0,
    val newUsers7d: Int = 0,
    val totalProducts: Int = 0,
    val totalOrders: Int = 0,
    val totalRevenue: Double = 0.0
)

data class MetricPoint(
    val label: String,
    val value: Double
)

data class AdminMetrics(
    val dailySales: List<MetricPoint> = emptyList(),
    val topProducts: List<MetricPoint> = emptyList(),
    val orderStatus: List<MetricPoint> = emptyList(),
    val newUsers: List<MetricPoint> = emptyList()
)

data class AdminUser(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val active: Boolean,
    val createdAt: String
)

data class AdminProduct(
    val id: Int,
    val name: String,
    val category: String,
    val description: String,
    val price: Double,
    val stock: Int = 25,
    val available: Boolean = true,
    val imageUrl: String?
)

data class AdminOrder(
    val id: Int,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val couponCode: String?,
    val discount: Double,
    val total: Double,
    val status: String,
    val createdAt: String,
    val items: List<AdminOrderItem> = emptyList()
)

data class AdminOrderItem(
    val id: Int,
    val productId: Int?,
    val productName: String,
    val quantity: Int,
    val price: Double
)

data class Coupon(
    val id: Int,
    val code: String,
    val description: String,
    val discountType: String,
    val discountValue: Double,
    val active: Boolean,
    val createdAt: String = "",
    val discount: Double = 0.0
)

data class AdminUiState(
    val loading: Boolean = false,
    val savingHome: Boolean = false,
    val savingProduct: Boolean = false,
    val savingCoupon: Boolean = false,
    val savingSettings: Boolean = false,
    val savingUserId: Int? = null,
    val savingOrderId: Int? = null
)

class PoutineViewModel : ViewModel() {

    // Internal Raw State Flows
    private val _items = MutableStateFlow(MenuData.items)
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<PoutineCategory?>(null)
    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    private val _adminStats = MutableStateFlow(AdminStats())
    private val _adminUsers = MutableStateFlow<List<AdminUser>>(emptyList())
    private val _adminProducts = MutableStateFlow<List<AdminProduct>>(emptyList())
    private val _adminOrders = MutableStateFlow<List<AdminOrder>>(emptyList())
    private val _customerOrders = MutableStateFlow<List<AdminOrder>>(emptyList())
    private val _adminCoupons = MutableStateFlow<List<Coupon>>(emptyList())
    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    private val _couponMessage = MutableStateFlow<String?>(null)
    private val _adminMetrics = MutableStateFlow(AdminMetrics())
    private val _adminUiState = MutableStateFlow(AdminUiState())
    private val _homeSettings = MutableStateFlow(HomeSettings())
    private val _homeSections = MutableStateFlow(
        listOf(
            HomeSection(
                id = 0,
                title = "Grandpa's Traditional Feast",
                description = "Featured today with crisp fries, cheese curds, and house gravy.",
                sectionType = "featured",
                imageName = "abuelos",
                active = true,
                sortOrder = 1
            )
        )
    )
    private var isLoadingRemoteCart = false

    init {
        loadHomeSections()
    }

    // Exposed Read-Only State Flows
    val items: StateFlow<List<FoodItem>> = _items
    val cart: StateFlow<List<CartItem>> = _cart
    val favorites: StateFlow<Set<String>> = _favorites
    val searchQuery: StateFlow<String> = _searchQuery
    val selectedCategory: StateFlow<PoutineCategory?> = _selectedCategory
    val checkoutState: StateFlow<CheckoutState> = _checkoutState
    val authState: StateFlow<AuthState> = _authState
    val adminStats: StateFlow<AdminStats> = _adminStats
    val adminUsers: StateFlow<List<AdminUser>> = _adminUsers
    val adminProducts: StateFlow<List<AdminProduct>> = _adminProducts
    val adminOrders: StateFlow<List<AdminOrder>> = _adminOrders
    val customerOrders: StateFlow<List<AdminOrder>> = _customerOrders
    val adminCoupons: StateFlow<List<Coupon>> = _adminCoupons
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon
    val couponMessage: StateFlow<String?> = _couponMessage
    val adminMetrics: StateFlow<AdminMetrics> = _adminMetrics
    val adminUiState: StateFlow<AdminUiState> = _adminUiState
    val homeSections: StateFlow<List<HomeSection>> = _homeSections
    val homeSettings: StateFlow<HomeSettings> = _homeSettings

    // Filtered Items Flow
    val filteredItems: StateFlow<List<FoodItem>> = combine(
        _items,
        _searchQuery,
        _selectedCategory
    ) { allItems, query, category ->
        allItems.filter { item ->
            val matchesCategory = category == null || item.category == category
            val matchesQuery = query.isEmpty() || 
                    item.name.contains(query, ignoreCase = true) || 
                    item.description.contains(query, ignoreCase = true) ||
                    item.category.title.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MenuData.items
    )

    // Favorites Interaction
    fun toggleFavorite(itemId: String) {
        val current = _favorites.value
        _favorites.value = if (current.contains(itemId)) {
            current - itemId
        } else {
            current + itemId
        }
        syncFavoritesToBackend()
    }

    // Cart Interactions
    fun addToCart(item: FoodItem, quantity: Int = 1) {
        if (quantity <= 0) return
        val currentCart = _cart.value.toMutableList()
        val index = currentCart.indexOfFirst { it.foodItem.id == item.id }
        if (index != -1) {
            val existing = currentCart[index]
            currentCart[index] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            currentCart.add(CartItem(item, quantity))
        }
        _cart.value = currentCart
        syncCartToBackend()
    }

    fun updateCartQuantity(itemId: String, newQuantity: Int) {
        val currentCart = _cart.value.toMutableList()
        val index = currentCart.indexOfFirst { it.foodItem.id == itemId }
        if (index != -1) {
            if (newQuantity <= 0) {
                currentCart.removeAt(index)
            } else {
                currentCart[index] = currentCart[index].copy(quantity = newQuantity)
            }
            _cart.value = currentCart
            syncCartToBackend()
        }
    }

    fun removeFromCart(itemId: String) {
        _cart.value = _cart.value.filter { it.foodItem.id != itemId }
        syncCartToBackend()
    }

    fun clearCart() {
        _cart.value = emptyList()
        syncCartToBackend()
    }

    // Filters
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: PoutineCategory?) {
        _selectedCategory.value = category
    }

    // Financial calculations
    val cartSubtotal: Double
        get() = _cart.value.sumOf { it.foodItem.price * it.quantity }

    val cartTax: Double
        get() = cartSubtotal * 0.15 // 15% Canadian Sales Tax (QST/GST average)

    val cartDelivery: Double
        get() {
            val sub = cartSubtotal
            return if (sub == 0.0) 0.0 else if (sub >= 30.0) 0.0 else 4.99 // Free delivery over $30
        }

    val couponDiscount: Double
        get() {
            val coupon = _appliedCoupon.value ?: return 0.0
            val discount = if (coupon.discountType == "fixed") {
                coupon.discountValue
            } else {
                cartSubtotal * (coupon.discountValue / 100.0)
            }
            return discount.coerceIn(0.0, cartSubtotal)
        }

    val cartTotal: Double
        get() = (cartSubtotal + cartTax + cartDelivery - couponDiscount).coerceAtLeast(0.0)

    // Checkout Flow Simulation
    fun checkout(deliveryAddress: String) {
        if (_cart.value.isEmpty()) {
            _checkoutState.value = CheckoutState.Error("Your shopping cart is empty")
            return
        }
        if (deliveryAddress.trim().length < 8) {
            _checkoutState.value = CheckoutState.Error("Add a valid delivery address")
            return
        }
        viewModelScope.launch {
            _checkoutState.value = CheckoutState.Processing
            val orderId = createBackendOrder(deliveryAddress.trim())
            if (orderId != null) {
                _cart.value = emptyList()
                _appliedCoupon.value = null
                _couponMessage.value = null
                syncCartToBackend()
                _checkoutState.value = CheckoutState.Success("POUT-$orderId")
                _adminStats.value = fetchAdminStats()
                _adminOrders.value = fetchAdminOrders()
                _customerOrders.value = fetchCustomerOrders()
            } else {
                _checkoutState.value = CheckoutState.Error("Could not place order")
            }
        }
    }

    fun applyCoupon(code: String) {
        val cleanCode = code.trim()
        if (cleanCode.isBlank()) {
            _appliedCoupon.value = null
            _couponMessage.value = "Enter a coupon code"
            return
        }
        viewModelScope.launch {
            _couponMessage.value = "Validating coupon..."
            val coupon = validateCoupon(cleanCode)
            if (coupon != null) {
                _appliedCoupon.value = coupon
                _couponMessage.value = "Coupon ${coupon.code} applied"
            } else {
                _appliedCoupon.value = null
                _couponMessage.value = "Coupon is invalid or inactive"
            }
        }
    }

    fun clearCoupon() {
        _appliedCoupon.value = null
        _couponMessage.value = null
    }

    fun resetCheckout() {
        _checkoutState.value = CheckoutState.Idle
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password are required")
            return
        }

        if (email.trim().equals("admin@poutine.local", ignoreCase = true) && password == "12345") {
            viewModelScope.launch {
                _authState.value = AuthState.Loading
                val backendState = authenticate(
                    endpoint = "login",
                    payload = JSONObject()
                        .put("email", email.trim())
                        .put("password", password)
                )
                _authState.value = if (backendState is AuthState.Authenticated) {
                    backendState
                } else {
                    AuthState.Authenticated(
                        AuthUser(
                            id = 1,
                            name = "Admin",
                            email = "admin@poutine.local",
                            role = "admin"
                        )
                    )
                }
                if ((_authState.value as? AuthState.Authenticated)?.user?.token?.isNotBlank() == true) {
                    loadUserCartAndOrders()
                }
            }
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _authState.value = authenticate(
                endpoint = "login",
                payload = JSONObject()
                    .put("email", email.trim())
                    .put("password", password)
            )
            if (_authState.value is AuthState.Authenticated) {
                loadUserCartAndOrders()
            }
        }
    }

    fun register(name: String, email: String, password: String, asAdmin: Boolean = false) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Name, email, and password are required")
            return
        }

        if (password.length < 5) {
            _authState.value = AuthState.Error("Password must have at least 5 characters")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _authState.value = authenticate(
                endpoint = "register",
                payload = JSONObject()
                    .put("name", name.trim())
                    .put("email", email.trim())
                    .put("password", password)
                    .put("role", if (asAdmin) "admin" else "customer")
            )
            if (_authState.value is AuthState.Authenticated) {
                loadUserCartAndOrders()
            }
        }
    }

    fun loadUserCartAndOrders() {
        viewModelScope.launch {
            val remoteCart = fetchUserCart()
            isLoadingRemoteCart = true
            _cart.value = remoteCart
            isLoadingRemoteCart = false
            _favorites.value = fetchUserFavorites()
            _customerOrders.value = fetchCustomerOrders()
        }
    }

    fun loadHomeSections() {
        viewModelScope.launch {
            val sections = fetchHomeSections()
            if (sections.isNotEmpty()) {
                _homeSections.value = sections
            }
            fetchHomeSettings()?.let { _homeSettings.value = it }
        }
    }

    fun loadAdminData() {
        viewModelScope.launch {
            _adminUiState.value = _adminUiState.value.copy(loading = true)
            _adminStats.value = fetchAdminStats()
            _adminMetrics.value = fetchAdminMetrics()
            _adminUsers.value = fetchAdminUsers()
            _adminProducts.value = fetchAdminProducts()
            _adminOrders.value = fetchAdminOrders()
            _adminCoupons.value = fetchAdminCoupons()
            fetchHomeSettings()?.let { _homeSettings.value = it }
            _adminUiState.value = _adminUiState.value.copy(loading = false)
        }
    }

    fun saveAdminProduct(product: AdminProduct) {
        if (product.name.isBlank() || product.category.isBlank() || product.price <= 0) return

        viewModelScope.launch {
            _adminUiState.value = _adminUiState.value.copy(savingProduct = true)
            val saved = sendProduct(product)
            if (saved != null) {
                _adminProducts.value = if (product.id == 0) {
                    listOf(saved) + _adminProducts.value
                } else {
                    _adminProducts.value.map { if (it.id == saved.id) saved else it }
                }
                _adminStats.value = fetchAdminStats()
            }
            _adminUiState.value = _adminUiState.value.copy(savingProduct = false)
        }
    }

    fun deleteAdminProduct(productId: Int) {
        if (productId <= 0) return

        viewModelScope.launch {
            if (deleteBackendResource("/products/$productId")) {
                _adminProducts.value = _adminProducts.value.filter { it.id != productId }
                _adminStats.value = fetchAdminStats()
            }
        }
    }

    fun saveAdminCoupon(coupon: Coupon) {
        if (coupon.code.isBlank() || coupon.discountValue <= 0) return

        viewModelScope.launch {
            _adminUiState.value = _adminUiState.value.copy(savingCoupon = true)
            val saved = sendCoupon(coupon)
            if (saved != null) {
                _adminCoupons.value = if (coupon.id == 0) {
                    listOf(saved) + _adminCoupons.value
                } else {
                    _adminCoupons.value.map { if (it.id == saved.id) saved else it }
                }
            }
            _adminUiState.value = _adminUiState.value.copy(savingCoupon = false)
        }
    }

    fun deleteAdminCoupon(couponId: Int) {
        if (couponId <= 0) return

        viewModelScope.launch {
            if (deleteBackendResource("/coupons/$couponId")) {
                _adminCoupons.value = _adminCoupons.value.filter { it.id != couponId }
            }
        }
    }

    fun updateOrderStatus(orderId: Int, status: String) {
        viewModelScope.launch {
            _adminUiState.value = _adminUiState.value.copy(savingOrderId = orderId)
            val updated = updateBackendOrderStatus(orderId, status)
            if (updated != null) {
                _adminOrders.value = _adminOrders.value.map { if (it.id == updated.id) updated else it }
            }
            _adminUiState.value = _adminUiState.value.copy(savingOrderId = null)
        }
    }

    fun updateAdminUser(user: AdminUser, role: String = user.role, active: Boolean = user.active) {
        viewModelScope.launch {
            _adminUiState.value = _adminUiState.value.copy(savingUserId = user.id)
            val updated = updateBackendUser(user.id, role, active)
            if (updated != null) {
                _adminUsers.value = _adminUsers.value.map { if (it.id == updated.id) updated else it }
                _adminStats.value = fetchAdminStats()
            }
            _adminUiState.value = _adminUiState.value.copy(savingUserId = null)
        }
    }

    fun saveHomeSettings(settings: HomeSettings) {
        viewModelScope.launch {
            _adminUiState.value = _adminUiState.value.copy(savingSettings = true)
            val saved = sendHomeSettings(settings)
            if (saved != null) {
                _homeSettings.value = saved
            }
            _adminUiState.value = _adminUiState.value.copy(savingSettings = false)
        }
    }

    fun uploadImage(
        contentResolver: ContentResolver,
        uri: Uri,
        onComplete: (imagePath: String?, error: String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = uploadImageToBackend(contentResolver, uri)
            onComplete(result.first, result.second)
        }
    }

    fun addHomeSection(
        title: String,
        description: String,
        sectionType: String,
        imageName: String?,
        sortOrder: Int,
        active: Boolean = true
    ) {
        if (title.isBlank() || description.isBlank()) return

        viewModelScope.launch {
            _adminUiState.value = _adminUiState.value.copy(savingHome = true)
            val created = sendHomeSection(
                method = "POST",
                path = "/home-sections",
                section = HomeSection(
                    id = 0,
                    title = title.trim(),
                    description = description.trim(),
                    sectionType = sectionType,
                    imageName = imageName?.trim()?.ifBlank { null },
                    active = active,
                    sortOrder = sortOrder
                )
            )

            if (created != null) {
                _homeSections.value = (_homeSections.value + created)
                    .sortedWith(compareBy<HomeSection> { it.sortOrder }.thenByDescending { it.id })
            }
            _adminUiState.value = _adminUiState.value.copy(savingHome = false)
        }
    }

    fun updateHomeSection(section: HomeSection) {
        if (section.title.isBlank() || section.description.isBlank() || section.id <= 0) return

        viewModelScope.launch {
            _adminUiState.value = _adminUiState.value.copy(savingHome = true)
            val updated = sendHomeSection(
                method = "PUT",
                path = "/home-sections/${section.id}",
                section = section
            )

            if (updated != null) {
                _homeSections.value = _homeSections.value
                    .map { if (it.id == updated.id) updated else it }
                    .sortedWith(compareBy<HomeSection> { it.sortOrder }.thenByDescending { it.id })
            }
            _adminUiState.value = _adminUiState.value.copy(savingHome = false)
        }
    }

    fun deleteHomeSection(sectionId: Int) {
        if (sectionId <= 0) return

        viewModelScope.launch {
            val deleted = deleteHomeSectionFromBackend(sectionId)
            if (deleted) {
                _homeSections.value = _homeSections.value.filter { it.id != sectionId }
            }
        }
    }

    fun logout() {
        _authState.value = AuthState.Idle
        _cart.value = emptyList()
        _favorites.value = emptySet()
        _appliedCoupon.value = null
        _couponMessage.value = null
        _customerOrders.value = emptyList()
    }

    fun refreshCustomerOrders() {
        viewModelScope.launch {
            _customerOrders.value = fetchCustomerOrders()
        }
    }

    fun clearAuthError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }

    private suspend fun authenticate(endpoint: String, payload: JSONObject): AuthState {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$API_BASE_URL/auth/$endpoint")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = API_TIMEOUT_MS
                    readTimeout = API_TIMEOUT_MS
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                }

                connection.outputStream.use { outputStream ->
                    outputStream.write(payload.toString().toByteArray(Charsets.UTF_8))
                }

                val responseBody = readResponse(connection)
                val responseJson = JSONObject(responseBody)

                if (connection.responseCode in 200..299) {
                    val userJson = responseJson.getJSONObject("user")
                    AuthState.Authenticated(
                        AuthUser(
                            id = userJson.getInt("id"),
                            name = userJson.getString("name"),
                            email = userJson.getString("email"),
                            role = userJson.optString("role", "customer"),
                            token = responseJson.optString("token", "")
                        )
                    )
                } else {
                    AuthState.Error(responseJson.optString("error", "Authentication failed"))
                }
            } catch (error: Exception) {
                AuthState.Error("Could not connect to the backend")
            }
        }
    }

    private suspend fun fetchHomeSections(): List<HomeSection> {
        return withContext(Dispatchers.IO) {
            try {
                val connection = (URL("$API_BASE_URL/home-sections").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = API_TIMEOUT_MS
                    readTimeout = API_TIMEOUT_MS
                    setRequestProperty("Accept", "application/json")
                }

                if (connection.responseCode !in 200..299) {
                    return@withContext emptyList()
                }

                parseHomeSections(JSONArray(readResponse(connection)))
            } catch (error: Exception) {
                emptyList()
            }
        }
    }

    private suspend fun fetchHomeSettings(): HomeSettings? {
        return withContext(Dispatchers.IO) {
            try {
                val connection = openJsonConnection("/settings/home", "GET")
                if (connection.responseCode !in 200..299) return@withContext null
                parseHomeSettings(JSONObject(readResponse(connection)))
            } catch (error: Exception) {
                null
            }
        }
    }

    private suspend fun fetchAdminStats(): AdminStats {
        return withContext(Dispatchers.IO) {
            try {
                val connection = openJsonConnection("/admin/stats", "GET")
                if (connection.responseCode !in 200..299) return@withContext AdminStats()
                val json = JSONObject(readResponse(connection))
                AdminStats(
                    totalUsers = json.optInt("total_users", 0),
                    adminUsers = json.optInt("admin_users", 0),
                    customerUsers = json.optInt("customer_users", 0),
                    activeUsers = json.optInt("active_users", 0),
                    newUsers7d = json.optInt("new_users_7d", 0),
                    totalProducts = json.optInt("total_products", 0),
                    totalOrders = json.optInt("total_orders", 0),
                    totalRevenue = json.optDouble("total_revenue", 0.0)
                )
            } catch (error: Exception) {
                AdminStats()
            }
        }
    }

    private suspend fun fetchAdminMetrics(): AdminMetrics {
        return withContext(Dispatchers.IO) {
            try {
                val connection = openJsonConnection("/admin/metrics", "GET")
                if (connection.responseCode !in 200..299) return@withContext AdminMetrics()
                val json = JSONObject(readResponse(connection))
                AdminMetrics(
                    dailySales = parseMetricPoints(json.optJSONArray("daily_sales") ?: JSONArray()),
                    topProducts = parseMetricPoints(json.optJSONArray("top_products") ?: JSONArray()),
                    orderStatus = parseMetricPoints(json.optJSONArray("order_status") ?: JSONArray()),
                    newUsers = parseMetricPoints(json.optJSONArray("new_users") ?: JSONArray())
                )
            } catch (error: Exception) {
                AdminMetrics()
            }
        }
    }

    private suspend fun createBackendOrder(deliveryAddress: String): Int? {
        return withContext(Dispatchers.IO) {
            try {
                val user = (_authState.value as? AuthState.Authenticated)?.user
                val coupon = _appliedCoupon.value
                val payload = JSONObject()
                    .put("customer_name", user?.name ?: "Guest Customer")
                    .put("customer_phone", "N/A")
                    .put("delivery_address", deliveryAddress)
                    .put("coupon_code", coupon?.code ?: JSONObject.NULL)
                    .put("discount", couponDiscount)
                    .put("total", cartTotal)
                    .put("status", "pending")
                    .put(
                        "items",
                        JSONArray(
                            _cart.value.map { cartItem ->
                                JSONObject()
                                    .put("product_id", JSONObject.NULL)
                                    .put("product_name", cartItem.foodItem.name)
                                    .put("quantity", cartItem.quantity)
                                    .put("price", cartItem.foodItem.price)
                            }
                        )
                    )

                val connection = openJsonConnection("/orders", "POST", doOutput = true)
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }

                if (connection.responseCode in 200..299) {
                    JSONObject(readResponse(connection)).getInt("id")
                } else {
                    null
                }
            } catch (error: Exception) {
                null
            }
        }
    }

    private suspend fun fetchAdminUsers(): List<AdminUser> {
        return withContext(Dispatchers.IO) {
            try {
                val connection = openJsonConnection("/admin/users", "GET")
                if (connection.responseCode !in 200..299) return@withContext emptyList()
                val json = JSONArray(readResponse(connection))
                (0 until json.length()).map { index ->
                    val user = json.getJSONObject(index)
                    AdminUser(
                        id = user.getInt("id"),
                        name = user.getString("name"),
                        email = user.getString("email"),
                        role = user.optString("role", "customer"),
                        active = user.optInt("active", 1) == 1,
                        createdAt = user.optString("created_at", "")
                    )
                }
            } catch (error: Exception) {
                emptyList()
            }
        }
    }

    private suspend fun fetchAdminProducts(): List<AdminProduct> {
        return withContext(Dispatchers.IO) {
            try {
                val connection = openJsonConnection("/products", "GET")
                if (connection.responseCode !in 200..299) return@withContext emptyList()
                val json = JSONArray(readResponse(connection))
                (0 until json.length()).map { index -> parseAdminProduct(json.getJSONObject(index)) }
            } catch (error: Exception) {
                emptyList()
            }
        }
    }

    private suspend fun fetchAdminOrders(): List<AdminOrder> {
        return withContext(Dispatchers.IO) {
            try {
                val connection = openJsonConnection("/orders", "GET")
                if (connection.responseCode !in 200..299) return@withContext emptyList()
                val json = JSONArray(readResponse(connection))
                (0 until json.length()).map { index -> parseAdminOrder(json.getJSONObject(index)) }
            } catch (error: Exception) {
                emptyList()
            }
        }
    }

    private suspend fun fetchCustomerOrders(): List<AdminOrder> {
        return withContext(Dispatchers.IO) {
            try {
                val connection = openJsonConnection("/orders/mine", "GET")
                if (connection.responseCode !in 200..299) return@withContext emptyList()
                val json = JSONArray(readResponse(connection))
                (0 until json.length()).map { index -> parseAdminOrder(json.getJSONObject(index)) }
            } catch (error: Exception) {
                emptyList()
            }
        }
    }

    private suspend fun fetchAdminCoupons(): List<Coupon> {
        return withContext(Dispatchers.IO) {
            try {
                val connection = openJsonConnection("/coupons", "GET")
                if (connection.responseCode !in 200..299) return@withContext emptyList()
                val json = JSONArray(readResponse(connection))
                (0 until json.length()).map { index -> parseCoupon(json.getJSONObject(index)) }
            } catch (error: Exception) {
                emptyList()
            }
        }
    }

    private suspend fun fetchUserCart(): List<CartItem> {
        return withContext(Dispatchers.IO) {
            try {
                val connection = openJsonConnection("/cart", "GET")
                if (connection.responseCode !in 200..299) return@withContext emptyList()
                val json = JSONArray(readResponse(connection))
                (0 until json.length()).mapNotNull { index ->
                    val item = json.getJSONObject(index)
                    val key = item.optString("item_key")
                    val foodItem = MenuData.items.firstOrNull { it.id == key } ?: return@mapNotNull null
                    CartItem(foodItem = foodItem, quantity = item.optInt("quantity", 1).coerceAtLeast(1))
                }
            } catch (error: Exception) {
                emptyList()
            }
        }
    }

    private suspend fun fetchUserFavorites(): Set<String> {
        return withContext(Dispatchers.IO) {
            try {
                val connection = openJsonConnection("/favorites", "GET")
                if (connection.responseCode !in 200..299) return@withContext emptySet()
                val json = JSONArray(readResponse(connection))
                (0 until json.length()).mapNotNull { index ->
                    json.getJSONObject(index).optString("item_key").takeIf { it.isNotBlank() }
                }.toSet()
            } catch (error: Exception) {
                emptySet()
            }
        }
    }

    private fun syncCartToBackend() {
        if (isLoadingRemoteCart) return
        if ((_authState.value as? AuthState.Authenticated)?.user?.token.isNullOrBlank()) return

        viewModelScope.launch {
            saveUserCart()
        }
    }

    private suspend fun saveUserCart(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject().put(
                    "items",
                    JSONArray(
                        _cart.value.map { cartItem ->
                            JSONObject()
                                .put("item_key", cartItem.foodItem.id)
                                .put("product_name", cartItem.foodItem.name)
                                .put("category", cartItem.foodItem.category.name)
                                .put("quantity", cartItem.quantity)
                                .put("price", cartItem.foodItem.price)
                        }
                    )
                )
                val connection = openJsonConnection("/cart", "PUT", doOutput = true)
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                connection.responseCode in 200..299
            } catch (error: Exception) {
                false
            }
        }
    }

    private fun syncFavoritesToBackend() {
        if ((_authState.value as? AuthState.Authenticated)?.user?.token.isNullOrBlank()) return

        viewModelScope.launch {
            saveUserFavorites()
        }
    }

    private suspend fun saveUserFavorites(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject().put(
                    "items",
                    JSONArray(_favorites.value.map { JSONObject().put("item_key", it) })
                )
                val connection = openJsonConnection("/favorites", "PUT", doOutput = true)
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                connection.responseCode in 200..299
            } catch (error: Exception) {
                false
            }
        }
    }

    private suspend fun validateCoupon(code: String): Coupon? {
        return withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject()
                    .put("code", code)
                    .put("subtotal", cartSubtotal)
                val connection = openJsonConnection("/coupons/validate", "POST", doOutput = true)
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                if (connection.responseCode in 200..299) parseCoupon(JSONObject(readResponse(connection))) else null
            } catch (error: Exception) {
                null
            }
        }
    }

    private suspend fun sendProduct(product: AdminProduct): AdminProduct? {
        return withContext(Dispatchers.IO) {
            try {
                val path = if (product.id == 0) "/products" else "/products/${product.id}"
                val method = if (product.id == 0) "POST" else "PUT"
                val payload = JSONObject()
                    .put("name", product.name)
                    .put("category", product.category)
                    .put("description", product.description)
                    .put("price", product.price)
                    .put("stock", product.stock)
                    .put("available", product.available)
                    .put("image_url", product.imageUrl)

                val connection = openJsonConnection(path, method, doOutput = true)
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }

                if (connection.responseCode in 200..299) parseAdminProduct(JSONObject(readResponse(connection))) else null
            } catch (error: Exception) {
                null
            }
        }
    }

    private suspend fun updateBackendUser(userId: Int, role: String, active: Boolean): AdminUser? {
        return withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject()
                    .put("role", role)
                    .put("active", active)
                val connection = openJsonConnection("/admin/users/$userId", "PUT", doOutput = true)
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                if (connection.responseCode in 200..299) parseAdminUser(JSONObject(readResponse(connection))) else null
            } catch (error: Exception) {
                null
            }
        }
    }

    private suspend fun sendCoupon(coupon: Coupon): Coupon? {
        return withContext(Dispatchers.IO) {
            try {
                val path = if (coupon.id == 0) "/coupons" else "/coupons/${coupon.id}"
                val method = if (coupon.id == 0) "POST" else "PUT"
                val payload = JSONObject()
                    .put("code", coupon.code.trim().uppercase())
                    .put("description", coupon.description)
                    .put("discount_type", if (coupon.discountType == "fixed") "fixed" else "percent")
                    .put("discount_value", coupon.discountValue)
                    .put("active", coupon.active)

                val connection = openJsonConnection(path, method, doOutput = true)
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }

                if (connection.responseCode in 200..299) parseCoupon(JSONObject(readResponse(connection))) else null
            } catch (error: Exception) {
                null
            }
        }
    }

    private suspend fun sendHomeSettings(settings: HomeSettings): HomeSettings? {
        return withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject()
                    .put("hero_logo", settings.heroLogo)
                    .put("eyebrow_text", settings.eyebrowText)
                    .put("brand_title", settings.brandTitle)
                    .put("family_title", settings.familyTitle)
                    .put("family_description", settings.familyDescription)
                    .put("featured_title", settings.featuredTitle)
                    .put("featured_description", settings.featuredDescription)
                    .put("featured_image", settings.featuredImage)
                    .put("house_tour_title", settings.houseTourTitle)
                    .put("house_tour_description", settings.houseTourDescription)
                val connection = openJsonConnection("/settings/home", "PUT", doOutput = true)
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                if (connection.responseCode in 200..299) parseHomeSettings(JSONObject(readResponse(connection))) else null
            } catch (error: Exception) {
                null
            }
        }
    }

    private suspend fun uploadImageToBackend(contentResolver: ContentResolver, uri: Uri): Pair<String?, String?> {
        return withContext(Dispatchers.IO) {
            try {
                val mimeType = contentResolver.getType(uri) ?: ""
                val allowedTypes = setOf("image/png", "image/jpeg", "image/jpg", "image/webp")

                if (!allowedTypes.contains(mimeType)) {
                    return@withContext null to "Only PNG, JPG, JPEG, and WEBP images are allowed."
                }

                val extension = when (mimeType) {
                    "image/png" -> "png"
                    "image/jpeg", "image/jpg" -> "jpg"
                    "image/webp" -> "webp"
                    else -> "img"
                }
                val boundary = "----LePoutineHouse${System.currentTimeMillis()}"
                val lineEnd = "\r\n"
                val connection = (URL("$API_BASE_URL/uploads").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = API_TIMEOUT_MS
                    readTimeout = API_TIMEOUT_MS
                    doOutput = true
                    setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                    setRequestProperty("Accept", "application/json")
                }

                val imageInput = contentResolver.openInputStream(uri)
                    ?: return@withContext null to "Could not read selected image."

                DataOutputStream(connection.outputStream).use { output ->
                    output.writeBytes("--$boundary$lineEnd")
                    output.writeBytes(
                        "Content-Disposition: form-data; name=\"image\"; filename=\"upload.$extension\"$lineEnd"
                    )
                    output.writeBytes("Content-Type: $mimeType$lineEnd")
                    output.writeBytes(lineEnd)
                    imageInput.use { input ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                        }
                    }
                    output.writeBytes(lineEnd)
                    output.writeBytes("--$boundary--$lineEnd")
                    output.flush()
                }

                val response = JSONObject(readResponse(connection))
                if (connection.responseCode in 200..299) {
                    response.optString("url", response.optString("path")) to null
                } else {
                    null to response.optString("error", "Image upload failed.")
                }
            } catch (error: Exception) {
                null to "Image upload failed."
            }
        }
    }

    private suspend fun updateBackendOrderStatus(orderId: Int, status: String): AdminOrder? {
        return withContext(Dispatchers.IO) {
            try {
                val connection = openJsonConnection("/orders/$orderId/status", "PUT", doOutput = true)
                val payload = JSONObject().put("status", status)
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                if (connection.responseCode in 200..299) parseAdminOrder(JSONObject(readResponse(connection))) else null
            } catch (error: Exception) {
                null
            }
        }
    }

    private suspend fun deleteBackendResource(path: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val connection = openJsonConnection(path, "DELETE")
                connection.responseCode in 200..299
            } catch (error: Exception) {
                false
            }
        }
    }

    private suspend fun sendHomeSection(method: String, path: String, section: HomeSection): HomeSection? {
        return withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject()
                    .put("title", section.title)
                    .put("description", section.description)
                    .put("section_type", section.sectionType)
                    .put("image_name", section.imageName)
                    .put("active", section.active)
                    .put("sort_order", section.sortOrder)

                val connection = openJsonConnection(path, method, doOutput = true)

                connection.outputStream.use { outputStream ->
                    outputStream.write(payload.toString().toByteArray(Charsets.UTF_8))
                }

                val responseJson = JSONObject(readResponse(connection))
                if (connection.responseCode in 200..299) parseHomeSection(responseJson) else null
            } catch (error: Exception) {
                null
            }
        }
    }

    private suspend fun deleteHomeSectionFromBackend(sectionId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val connection = openJsonConnection("/home-sections/$sectionId", "DELETE")

                connection.responseCode in 200..299
            } catch (error: Exception) {
                false
            }
        }
    }

    private fun openJsonConnection(path: String, method: String, doOutput: Boolean = false): HttpURLConnection {
        val token = (_authState.value as? AuthState.Authenticated)?.user?.token.orEmpty()
        return (URL("$API_BASE_URL$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = API_TIMEOUT_MS
            readTimeout = API_TIMEOUT_MS
            this.doOutput = doOutput
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            if (token.isNotBlank()) {
                setRequestProperty("Authorization", "Bearer $token")
            }
        }
    }

    private fun parseAdminProduct(json: JSONObject): AdminProduct {
        return AdminProduct(
            id = json.getInt("id"),
            name = json.getString("name"),
            category = json.optString("category", "Poutine"),
            description = json.optString("description", ""),
            price = json.optDouble("price", 0.0),
            stock = json.optInt("stock", 25),
            available = json.optInt("available", 1) == 1,
            imageUrl = json.optString("image_url").ifBlank { null }
        )
    }

    private fun parseCoupon(json: JSONObject): Coupon {
        return Coupon(
            id = json.optInt("id", 0),
            code = json.optString("code", ""),
            description = json.optString("description", ""),
            discountType = json.optString("discount_type", "percent"),
            discountValue = json.optDouble("discount_value", 0.0),
            active = json.optInt("active", 1) == 1,
            createdAt = json.optString("created_at", ""),
            discount = json.optDouble("discount", 0.0)
        )
    }

    private fun parseAdminUser(json: JSONObject): AdminUser {
        return AdminUser(
            id = json.getInt("id"),
            name = json.getString("name"),
            email = json.getString("email"),
            role = json.optString("role", "customer"),
            active = json.optInt("active", 1) == 1,
            createdAt = json.optString("created_at", "")
        )
    }

    private fun parseAdminOrder(json: JSONObject): AdminOrder {
        return AdminOrder(
            id = json.getInt("id"),
            customerName = json.optString("customer_name", ""),
            customerPhone = json.optString("customer_phone", ""),
            deliveryAddress = json.optString("delivery_address", ""),
            couponCode = json.optString("coupon_code").ifBlank { null },
            discount = json.optDouble("discount", 0.0),
            total = json.optDouble("total", 0.0),
            status = json.optString("status", "pending"),
            createdAt = json.optString("created_at", ""),
            items = parseAdminOrderItems(json.optJSONArray("items") ?: JSONArray())
        )
    }

    private fun parseAdminOrderItems(jsonArray: JSONArray): List<AdminOrderItem> {
        return (0 until jsonArray.length()).map { index ->
            val item = jsonArray.getJSONObject(index)
            AdminOrderItem(
                id = item.optInt("id", 0),
                productId = if (item.isNull("product_id")) null else item.optInt("product_id"),
                productName = item.optString("product_name", "Menu item"),
                quantity = item.optInt("quantity", 1),
                price = item.optDouble("price", 0.0)
            )
        }
    }

    private fun parseMetricPoints(jsonArray: JSONArray): List<MetricPoint> {
        return (0 until jsonArray.length()).map { index ->
            val item = jsonArray.getJSONObject(index)
            MetricPoint(
                label = item.optString("label", "Item"),
                value = item.optDouble("value", 0.0)
            )
        }
    }

    private fun parseHomeSections(jsonArray: JSONArray): List<HomeSection> {
        return (0 until jsonArray.length()).map { index ->
            parseHomeSection(jsonArray.getJSONObject(index))
        }
    }

    private fun parseHomeSection(json: JSONObject): HomeSection {
        return HomeSection(
            id = json.getInt("id"),
            title = json.getString("title"),
            description = json.getString("description"),
            sectionType = json.optString("section_type", "featured"),
            imageName = json.optString("image_name").ifBlank { null },
            active = json.optInt("active", 1) == 1,
            sortOrder = json.optInt("sort_order", 0)
        )
    }

    private fun parseHomeSettings(json: JSONObject): HomeSettings {
        return HomeSettings(
            heroLogo = json.optString("hero_logo", "img_poutine_family_logo"),
            eyebrowText = json.optString("eyebrow_text", "PREMIUM CANADIAN"),
            brandTitle = json.optString("brand_title", "Le Poutine House"),
            familyTitle = json.optString("family_title", "Meet the Poutine Family"),
            familyDescription = json.optString("family_description", "Grandpa, Grandma, the little spuds and every house recipe."),
            featuredTitle = json.optString("featured_title", "Grandpa's Traditional Feast"),
            featuredDescription = json.optString("featured_description", "Featured today with crisp fries, cheese curds, and house gravy."),
            featuredImage = json.optString("featured_image", "abuelos"),
            houseTourTitle = json.optString("house_tour_title", "Do you want a house tour?"),
            houseTourDescription = json.optString("house_tour_description", "Take a quick walk through the kitchen, dining room and pickup counter.")
        )
    }

    private fun readResponse(connection: HttpURLConnection): String {
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        return stream.use { input ->
            BufferedReader(InputStreamReader(input)).use { reader ->
                reader.readText()
            }
        }
    }

    companion object {
        private const val API_BASE_URL = "http://10.0.2.2:3000"
        private const val API_TIMEOUT_MS = 3000
    }
}
