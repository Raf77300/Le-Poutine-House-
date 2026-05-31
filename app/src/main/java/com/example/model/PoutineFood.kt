package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.R

enum class PoutineCategory(
    val id: String,
    val title: String,
    val characterName: String,
    val characterRole: String,
    val emoji: String,
    val colorHex: Long,
    val description: String
) {
    MR_POUTINE("mr_poutine", "Mr. Poutine Classics", "Mr. Poutine", "Father", "🧔🥔", 0xFFD8A25A, "Robust, hearty classics with bold lumberjack flavor."),
    MRS_POUTINE("mrs_poutine", "Mrs. Poutine Favorites", "Mrs. Poutine", "Mother", "👩🥔", 0xFFE8C08D, "Elegant, lighter, and creatively curated gourmet favorites."),
    KIDS("kids", "Little Potatoes Kids Menu", "Little Potatoes", "Kids", "👦🥔", 0xFFF0E0D0, "Fun-sized, delicious bites perfect for growing spuds."),
    GRANDPA("grandpa", "Grandpa's Traditional Recipes", "Grandpa Poutine", "Grandfather", "👴🥔", 0xFFC62828, "Wood-fired, rich traditional recipes passed down through decades."),
    GRANDMA("grandma", "Grandma's Homemade Specials", "Grandma Poutine", "Grandmother", "👵🥔", 0xFFE53935, "Warm, rich comforting poutine specials cooked with family love."),
    COMBOS("combos", "Family Feast Combos", "The Potato Family", "Full Family", "👨‍👩‍👧‍👦🥔", 0xFF1A1A1A, "Gigantic poutine bundles built to bring the whole family together.")
}

data class FoodItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: PoutineCategory,
    val isFeatured: Boolean = false,
    val rating: Double = 4.8,
    val prepTime: String = "15-20 min",
    val ingredients: List<String> = emptyList(),
    val tag: String? = null
)

