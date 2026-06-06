package com.calfteam.petcare.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.FavoriteBorder
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
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calfteam.petcare.viewmodel.AuthViewModel
import android.content.Intent
import androidx.compose.foundation.clickable
val TealColor = Color(0xFF0B7B7D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("Adopter") }

    val context = LocalContext.current // Untuk memunculkan Toast
    val loginStatus by viewModel.loginStatus.collectAsState() // Mendengarkan status login

    LaunchedEffect(loginStatus) {
        loginStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- LOGO & TITLE ---


        Spacer(modifier = Modifier.height(32.dp))

        // --- WELCOME TEXT ---
        Text(
            text = "Welcome Back",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Log in to manage your pets and applications.",
            fontSize = 14.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- ROLE SELECTION (Adopter / Donor) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RoleButton(
                modifier = Modifier.weight(1f),
                text = "Adopter",
                icon = Icons.Rounded.FavoriteBorder,
                isSelected = selectedRole == "Adopter",
                onClick = { selectedRole = "Adopter" }
            )
            RoleButton(
                modifier = Modifier.weight(1f),
                text = "Donor",
                icon = Icons.Rounded.VolunteerActivism,
                isSelected = selectedRole == "Donor",
                onClick = { selectedRole = "Donor" }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- EMAIL INPUT ---
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email Address") },
            placeholder = { Text("hello@petcare.com") }, // Placeholder ikut disesuaikan
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealColor,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- PASSWORD INPUT ---
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            placeholder = { Text("••••••••") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle password visibility")
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealColor,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Text(
            text = "Forgot password?",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- SIGN IN BUTTON ---
        Button(
            onClick = {
                // Panggil logika login dari ViewModel
                viewModel.signIn(email, password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealColor)
        ) {
            Text(text = "Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- SIGN UP LINK ---
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Don't have an account? ", color = Color.Gray, fontSize = 14.sp)
            Text(
                text = "Sign Up",
                color = Color(0xFFB06A26),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { // Tambahkan modifier ini
                    // Ganti SignUpActivity::class.java dengan nama Activity tujuan lu
                    val intent = Intent(context, SignUpActivity::class.java)
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun RoleButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) TealColor else Color.White
    val contentColor = if (isSelected) Color.White else TealColor

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) TealColor else Color.LightGray),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = text, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = text, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}