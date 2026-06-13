package com.calfteam.petcare.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.calfteam.petcare.data.repository.PetRepository
import com.calfteam.petcare.ui.screens.home.HomeScreen
import com.calfteam.petcare.ui.screens.post.AddPostScreen
import com.calfteam.petcare.utils.AppwriteConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(userName: String) {
    val context = LocalContext.current
    val client = remember { AppwriteConfig.getClient(context) }
    val petRepository = remember { PetRepository(client) }

    var selectedItem by remember { mutableStateOf(0) }
    val bottomNavItems = listOf("Home", "Search", "Post", "Profile")
    val bottomNavIcons = listOf(Icons.Filled.Home, Icons.Filled.Search, Icons.Filled.AddCircle, Icons.Filled.Person)

    Scaffold(
        topBar = {
            // Copy paste TopAppBar lu ke sini
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(imageVector = bottomNavIcons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00666E),
                            selectedTextColor = Color(0xFF00666E),
                            indicatorColor = Color(0xFFE0F2F1)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedItem == 0) {
                FloatingActionButton(
                    onClick = { selectedItem = 2 },
                    containerColor = Color(0xFF00666E),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
            }
        }
    ) { innerPadding ->
        // Navigasi yang super rapi!
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> HomeScreen(petRepository = petRepository)
                1 -> { /* Nanti isi SearchScreen() */ }
                2 -> AddPostScreen(
                    petRepository = petRepository,
                    onNavigateToHome = { selectedItem = 0 } // Callback kembali ke home
                )
                3 -> { /* Nanti isi ProfileScreen(userName = userName) */ }
            }
        }
    }
}