object MenuData {
    val items = listOf(
        // Mr. Poutine Classics (Mr. Poutine - Father)
        FoodItem(
            id = "mr_poutine_classic",
            name = "Traditional Quebecer Poutine",
            description = "The ultimate gold standard. Extra crispy double-fried Kennebec potatoes loaded with premium squeaky cheese curds, drowned in our world-famous piping hot brown gravy.",
            price = 12.99,
            category = PoutineCategory.MR_POUTINE,
            isFeatured = true,
            rating = 4.9,
            prepTime = "12 min",
            ingredients = listOf("Double-fried Kennebec potatoes", "Fresh cheese curds", "Signature beef & chicken gravy blend"),
            tag = "Bestseller"
        ),
        FoodItem(
            id = "mr_poutine_lumberjack",
            name = "The Lumberjack Poutine",
            description = "A massive serving of crisp potatoes topped with double cheese curds, heaps of sweet caramelized onions, and tons of maple-smoked bacon strips.",
            price = 15.99,
            category = PoutineCategory.MR_POUTINE,
            isFeatured = true,
            rating = 4.8,
            prepTime = "15 min",
            ingredients = listOf("French fries", "Double cheese curds", "Caramelized onions", "Maple-smoked bacon", "Smoky BBQ hint"),
            tag = "Hearty"
        ),
        FoodItem(
            id = "mr_poutine_champ",
            name = "The Ultimate Champ",
            description = "Father's crown jewel. Rich, tender slow-cooked beef brisket shredded over potatoes, topped with sautéed wild forest mushrooms and dark rich pepper gravy.",
            price = 16.99,
            category = PoutineCategory.MR_POUTINE,
            rating = 4.9,
            prepTime = "18 min",
            ingredients = listOf("French fries", "Slow-cooked beef brisket", "Wild forest mushrooms", "Cheese curds", "Gourmet pepper gravy"),
            tag = "Premium"
        ),

        // Mrs. Poutine Favorites (Mrs. Poutine - Mother)
        FoodItem(
            id = "mrs_poutine_veggie",
            name = "Gourmet Veggie Bliss",
            description = "A colorful, vibrant vegetarian masterpiece. Golden fries topped with sweet peas, roasted red and yellow bell peppers, grilled baby zucchini, and our savory organic herb gravy.",
            price = 14.50,
            category = PoutineCategory.MRS_POUTINE,
            rating = 4.7,
            prepTime = "14 min",
            ingredients = listOf("Golden French fries", "Cheese curds", "Roasted bell peppers", "Sweet peas", "Zucchini", "Organic veggie gravy"),
            tag = "Vegetarian"
        ),
        FoodItem(
            id = "mrs_poutine_truffle",
            name = "Truffle Cream Infusion",
            description = "Elegant, decadent, and luxurious. Premium cheese curds and wild forest mushrooms, finished with a generous drizzle of real black truffle oil and creamy peppercorn reduction.",
            price = 16.50,
            category = PoutineCategory.MRS_POUTINE,
            isFeatured = true,
            rating = 4.9,
            prepTime = "15 min",
            ingredients = listOf("Selected potatoes", "Truffle oil", "Cheese curds", "Forest mushrooms", "White peppercorn cream gravy"),
            tag = "Gourmet"
        ),
        FoodItem(
            id = "mrs_poutine_smoked",
            name = "Montreal Smoked Meat Special",
            description = "A savory Quebec tribute. Heaps of tender, lean Montreal-style smoked meat over golden fries and squeaky curds, topped with a tangy French mustard drizzle and a crunchy dill pickle.",
            price = 15.99,
            category = PoutineCategory.MRS_POUTINE,
            rating = 4.8,
            prepTime = "12 min",
            ingredients = listOf("Kennebec fries", "Montreal smoked meat", "Cheese curds", "Yellow mustard", "Quartered dill pickles"),
            tag = "Local Legend"
        ),

        // Little Potatoes Kids Menu (Kids)
        FoodItem(
            id = "kids_cup",
            name = "Mini Hero Poutine Cup",
            description = "The classic favorite, sized perfectly for little heroes. A fun, mess-free cup with bite-sized fries, mini cheese curds, and mild, savory child-friendly gravy.",
            price = 7.99,
            category = PoutineCategory.KIDS,
            rating = 4.8,
            prepTime = "10 min",
            ingredients = listOf("Shoestring potatoes", "Mini cheese curds", "Mild vegetable-infused gravy"),
            tag = "Kids Favorite"
        ),
        FoodItem(
            id = "kids_sausage",
            name = "Potato Boy's Sausage Roll",
            description = "Our playful Little Potato Boy's personal pick: Crispy fries tossed with tender sliced premium hot dog sausages, golden sweetcorn, and mild brown gravy.",
            price = 8.99,
            category = PoutineCategory.KIDS,
            rating = 4.6,
            prepTime = "10 min",
            ingredients = listOf("Fries", "Sliced hot dog sausages", "Sweetcorn", "Cheese curds", "Mild gravy"),
            tag = "Fun Eat"
        ),
        FoodItem(
            id = "kids_sweet",
            name = "Potato Girl's Sweet Potato Magic",
            description = "A vibrant sweet-tooth twist. Crispy sweet potato fires served with warm creamy brown gravy, tiny cheese curds, and a touch of maple honey glaze.",
            price = 8.99,
            category = PoutineCategory.KIDS,
            rating = 4.7,
            prepTime = "11 min",
            ingredients = listOf("Sweet potato fries", "Mini cheese curds", "Mild gravy", "Maple honey drizzle"),
            tag = "Sweet & Salty"
        ),

        // Grandpa's Traditional Recipes (Grandpa - Grandfather)
        FoodItem(
            id = "grandpa_pork",
            name = "The Woodsmoke BBQ Pulled Pork",
            description = "Savory, wood-fired heritage. Our slow-smoked cherrywood pork shoulder, pulled by hand, piled on fries with rich squeaky cheese curds, hickory BBQ sauce, and thick dark beef gravy.",
            price = 16.00,
            category = PoutineCategory.GRANDPA,
            isFeatured = true,
            rating = 4.9,
            prepTime = "16 min",
            ingredients = listOf("Double-cooked fries", "Cherrywood pulled pork", "Curds", "Hickory BBQ sauce", "Dark beef gravy"),
            tag = "Grandpa's Pick"
        ),
        FoodItem(
            id = "grandpa_hunter",
            name = "The Hunter's Venison Feast",
            description = "A rustic, robust wilderness feast. Sliced wood-braised venison links, caramelized wild shallots, melting curds, and deep red wine reduction poured over double-crisp fries.",
            price = 17.50,
            category = PoutineCategory.GRANDPA,
            rating = 4.9,
            prepTime = "20 min",
            ingredients = listOf("Rustic skin-on fries", "Venison sausage links", "Eschalots", "Cheese curds", "Red wine reduction gravy"),
            tag = "Chef Special"
        ),
        FoodItem(
            id = "grandpa_1957",
            name = "Grandpa's Original 1957",
            description = "Tribute to the origins. The absolute purest formulation from 1957. Twice-fried Quebec red potatoes, unadulterated pure white curds, and grandpa's secret recipe beef gravy.",
            price = 12.00,
            category = PoutineCategory.GRANDPA,
            rating = 4.8,
            prepTime = "10 min",
            ingredients = listOf("Hand-cut red potatoes", "Unpasteurized-style curds", "1957 Secret gravy blend"),
            tag = "Vintage Heritage"
        ),

        // Grandma's Homemade Specials (Grandma - Grandmother)
        FoodItem(
            id = "grandma_thanksgiving",
            name = "Thanksgiving Day Poutine",
            description = "Every day is Thanksgiving with Grandma. Oven-roasted moist turkey breast, crispy skin cracklings, fresh cheese curds, seasoned poultry gravy, topped with a spoonful of sweet house-made cranberry compound.",
            price = 16.90,
            category = PoutineCategory.GRANDMA,
            isFeatured = true,
            rating = 4.9,
            prepTime = "15 min",
            ingredients = listOf("Thick cut fries", "Roasted turkey breast", "Turkey skin cracklings", "Herb light poultry gravy", "Homemade cranberry compote"),
            tag = "Grandma's Specialty"
        ),
        FoodItem(
            id = "grandma_butter_chicken",
            name = "Artisanal Butter Chicken Poutine",
            description = "West meets East in perfect Harmony. Beautiful crispy fries and fresh cheese curds, topped with grandma's slow-simmered, aromatic butter chicken with succulent cream-marinated organic chicken thigh chunks.",
            price = 15.90,
            category = PoutineCategory.GRANDMA,
            rating = 4.8,
            prepTime = "14 min",
            ingredients = listOf("Golden french fries", "Succulent butter chicken pieces", "Premium cheese curds", "Aromatics & coriander"),
            tag = "Fusion Favorite"
        ),
        FoodItem(
            id = "grandma_meatball",
            name = "Grandma's Meatball Comfort",
            description = "Thick, golden skin-on fries loaded with grandma's juicy, hand-rolled Italian beef meatballs, covered in a rich tomato-beef gravy fusion, topped with fresh shaved parmesan and cheese curds.",
            price = 16.50,
            category = PoutineCategory.GRANDMA,
            rating = 4.7,
            prepTime = "18 min",
            ingredients = listOf("Skin-on potatoes", "Hand-rolled beef meatballs", "Melted cheese curds", "Tomato gravy reduction", "Shaved parmesan"),
            tag = "Pure Comfort"
        ),

        // Family Feast Combos (Full Family)
        FoodItem(
            id = "combo_feast",
            name = "The Potato Family Feast (4-6 Pax)",
            description = "The ultimate reunion meal! Includes 1 Jumbo Traditional Quebecer Poutine, 1 Truffle Cream Infusion, 2 Kids Mini Hero Poutine Cups, 1 Large Sweet Onion Ring basket, and a choice of 4 premium craft sodas.",
            price = 49.99,
            category = PoutineCategory.COMBOS,
            isFeatured = true,
            rating = 5.0,
            prepTime = "25 min",
            ingredients = listOf("Jumbo Quebecer", "Truffle Cream", "2x Kids Cups", "1x Onion Rings", "4x Craft Beverages"),
            tag = "Super Deal"
        ),
        FoodItem(
            id = "combo_grandparents",
            name = "Grandparents' Sunday Special (2-3 Pax)",
            description = "Warm sharing. Includes 1 Grandpa's Woodsmoke BBQ Pulled Pork poutine, 1 Grandma's Thanksgiving Day Poutine, 1 Sharing bowl of Golden Maple-Glazed Fried Cauliflower bites, and 2 craft sodas.",
            price = 32.99,
            category = PoutineCategory.COMBOS,
            rating = 4.9,
            prepTime = "20 min",
            ingredients = listOf("Woodsmoke Pulled Pork", "Thanksgiving Poutine", "Maple Fried Cauliflower", "2x Craft Sodas"),
            tag = "Sharing Joy"
        ),
        FoodItem(
            id = "combo_couple",
            name = "The Double Couple Deal (2 Pax)",
            description = "Cozy date night poutine combo. 1 Lumberjack Poutine, 1 Mrs. Poutine's Truffle Cream Infusion, plus a shared skillet of crispy cinnamon sweet-potato dessert puffs.",
            price = 30.00,
            category = PoutineCategory.COMBOS,
            rating = 4.8,
            prepTime = "18 min",
            ingredients = listOf("Lumberjack Poutine", "Truffle Cream Infusion", "Cinnamon Sweet-Potato Dessert Puffs"),
            tag = "Date Choice"
        )
    )
}
