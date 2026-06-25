package com.calfteam.petcare.ui.screens.post

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.calfteam.petcare.utils.LocationUtils
import kotlinx.coroutines.launch
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    initialLat: Double? = null,
    initialLng: Double? = null,
    onConfirm: (lat: Double, lng: Double, address: String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // Default: Jakarta
    val startLat = initialLat ?: -6.2088
    val startLng = initialLng ?: 106.8456

    var pickedLat by remember { mutableStateOf(startLat) }
    var pickedLng by remember { mutableStateOf(startLng) }
    var pickedAddress by remember { mutableStateOf<String?>(null) }
    var isResolvingAddress by remember { mutableStateOf(false) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    fun resolveAddress(lat: Double, lng: Double) {
        coroutineScope.launch {
            isResolvingAddress = true
            val addr = LocationUtils.reverseGeocode(context, lat, lng)
            pickedAddress = addr
            isResolvingAddress = false
        }
    }

    // Resolve alamat awal
    LaunchedEffect(startLat, startLng) {
        resolveAddress(startLat, startLng)
    }

    BackHandler { onCancel() }

    Scaffold(
        containerColor = Color(0xFFFBF9F8),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pilih Lokasi di Peta",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Batal",
                            tint = Color(0xFF1A1A1A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Alamat preview
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0F2F1))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF00666E),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Lokasi Terpilih",
                                fontSize = 11.sp,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium
                            )
                            if (isResolvingAddress && pickedAddress == null) {
                                Text(
                                    text = "Mencari alamat...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1A1A1A)
                                )
                            } else {
                                Text(
                                    text = pickedAddress
                                        ?.takeIf { it.isNotBlank() }
                                        ?: "Alamat tidak tersedia untuk titik ini",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1A1A1A),
                                    maxLines = 2
                                )
                            }
                            Text(
                                text = "%.5f, %.5f".format(pickedLat, pickedLng),
                                fontSize = 10.sp,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            onConfirm(pickedLat, pickedLng, pickedAddress.orEmpty())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00666E)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isResolvingAddress || pickedAddress != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Konfirmasi Lokasi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFE5E7EB))
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.5)
                        controller.setCenter(GeoPoint(startLat, startLng))

                        // Marker di tengah layar (diperbarui via tap)
                        val marker = Marker(this).apply {
                            position = GeoPoint(startLat, startLng)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Lokasi Terpilih"
                        }
                        overlays.add(marker)

                        // Tap handler: pindah marker + reverse geocode
                        val tapOverlay = MapEventsOverlay(object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                p ?: return false
                                pickedLat = p.latitude
                                pickedLng = p.longitude
                                marker.position = p
                                invalidate()
                                resolveAddress(p.latitude, p.longitude)
                                return true
                            }

                            override fun longPressHelper(p: GeoPoint?): Boolean = false
                        })
                        overlays.add(tapOverlay)

                        mapViewRef = this
                    }
                },
                update = { mapView ->
                    // Lifecycle management untuk osmdroid
                    mapView.onResume()
                }
            )

            // FAB: center ke lokasi saya saat ini
            FloatingActionButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        coroutineScope.launch {
                            // Pakai LocationRepository yang sudah ada
                            val locationRepository = com.calfteam.petcare.data.repository.LocationRepository(context)
                            val result = locationRepository.getCurrentLocation()
                            if (result.isSuccess) {
                                val (lat, lng) = result.getOrNull() ?: return@launch
                                pickedLat = lat
                                pickedLng = lng
                                mapViewRef?.controller?.animateTo(GeoPoint(lat, lng))
                                mapViewRef?.overlays?.filterIsInstance<Marker>()?.firstOrNull()?.position =
                                    GeoPoint(lat, lng)
                                mapViewRef?.invalidate()
                                resolveAddress(lat, lng)
                            }
                        }
                    } else {
                        // Permission belum ada, cukup center ke posisi sekarang
                        mapViewRef?.controller?.animateTo(GeoPoint(pickedLat, pickedLng))
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
                    .shadow(6.dp, CircleShape, clip = false),
                containerColor = Color.White,
                contentColor = Color(0xFF00666E)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Lokasi saya",
                    tint = Color(0xFF00666E)
                )
            }

            // Petunjuk di atas peta
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF00666E),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tap peta untuk pilih lokasi",
                        fontSize = 12.sp,
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Lifecycle: pause/resume MapView untuk hemat resource
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val map = mapViewRef
            when (event) {
                Lifecycle.Event.ON_RESUME -> map?.onResume()
                Lifecycle.Event.ON_PAUSE -> map?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewRef?.onDetach()
        }
    }
}