package com.calfteam.petcare.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Brand & semantic colors — konsisten dengan tema utama project
internal val BrandTeal = Color(0xFF00666E)
internal val BrandTealDeep = Color(0xFF008B95)
internal val BrandTealLight = Color(0xFFE0F2F1)
internal val BrandTealSoft = Color(0xFFF0F9FA)
internal val TextPrimary = Color(0xFF1A1A1A)
internal val TextSecondary = Color(0xFF6B7280)
internal val BackgroundColor = Color(0xFFFBF9F8)
internal val DividerColor = Color(0xFFE5E7EB)

/**
 * Header section untuk auth screen: gradient teal lembut dengan logo,
 * nama brand, dan tagline. Menggantikan header teks polos.
 */
@Composable
internal fun AuthHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrandTealLight,
                        BrandTealSoft,
                        BackgroundColor
                    )
                )
            )
            .padding(top = 32.dp, bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        clip = false
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(BrandTealDeep, BrandTeal)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "PetCare",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandTeal,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

/**
 * OutlinedTextField dengan leading icon dan theming teal — untuk auth screens.
 */
@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder, fontSize = 13.sp, color = TextSecondary) }
        } else null,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandTeal,
                modifier = Modifier.size(20.dp)
            )
        },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrandTeal,
            focusedLabelColor = BrandTeal,
            focusedLeadingIconColor = BrandTeal,
            cursorColor = BrandTeal,
            unfocusedBorderColor = DividerColor
        )
    )
}

/**
 * Password field dengan toggle visibility, theming teal, dan auto-fill.
 */
@Composable
internal fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Password",
    placeholder: String = "Minimal 8 karakter",
    imeAction: ImeAction = ImeAction.Done,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = {
            Text(placeholder, fontSize = 13.sp, color = TextSecondary)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = BrandTeal,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            val image = if (passwordVisible) Icons.Default.Visibility
            else Icons.Default.VisibilityOff
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = image,
                    contentDescription = "Toggle password visibility",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
        visualTransformation = if (passwordVisible) VisualTransformation.None
        else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrandTeal,
            focusedLabelColor = BrandTeal,
            focusedLeadingIconColor = BrandTeal,
            cursorColor = BrandTeal,
            unfocusedBorderColor = DividerColor
        )
    )
}

/**
 * Password strength indicator (4 segment bar) untuk SignUpScreen.
 */
@Composable
internal fun PasswordStrengthIndicator(
    password: String,
    modifier: Modifier = Modifier
) {
    val strength = remember(password) {
        when {
            password.length < 6 -> 0
            password.length < 8 -> 1
            password.length < 10 && password.any { it.isDigit() } -> 2
            password.length >= 8 &&
                    password.any { it.isDigit() } &&
                    password.any { it.isUpperCase() } -> 3
            else -> 2
        }
    }
    val labels = listOf("Terlalu pendek", "Lemah", "Cukup", "Kuat")
    val barColors = listOf(
        Color(0xFFEF4444),
        Color(0xFFF59E0B),
        Color(0xFF3B82F6),
        Color(0xFF10B981)
    )

    if (password.isEmpty()) return

    Column(modifier = modifier.padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (index < strength) barColors[strength]
                            else DividerColor
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Kekuatan: ${labels[strength]}",
            fontSize = 11.sp,
            color = barColors[strength],
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Tombol utama (Sign In / Sign Up) dengan icon — rounded 14dp, height 54dp.
 */
@Composable
internal fun AuthPrimaryButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandTeal,
            disabledContainerColor = BrandTeal.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * Footer link untuk berpindah antara Login <-> SignUp.
 */
@Composable
internal fun AuthFooterLink(
    prefix: String,
    actionText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = prefix,
            fontSize = 13.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = actionText,
            fontSize = 13.sp,
            color = BrandTeal,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable { onClick() }
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

/**
 * Link "Lupa password" — tampil sebagai teks teal dengan underline visual
 * yang bisa diklik. Untuk LoginScreen.
 */
@Composable
internal fun ForgotPasswordLink(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = "Lupa password?",
            fontSize = 12.sp,
            color = BrandTeal,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable { onClick() }
                .padding(horizontal = 4.dp, vertical = 4.dp)
        )
    }
}