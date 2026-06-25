package com.calfteam.petcare.ui.screens.post

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Shared brand & semantic colors for AddPost & EditPost screens
internal val BrandTeal = Color(0xFF00666E)
internal val BrandTealLight = Color(0xFFE0F2F1)
internal val WarningOrange = Color(0xFFFF6B35)
internal val WarningOrangeLight = Color(0xFFFFF1E8)
internal val TextPrimary = Color(0xFF1A1A1A)
internal val TextSecondary = Color(0xFF6B7280)
internal val BackgroundColor = Color(0xFFFBF9F8)
internal val DividerColor = Color(0xFFE5E7EB)

/**
 * Header section untuk membagi form menjadi bagian-bagian visual yang jelas.
 */
@Composable
internal fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

/**
 * Image picker dengan empty state yang inviting (icon box, primary text, hint)
 * dan overlay "Ganti" badge saat sudah ada gambar.
 *
 * @param imageUri Uri gambar baru yang dipilih user (AddPost mode)
 * @param fallbackUrl URL gambar existing (EditPost mode, saat imageUri null)
 */
@Composable
internal fun PostImagePicker(
    imageUri: Uri?,
    fallbackUrl: String? = null,
    onPick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(220.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false
            )
            .clip(RoundedCornerShape(20.dp))
            .background(BrandTealLight)
            .border(
                width = 1.5.dp,
                color = BrandTeal.copy(alpha = 0.35f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onPick() },
        contentAlignment = Alignment.Center
    ) {
        val activeUri = imageUri
        if (activeUri != null) {
            AsyncImage(
                model = activeUri,
                contentDescription = "Selected Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            ImageOverlayBadge(
                text = "Ganti Foto",
                icon = Icons.Default.Edit,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            )
        } else if (!fallbackUrl.isNullOrEmpty()) {
            // Edit mode: tampilkan foto lama sebagai default
            AsyncImage(
                model = fallbackUrl,
                contentDescription = "Current Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            ImageOverlayBadge(
                text = "Ganti Foto (Opsional)",
                icon = Icons.Default.Edit,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            )
        } else {
            // Empty state: inviting placeholder
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(BrandTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Tambah Foto Hewan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap untuk pilih dari galeri",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ImageOverlayBadge(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Pilihan tipe listing (Adopsi / Hilang) sebagai 2 kartu visual
 * menggantikan FilterChip bawaan yang terlalu plain.
 */
@Composable
internal fun ListingTypeSelector(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ListingTypeCard(
            label = "Adopsi",
            description = "Cari rumah baru",
            icon = Icons.Default.FavoriteBorder,
            accentColor = BrandTeal,
            isSelected = selected.equals("Adoption", ignoreCase = true),
            onClick = { onSelect("Adoption") },
            modifier = Modifier.weight(1f)
        )
        ListingTypeCard(
            label = "Hilang",
            description = "Butuh bantuan temukan",
            icon = Icons.Default.LocationSearching,
            accentColor = WarningOrange,
            isSelected = selected.equals("Missing", ignoreCase = true),
            onClick = { onSelect("Missing") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ListingTypeCard(
    label: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) accentColor else Color.White
    val contentColor = if (isSelected) Color.White else TextPrimary
    val descColor = if (isSelected) Color.White.copy(alpha = 0.85f) else TextSecondary

    Column(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 4.dp else 1.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            )
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) accentColor else DividerColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 18.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) Color.White.copy(alpha = 0.22f)
                    else accentColor.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else accentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            fontSize = 11.sp,
            color = descColor
        )
    }
}

/**
 * OutlinedTextField dengan leading icon, label, dan theming teal.
 */
@Composable
internal fun IconTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder, fontSize = 13.sp) }
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
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        singleLine = maxLines == 1,
        maxLines = maxLines,
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
 * Location section untuk listing Adoption (otomatis GPS).
 */
@Composable
internal fun AdoptionLocationSection(
    location: String,
    address: String,
    isGettingLocation: Boolean,
    onGetLocation: () -> Unit,
    onReset: () -> Unit,
    onOpenMapPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        if (location.isEmpty()) {
            OutlinedButton(
                onClick = onGetLocation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isGettingLocation,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandTeal),
                border = BorderStroke(1.5.dp, BrandTeal)
            ) {
                if (isGettingLocation) {
                    CircularProgressIndicator(
                        color = BrandTeal,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Gunakan Lokasi Saya (GPS)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            MapPickerLink(onClick = onOpenMapPicker)
        } else {
            LocationFilledCard(
                address = address,
                location = location,
                onReset = onReset
            )
            Spacer(modifier = Modifier.height(8.dp))
            MapPickerLink(onClick = onOpenMapPicker, isEdit = true)
        }
    }
}

/**
 * Location section untuk listing Missing (manual + GPS helper).
 * `address` = teks terbaca (display), `location` = "lat,lng" (untuk distance).
 */
@Composable
internal fun MissingLocationSection(
    address: String,
    location: String,
    isGettingLocation: Boolean,
    onAddressChange: (String) -> Unit,
    onGetLocation: () -> Unit,
    onReset: () -> Unit,
    onOpenMapPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        if (address.isNotEmpty() || location.isNotEmpty()) {
            // Tampilkan filled card dengan opsi reset
            LocationFilledCard(
                address = address,
                location = location,
                onReset = onReset
            )
            Spacer(modifier = Modifier.height(8.dp))
            MapPickerLink(onClick = onOpenMapPicker, isEdit = true)
        } else {
            // Empty state: input manual
            IconTextField(
                value = address,
                onValueChange = onAddressChange,
                label = "Lokasi Terakhir Dilihat",
                icon = Icons.Default.LocationOn,
                placeholder = "Cth: Jakarta Barat, Kemang, atau alamat lengkap"
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onGetLocation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isGettingLocation,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandTeal),
                border = BorderStroke(1.dp, BrandTeal.copy(alpha = 0.5f))
            ) {
                if (isGettingLocation) {
                    CircularProgressIndicator(
                        color = BrandTeal,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Helper: Isi dari GPS saya",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            MapPickerLink(onClick = onOpenMapPicker)
        }
    }
}

/**
 * Link kecil di bawah location section: "Atau pilih di peta".
 * Membuka MapPickerScreen.
 */
@Composable
private fun MapPickerLink(
    onClick: () -> Unit,
    isEdit: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Map,
            contentDescription = null,
            tint = BrandTeal,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isEdit) "Atau pilih lokasi lain di peta"
            else "Atau pilih lokasi di peta",
            fontSize = 12.sp,
            color = BrandTeal,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LocationFilledCard(
    address: String,
    location: String,
    onReset: () -> Unit
) {
    // Prioritaskan address untuk display; fallback ke koordinat kalau address kosong
    val displayPrimary = address.ifBlank { location }
    val showCoordinates = address.isNotBlank() &&
            location.isNotBlank() &&
            location != address

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
            .clip(RoundedCornerShape(12.dp))
            .background(BrandTealLight)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(BrandTeal),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Lokasi Terisi",
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = displayPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (showCoordinates) {
                Text(
                    text = location,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        TextButton(
            onClick = onReset,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Ubah",
                color = BrandTeal,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}