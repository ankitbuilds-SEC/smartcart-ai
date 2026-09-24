package com.example.data.seed

import com.example.data.model.*

object SeedData {

    fun getDemoUsers(): List<UserEntity> = listOf(
        UserEntity(
            id = 1L,
            userCode = "SC-USER-000001",
            email = "demo.customer@example.com",
            passwordHash = "demo123",
            fullName = "Ankit Sharma",
            phone = "+91 98765 43210",
            role = "customer",
            preferredCategories = "Electronics,Audio & Wearables,Gaming,Fashion,Books",
            preferredBrands = "Apple,Sony,Nike,Logitech,Samsung",
            preferredMinPrice = 1500.0,
            preferredMaxPrice = 150000.0,
            riskStatus = "NORMAL"
        ),
        UserEntity(
            id = 2L,
            userCode = "SC-ADMIN-000002",
            email = "demo.admin@example.com",
            passwordHash = "admin123",
            fullName = "SmartCart Admin",
            phone = "+91 98765 00000",
            role = "admin",
            preferredCategories = "All",
            preferredBrands = "All",
            preferredMinPrice = 0.0,
            preferredMaxPrice = 500000.0,
            riskStatus = "NORMAL"
        )
    )

    fun getDemoMerchants(): List<MerchantEntity> = listOf(
        MerchantEntity(
            id = 1L,
            name = "Amazon",
            slug = "amazon",
            logoUrl = "",
            websiteUrl = "https://www.amazon.in",
            affiliateParam = "tag=smartcartai-21",
            status = "ACTIVE",
            isAuthorized = true
        ),
        MerchantEntity(
            id = 2L,
            name = "Flipkart",
            slug = "flipkart",
            logoUrl = "",
            websiteUrl = "https://www.flipkart.com",
            affiliateParam = "affid=smartcartai",
            status = "ACTIVE",
            isAuthorized = true
        ),
        MerchantEntity(
            id = 3L,
            name = "Croma",
            slug = "croma",
            logoUrl = "",
            websiteUrl = "https://www.croma.com",
            affiliateParam = "partner=smartcart",
            status = "ACTIVE",
            isAuthorized = true
        ),
        MerchantEntity(
            id = 4L,
            name = "Myntra",
            slug = "myntra",
            logoUrl = "",
            websiteUrl = "https://www.myntra.com",
            affiliateParam = "src=smartcartai",
            status = "ACTIVE",
            isAuthorized = true
        ),
        MerchantEntity(
            id = 5L,
            name = "Tata CLiQ",
            slug = "tatacliq",
            logoUrl = "",
            websiteUrl = "https://www.tatacliq.com",
            affiliateParam = "ref=smartcart",
            status = "ACTIVE",
            isAuthorized = true
        ),
        MerchantEntity(
            id = 6L,
            name = "Ajio",
            slug = "ajio",
            logoUrl = "",
            websiteUrl = "https://www.ajio.com",
            affiliateParam = "utm_source=smartcart",
            status = "ACTIVE",
            isAuthorized = true
        ),
        MerchantEntity(
            id = 7L,
            name = "Meesho",
            slug = "meesho",
            logoUrl = "",
            websiteUrl = "https://www.meesho.com",
            affiliateParam = "ref=smartcart_deal",
            status = "ACTIVE",
            isAuthorized = true
        )
    )

