package com.akkeylab.zenith

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.LocalSpatialConfiguration
import androidx.xr.compose.platform.setSubspaceContent
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.movable
import androidx.xr.compose.subspace.layout.resizable
import androidx.xr.compose.subspace.layout.width
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import com.akkeylab.zenith.ui.theme.ZenithTheme
import kotlinx.coroutines.guava.await
import androidx.core.net.toUri
import androidx.xr.compose.spatial.EdgeOffset
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterEdge
import androidx.xr.compose.subspace.layout.SpatialRoundedCornerShape

class MainActivity : ComponentActivity() {

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {}

        setSubspaceContent {
            ZenithTheme {
                Subspace {
                    MySpatialContent()
                }
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun MySpatialContent() {
    val session = checkNotNull(LocalSession.current)
    var modelEntity by remember { mutableStateOf<GltfModelEntity?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }

    SpatialPanel(
        SubspaceModifier
            .width(1280.dp)
            .height(800.dp)
            .resizable()
            .movable()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(32.dp),
            color = Color.White.copy(alpha = 0.9f),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                MainContent(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 64.dp)
                )
            }
        }
        LaunchedEffect(selectedTab) {
            modelEntity?.dispose()

            val modelPath = when (selectedTab) {
                0 -> "models/girl.gltf"
                1 -> "models/animation/scene.gltf"
                else -> "models/girl.gltf"
            }
            val model = GltfModel.create(session, modelPath).await()

            val translation = when (selectedTab) {
                0 -> Vector3(0f, -0.5f, 0.2f)
                1 -> Vector3(0f, -0.1f, 0.2f)
                else -> Vector3(0f, -0.5f, 0.2f)
            }
            val entity = GltfModelEntity.create(
                session = session,
                model = model,
                pose = Pose(
                    translation = translation,
                    rotation = Quaternion.fromEulerAngles(0f, 0f, 0f)
                )
            )
            val scale = when (selectedTab) {
                0 -> 0.5f
                1 -> 0.2f
                else -> 0.5f
            }
            entity.setScale(scale)
            if (selectedTab == 1) {
                entity.startAnimation(loop = true, animationName = "Animation")
            }
            modelEntity = entity
        }
        DisposableEffect(Unit) {
            onDispose {
                modelEntity?.dispose()
            }
        }
        Orbiter(
            position = OrbiterEdge.Start,
            alignment = Alignment.CenterVertically,
            offset = EdgeOffset.inner(16.dp),
            shape = SpatialRoundedCornerShape(CornerSize(percent = 50))
        ) {
            Surface(
                modifier = Modifier
                    .width(100.dp)
                    .height(400.dp),
                color = Color.White.copy(alpha = 0.85f),
                shape = RoundedCornerShape(32.dp),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OrbiterButton(
                        icon = Icons.Filled.Person,
                        selected = selectedTab == 0,
                        contentDescription = "Static Model"
                    ) { selectedTab = 0 }

                    Spacer(modifier = Modifier.height(16.dp))

                    OrbiterButton(
                        icon = Icons.Filled.PlayArrow,
                        selected = selectedTab == 1,
                        contentDescription = "Animated Model"
                    ) { selectedTab = 1 }
                }
            }
        }
    }
}

@Composable
fun MainContent(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "3D Anime Character girl for Blender C1",
            color = Color.Magenta,
            fontSize = 12.sp,
            modifier = Modifier.clickable {
                ctx.startActivity(Intent(Intent.ACTION_VIEW, "https://skfb.ly/oyACQ".toUri()))
            }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "by CGCOOL is licensed under",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "Creative Commons Attribution",
                color = Color.Magenta,
                fontSize = 12.sp,
                modifier = Modifier.clickable {
                    ctx.startActivity(Intent(
                        Intent.ACTION_VIEW,
                        "http://creativecommons.org/licenses/by/4.0/".toUri()
                    ))
                }
            )
        }
    }
}

@Composable
fun OrbiterButton(
    icon: ImageVector,
    selected: Boolean,
    contentDescription: String?,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) Color(0xFFDFBBFF) else Color.White
    val iconTint = if (selected) Color.White else Color.LightGray

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        modifier = Modifier
            .size(56.dp)
            .shadow(if (selected) 8.dp else 2.dp, RoundedCornerShape(18.dp)),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OrbiterButtonPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color(0xFFF0F0F0))
            .padding(24.dp)
    ) {
        OrbiterButton(
            Icons.Filled.Person,
            selected = false,
            contentDescription = "Static Model",
            onClick = {}
        )

        OrbiterButton(
            Icons.Filled.PlayArrow,
            selected = true,
            contentDescription = "Animated Model",
            onClick = {}
        )
    }
}
