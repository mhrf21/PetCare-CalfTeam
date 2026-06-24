package com.calfteam.petcare.ui.screens.post

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/** Jenis hewan yang umum dipelihara, dipakai untuk dropdown pada form Post & Edit. */
val commonPetTypes = listOf(
    "Kucing",
    "Anjing",
    "Kelinci",
    "Hamster",
    "Marmut",
    "Burung",
    "Ikan",
    "Kura-kura",
    "Ular",
    "Iguana",
    "Sugar Glider",
    "Landak Mini",
    "Lainnya"
)

/**
 * Dropdown pemilih jenis hewan. Nilai bebas yang sudah ada (mis. dari postingan lama)
 * tetap ditampilkan walaupun tidak ada di [commonPetTypes].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetTypeDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Jenis Hewan"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("Pilih jenis hewan") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            commonPetTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        onValueChange(type)
                        expanded = false
                    }
                )
            }
        }
    }
}