    fun getProducts(): List<ProductEntity> {
        val list = mutableListOf<ProductEntity>()
        var idCounter = 1L

        fun addP(
            name: String,
            brand: String,
            category: String,
            subcategory: String,
            price: Double,
            discount: Int,
            stock: Int,
            rating: Double,
            reviewCount: Int,
            tags: String,
            desc: String,
            specs: String,
            featured: Boolean = false,
            pop: Double = 75.0
        ) {
            val finalP = price * (1.0 - discount / 100.0)
            list.add(
                ProductEntity(
                    id = idCounter,
                    sku = "SKU-${category.take(3).uppercase()}-${idCounter.toString().padStart(4, '0')}",
                    name = name,
                    slug = name.lowercase().replace(" ", "-").replace("&", "and"),
                    description = desc,
                    brand = brand,
                    category = category,
                    subcategory = subcategory,
                    price = price,
                    discountPercent = discount,
                    finalPrice = finalP,
                    stock = stock,
                    rating = rating,
                    reviewCount = reviewCount,
                    imageUrl = "",
                    tags = tags,
                    specifications = specs,
                    popularityScore = pop,
                    isFeatured = featured,
                    dataSource = "DEMO",
                    providerName = "SmartCart Catalog"
                )
            )
            idCounter++
        }

        // --- 1. Electronics & Laptops ---
        addP("Apple MacBook Pro 14 M3 Pro", "Apple", "Electronics", "Laptops", 199900.0, 10, 24, 4.9, 342, "laptop,apple,m3,macbook,macos,pro", "Supercharged by M3 Pro with 12-core CPU, Liquid Retina XDR display, up to 18 hours battery life.", "Chip: M3 Pro | RAM: 18GB | Storage: 512GB SSD | Screen: 14.2-inch XDR", true, 98.0)
        addP("Dell XPS 15 OLED", "Dell", "Electronics", "Laptops", 175000.0, 12, 18, 4.7, 189, "laptop,dell,xps,oled,windows,creative", "Stunning 3.5K OLED touch display, Intel Core i7 13th Gen, NVIDIA RTX 4060 graphics.", "CPU: Intel i7-13700H | RAM: 32GB | Storage: 1TB SSD | GPU: RTX 4060", true, 92.0)
        addP("ASUS ROG Zephyrus G16", "ASUS", "Electronics", "Laptops", 189990.0, 15, 15, 4.8, 215, "gaming laptop,asus,rog,rtx 4070,oled,fast", "Ultra-slim gaming laptop with 240Hz OLED display, Intel Core Ultra 9, NVIDIA RTX 4070.", "CPU: Intel Ultra 9 | GPU: RTX 4070 8GB | RAM: 32GB | Screen: 16-inch 240Hz", true, 94.0)
        addP("Apple iPad Pro 11-inch M4", "Apple", "Electronics", "Tablets", 99900.0, 5, 35, 4.9, 520, "tablet,apple,ipad,m4,oled,pencil", "Ultra-thin design powered by revolutionary M4 chip with Ultra Retina XDR OLED display.", "Chip: Apple M4 | Storage: 256GB | Display: Tandem OLED 120Hz ProMotion", true, 97.0)
        addP("LG UltraFine 27-inch 4K Monitor", "LG", "Electronics", "Monitors", 34990.0, 20, 45, 4.7, 412, "monitor,lg,4k,ips,hdr,type-c", "Crisp 4K UHD IPS display with USB-C 90W power delivery, HDR400 and sRGB 99% accuracy.", "Resolution: 3840x2160 | Refresh: 60Hz | Ports: USB-C, HDMI x2, DP", false, 88.0)
        addP("Samsung Odyssey OLED G9 49-inch", "Samsung", "Electronics", "Monitors", 129990.0, 18, 10, 4.9, 175, "curved monitor,samsung,odyssey,oled,gaming,ultrawide", "Immersive 49-inch dual QHD curved OLED gaming monitor with 240Hz refresh rate and 0.03ms response.", "Resolution: 5120x1440 | Curve: 1800R | Refresh: 240Hz | Panel: QD-OLED", true, 96.0)

        // --- 2. Smartphones ---
        addP("Samsung Galaxy S24 Ultra", "Samsung", "Smartphones", "Flagship", 129999.0, 10, 40, 4.8, 890, "smartphone,samsung,galaxy,s24,ai,5g,camera", "Galaxy AI is here. Titanium frame, Snapdragon 8 Gen 3, 200MP camera and embedded S Pen.", "Camera: 200MP Quad | Battery: 5000mAh | Display: 6.8 Dynamic AMOLED 2X", true, 99.0)
        addP("Apple iPhone 15 Pro Max", "Apple", "Smartphones", "Flagship", 159900.0, 6, 32, 4.9, 1240, "iphone,apple,ios,15 pro,titanium,5g", "Forged in titanium with groundbreaking A17 Pro chip, Action button, and 5x optical zoom camera.", "Chip: A17 Pro | Camera: 48MP 5x Zoom | Display: 6.7 Super Retina XDR", true, 99.0)
        addP("Google Pixel 8 Pro", "Google", "Smartphones", "Flagship", 106999.0, 15, 25, 4.7, 430, "pixel,google,android,ai,tensor,camera", "The all-pro phone engineered by Google with Tensor G3 chip and next-gen AI camera tools.", "Chip: Google Tensor G3 | Camera: 50MP Triple | Display: 6.7 Super Actua 120Hz", true, 93.0)
        addP("OnePlus 12 5G", "OnePlus", "Smartphones", "Flagship Killer", 64999.0, 8, 48, 4.7, 560, "oneplus,smartphone,5g,snapdragon,fast charge", "Snapdragon 8 Gen 3, 4th Gen Hasselblad Camera, and 100W SUPERVOOC ultra-fast charging.", "RAM: 16GB | Storage: 512GB | Battery: 5400mAh 100W | Display: 2K ProXDR", false, 91.0)
        addP("Nothing Phone (2)", "Nothing", "Smartphones", "Mid-Range", 37999.0, 20, 55, 4.6, 680, "nothing,glyph,smartphone,transparent,design", "Iconic Glyph Interface, Nothing OS 2.5, Snapdragon 8+ Gen 1 and dual 50MP Sony sensors.", "Display: 6.7 LTPO OLED 120Hz | Battery: 4700mAh | OS: Nothing OS", false, 87.0)

        // --- 3. Audio & Wearables ---
        addP("Sony WH-1000XM5 Wireless Headphones", "Sony", "Audio & Wearables", "Headphones", 29990.0, 15, 45, 4.9, 1560, "headphones,sony,anc,wireless,bluetooth,hi-res", "Industry-leading noise cancellation with two processors and 8 microphones. Up to 30 hours battery.", "Drivers: 30mm | Battery: 30h with ANC | Codecs: LDAC, AAC, SBC", true, 99.0)
        addP("Apple AirPods Pro 2 (USB-C)", "Apple", "Audio & Wearables", "Earbuds", 24900.0, 8, 80, 4.8, 2300, "airpods,apple,earbuds,anc,spatial audio,type-c", "Up to 2x more Active Noise Cancellation, Adaptive Audio, and Personalized Spatial Audio.", "Chip: H2 | Case: MagSafe USB-C with speaker | Battery: 6h + 24h case", true, 98.0)
        addP("Bose QuietComfort Ultra Headphones", "Bose", "Audio & Wearables", "Headphones", 35900.0, 10, 25, 4.8, 540, "headphones,bose,quietcomfort,anc,spatial audio", "World-class noise cancellation with revolutionary Bose Immersive Audio spatial sound.", "Battery: 24h | Bluetooth 5.3 | CustomTune audio calibration", false, 92.0)
        addP("Apple Watch Series 9 GPS", "Apple", "Audio & Wearables", "Smartwatches", 41900.0, 7, 50, 4.9, 980, "apple watch,smartwatch,fitness,heart rate,ecg", "Double tap gesture, brighter display, on-device Siri, and advanced health sensors.", "Case: 45mm Aluminum | Battery: 18h | Sensors: Blood Oxygen, ECG, Temp", true, 96.0)
        addP("JBL Flip 6 Portable Bluetooth Speaker", "JBL", "Audio & Wearables", "Speakers", 11999.0, 25, 90, 4.7, 2400, "speaker,jbl,waterproof,bluetooth,bass,portable", "Bold JBL Original Pro Sound with 2-way speaker system and IP67 waterproof/dustproof.", "Battery: 12h | IP67 | PartyBoost pairable", false, 94.0)

        // --- 4. Gaming ---
        addP("Sony PlayStation 5 Slim Console", "Sony", "Gaming", "Consoles", 54990.0, 9, 25, 4.9, 2100, "ps5,playstation,console,gaming,4k,sony", "Experience lightning-fast loading with an ultra-high speed SSD and haptic DualSense controller.", "Storage: 1TB SSD | Video: 4K 120Hz, 8K | Tempest 3D AudioTech", true, 99.0)
        addP("Nintendo Switch OLED Model", "Nintendo", "Gaming", "Handheld", 31999.0, 12, 35, 4.8, 1890, "nintendo,switch,oled,handheld,mario,zelda", "7-inch vibrant OLED screen, wide adjustable stand, wired LAN port and 64GB internal storage.", "Display: 7-inch OLED | Battery: 4.5 - 9h | Modes: Handheld, Tabletop, TV", true, 95.0)
        addP("Logitech G Pro X Superlight 2", "Logitech", "Gaming", "Accessories", 14995.0, 15, 60, 4.9, 870, "mouse,logitech,gaming mouse,lightweight,esports", "Icon of esports mice weighing just 60 grams with LIGHTFORCE hybrid switches and HERO 2 sensor.", "Sensor: HERO 2 32,000 DPI | Weight: 60g | Battery: 95h", false, 93.0)

        // --- 5. Smart Home & Kitchen ---
        addP("Dyson V15 Detect Cordless Vacuum", "Dyson", "Home & Kitchen", "Cleaning", 62900.0, 8, 22, 4.8, 640, "dyson,vacuum,cordless,laser,hepa", "Laser reveals invisible dust on hard floors. Piezo sensor automatically optimizes suction.", "Suction: 230 AW | Run time: 60 mins | Whole-machine HEPA filtration", false, 93.0)
        addP("Instant Pot Duo Crisp 8L Air Fryer", "Instant Pot", "Home & Kitchen", "Kitchen", 18999.0, 25, 40, 4.7, 1150, "instant pot,air fryer,pressure cooker,kitchen", "11-in-1 multi-use cooker that pressure cooks, air fries, roasts, bakes, dehydrates and slow cooks.", "Capacity: 8 Liters | Power: 1500W | 2 interchangeable lids", false, 88.0)
        addP("Philips Hue White & Color Ambiance Kit", "Philips", "Home & Kitchen", "Lighting", 13999.0, 20, 45, 4.8, 890, "philips hue,smart lights,rgb,zigbee,bridge", "Transform your home with 16 million colors, automated schedules, and Hue Bridge.", "Bulbs: 3x E27 1100 lumens | Hue Bridge included", false, 89.0)

        // --- 6. Fashion & Apparel ---
        addP("Nike Air Jordan 1 Retro High OG", "Nike", "Fashion", "Footwear", 16995.0, 5, 30, 4.9, 1850, "sneakers,nike,jordan,shoes,streetwear,basketball", "Iconic basketball shoe crafted from premium leather with encapsulated Air-Sole cushioning.", "Material: Genuine Leather | Sole: Rubber cupsole | Style: High-top", true, 97.0)
        addP("Adidas Ultraboost Light Running Shoes", "Adidas", "Fashion", "Footwear", 18999.0, 20, 45, 4.8, 1240, "running shoes,adidas,ultraboost,boost,lightweight", "Lightest Ultraboost ever made with 30% lighter Light BOOST material and Primeknit+ upper.", "Weight: 299g | Midsole: Light BOOST | Outsole: Continental Rubber", true, 92.0)
        addP("Levi's 511 Slim Fit Stretch Jeans", "Levi's", "Fashion", "Men's Apparel", 3999.0, 25, 80, 4.6, 3400, "jeans,levis,denim,slim fit,casual,pants", "Modern slim-cut with room to move. Crafted from premium stretch denim for all-day comfort.", "Fit: Slim | Rise: Mid | Fabric: 99% Cotton, 1% Elastane", false, 90.0)
        addP("Ray-Ban Aviator Classic Sunglasses", "Ray-Ban", "Fashion", "Accessories", 10590.0, 12, 60, 4.8, 2800, "sunglasses,rayban,aviator,classic,eyewear,uv", "Originally designed for U.S. aviators in 1937 with crystal green G-15 UV protection lenses.", "Frame: Polished Gold Metal | Lens: G-15 Green Crystal | 100% UV400", false, 94.0)
        addP("Casio G-Shock GA-2100 'CasiOak'", "Casio", "Fashion", "Watches", 8995.0, 10, 75, 4.8, 1920, "watch,casio,g-shock,casioak,rugged,matte black", "Carbon Core Guard structure with octagonal bezel and ultra-thin 11.8mm shock-resistant case.", "Water Resistance: 200m | Battery: 3 years | Dual LED light", false, 91.0)
        addP("Bellroy Classic Backpack Plus 24L", "Bellroy", "Fashion", "Bags", 16900.0, 8, 28, 4.8, 380, "backpack,bellroy,laptop bag,minimalist,travel", "Refined urban backpack with dual-compartment layout and separate 16-inch laptop pocket.", "Capacity: 24L | Material: Recycled Dura Nylon | Water resistant zippers", false, 86.0)

        // --- 7. Beauty & Personal Care ---
        addP("Estée Lauder Advanced Night Repair Serum", "Estée Lauder", "Beauty", "Skincare", 8900.0, 15, 60, 4.9, 1420, "skincare,serum,anti-aging,hydration,night repair", "Revolutionary multi-recovery complex serum that reduces key signs of aging and boosts radiance.", "Volume: 50ml | Skin Type: All | Key: Hyaluronic Acid & Chronolux", true, 95.0)
        addP("Dyson Airwrap Multi-Styler Complete Long", "Dyson", "Beauty", "Haircare", 49900.0, 5, 25, 4.8, 980, "hair styler,dyson,airwrap,curler,coanda effect", "Curl, shape, smooth and hide flyaways using the Coanda effect without extreme heat.", "Attachments: 6 styling heads | Heat Control: Intelligent glass bead thermistor", true, 96.0)
        addP("Dior Sauvage Eau de Parfum 100ml", "Dior", "Beauty", "Fragrance", 12500.0, 10, 45, 4.9, 2100, "perfume,dior,sauvage,fragrance,luxury,men", "Noble freshness with Calabrian bergamot and the mysterious trail of Papua New Guinean vanilla.", "Volume: 100ml | Concentration: Eau de Parfum | Fragrance Family: Fresh Spicy", true, 97.0)
        addP("Laneige Lip Sleeping Mask Berry 20g", "Laneige", "Beauty", "Skincare", 1400.0, 20, 150, 4.8, 3400, "lip balm,laneige,k-beauty,moisturizer,berry", "Enriched with Berry Fruit Complex and Vitamin C to deliver intense moisture overnight.", "Weight: 20g | Key Ingredients: Vitamin C, Shea Butter, Coconut Oil", false, 91.0)

        // --- 8. Toys & Games ---
        addP("LEGO Star Wars Millennium Falcon 75257", "LEGO", "Toys", "Building Sets", 14999.0, 10, 30, 4.9, 870, "lego,star wars,toys,building,falcon,kids,collector", "Iconic Corellian freighter with rotating top and bottom gun turrets, 2 spring-loaded shooters and 7 minifigures.", "Pieces: 1351 | Age: 9+ | Model: 75257", true, 94.0)
        addP("Monopoly Ultimate Banking Board Game", "Hasbro", "Toys", "Board Games", 2499.0, 25, 90, 4.7, 1600, "monopoly,hasbro,board game,family game,kids,party", "Fast-paced tap-and-play banking unit replaces cash. Instant financial transactions and property cards.", "Players: 2-4 | Age: 8+ | Requires 3x AAA batteries", false, 88.0)
        addP("Educational Solar Robot Kit 14-in-1", "StemKid", "Toys", "Educational", 1899.0, 30, 120, 4.6, 920, "stem,educational toys,robot,solar,science kit,10 year old", "Build 14 different motorized solar-powered robots that crawl, roll, and float on water.", "Ages: 8-12 | Science: Green Energy Solar & Mechanics | No batteries needed", false, 87.0)

        // --- 9. Sports & Fitness ---
        addP("Bowflex SelectTech 552 Dumbbells", "Bowflex", "Sports", "Strength", 38990.0, 12, 16, 4.9, 1420, "dumbbells,bowflex,strength,weights,home gym", "Replaces 15 sets of weights in one compact system that adjusts from 2kg to 24kg per dumbbell.", "Adjustment: 2kg to 24kg per hand | Selection dial | Storage trays included", true, 95.0)
        addP("Manduka PRO Yoga Mat 6mm", "Manduka", "Sports", "Yoga", 11999.0, 10, 40, 4.9, 870, "yoga mat,manduka,cushion,grip,durable", "The legendary yoga mat backed by a lifetime guarantee with ultra-dense 6mm cushioning.", "Thickness: 6mm | Dimensions: 180cm x 66cm | Closed-cell surface", false, 91.0)
        addP("SS Ton Reserve Edition Cricket Bat", "SS", "Sports", "Cricket", 24999.0, 15, 20, 4.8, 380, "cricket bat,ss ton,english willow,sports,cricket", "Handcrafted from grade 1 English willow with thick edges and massive sweet spot.", "Material: Grade 1 English Willow | Weight: 1180g | Short handle", false, 89.0)

        // --- 10. Books ---
        addP("Atomic Habits by James Clear", "Penguin", "Books", "Self-Help", 799.0, 30, 150, 4.9, 9800, "book,atomic habits,james clear,self-improvement,bestseller", "An easy and proven way to build good habits and break bad ones. Over 15 million copies sold.", "Pages: 320 | Format: Hardcover | Publisher: Penguin Random House", true, 99.0)
        addP("Designing Data-Intensive Applications", "O'Reilly", "Books", "Technology", 2499.0, 20, 80, 4.9, 3400, "book,software,database,system design,architecture,oreilly", "The definitive guide to the principles and architectures of scalable, reliable data systems.", "Author: Martin Kleppmann | Pages: 616 | Publisher: O'Reilly Media", true, 98.0)
        addP("Sapiens: A Brief History of Humankind", "Vintage", "Books", "History", 699.0, 25, 120, 4.8, 8600, "book,sapiens,history,yuval harari,anthropology", "100,000 years ago, at least six human species inhabited the earth. Today there is just one. Us.", "Author: Yuval Noah Harari | Pages: 498 | Publisher: Vintage", false, 95.0)

        // --- 11. Grocery & Gourmet ---
        addP("Blue Tokai Vienna Roast Coffee Beans 500g", "Blue Tokai", "Grocery", "Beverages", 890.0, 10, 80, 4.8, 1240, "coffee,blue tokai,arabica,whole beans,dark roast", "Specialty 100% Arabica artisanal roasted coffee with notes of dark chocolate and toasted walnut.", "Origin: Chikkamagaluru | Roast: Dark | Weight: 500g", false, 90.0)
        addP("Organic India Tulsi Green Tea 100 Bags", "Organic India", "Grocery", "Beverages", 550.0, 15, 120, 4.7, 2100, "green tea,tulsi,organic,health,beverage,antioxidants", "Certified organic whole leaf green tea blended with Rama, Krishna and Vana Tulsi.", "Count: 100 Tea Bags | USDA Organic | Caffeine: Low", false, 86.0)

        // --- 12. Automotive ---
        addP("70mai Pro Plus+ A500S Dual Dash Cam", "70mai", "Automotive", "Electronics", 11999.0, 18, 40, 4.7, 850, "dash cam,car camera,70mai,gps,night vision,car safety", "Front 1944P and rear 1080P dual recording with Sony sensor, built-in GPS and ADAS.", "Resolution: 2.7K Front + 1080p Rear | Night Owl Vision | 24h Parking Guard", true, 91.0)
        addP("3M Complete Car Care Kit 5-in-1", "3M", "Automotive", "Car Care", 1899.0, 25, 65, 4.6, 1450, "car wash,3m,shampoo,wax,microfiber,cleaner,car polish", "Professional grade car care kit containing Car Wash Shampoo, Tyre Dresser, Dashboard Dresser and Wax.", "Includes: 4 Care Liquids + Microfiber Cloth | Safe on all paints", false, 85.0)

        // --- 13. Travel & Luggage ---
        addP("Samsonite Freeform Hardside Spinner 28", "Samsonite", "Travel", "Luggage", 18990.0, 20, 30, 4.8, 920, "luggage,suitcase,samsonite,travel,hardshell,spinner", "Ultra-light, ultra-strong polypropylene shell with recessed TSA combination lock and double spinner wheels.", "Material: Polypropylene | Size: 28-inch Checked | Weight: 4.3kg", true, 92.0)
        addP("Peak Design Travel Backpack 45L", "Peak Design", "Travel", "Bags", 34990.0, 10, 15, 4.9, 410, "travel backpack,peak design,carry on,camera bag,weatherproof", "Versatile carry-on backpack with dual side access, tuck-away straps and weatherproof 400D nylon canvas.", "Volume: 45L expanded | Laptop: Up to 16-inch | Lifetime Warranty", false, 93.0)

        // --- 14. Pet Products ---
        addP("Furbo 360 Dog Camera with Treat Tossing", "Furbo", "Pet Products", "Smart Pet", 19999.0, 15, 25, 4.7, 540, "pet camera,dog,treat tosser,furbo,wifi,barking alert", "360-degree rotating view in full 1080p HD, color night vision, 2-way audio and treat flinger.", "Field of View: 360 Rotating | Audio: 2-Way with Real-time Bark Alert", false, 88.0)
        addP("Royal Canin Maxi Adult Dog Food 15kg", "Royal Canin", "Pet Products", "Pet Food", 7990.0, 12, 50, 4.8, 1890, "dog food,royal canin,maxi adult,nutrition,pets", "Tailored nutrition for large breed dogs from 15 months to 5 years with EPA/DHA for bone & joint support.", "Weight: 15kg | Breed Size: 26-44kg | High Digestibility Protein", false, 91.0)

        // --- 15. Office & Stationery ---
        addP("Lamy 2000 Makrolon Fountain Pen", "Lamy", "Office & Stationery", "Writing", 18900.0, 10, 35, 4.9, 680, "fountain pen,lamy,luxury,writing,stationery,makrolon", "Timeless Bauhaus design masterpiece made of fiberglass reinforced Makrolon with 14k gold nib.", "Nib: 14K Gold Platinum-plated | Piston filling system | Made in Germany", true, 95.0)
        addP("Moleskine Classic Leather Hardcover Notebook", "Moleskine", "Office & Stationery", "Notebooks", 2199.0, 15, 90, 4.8, 2300, "notebook,moleskine,journal,leather,stationery,planner", "Legendary notebook with ivory acid-free paper, expandable inner pocket and elastic closure.", "Pages: 240 Ruled | Paper: 70 gsm acid-free | Size: Large 13x21cm", false, 92.0)

        return list
    }

