const express = require("express");
const cors = require("cors");
const mysql = require("mysql2/promise");
const bcrypt = require("bcryptjs");
const multer = require("multer");
const path = require("path");
const fs = require("fs");

const app = express();
const uploadDir = path.join(__dirname, "uploads");
const allowedImageTypes = new Set(["image/png", "image/jpeg", "image/jpg", "image/webp"]);

if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

app.use(cors());
app.use(express.json());
app.use("/uploads", express.static(uploadDir));

const imageStorage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    const extension = path.extname(file.originalname).toLowerCase();
    const safeName = `${Date.now()}-${Math.round(Math.random() * 1e9)}${extension}`;
    cb(null, safeName);
  }
});

const upload = multer({
  storage: imageStorage,
  limits: {
    fileSize: 5 * 1024 * 1024
  },
  fileFilter: (req, file, cb) => {
    if (!allowedImageTypes.has(file.mimetype)) {
      return cb(new Error("Only PNG, JPG, JPEG, and WEBP images are allowed"));
    }

    cb(null, true);
  }
});

const dbConfig = {
  host: process.env.DB_HOST || "mysql",
  user: process.env.DB_USER || "poutine_user",
  password: process.env.DB_PASSWORD || "poutine123",
  database: process.env.DB_NAME || "le_poutine_house",
  port: Number(process.env.DB_PORT || 3306),
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
};

const port = Number(process.env.PORT || 3000);
let pool;

const sampleProducts = [
  {
    name: "Classic Poutine",
    category: "Poutine",
    description: "Crispy fries, cheese curds, and rich brown gravy.",
    price: 8.99,
    image_url: "https://example.com/images/classic-poutine.jpg"
  },
  {
    name: "Bacon Poutine",
    category: "Poutine",
    description: "Classic poutine topped with smoky bacon pieces.",
    price: 10.99,
    image_url: "https://example.com/images/bacon-poutine.jpg"
  },
  {
    name: "Chicken Poutine",
    category: "Poutine",
    description: "Tender chicken over fries, cheese curds, and gravy.",
    price: 11.99,
    image_url: "https://example.com/images/chicken-poutine.jpg"
  },
  {
    name: "Double Cheese Poutine",
    category: "Poutine",
    description: "Extra cheese curds with creamy cheese sauce and gravy.",
    price: 10.49,
    image_url: "https://example.com/images/double-cheese-poutine.jpg"
  },
  {
    name: "Family Combo",
    category: "Combo",
    description: "Large poutine, sides, and drinks for the whole family.",
    price: 27.99,
    image_url: "https://example.com/images/family-combo.jpg"
  },
  {
    name: "Kids Poutine",
    category: "Kids",
    description: "A smaller serving of the classic favorite.",
    price: 5.99,
    image_url: "https://example.com/images/kids-poutine.jpg"
  },
  {
    name: "Grandpa Traditional Feast",
    category: "Special",
    description: "A hearty traditional plate with classic poutine flavors.",
    price: 14.99,
    image_url: "https://example.com/images/grandpa-traditional-feast.jpg"
  },
  {
    name: "Grandma Homemade Special",
    category: "Special",
    description: "Homestyle poutine with a comforting house gravy.",
    price: 13.99,
    image_url: "https://example.com/images/grandma-homemade-special.jpg"
  }
];

const defaultHomeSections = [
  {
    title: "Grandpa's Traditional Feast",
    description: "Featured today with crisp fries, cheese curds, and house gravy.",
    section_type: "featured",
    image_name: "abuelos",
    sort_order: 1
  },
  {
    title: "Do you want a house tour?",
    description: "Take a quick walk through the kitchen, dining room and pickup counter.",
    section_type: "tour",
    image_name: "img_poutine_hero",
    sort_order: 2
  }
];

const defaultAppSettings = {
  hero_logo: "img_poutine_family_logo",
  eyebrow_text: "PREMIUM CANADIAN",
  brand_title: "Le Poutine House",
  family_title: "Meet the Poutine Family",
  family_description: "Grandpa, Grandma, the little spuds and every house recipe.",
  featured_title: "Grandpa's Traditional Feast",
  featured_description: "Featured today with crisp fries, cheese curds, and house gravy.",
  featured_image: "abuelos",
  house_tour_title: "Do you want a house tour?",
  house_tour_description: "Take a quick walk through the kitchen, dining room and pickup counter."
};

