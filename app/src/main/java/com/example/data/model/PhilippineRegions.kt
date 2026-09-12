package com.example.data.model

data class PhilippineProvince(
    val name: String,
    val roomTag: String,
    val region: String,
    val topic: String
) {
    val roomId: String
        get() = "room_${name.lowercase().replace(" ", "_").replace("'", "")}"
}

data class PhilippineRegionInfo(
    val id: String,
    val name: String,
    val shortName: String,
    val islandGroup: String, // "Luzon", "Visayas", "Mindanao"
    val description: String,
    val emoji: String,
    val highlights: String
)

object PhilippineRegions {
    val ISLAND_GROUPS = listOf("All", "Luzon", "Visayas", "Mindanao")

    val REGIONS = listOf(
        // Luzon
        PhilippineRegionInfo(
            id = "ncr",
            name = "NCR (Metro Manila)",
            shortName = "Metro Manila",
            islandGroup = "Luzon",
            description = "National Capital Region and economic center of the Philippines",
            emoji = "\uD83C\uDFD9\uFE0F",
            highlights = "Manila, Quezon City, Makati, BGC, Pasig, Mandaluyong"
        ),
        PhilippineRegionInfo(
            id = "car",
            name = "CAR (Cordillera)",
            shortName = "Cordillera",
            islandGroup = "Luzon",
            description = "Pine-covered mountain highlands, rich culture and cool climate",
            emoji = "\u26F0\uFE0F",
            highlights = "Baguio City, Benguet, Sagada, Banaue Rice Terraces"
        ),
        PhilippineRegionInfo(
            id = "region_1",
            name = "Region I (Ilocos)",
            shortName = "Ilocos Region",
            islandGroup = "Luzon",
            description = "Historic cobblestone towns, Pacific coasts and iconic cuisine",
            emoji = "\uD83C\uDF0A",
            highlights = "Vigan, Bangui Windmills, La Union Elyu Surf, Hundred Islands"
        ),
        PhilippineRegionInfo(
            id = "region_2",
            name = "Region II (Cagayan Valley)",
            shortName = "Cagayan Valley",
            islandGroup = "Luzon",
            description = "Vast agricultural river valleys, rolling hills and northern islands",
            emoji = "\uD83C\uDF3E",
            highlights = "Batanes, Cagayan River, Palaui Island, Isabela"
        ),
        PhilippineRegionInfo(
            id = "region_3",
            name = "Region III (Central Luzon)",
            shortName = "Central Luzon",
            islandGroup = "Luzon",
            description = "Culinary capital, vibrant festivals and historical landmarks",
            emoji = "\uD83C\uDFEE",
            highlights = "Pampanga, Bulacan, Subic Zambales, Bataan, Aurora Baler"
        ),
        PhilippineRegionInfo(
            id = "region_4a",
            name = "Region IV-A (CALABARZON)",
            shortName = "CALABARZON",
            islandGroup = "Luzon",
            description = "Volcanic lakes, historic heritage, hot springs and lush mountains",
            emoji = "\uD83C\uDF0B",
            highlights = "Cavite, Tagaytay, Laguna Hot Springs, Batangas, Antipolo"
        ),
        PhilippineRegionInfo(
            id = "region_4b",
            name = "Region IV-B (MIMAROPA)",
            shortName = "MIMAROPA",
            islandGroup = "Luzon",
            description = "World-class tropical islands, coral reefs and pristine beaches",
            emoji = "\uD83C\uDFD6\uFE0F",
            highlights = "Palawan, El Nido, Coron, Puerto Galera, Romblon"
        ),
        PhilippineRegionInfo(
            id = "region_5",
            name = "Region V (Bicol)",
            shortName = "Bicol Region",
            islandGroup = "Luzon",
            description = "Perfect-cone Mayon Volcano, spicy gastronomy and watersports",
            emoji = "\uD83C\uDF36\uFE0F",
            highlights = "Albay Mayon, Caramoan, Donsol Whalesharks, CWC"
        ),

        // Visayas
        PhilippineRegionInfo(
            id = "region_6",
            name = "Region VI (Western Visayas)",
            shortName = "Western Visayas",
            islandGroup = "Visayas",
            description = "World-famous Boracay, heritage districts and colorful festivals",
            emoji = "\uD83C\uDF89",
            highlights = "Iloilo Dinagyang, Boracay, Antique, Capiz, Guimaras"
        ),
        PhilippineRegionInfo(
            id = "nir",
            name = "Negros Island Region (NIR)",
            shortName = "Negros Island Region",
            islandGroup = "Visayas",
            description = "Sugarlandia heritage, mountain landscapes and island coastlines",
            emoji = "\uD83C\uDF3E",
            highlights = "Bacolod, Negros Occidental, Dumaguete, Negros Oriental, Siquijor"
        ),
        PhilippineRegionInfo(
            id = "region_7",
            name = "Region VII (Central Visayas)",
            shortName = "Central Visayas",
            islandGroup = "Visayas",
            description = "Queen City of the South, Chocolate Hills, tarsiers and island diving",
            emoji = "\uD83C\uDFDD\uFE0F",
            highlights = "Cebu City, Mactan, Bohol Chocolate Hills, Panglao"
        ),
        PhilippineRegionInfo(
            id = "region_8",
            name = "Region VIII (Eastern Visayas)",
            shortName = "Eastern Visayas",
            islandGroup = "Visayas",
            description = "Longest bridges, limestone rock formations and historic landings",
            emoji = "\uD83C\uDF09",
            highlights = "San Juanico Bridge, Leyte, Samar Sohoton Caves, Calicoan"
        ),

        // Mindanao
        PhilippineRegionInfo(
            id = "region_9",
            name = "Region IX (Zamboanga)",
            shortName = "Zamboanga Peninsula",
            islandGroup = "Mindanao",
            description = "City of Flowers, pink sand beaches, Spanish-creole heritage",
            emoji = "\uD83C\uDF3A",
            highlights = "Zamboanga City, Dapitan, Sulu, Sta. Cruz Pink Beach"
        ),
        PhilippineRegionInfo(
            id = "region_10",
            name = "Region X (Northern Mindanao)",
            shortName = "Northern Mindanao",
            islandGroup = "Mindanao",
            description = "White water rapids, volcanic island born of fire and pineapple plateaus",
            emoji = "\uD83C\uDF4D",
            highlights = "Cagayan de Oro, Bukidnon, Camiguin Island, Iligan Falls"
        ),
        PhilippineRegionInfo(
            id = "region_11",
            name = "Region XI (Davao)",
            shortName = "Davao Region",
            islandGroup = "Mindanao",
            description = "Majestic Mount Apo peak, durian capital and island garden of Samal",
            emoji = "\uD83E\uDD85",
            highlights = "Davao City, Mt. Apo, Samal Island, Aliwagwag Falls"
        ),
        PhilippineRegionInfo(
            id = "region_12",
            name = "Region XII (SOCCSKSARGEN)",
            shortName = "SOCCSKSARGEN",
            islandGroup = "Mindanao",
            description = "Tuna capital of the Philippines, Lake Sebu waterfalls and T'nalak weaving",
            emoji = "\uD83D\uDC1F",
            highlights = "General Santos City, Lake Sebu 7 Falls, Asik-Asik Falls"
        ),
        PhilippineRegionInfo(
            id = "region_13",
            name = "Region XIII (Caraga)",
            shortName = "Caraga",
            islandGroup = "Mindanao",
            description = "Surfing capital of the Philippines, enchanted rivers and ancient balangay",
            emoji = "\uD83C\uDFC4",
            highlights = "Siargao Island, Hinatuan Enchanted River, Butuan, Tinuy-an"
        ),
        PhilippineRegionInfo(
            id = "barmm",
            name = "BARMM (Bangsamoro)",
            shortName = "Bangsamoro",
            islandGroup = "Mindanao",
            description = "Grand Mosques, scenic Lake Lanao, Torogan royal houses and island atolls",
            emoji = "\uD83D\uDD4C",
            highlights = "Grand Mosque of Cotabato, Lake Lanao, Basilan, Tawi-Tawi"
        )
    )

