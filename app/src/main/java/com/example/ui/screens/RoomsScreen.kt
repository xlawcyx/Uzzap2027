package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatroomEntity
import com.example.data.model.PhilippineRegionInfo
import com.example.data.model.PhilippineRegions
import com.example.ui.components.EmptyListState
import com.example.ui.components.RefreshableScreen
import com.example.ui.theme.UzzapOrange

@Composable
fun RoomsScreen(
    rooms: List<ChatroomEntity>,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    onRoomClick: (ChatroomEntity) -> Unit,
    onToggleJoin: (String, Boolean) -> Unit,
    onCreateRoomClick: () -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier,
    selectedRegion: String? = null,
    onSelectRegion: (String?) -> Unit = {}
) {
    // If selectedRegion is passed from outside, use it; otherwise fallback to internal navigation
    var localSelectedRegion by rememberSaveable { mutableStateOf<String?>(null) }
    val effectiveSelectedRegion = selectedRegion ?: localSelectedRegion

    fun updateSelectedRegion(region: String?) {
        localSelectedRegion = region
        onSelectRegion(region)
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedIslandGroup by rememberSaveable { mutableStateOf("All") }
    var provinceFilterJoinedOnly by rememberSaveable { mutableStateOf(false) }

    // Intercept back navigation when inside a region's provinces
    BackHandler(enabled = effectiveSelectedRegion != null) {
        updateSelectedRegion(null)
    }

    RefreshableScreen(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        if (effectiveSelectedRegion == null) {
            // ==================== LEVEL 1: REGIONS VIEW ====================
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
            ) {
                // Search field (matches either region or province)
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search region, province, or city (e.g. Cebu, NCR, Davao)...",
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UzzapOrange,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("search_regions_field")
                    )
                }

                // If searching, show direct matching provinces for fast jump
                if (searchQuery.isNotBlank()) {
                    val query = searchQuery.trim().lowercase()
                    val matchingProvinces = rooms.filter { room ->
                        room.name.lowercase().contains(query) ||
                            room.topic.lowercase().contains(query) ||
                            room.category.lowercase().contains(query)
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MATCHING PROVINCES (${matchingProvinces.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    if (matchingProvinces.isEmpty()) {
                        item {
                            EmptyListState(
                                title = "No Chatrooms Found",
                                subtitle = "No provinces or regions matched '$searchQuery'"
                            )
                        }
                    } else {
                        items(matchingProvinces, key = { it.id }) { room ->
                            ProvinceChatroomRow(
                                room = room,
                                onEnterClick = { onRoomClick(room) },
                                onToggleJoin = { onToggleJoin(room.id, !room.isJoined) }
                            )
                        }
                    }
                } else {
                    // Island Group Filter Chips
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "EXPLORE BY ISLAND GROUP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val filterOptions = listOf("All", "Luzon", "Visayas", "Mindanao", "My Joined Rooms")
                                items(filterOptions) { option ->
                                    val isSelected = selectedIslandGroup == option
                                    val badgeCount = when (option) {
                                        "All" -> PhilippineRegions.REGIONS.size
                                        "Luzon" -> PhilippineRegions.REGIONS.count { it.islandGroup == "Luzon" }
                                        "Visayas" -> PhilippineRegions.REGIONS.count { it.islandGroup == "Visayas" }
                                        "Mindanao" -> PhilippineRegions.REGIONS.count { it.islandGroup == "Mindanao" }
                                        "My Joined Rooms" -> rooms.count { it.isJoined }
                                        else -> 0
                                    }

                                    Surface(
                                        onClick = { selectedIslandGroup = option },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected) UzzapOrange else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.testTag("island_group_filter_$option")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        ) {
                                            Text(
                                                text = if (option == "My Joined Rooms") "★ $option ($badgeCount)" else "$option ($badgeCount)",
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Welcome / Instructions Banner
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, UzzapOrange.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(UzzapOrange),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Philippine Administrative Regions",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Click any region below to drill down into its official province chatrooms, join local channels, and start chatting!",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // If "My Joined Rooms" tab is selected, show list of joined rooms directly
                    if (selectedIslandGroup == "My Joined Rooms") {
                        val joinedRooms = rooms.filter { it.isJoined }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MY JOINED CHATROOMS (${joinedRooms.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        if (joinedRooms.isEmpty()) {
                            item {
                                EmptyListState(
                                    title = "No Joined Rooms Yet",
                                    subtitle = "Select any region below and click 'Join' to pin chatrooms here!"
                                )
                            }
                        } else {
                            items(joinedRooms, key = { it.id }) { room ->
                                ProvinceChatroomRow(
                                    room = room,
                                    onEnterClick = { onRoomClick(room) },
                                    onToggleJoin = { onToggleJoin(room.id, !room.isJoined) }
                                )
                            }
                        }
                    }

                    // Regions List
                    val filteredRegions = PhilippineRegions.REGIONS.filter { region ->
                        if (selectedIslandGroup == "All" || selectedIslandGroup == "My Joined Rooms") {
                            true
                        } else {
                            region.islandGroup.equals(selectedIslandGroup, ignoreCase = true)
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PHILIPPINE REGIONS (${filteredRegions.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Tap to view provinces",
                                fontSize = 11.sp,
                                color = UzzapOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    items(filteredRegions, key = { it.id }) { regionInfo ->
                        val provincesInRegion = rooms.filter { it.category == regionInfo.name }
                        val joinedCount = provincesInRegion.count { it.isJoined }
                        val totalChatters = provincesInRegion.sumOf { it.chatterCount }

                        RegionRowCard(
                            region = regionInfo,
                            provinceCount = PhilippineRegions.provincesFor(regionInfo.name).size,
                            joinedCount = joinedCount,
                            totalChatters = totalChatters,
                            onClick = {
                                updateSelectedRegion(regionInfo.name)
                                onCategoryChange(regionInfo.name)
                            }
                        )
                    }
                }
            }
        } else {
            // ==================== LEVEL 2: PROVINCES IN REGION VIEW ====================
            val regionInfo = PhilippineRegions.getRegionInfo(effectiveSelectedRegion)
            val provincesInRegion = rooms.filter { it.category == effectiveSelectedRegion }
            var provinceSearchQuery by remember { mutableStateOf("") }

            val filteredProvinces = provincesInRegion.filter { room ->
                val matchesJoined = if (provinceFilterJoinedOnly) room.isJoined else true
                val matchesSearch = if (provinceSearchQuery.isBlank()) {
                    true
                } else {
                    val q = provinceSearchQuery.trim().lowercase()
                    room.name.lowercase().contains(q) || room.topic.lowercase().contains(q)
                }
                matchesJoined && matchesSearch
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
            ) {
                // Breadcrumb / Back Navigation Bar
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { updateSelectedRegion(null) },
                                modifier = Modifier.testTag("back_to_regions_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Regions",
                                    tint = UzzapOrange
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Regions",
                                        fontSize = 12.sp,
                                        color = UzzapOrange,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable { updateSelectedRegion(null) }
                                    )
                                    Text(
                                        text = " / ",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = regionInfo.shortName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = regionInfo.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                color = UzzapOrange.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = regionInfo.islandGroup,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UzzapOrange,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Region Header Summary Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, UzzapOrange.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = regionInfo.emoji,
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                                Column {
                                    Text(
                                        text = regionInfo.description,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Key areas: ${regionInfo.highlights}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = UzzapOrange,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${provincesInRegion.size} Provinces / Cities",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${provincesInRegion.sumOf { it.chatterCount }} chatters online",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF10B981)
                                    )
                                }

                                Surface(
                                    color = if (provincesInRegion.any { it.isJoined }) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${provincesInRegion.count { it.isJoined }} Joined",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (provincesInRegion.any { it.isJoined }) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Filter & Search inside Region
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = provinceSearchQuery,
                            onValueChange = { provinceSearchQuery = it },
                            placeholder = {
                                Text(
                                    "Filter provinces in ${regionInfo.shortName}...",
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (provinceSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { provinceSearchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = UzzapOrange,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_province_in_region_field")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = { provinceFilterJoinedOnly = false },
                                shape = RoundedCornerShape(12.dp),
                                color = if (!provinceFilterJoinedOnly) UzzapOrange else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.testTag("filter_all_provinces")
                            ) {
                                Text(
                                    text = "All Provinces (${provincesInRegion.size})",
                                    fontSize = 11.sp,
                                    fontWeight = if (!provinceFilterJoinedOnly) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!provinceFilterJoinedOnly) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }

                            Surface(
                                onClick = { provinceFilterJoinedOnly = true },
                                shape = RoundedCornerShape(12.dp),
                                color = if (provinceFilterJoinedOnly) UzzapOrange else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.testTag("filter_joined_provinces")
                            ) {
                                Text(
                                    text = "Joined Only (${provincesInRegion.count { it.isJoined }})",
                                    fontSize = 11.sp,
                                    fontWeight = if (provinceFilterJoinedOnly) FontWeight.Bold else FontWeight.Medium,
                                    color = if (provinceFilterJoinedOnly) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // List Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PROVINCE CHATROOMS (${filteredProvinces.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                if (filteredProvinces.isEmpty()) {
                    item {
                        EmptyListState(
                            title = "No Provinces Found",
                            subtitle = if (provinceSearchQuery.isNotBlank()) {
                                "No provinces match '$provinceSearchQuery'"
                            } else {
                                "No joined chatrooms in this region yet. Switch to 'All' to explore!"
                            }
                        )
                    }
                } else {
                    items(filteredProvinces, key = { it.id }) { room ->
                        ProvinceChatroomRow(
                            room = room,
                            onEnterClick = { onRoomClick(room) },
                            onToggleJoin = { onToggleJoin(room.id, !room.isJoined) }
                        )
                    }
                }
            }
        }

        // FAB to create custom room
        FloatingActionButton(
            onClick = onCreateRoomClick,
            containerColor = UzzapOrange,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
                .testTag("create_room_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Room"
            )
        }
    }
}

@Composable
fun RegionRowCard(
    region: PhilippineRegionInfo,
    provinceCount: Int,
    joinedCount: Int,
    totalChatters: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("region_card_${region.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Region Emoji/Icon Box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(UzzapOrange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = region.emoji,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = region.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = region.islandGroup,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = region.highlights,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$provinceCount Provinces",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " • ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$totalChatters chatters",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF10B981)
                    )

                    if (joinedCount > 0) {
                        Text(
                            text = " • ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "✓ $joinedCount Joined",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Chevron Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "View provinces",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ProvinceChatroomRow(
    room: ChatroomEntity,
    onEnterClick: () -> Unit,
    onToggleJoin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onEnterClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            1.dp,
            if (room.isJoined) UzzapOrange.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("room_${room.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Room Tag Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (room.isJoined) UzzapOrange else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = if (room.isJoined) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Title and details
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = room.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (room.isJoined) {
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Joined",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = room.topic,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${room.chatterCount} chatters online",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: JOIN and LEAVE buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (room.isJoined) {
                    // LEAVE BUTTON
                    OutlinedButton(
                        onClick = onToggleJoin,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                        modifier = Modifier.testTag("leave_room_${room.id}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Leave",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Leave",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFEF4444)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // ENTER BUTTON
                    Button(
                        onClick = onEnterClick,
                        colors = ButtonDefaults.buttonColors(containerColor = UzzapOrange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("enter_room_${room.id}")
                    ) {
                        Text(
                            text = "Enter Room",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // ENTER AS GUEST / PREVIEW
                    OutlinedButton(
                        onClick = onEnterClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("preview_room_${room.id}")
                    ) {
                        Text(
                            text = "Enter",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // JOIN BUTTON
                    Button(
                        onClick = onToggleJoin,
                        colors = ButtonDefaults.buttonColors(containerColor = UzzapOrange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("join_room_${room.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Join",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Join",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