    fun getDemoOffers(products: List<ProductEntity>): List<MerchantOfferEntity> {
        val offers = mutableListOf<MerchantOfferEntity>()
        var idCounter = 1L
        val now = System.currentTimeMillis()

        for (p in products) {
            // Offer 1: Amazon
            val amzDiscount = p.discountPercent + ((-2..3).random().coerceAtLeast(0))
            val amzPrice = p.price * (1.0 - amzDiscount / 100.0)
            offers.add(
                MerchantOfferEntity(
                    id = idCounter++,
                    merchantId = 1L,
                    productId = p.id,
                    merchantName = "Amazon",
                    currentPrice = amzPrice,
                    originalPrice = p.price,
                    discountPercent = amzDiscount,
                    couponCode = if (p.finalPrice > 5000) "AMZSAVE500" else "",
                    couponDiscount = if (p.finalPrice > 5000) "₹500 off" else "",
                    externalUrl = "https://www.amazon.in/dp/B0${p.sku.replace("-", "").take(8)}",
                    affiliateUrl = "https://www.amazon.in/dp/B0${p.sku.replace("-", "").take(8)}?tag=smartcartai-21",
                    availability = if (p.stock > 0) "In Stock (Prime Available)" else "Out of Stock",
                    deliveryInfo = "Next Day Delivery Available",
                    isLive = false,
                    lastUpdated = now - (10..180).random() * 60000L
                )
            )

            // Offer 2: Flipkart
            val fkDiscount = p.discountPercent + ((-3..4).random().coerceAtLeast(0))
            val fkPrice = p.price * (1.0 - fkDiscount / 100.0)
            offers.add(
                MerchantOfferEntity(
                    id = idCounter++,
                    merchantId = 2L,
                    productId = p.id,
                    merchantName = "Flipkart",
                    currentPrice = fkPrice,
                    originalPrice = p.price,
                    discountPercent = fkDiscount,
                    couponCode = if (fkDiscount > 15) "FKDEAL" else "",
                    couponDiscount = if (fkDiscount > 15) "Extra 5% off on Axis Card" else "",
                    externalUrl = "https://www.flipkart.com/item/${p.slug}/p/itm${p.id}123",
                    affiliateUrl = "https://www.flipkart.com/item/${p.slug}/p/itm${p.id}123?affid=smartcartai",
                    availability = if (p.stock > 0) "In Stock (Flipkart Assured)" else "Out of Stock",
                    deliveryInfo = "Delivery by Tomorrow, 5 PM",
                    isLive = false,
                    lastUpdated = now - (15..240).random() * 60000L
                )
            )

            // Offer 3: Merchant according to category (Croma for electronics, Myntra for fashion/beauty, etc.)
            val thirdMerchant = when (p.category) {
                "Fashion", "Beauty" -> Pair(4L, "Myntra")
                "Electronics", "Smartphones", "Audio & Wearables", "Gaming" -> Pair(3L, "Croma")
                "Sports", "Travel" -> Pair(5L, "Tata CLiQ")
                else -> Pair(6L, "Ajio")
            }

            val thirdDiscount = p.discountPercent + ((-4..5).random().coerceAtLeast(0))
            val thirdPrice = p.price * (1.0 - thirdDiscount / 100.0)
            val thirdDomain = when (thirdMerchant.first) {
                3L -> "https://www.croma.com/p/${p.id}09"
                4L -> "https://www.myntra.com/${p.slug}/${p.id}"
                5L -> "https://www.tatacliq.com/p-mp${p.id}99"
                else -> "https://www.ajio.com/p/${p.id}44"
            }

            offers.add(
                MerchantOfferEntity(
                    id = idCounter++,
                    merchantId = thirdMerchant.first,
                    productId = p.id,
                    merchantName = thirdMerchant.second,
                    currentPrice = thirdPrice,
                    originalPrice = p.price,
                    discountPercent = thirdDiscount,
                    couponCode = if (thirdDiscount > 10) "FLASH10" else "",
                    couponDiscount = if (thirdDiscount > 10) "10% off with coupon" else "",
                    externalUrl = thirdDomain,
                    affiliateUrl = "$thirdDomain?ref=smartcartai",
                    availability = "In Stock (Authorized Dealer)",
                    deliveryInfo = "Standard Delivery (2-3 Days)",
                    isLive = false,
                    lastUpdated = now - (30..300).random() * 60000L
                )
            )
        }

        return offers
    }

