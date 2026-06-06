package com.calfteam.petcare.ui.auth


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Warna kustom
//val TealColor = Color(0xFF0B7B7D)
val LightTealBg = Color(0xFFE8F3F3) // Warna background saat Adopter/Donor dipilih
val LightGrayBg = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen() {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("Adopter") } // State untuk tombol

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFB)) // Warna background dasar sedikit abu/off-white
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- LOGO & TITLE HORIZONTAL ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Pets,
                contentDescription = "App Logo",
                tint = TealColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PetCare",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TealColor
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- HEADER TEXT ---
        Text(
            text = "Create an Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Join thousands of pet lovers making a difference today.",
            fontSize = 14.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- ROLE SELECTION (Adopter / Donor) ---
        Text(
            text = "I WANT TO BE AN...",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RoleSelectionCard(
                modifier = Modifier.weight(1f),
                text = "Adopter",
                icon = Icons.Rounded.Home, // Menggunakan icon rumah
                isSelected = selectedRole == "Adopter",
                onClick = { selectedRole = "Adopter" }
            )
            RoleSelectionCard(
                modifier = Modifier.weight(1f),
                text = "Donor",
                icon = Icons.Rounded.VolunteerActivism, // Menggunakan icon tangan & hati
                isSelected = selectedRole == "Donor",
                onClick = { selectedRole = "Donor" }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- FULL NAME INPUT ---
        Text(text = "Full Name", fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter your name") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealColor,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,      // Menggantikan containerColor
                unfocusedContainerColor = Color.White     // Menggantikan containerColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- EMAIL INPUT ---
        Text(text = "Email Address", fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("name@example.com") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealColor,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,      // Menggantikan containerColor
                unfocusedContainerColor = Color.White     // Menggantikan containerColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- PASSWORD INPUT ---
        Text(text = "Password", fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("At least 8 characters") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle password visibility", tint = Color.Gray)
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealColor,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,      // Menggantikan containerColor
                unfocusedContainerColor = Color.White     // Menggantikan containerColor
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- SIGN UP BUTTON ---
        Button(
            onClick = { /* Nanti kita isi logika Register Appwrite di sini */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealColor)
        ) {
            Text(text = "Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- SIGN IN LINK ---
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Already have an account? ", color = Color.Gray, fontSize = 14.sp)
            Text(
                text = "Sign In",
                color = TealColor, // Warnanya teal sesuai gambar
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    // Nanti diisi logika untuk kembali ke halaman Login
                }
            )
        }
    }
}

// Komponen Card kustom untuk Adopter & Donor
@Composable
fun RoleSelectionCard(
    modifier: Modifier = Modifier,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) LightTealBg else LightGrayBg
    val borderColor = if (isSelected) TealColor else Color.LightGray
    val contentColor = if (isSelected) TealColor else Color.DarkGray

    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, borderColor),
        color = backgroundColor
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = contentColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = text, fontWeight = FontWeight.SemiBold, color = contentColor)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen()
}