    fun getRegionInfo(regionName: String): PhilippineRegionInfo {
        return REGIONS.firstOrNull { it.name.equals(regionName, ignoreCase = true) }
            ?: PhilippineRegionInfo(
                id = "custom",
                name = regionName,
                shortName = regionName,
                islandGroup = "Philippines",
                description = "Philippine administrative region",
                emoji = "\uD83C\uDDF5\uD83C\uDDED",
                highlights = regionName
            )
    }

    val REGION_LIST = listOf("All") + REGIONS.map { it.name }

    val PROVINCES = listOf(
        // 1. NCR (Metro Manila)
        PhilippineProvince("Manila", "#Manila", "NCR (Metro Manila)", "Kabisera ng Pilipinas - Intramuros, baywalk sunset, at sentro ng kasaysayan."),
        PhilippineProvince("Quezon City", "#QuezonCity", "NCR (Metro Manila)", "City of Stars - Unibersidad, media networks, parks at sining."),
        PhilippineProvince("Makati", "#Makati", "NCR (Metro Manila)", "Financial capital ng bansa - Ayala Avenue, CBD, komersyo at lifestyle."),
        PhilippineProvince("Taguig", "#Taguig", "NCR (Metro Manila)", "Bonifacio Global City (BGC) - Modernong tech, sining at lifestyle hub."),
        PhilippineProvince("Pasig", "#Pasig", "NCR (Metro Manila)", "Green City - Ortigas Center business district at Kapitolyo food strip."),
        PhilippineProvince("Mandaluyong", "#Mandaluyong", "NCR (Metro Manila)", "Tiger City of the Philippines - Komersyo at sentro ng Metro."),
        PhilippineProvince("Caloocan", "#Caloocan", "NCR (Metro Manila)", "Makasaysayang Monumento ni Bonifacio at hilagang tarangkahan."),
        PhilippineProvince("Parañaque", "#Parañaque", "NCR (Metro Manila)", "Bay City - Entertainment district, coastal trades at airport hub."),
        PhilippineProvince("Pasay", "#Pasay", "NCR (Metro Manila)", "Travel & events hub - NAIA gateway at seaside boardwalk."),
        PhilippineProvince("Las Piñas", "#LasPiñas", "NCR (Metro Manila)", "Lungsod ng makasaysayang Bamboo Organ at asinang pangkabuhayan."),
        PhilippineProvince("Marikina", "#Marikina", "NCR (Metro Manila)", "Shoe Capital ng Pilipinas - Malinis na river park at disiplinadong komunidad."),
        PhilippineProvince("Muntinlupa", "#Muntinlupa", "NCR (Metro Manila)", "Emerald City - Filinvest Alabang business park at timog tarangkahan."),
        PhilippineProvince("Malabon", "#Malabon", "NCR (Metro Manila)", "Heritage city na kilala sa pancit Malabon at makasaysayang bahay."),
        PhilippineProvince("Navotas", "#Navotas", "NCR (Metro Manila)", "Fishing Capital ng Pilipinas at sentro ng kabuhayang pandagat."),
        PhilippineProvince("San Juan", "#SanJuan", "NCR (Metro Manila)", "Makasaysayang Pinaglabanan Shrine at sentrong komersyal ng Metro Manila."),
        PhilippineProvince("Valenzuela", "#Valenzuela", "NCR (Metro Manila)", "Masiglang industriyal at residential gateway ng hilagang Metro Manila."),
        PhilippineProvince("Pateros", "#Pateros", "NCR (Metro Manila)", "Nag-iisang munisipalidad ng Metro Manila, kilala sa balut at alfombra."),

        // 2. CAR (Cordillera)
        PhilippineProvince("Benguet", "#Benguet", "CAR (Cordillera)", "Salad Bowl ng Pilipinas, strawberry fields at City of Pines Baguio."),
        PhilippineProvince("Mountain Province", "#MountainProvince", "CAR (Cordillera)", "Sagada caves, hanging coffins, at fog-kissed pine mountains."),
        PhilippineProvince("Ifugao", "#Ifugao", "CAR (Cordillera)", "Ancient Banaue at Batad Rice Terraces, UNESCO World Heritage."),
        PhilippineProvince("Kalinga", "#Kalinga", "CAR (Cordillera)", "Tahanan ni Apo Whang-od, Chico River at mayamang kultura ng Cordillera."),
        PhilippineProvince("Abra", "#Abra", "CAR (Cordillera)", "Kaparkan cascading falls, likas na batis at habing Tingguian."),
        PhilippineProvince("Apayao", "#Apayao", "CAR (Cordillera)", "Last nature frontier ng Cordillera, underground rivers at gubat."),

        // 3. Region I (Ilocos)
        PhilippineProvince("Ilocos Norte", "#IlocosNorte", "Region I (Ilocos)", "Bangui windmills, Paoay Church, sand dunes at masarap na empanada."),
        PhilippineProvince("Ilocos Sur", "#IlocosSur", "Region I (Ilocos)", "UNESCO Heritage City ng Vigan, Calle Crisologo at kalesa."),
        PhilippineProvince("La Union", "#LaUnion", "Region I (Ilocos)", "ELYU Surfing Capital ng Hilagang Luzon, sunsets at food hubs."),
        PhilippineProvince("Pangasinan", "#Pangasinan", "Region I (Ilocos)", "Hundred Islands National Park, Alaminos, Lingayen Gulf at bangus."),

        // 4. Region II (Cagayan Valley)
        PhilippineProvince("Batanes", "#Batanes", "Region II (Cagayan Valley)", "Tahanan ng mga Ivatan, rolling hills, stone houses at lighthouses."),
        PhilippineProvince("Cagayan", "#Cagayan", "Region II (Cagayan Valley)", "Palaui Island, Callao Caves at ang pinakamahabang ilog sa bansa."),
        PhilippineProvince("Isabela", "#Isabela", "Region II (Cagayan Valley)", "Queen Province ng Hilaga, Magat Dam at mayamang sakahan."),
        PhilippineProvince("Nueva Vizcaya", "#NuevaVizcaya", "Region II (Cagayan Valley)", "Citrus Capital ng Pilipinas at tarangkahan ng lambak."),
        PhilippineProvince("Quirino", "#Quirino", "Region II (Cagayan Valley)", "Governor's Rapids, Aglipay Caves at malinis na ilog."),

        // 5. Region III (Central Luzon)
        PhilippineProvince("Pampanga", "#Pampanga", "Region III (Central Luzon)", "Culinary Capital ng Pilipinas, sisig, Giant Lantern Festival."),
        PhilippineProvince("Bulacan", "#Bulacan", "Region III (Central Luzon)", "Lalawigan ng mga Bayani, Barasoain Church, minasa at pastillas."),
        PhilippineProvince("Zambales", "#Zambales", "Region III (Central Luzon)", "Subic Bay Freeport, Anawangin Cove, mangoes at dalampasigan."),
        PhilippineProvince("Bataan", "#Bataan", "Region III (Central Luzon)", "Mount Samat Shrine of Valor, kasaysayan at industrial zone."),
        PhilippineProvince("Nueva Ecija", "#NuevaEcija", "Region III (Central Luzon)", "Rice Granary ng Pilipinas at sentro ng agrikultura."),
        PhilippineProvince("Tarlac", "#Tarlac", "Region III (Central Luzon)", "Puso ng Gitnang Luzon, tubuhan at mayamang kasaysayan."),
        PhilippineProvince("Aurora", "#Aurora", "Region III (Central Luzon)", "Baler surfing birthplace, Pacific swells at Sierra Madre."),

        // 6. Region IV-A (CALABARZON)
        PhilippineProvince("Cavite", "#Cavite", "Region IV-A (CALABARZON)", "Duyan ng Kasarinlan ng Pilipinas, Tagaytay Ridge at kasaysayan."),
        PhilippineProvince("Laguna", "#Laguna", "Region IV-A (CALABARZON)", "Lawa ng Laguna, Mt. Makiling, bukal, resorts at Pagsanjan Falls."),
        PhilippineProvince("Batangas", "#Batangas", "Region IV-A (CALABARZON)", "Ala eh! Taal Volcano, Anilao diving spots at kapeng barako."),
        PhilippineProvince("Rizal", "#Rizal", "Region IV-A (CALABARZON)", "Duyan ng Sining, Antipolo pilgrimage, bundok at tanawin."),
        PhilippineProvince("Quezon", "#Quezon", "Region IV-A (CALABARZON)", "Pahiyas Festival sa Lucban, Mt. Banahaw at niyugan."),

        // 7. Region IV-B (MIMAROPA)
        PhilippineProvince("Palawan", "#Palawan", "Region IV-B (MIMAROPA)", "Huling Frontera ng Kalikasan, El Nido, Coron at Underground River."),
        PhilippineProvince("Oriental Mindoro", "#OrientalMindoro", "Region IV-B (MIMAROPA)", "Puerto Galera diving sanctuaries at Calapan port."),
        PhilippineProvince("Occidental Mindoro", "#OccidentalMindoro", "Region IV-B (MIMAROPA)", "Apo Reef Natural Park at tahanan ng Tamaraw."),
        PhilippineProvince("Marinduque", "#Marinduque", "Region IV-B (MIMAROPA)", "Puso ng Pilipinas at Moriones Festival."),
        PhilippineProvince("Romblon", "#Romblon", "Region IV-B (MIMAROPA)", "Marble Capital ng Pilipinas, Cresta de Gallo at malinaw na dagat."),

        // 8. Region V (Bicol)
        PhilippineProvince("Albay", "#Albay", "Region V (Bicol)", "Bulkang Mayon, Cagsawa ruins, sili ice cream at Bicol Express."),
        PhilippineProvince("Camarines Sur", "#CamarinesSur", "Region V (Bicol)", "Caramoan pristine islands, CWC wakeboarding at Peñafrancia."),
        PhilippineProvince("Sorsogon", "#Sorsogon", "Region V (Bicol)", "Donsol butanding whale sharks at Lawa ng Bulusan."),
        PhilippineProvince("Catanduanes", "#Catanduanes", "Region V (Bicol)", "The Happy Island, Puraran surfing at Pacific sea cliffs."),
        PhilippineProvince("Camarines Norte", "#CamarinesNorte", "Region V (Bicol)", "Calaguas virgin white sand beaches at Bagasbas surf."),
        PhilippineProvince("Masbate", "#Masbate", "Region V (Bicol)", "Rodeo Capital ng Pilipinas at luntiang rancho."),

        // 9. Region VI (Western Visayas)
        PhilippineProvince("Iloilo", "#Iloilo", "Region VI (Western Visayas)", "Dinagyang Festival, Miagao UNESCO Church at La Paz Batchoy."),
        PhilippineProvince("Aklan", "#Aklan", "Region VI (Western Visayas)", "Mundong-kilalang Boracay Island at Kalibo Ati-Atihan."),
        PhilippineProvince("Antique", "#Antique", "Region VI (Western Visayas)", "Kawa hot bath, Malumpati cold spring at baybayin."),
        PhilippineProvince("Capiz", "#Capiz", "Region VI (Western Visayas)", "Seafood Capital ng Pilipinas at makasaysayang Roxas City."),
        PhilippineProvince("Guimaras", "#Guimaras", "Region VI (Western Visayas)", "Pinakamatamis na mangga sa mundo at mapayapang isla."),

        // 10. Negros Island Region (NIR)
        PhilippineProvince("Negros Occidental", "#NegrosOccidental", "Negros Island Region (NIR)", "Sugarlandia, Bacolod MassKara Festival at The Ruins."),
        PhilippineProvince("Negros Oriental", "#NegrosOriental", "Negros Island Region (NIR)", "Dumaguete City of Gentle People, Apo Island diving."),
        PhilippineProvince("Siquijor", "#Siquijor", "Negros Island Region (NIR)", "Isla ng hiwaga, Cambugahay Falls at puting buhangin."),

        // 11. Region VII (Central Visayas)
        PhilippineProvince("Cebu", "#Cebu", "Region VII (Central Visayas)", "Queen City of the South, Magellan's Cross, lechon at beaches."),
        PhilippineProvince("Bohol", "#Bohol", "Region VII (Central Visayas)", "Chocolate Hills, Philippine Tarsier, Panglao at Loboc River."),

        // 12. Region VIII (Eastern Visayas)
        PhilippineProvince("Leyte", "#Leyte", "Region VIII (Eastern Visayas)", "San Juanico Bridge, Tacloban at MacArthur Landing Memorial."),
        PhilippineProvince("Samar", "#Samar", "Region VIII (Eastern Visayas)", "Sohoton Caves, natural limestone bridge at talon."),
        PhilippineProvince("Eastern Samar", "#EasternSamar", "Region VIII (Eastern Visayas)", "Calicoan Island surfing, Guiuan at simoy ng Pacific."),
        PhilippineProvince("Northern Samar", "#NorthernSamar", "Region VIII (Eastern Visayas)", "Biri rock formations, Capul Island at parola."),
        PhilippineProvince("Southern Leyte", "#SouthernLeyte", "Region VIII (Eastern Visayas)", "Limasawa Island first mass site at whale shark diving."),
        PhilippineProvince("Biliran", "#Biliran", "Region VIII (Eastern Visayas)", "Isla ng mga kamangha-manghang talon at Sambawan paradise."),

        // 13. Region IX (Zamboanga)
        PhilippineProvince("Zamboanga del Sur", "#ZamboangaDelSur", "Region IX (Zamboanga)", "City of Flowers, Fort Pilar, Sta. Cruz pink beach."),
        PhilippineProvince("Zamboanga del Norte", "#ZamboangaDelNorte", "Region IX (Zamboanga)", "Makasaysayang Dapitan Rizal Shrine at Dakak beach."),
        PhilippineProvince("Zamboanga Sibugay", "#ZamboangaSibugay", "Region IX (Zamboanga)", "Sentro ng talaba at kabuhayang pandagat."),
        PhilippineProvince("Sulu", "#Sulu", "Region IX (Zamboanga)", "Perlas ng Dagat Sulu at mayamang kasaysayan ng sultanato."),

        // 14. Region X (Northern Mindanao)
        PhilippineProvince("Misamis Oriental", "#MisamisOriental", "Region X (Northern Mindanao)", "Cagayan de Oro white water rafting at trade center."),
        PhilippineProvince("Bukidnon", "#Bukidnon", "Region X (Northern Mindanao)", "High-altitude pineapple plantations at Mount Kitanglad."),
        PhilippineProvince("Camiguin", "#Camiguin", "Region X (Northern Mindanao)", "Island Born of Fire, Sunken Cemetery at matamis na lansones."),
        PhilippineProvince("Lanao del Norte", "#LanaoDelNorte", "Region X (Northern Mindanao)", "Maria Cristina Falls at City of Majestic Waterfalls."),
        PhilippineProvince("Misamis Occidental", "#MisamisOccidental", "Region X (Northern Mindanao)", "Mount Malindang Range Peace Park at Ozamiz."),

        // 15. Region XI (Davao)
        PhilippineProvince("Davao del Sur", "#DavaoDelSur", "Region XI (Davao)", "Tuktok ng Mount Apo, Durian Capital at Davao City."),
        PhilippineProvince("Davao del Norte", "#DavaoDelNorte", "Region XI (Davao)", "Banana Capital ng Pilipinas, Tagum at Samal Island."),
        PhilippineProvince("Davao Oriental", "#DavaoOriental", "Region XI (Davao)", "Sunrise Capital ng Pilipinas, Aliwagwag Falls at Dahican."),
        PhilippineProvince("Davao de Oro", "#DavaoDeOro", "Region XI (Davao)", "Maragusan cold spring, bundok at mina."),
        PhilippineProvince("Davao Occidental", "#DavaoOccidental", "Region XI (Davao)", "Katimugang baybayin at Sarangani islands."),

        // 16. Region XII (SOCCSKSARGEN)
        PhilippineProvince("South Cotabato", "#SouthCotabato", "Region XII (SOCCSKSARGEN)", "Tuna Capital General Santos, Lake Sebu 7 falls at T'nalak."),
        PhilippineProvince("Cotabato", "#Cotabato", "Region XII (SOCCSKSARGEN)", "Asik-Asik curtain falls, Kidapawan at Mt. Apo trails."),
        PhilippineProvince("Sultan Kudarat", "#SultanKudarat", "Region XII (SOCCSKSARGEN)", "Tacurong bird sanctuary at gintong kapitolyo."),
        PhilippineProvince("Sarangani", "#Sarangani", "Region XII (SOCCSKSARGEN)", "Gumasa white sand beach, paragliding at Maitum jars."),

        // 17. Region XIII (Caraga)
        PhilippineProvince("Surigao del Norte", "#SurigaoDelNorte", "Region XIII (Caraga)", "Siargao Island Surfing Capital of the Philippines, Cloud 9."),
        PhilippineProvince("Surigao del Sur", "#SurigaoDelSur", "Region XIII (Caraga)", "Hinatuan Enchanted River at Tinuy-an Falls."),
        PhilippineProvince("Agusan del Norte", "#AgusanDelNorte", "Region XIII (Caraga)", "Makasaysayang Butuan City at Sinaunang Balangay boats."),
        PhilippineProvince("Agusan del Sur", "#AgusanDelSur", "Region XIII (Caraga)", "Agusan Marsh wildlife sanctuary at ilog."),
        PhilippineProvince("Dinagat Islands", "#DinagatIslands", "Region XIII (Caraga)", "Mahiwagang isla, tidal rock pools at hidden lagoons."),

        // 18. BARMM (Bangsamoro)
        PhilippineProvince("Maguindanao del Norte", "#MaguindanaoDelNorte", "BARMM (Bangsamoro)", "Grand Mosque of Cotabato at makasaysayang kultura."),
        PhilippineProvince("Maguindanao del Sur", "#MaguindanaoDelSur", "BARMM (Bangsamoro)", "Tradisyon ng Inaul weaving at kapatagan."),
        PhilippineProvince("Lanao del Sur", "#LanaoDelSur", "BARMM (Bangsamoro)", "Scenic Lake Lanao, Marawi at sagradong arkitekturang Torogan."),
        PhilippineProvince("Tawi-Tawi", "#TawiTawi", "BARMM (Bangsamoro)", "Pinakatimog na lalawigan ng Pilipinas, Bud Bongao at atolls."),
        PhilippineProvince("Basilan", "#Basilan", "BARMM (Bangsamoro)", "Malamawi white beach at makukulay na habing Yakan.")
    )

    fun provincesFor(regionName: String): List<PhilippineProvince> =
        PROVINCES.filter { it.region.equals(regionName, ignoreCase = true) }

    fun getProvinceByRoomId(roomId: String): PhilippineProvince? =
        PROVINCES.firstOrNull { it.roomId == roomId }
}