    fun getDemoPriceHistory(products: List<ProductEntity>): List<PriceHistoryEntity> {
        val list = mutableListOf<PriceHistoryEntity>()
        var idCounter = 1L
        val day = 86400000L
        val now = System.currentTimeMillis()

        for (p in products.take(25)) {
            val p1 = p.price * 0.98
            val p2 = p.price * 0.92
            val p3 = p.finalPrice

            list.add(PriceHistoryEntity(id = idCounter++, productId = p.id, merchantName = "Amazon", price = p1, recordedAt = now - 30 * day))
            list.add(PriceHistoryEntity(id = idCounter++, productId = p.id, merchantName = "Amazon", price = p2, recordedAt = now - 14 * day))
            list.add(PriceHistoryEntity(id = idCounter++, productId = p.id, merchantName = "Amazon", price = p3, recordedAt = now - 2 * day))
        }

        return list
    }

    fun getDemoPromoCampaigns(): List<PromoCampaignEntity> {
        val now = System.currentTimeMillis()
        val day = 86400000L
        return listOf(
            PromoCampaignEntity(
                id = 1L,
                promoCode = "SMART10",
                campaignName = "SmartCart AI Kickoff Discount",
                discountType = "PERCENTAGE",
                discountValue = 10.0,
                minOrderValue = 2000.0,
                maxDiscount = 500.0,
                startDate = now - 5 * day,
                expiryDate = now + 25 * day,
                usageLimit = 5000,
                perUserLimit = 1,
                status = "ACTIVE"
            ),
            PromoCampaignEntity(
                id = 2L,
                promoCode = "SUPERAI20",
                campaignName = "AI Concierge Exclusive Deal",
                discountType = "PERCENTAGE",
                discountValue = 20.0,
                minOrderValue = 3500.0,
                maxDiscount = 1000.0,
                startDate = now - 2 * day,
                expiryDate = now + 15 * day,
                usageLimit = 2000,
                perUserLimit = 1,
                status = "ACTIVE"
            ),
            PromoCampaignEntity(
                id = 3L,
                promoCode = "WELCOME500",
                campaignName = "New Shopper First Order",
                discountType = "FIXED",
                discountValue = 500.0,
                minOrderValue = 4000.0,
                maxDiscount = 500.0,
                startDate = now - 10 * day,
                expiryDate = now + 60 * day,
                usageLimit = 10000,
                perUserLimit = 1,
                status = "ACTIVE"
            )
        )
    }