async function ensureColumn(tableName, columnName, definition) {
  const [[column]] = await pool.query(
    `
      SELECT COLUMN_NAME
      FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA = ?
        AND TABLE_NAME = ?
        AND COLUMN_NAME = ?
    `,
    [dbConfig.database, tableName, columnName]
  );

  if (!column) {
    await pool.query(`ALTER TABLE ${tableName} ADD COLUMN ${columnName} ${definition}`);
  }
}

async function initializeDatabase() {
  pool = mysql.createPool(dbConfig);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS users (
      id INT AUTO_INCREMENT PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      email VARCHAR(255) NOT NULL UNIQUE,
      password_hash VARCHAR(255) NOT NULL,
      role VARCHAR(30) NOT NULL DEFAULT 'customer',
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
  `);

  await ensureColumn("users", "role", "VARCHAR(30) NOT NULL DEFAULT 'customer'");
  await ensureColumn("users", "active", "TINYINT(1) NOT NULL DEFAULT 1");

  await pool.query(`
    CREATE TABLE IF NOT EXISTS products (
      id INT AUTO_INCREMENT PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      category VARCHAR(100) NOT NULL,
      description TEXT,
      price DECIMAL(10, 2) NOT NULL,
      image_url VARCHAR(500),
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS home_sections (
      id INT AUTO_INCREMENT PRIMARY KEY,
      title VARCHAR(255) NOT NULL,
      description TEXT NOT NULL,
      section_type VARCHAR(60) NOT NULL DEFAULT 'featured',
      image_name VARCHAR(255),
      active TINYINT(1) NOT NULL DEFAULT 1,
      sort_order INT NOT NULL DEFAULT 0,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS orders (
      id INT AUTO_INCREMENT PRIMARY KEY,
      customer_name VARCHAR(255) NOT NULL,
      customer_phone VARCHAR(50) NOT NULL,
      total DECIMAL(10, 2) NOT NULL,
      status VARCHAR(50) NOT NULL DEFAULT 'pending',
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
  `);

  await pool.query(`
    CREATE TABLE IF NOT EXISTS order_items (
      id INT AUTO_INCREMENT PRIMARY KEY,
      order_id INT NOT NULL,
      product_id INT NULL,
      product_name VARCHAR(255),
      quantity INT NOT NULL,
      price DECIMAL(10, 2) NOT NULL,
      FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
      FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
    )
  `);

  await pool.query("ALTER TABLE order_items MODIFY product_id INT NULL");
  await ensureColumn("order_items", "product_name", "VARCHAR(255)");

  await pool.query(`
    CREATE TABLE IF NOT EXISTS app_settings (
      setting_key VARCHAR(100) PRIMARY KEY,
      setting_value TEXT NOT NULL,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    )
  `);

  const [[{ count }]] = await pool.query("SELECT COUNT(*) AS count FROM products");

  if (count === 0) {
    await pool.query(
      "INSERT INTO products (name, category, description, price, image_url) VALUES ?",
      [sampleProducts.map((product) => [
        product.name,
        product.category,
        product.description,
        product.price,
        product.image_url
      ])]
    );
  }

  const [[{ homeSectionCount }]] = await pool.query("SELECT COUNT(*) AS homeSectionCount FROM home_sections");

  if (homeSectionCount === 0) {
    await pool.query(
      "INSERT INTO home_sections (title, description, section_type, image_name, sort_order) VALUES ?",
      [defaultHomeSections.map((section) => [
        section.title,
        section.description,
        section.section_type,
        section.image_name,
        section.sort_order
      ])]
    );
  }

  await Promise.all(
    Object.entries(defaultAppSettings).map(([key, value]) =>
      pool.query(
        "INSERT IGNORE INTO app_settings (setting_key, setting_value) VALUES (?, ?)",
        [key, value]
      )
    )
  );
}

app.get("/", (req, res) => {
  res.json({ message: "Le Poutine House API is running" });
});

app.post("/uploads", (req, res) => {
  upload.single("image")(req, res, (error) => {
    if (error) {
      return res.status(400).json({ error: error.message });
    }

    if (!req.file) {
      return res.status(400).json({ error: "image file is required" });
    }

    res.status(201).json({
      filename: req.file.filename,
      path: `/uploads/${req.file.filename}`,
      url: `/uploads/${req.file.filename}`,
      mimetype: req.file.mimetype,
      size: req.file.size
    });
  });
});

app.get("/settings/home", async (req, res) => {
  try {
    const [rows] = await pool.query("SELECT setting_key, setting_value FROM app_settings");
    const settings = { ...defaultAppSettings };
    rows.forEach((row) => {
      settings[row.setting_key] = row.setting_value;
    });
    res.json(settings);
  } catch (error) {
    res.status(500).json({ error: "Failed to fetch home settings" });
  }
});

app.put("/settings/home", async (req, res) => {
  const allowedKeys = Object.keys(defaultAppSettings);
  const entries = Object.entries(req.body || {}).filter(([key]) => allowedKeys.includes(key));

  if (entries.length === 0) {
    return res.status(400).json({ error: "No valid settings provided" });
  }

  try {
    await Promise.all(
      entries.map(([key, value]) =>
        pool.query(
          `
            INSERT INTO app_settings (setting_key, setting_value)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)
          `,
          [key, String(value ?? "")]
        )
      )
    );

    const [rows] = await pool.query("SELECT setting_key, setting_value FROM app_settings");
    const settings = { ...defaultAppSettings };
    rows.forEach((row) => {
      settings[row.setting_key] = row.setting_value;
    });
    res.json(settings);
  } catch (error) {
    res.status(500).json({ error: "Failed to update home settings" });
  }
});

app.post("/auth/register", async (req, res) => {
  const { name, email, password, role } = req.body;

  if (!name || !email || !password) {
    return res.status(400).json({ error: "name, email, and password are required" });
  }

  if (password.length < 5) {
    return res.status(400).json({ error: "password must be at least 5 characters" });
  }

  try {
    const [[existingUser]] = await pool.query("SELECT id FROM users WHERE email = ?", [email]);

    if (existingUser) {
      return res.status(409).json({ error: "email is already registered" });
    }

    const passwordHash = await bcrypt.hash(password, 10);
    const userRole = role === "admin" ? "admin" : "customer";
    const [result] = await pool.query(
      "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)",
      [name, email, passwordHash, userRole]
    );

    res.status(201).json({
      message: "User registered successfully",
      user: {
        id: result.insertId,
        name,
        email,
        role: userRole
      }
    });
  } catch (error) {
    res.status(500).json({ error: "Failed to register user" });
  }
});

app.post("/auth/login", async (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(400).json({ error: "email and password are required" });
  }

  try {
    const [[user]] = await pool.query("SELECT * FROM users WHERE email = ?", [email]);

    if (!user) {
      return res.status(401).json({ error: "Invalid email or password" });
    }

    if (Number(user.active) !== 1) {
      return res.status(403).json({ error: "This user account is disabled" });
    }

    const passwordMatches = await bcrypt.compare(password, user.password_hash);

    if (!passwordMatches) {
      return res.status(401).json({ error: "Invalid email or password" });
    }

    res.json({
      message: "Login successful",
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        role: user.role || "customer"
      }
    });
  } catch (error) {
    res.status(500).json({ error: "Failed to login" });
  }
});

app.get("/products", async (req, res) => {
  try {
    const [products] = await pool.query("SELECT * FROM products ORDER BY id ASC");
    res.json(products);
  } catch (error) {
    res.status(500).json({ error: "Failed to fetch products" });
  }
});

app.put("/products/:id", async (req, res) => {
  const { id } = req.params;
  const { name, category, description, price, image_url } = req.body;

  if (!name || !category || price === undefined) {
    return res.status(400).json({ error: "name, category, and price are required" });
  }

  try {
    const [result] = await pool.query(
      `
        UPDATE products
        SET name = ?, category = ?, description = ?, price = ?, image_url = ?
        WHERE id = ?
      `,
      [name, category, description || null, price, image_url || null, id]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({ error: "Product not found" });
    }

    const [[product]] = await pool.query("SELECT * FROM products WHERE id = ?", [id]);
    res.json(product);
  } catch (error) {
    res.status(500).json({ error: "Failed to update product" });
  }
});

app.delete("/products/:id", async (req, res) => {
  const { id } = req.params;

  try {
    const [result] = await pool.query("DELETE FROM products WHERE id = ?", [id]);

    if (result.affectedRows === 0) {
      return res.status(404).json({ error: "Product not found" });
    }

    res.json({ message: "Product deleted successfully" });
  } catch (error) {
    res.status(500).json({ error: "Failed to delete product" });
  }
});

app.get("/home-sections", async (req, res) => {
  try {
    const [sections] = await pool.query(
      "SELECT id, title, description, section_type, image_name, active, sort_order, created_at FROM home_sections ORDER BY sort_order ASC, id DESC"
    );
    res.json(sections);
  } catch (error) {
    res.status(500).json({ error: "Failed to fetch home sections" });
  }
});

app.post("/home-sections", async (req, res) => {
  const { title, description, section_type, image_name, active = true, sort_order = 0 } = req.body;

  if (!title || !description) {
    return res.status(400).json({ error: "title and description are required" });
  }

  try {
    const [result] = await pool.query(
      "INSERT INTO home_sections (title, description, section_type, image_name, active, sort_order) VALUES (?, ?, ?, ?, ?, ?)",
      [title, description, section_type || "featured", image_name || null, active ? 1 : 0, Number(sort_order) || 0]
    );

    const [[section]] = await pool.query("SELECT * FROM home_sections WHERE id = ?", [result.insertId]);
    res.status(201).json(section);
  } catch (error) {
    res.status(500).json({ error: "Failed to create home section" });
  }
});

app.put("/home-sections/:id", async (req, res) => {
  const { id } = req.params;
  const { title, description, section_type, image_name, active = true, sort_order = 0 } = req.body;

  if (!title || !description) {
    return res.status(400).json({ error: "title and description are required" });
  }

  try {
    const [result] = await pool.query(
      `
        UPDATE home_sections
        SET title = ?, description = ?, section_type = ?, image_name = ?, active = ?, sort_order = ?
        WHERE id = ?
      `,
      [title, description, section_type || "featured", image_name || null, active ? 1 : 0, Number(sort_order) || 0, id]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({ error: "Home section not found" });
    }

    const [[section]] = await pool.query("SELECT * FROM home_sections WHERE id = ?", [id]);
    res.json(section);
  } catch (error) {
    res.status(500).json({ error: "Failed to update home section" });
  }
});

app.delete("/home-sections/:id", async (req, res) => {
  const { id } = req.params;

  try {
    const [result] = await pool.query("DELETE FROM home_sections WHERE id = ?", [id]);

    if (result.affectedRows === 0) {
      return res.status(404).json({ error: "Home section not found" });
    }

    res.json({ message: "Home section deleted successfully" });
  } catch (error) {
    res.status(500).json({ error: "Failed to delete home section" });
  }
});

app.post("/products", async (req, res) => {
  const { name, category, description, price, image_url } = req.body;

  if (!name || !category || price === undefined) {
    return res.status(400).json({ error: "name, category, and price are required" });
  }

  try {
    const [result] = await pool.query(
      "INSERT INTO products (name, category, description, price, image_url) VALUES (?, ?, ?, ?, ?)",
      [name, category, description || null, price, image_url || null]
    );

    const [[product]] = await pool.query("SELECT * FROM products WHERE id = ?", [result.insertId]);
    res.status(201).json(product);
  } catch (error) {
    res.status(500).json({ error: "Failed to create product" });
  }
});

app.get("/orders", async (req, res) => {
  try {
    const [orders] = await pool.query("SELECT * FROM orders ORDER BY created_at DESC");
    const [items] = await pool.query(`
      SELECT
        oi.id,
        oi.order_id,
        oi.product_id,
        COALESCE(oi.product_name, p.name, 'Menu item') AS product_name,
        oi.quantity,
        oi.price
      FROM order_items oi
      LEFT JOIN products p ON p.id = oi.product_id
      ORDER BY oi.id ASC
    `);

    const itemsByOrder = items.reduce((acc, item) => {
      acc[item.order_id] = acc[item.order_id] || [];
      acc[item.order_id].push(item);
      return acc;
    }, {});

    res.json(orders.map((order) => ({ ...order, items: itemsByOrder[order.id] || [] })));
  } catch (error) {
    res.status(500).json({ error: "Failed to fetch orders" });
  }
});

app.get("/orders/:id", async (req, res) => {
  const { id } = req.params;

  try {
    const [[order]] = await pool.query("SELECT * FROM orders WHERE id = ?", [id]);

    if (!order) {
      return res.status(404).json({ error: "Order not found" });
    }

    const [items] = await pool.query(
      `
        SELECT
          oi.id,
          oi.order_id,
          oi.product_id,
          COALESCE(oi.product_name, p.name, 'Menu item') AS product_name,
          oi.quantity,
          oi.price
        FROM order_items oi
        LEFT JOIN products p ON p.id = oi.product_id
        WHERE oi.order_id = ?
        ORDER BY oi.id ASC
      `,
      [id]
    );

    res.json({ ...order, items });
  } catch (error) {
    res.status(500).json({ error: "Failed to fetch order" });
  }
});

app.put("/orders/:id/status", async (req, res) => {
  const { id } = req.params;
  const { status } = req.body;

  if (!status) {
    return res.status(400).json({ error: "status is required" });
  }

  try {
    const [result] = await pool.query("UPDATE orders SET status = ? WHERE id = ?", [status, id]);

    if (result.affectedRows === 0) {
      return res.status(404).json({ error: "Order not found" });
    }

    const [[order]] = await pool.query("SELECT * FROM orders WHERE id = ?", [id]);
    res.json(order);
  } catch (error) {
    res.status(500).json({ error: "Failed to update order status" });
  }
});

app.get("/admin/users", async (req, res) => {
  try {
    const [users] = await pool.query(
      "SELECT id, name, email, role, active, created_at FROM users ORDER BY created_at DESC"
    );
    res.json(users);
  } catch (error) {
    res.status(500).json({ error: "Failed to fetch users" });
  }
});

app.put("/admin/users/:id", async (req, res) => {
  const { id } = req.params;
  const { role, active } = req.body;
  const safeRole = role === "admin" ? "admin" : "customer";
  const safeActive = active ? 1 : 0;

  try {
    const [result] = await pool.query(
      "UPDATE users SET role = ?, active = ? WHERE id = ?",
      [safeRole, safeActive, id]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({ error: "User not found" });
    }

    const [[user]] = await pool.query(
      "SELECT id, name, email, role, active, created_at FROM users WHERE id = ?",
      [id]
    );
    res.json(user);
  } catch (error) {
    res.status(500).json({ error: "Failed to update user" });
  }
});

app.get("/admin/stats", async (req, res) => {
  try {
    const [[userStats]] = await pool.query(`
      SELECT
        COUNT(*) AS total_users,
        SUM(CASE WHEN role = 'admin' THEN 1 ELSE 0 END) AS admin_users,
        SUM(CASE WHEN role <> 'admin' THEN 1 ELSE 0 END) AS customer_users,
        SUM(CASE WHEN active = 1 THEN 1 ELSE 0 END) AS active_users,
        SUM(CASE WHEN created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) AS new_users_7d
      FROM users
    `);
    const [[productStats]] = await pool.query("SELECT COUNT(*) AS total_products FROM products");
    const [[orderStats]] = await pool.query(`
      SELECT
        COUNT(*) AS total_orders,
        COALESCE(SUM(total), 0) AS total_revenue
      FROM orders
    `);

    res.json({
      total_users: Number(userStats.total_users || 0),
      admin_users: Number(userStats.admin_users || 0),
      customer_users: Number(userStats.customer_users || 0),
      active_users: Number(userStats.active_users || 0),
      new_users_7d: Number(userStats.new_users_7d || 0),
      total_products: Number(productStats.total_products || 0),
      total_orders: Number(orderStats.total_orders || 0),
      total_revenue: Number(orderStats.total_revenue || 0)
    });
  } catch (error) {
    res.status(500).json({ error: "Failed to fetch admin stats" });
  }
});

app.post("/orders", async (req, res) => {
  const { customer_name, customer_phone, total, status, items = [] } = req.body;

  if (!customer_name || !customer_phone || total === undefined) {
    return res.status(400).json({ error: "customer_name, customer_phone, and total are required" });
  }

  const connection = await pool.getConnection();

  try {
    await connection.beginTransaction();

    const [orderResult] = await connection.query(
      "INSERT INTO orders (customer_name, customer_phone, total, status) VALUES (?, ?, ?, ?)",
      [customer_name, customer_phone, total, status || "pending"]
    );

    const normalizedItems = Array.isArray(items) ? items.filter((item) => item.quantity > 0) : [];

    if (normalizedItems.length > 0) {
      const orderItems = normalizedItems.map((item) => [
        orderResult.insertId,
        item.product_id || null,
        item.product_name || item.name || "Menu item",
        Number(item.quantity) || 1,
        Number(item.price) || 0
      ]);

      await connection.query(
        "INSERT INTO order_items (order_id, product_id, product_name, quantity, price) VALUES ?",
        [orderItems]
      );
    }

    await connection.commit();

    const [[order]] = await pool.query("SELECT * FROM orders WHERE id = ?", [orderResult.insertId]);
    res.status(201).json(order);
  } catch (error) {
    await connection.rollback();
    res.status(500).json({ error: "Failed to create order" });
  } finally {
    connection.release();
  }
});

app.get("/health", async (req, res) => {
  try {
    await pool.query("SELECT 1");
    res.json({
      backend: "ok",
      database: "ok"
    });
  } catch (error) {
    res.status(503).json({
      backend: "ok",
      database: "error"
    });
  }
});

initializeDatabase()
  .then(() => {
    app.listen(port, () => {
      console.log(`Le Poutine House API running on port ${port}`);
    });
  })
  .catch((error) => {
    console.error("Failed to initialize database", error);
    process.exit(1);
  });