    fun getDemoMerchantCoupons(): List<MerchantCouponEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            MerchantCouponEntity(
                id = 1L,
                merchantId = 1L,
                merchantName = "Amazon",
                code = "AMZTECH500",
                discountDescription = "Flat ₹500 off on Premium Electronics",
                conditions = "Applicable on orders above ₹10,000 on Amazon website",
                expiryDate = "31 Oct 2026",
                source = "Verified Amazon Deals Feed",
                verifiedAt = now - 3600000L,
                externalUrl = "https://www.amazon.in"
            ),
            MerchantCouponEntity(
                id = 2L,
                merchantId = 2L,
                merchantName = "Flipkart",
                code = "FKFASHION200",
                discountDescription = "Instant ₹200 off on Fashion & Shoes",
                conditions = "Valid on minimum cart value of ₹1,499",
                expiryDate = "15 Nov 2026",
                source = "Flipkart Affiliate Partner Feed",
                verifiedAt = now - 7200000L,
                externalUrl = "https://www.flipkart.com"
            ),
            MerchantCouponEntity(
                id = 3L,
                merchantId = 4L,
                merchantName = "Myntra",
                code = "MYNTRAFIRST",
                discountDescription = "15% off up to ₹700 on Footwear & Bags",
                conditions = "Applicable on select apparel and accessories",
                expiryDate = "30 Nov 2026",
                source = "Official Merchant Network",
                verifiedAt = now - 1800000L,
                externalUrl = "https://www.myntra.com"
            ),
            MerchantCouponEntity(
                id = 4L,
                merchantId = 3L,
                merchantName = "Croma",
                code = "CROMAAPP",
                discountDescription = "₹750 instant discount on Smart Devices",
                conditions = "Valid on purchase of smart home or audio above ₹5,000",
                expiryDate = "31 Dec 2026",
                source = "Authorized Croma Feed",
                verifiedAt = now - 5400000L,
                externalUrl = "https://www.croma.com"
            )
        )
    }

    fun getDemoVouchers(): List<VoucherListingEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            VoucherListingEntity(
                id = 1L,
                sellerUserId = 1L,
                sellerName = "Ankit Sharma",
                sellerEmail = "demo.customer@example.com",
                brand = "Amazon Pay",
                voucherType = "Gift Card",
                faceValue = 2000.0,
                sellingPrice = 1850.0,
                discountPercent = 8,
                expiryDate = "31 Dec 2026",
                terms = "Redeemable on Amazon India for all physical goods and utility bills.",
                verificationInfo = "E-Gift voucher ending in ***7842, balance verified",
                quantity = 1,
                status = "VERIFIED",
                sellerConfirmationAgreed = true,
                createdAt = now - 48 * 3600000L,
                verifiedAt = now - 40 * 3600000L
            ),
            VoucherListingEntity(
                id = 2L,
                sellerUserId = 2L,
                sellerName = "Kunal Verma",
                sellerEmail = "kunal.v@example.com",
                brand = "Flipkart",
                voucherType = "Store Voucher",
                faceValue = 5000.0,
                sellingPrice = 4600.0,
                discountPercent = 8,
                expiryDate = "15 Jan 2027",
                terms = "Valid across all Flipkart categories. Can be clubbed with bank offers.",
                verificationInfo = "Physical gift card voucher ending in ***1190",
                quantity = 1,
                status = "VERIFIED",
                sellerConfirmationAgreed = true,
                createdAt = now - 36 * 3600000L,
                verifiedAt = now - 30 * 3600000L
            ),
            VoucherListingEntity(
                id = 3L,
                sellerUserId = 1L,
                sellerName = "Ankit Sharma",
                sellerEmail = "demo.customer@example.com",
                brand = "Myntra",
                voucherType = "E-Gift Card",
                faceValue = 1500.0,
                sellingPrice = 1250.0,
                discountPercent = 17,
                expiryDate = "28 Feb 2027",
                terms = "Legally transferable digital gift voucher for Myntra online store.",
                verificationInfo = "Alphanumeric coupon code with PIN attached",
                quantity = 1,
                status = "VERIFIED",
                sellerConfirmationAgreed = true,
                createdAt = now - 24 * 3600000L,
                verifiedAt = now - 18 * 3600000L
            ),
            VoucherListingEntity(
                id = 4L,
                sellerUserId = 1L,
                sellerName = "Ankit Sharma",
                sellerEmail = "demo.customer@example.com",
                brand = "Croma",
                voucherType = "Gift Card",
                faceValue = 3000.0,
                sellingPrice = 2700.0,
                discountPercent = 10,
                expiryDate = "31 Mar 2027",
                terms = "Redeemable both online at croma.com and all physical Croma stores.",
                verificationInfo = "Barcode verified, 16-digit card ending in ***9012",
                quantity = 1,
                status = "VERIFIED",
                sellerConfirmationAgreed = true,
                createdAt = now - 12 * 3600000L,
                verifiedAt = now - 6 * 3600000L
            ),
            VoucherListingEntity(
                id = 5L,
                sellerUserId = 1L,
                sellerName = "Ankit Sharma",
                sellerEmail = "demo.customer@example.com",
                brand = "Apple App Store",
                voucherType = "Digital Code",
                faceValue = 1000.0,
                sellingPrice = 900.0,
                discountPercent = 10,
                expiryDate = "31 Dec 2027",
                terms = "Redeemable for Apple Services, Apps, Games, and iCloud subscription.",
                verificationInfo = "Apple gift code ending in ***ABCD - pending admin review",
                quantity = 1,
                status = "PENDING_VERIFICATION",
                sellerConfirmationAgreed = true,
                createdAt = now - 2 * 3600000L,
                verifiedAt = null
            )
        )
    }

    fun getDemoNotifications(): List<NotificationEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            NotificationEntity(
                id = 1L,
                userId = 1L,
                title = "Price Drop Alert!",
                message = "The Sony WH-1000XM5 headphones you are tracking dropped by ₹4,499 on Amazon!",
                type = "PRICE_DROP",
                isRead = false,
                createdAt = now - 3600000L
            ),
            NotificationEntity(
                id = 2L,
                userId = 1L,
                title = "Voucher Listing Approved",
                message = "Your Amazon Pay ₹2,000 Gift Card was verified and is now live in the Voucher Marketplace.",
                type = "VOUCHER_STATUS",
                isRead = false,
                createdAt = now - 8 * 3600000L
            ),
            NotificationEntity(
                id = 3L,
                userId = 1L,
                title = "New Deal Match",
                message = "Top deal in Audio: Apple AirPods Pro 2 currently 8% off + extra merchant coupon available.",
                type = "DEAL",
                isRead = true,
                createdAt = now - 24 * 3600000L
            )
        )
    }

    fun getInitialEvents(): List<UserEventEntity> {
        val now = System.currentTimeMillis()
        val hour = 3600000L
        return listOf(
            UserEventEntity(userId = 1L, productId = 1L, eventType = "view", metadata = "home_featured", timestamp = now - 48 * hour),
            UserEventEntity(userId = 1L, productId = 1L, eventType = "click", metadata = "product_detail", timestamp = now - 48 * hour + 60000),
            UserEventEntity(userId = 1L, productId = 12L, eventType = "view", metadata = "search:sony headphones", timestamp = now - 36 * hour),
            UserEventEntity(userId = 1L, productId = 12L, eventType = "wishlist", metadata = "saved_for_later", timestamp = now - 36 * hour + 45000),
            UserEventEntity(userId = 1L, productId = 17L, eventType = "view", metadata = "category:gaming", timestamp = now - 24 * hour),
            UserEventEntity(userId = 1L, productId = 17L, eventType = "cart_add", metadata = "qty_1", timestamp = now - 24 * hour + 120000),
            UserEventEntity(userId = 1L, productId = 12L, eventType = "merchant_click", metadata = "merchant_Amazon", timestamp = now - 8 * hour),
            UserEventEntity(userId = 1L, productId = 12L, eventType = "rate", metadata = "rating_5_stars", timestamp = now - 4 * hour)
        )
    }

    fun getInitialReviews(): List<ReviewEntity> {
        val now = System.currentTimeMillis()
        val hour = 3600000L
        return listOf(
            ReviewEntity(productId = 1L, userId = 1L, userName = "Ankit S.", rating = 5, reviewText = "Incredible machine! The M3 Pro chip handles heavy 4K video editing without breaking a sweat and battery lasts literally all day. Outstanding screen.", sentiment = "Positive", sentimentScore = 0.98, helpfulVotes = 24, createdAt = now - 40 * hour),
            ReviewEntity(productId = 12L, userId = 1L, userName = "Ankit S.", rating = 5, reviewText = "Best noise cancellation on the market. Extremely comfortable for long flights and work calls. Sound is crisp, punchy, and clear.", sentiment = "Positive", sentimentScore = 0.96, helpfulVotes = 42, createdAt = now - 4 * hour),
            ReviewEntity(productId = 17L, userId = 2L, userName = "Rohit K.", rating = 5, reviewText = "PlayStation 5 Slim is sleek and super quiet. Spider-Man 2 looks mindblowing in 60fps performance mode. Worth every rupee!", sentiment = "Positive", sentimentScore = 0.95, helpfulVotes = 31, createdAt = now - 30 * hour),
            ReviewEntity(productId = 23L, userId = 1L, userName = "Ankit S.", rating = 5, reviewText = "Air Jordan 1s look incredible and the leather quality is premium. Definitely turns heads everywhere.", sentiment = "Positive", sentimentScore = 0.94, helpfulVotes = 18, createdAt = now - 10 * hour),
            ReviewEntity(productId = 33L, userId = 2L, userName = "Priya N.", rating = 5, reviewText = "Life changing book! Small 1% improvements really compound over time. Very practical actionable advice.", sentiment = "Positive", sentimentScore = 0.99, helpfulVotes = 85, createdAt = now - 60 * hour)
        )
    }

    fun getInitialOrders(): Pair<List<OrderEntity>, List<OrderItemEntity>> {
        val now = System.currentTimeMillis()
        val orders = listOf(
            OrderEntity(
                id = 1L,
                orderNumber = "ORD-2026-9812",
                userId = 1L,
                totalAmount = 39485.0,
                discountAmount = 3000.0,
                shippingFee = 0.0,
                finalAmount = 36485.0,
                status = "Delivered",
                shippingAddress = "42, Silicon Valley Avenue, Koramangala, Bengaluru, Karnataka 560034",
                paymentMethod = "Simulated UPI (Google Pay)",
                paymentStatus = "Paid",
                trackingNumber = "BLR-TRK-892147",
                createdAt = now - 120 * 3600000L
            )
        )

        val items = listOf(
            OrderItemEntity(id = 1L, orderId = 1L, productId = 12L, productName = "Sony WH-1000XM5 Wireless Headphones", productPrice = 25491.5, quantity = 1, subtotal = 25491.5)
        )

        return Pair(orders, items)
    }

    fun getInitialExperiment(): AIExperimentEntity {
        return AIExperimentEntity(
            id = 1L,
            experimentName = "Homepage Recommendation Engine AB Test",
            variantA = "Variant A: Popularity Baseline",
            variantB = "Variant B: Hybrid AI Engine",
            impressionsA = 1240,
            impressionsB = 1285,
            clicksA = 78,
            clicksB = 194,
            conversionsA = 14,
            conversionsB = 48,
            revenueA = 89400.0,
            revenueB = 348900.0,
            isActive = true
        )
    }
}
