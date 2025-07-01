package com.akkeylab.zenith

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

class MainActivity : ComponentActivity() {

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {}

        setSubspaceContent {
            ZenithTheme {
                val spatialConfiguration = LocalSpatialConfiguration.current

                Subspace {
                    MySpatialContent(
                        onRequestHomeSpaceMode = spatialConfiguration::requestHomeSpaceMode
                    )
                }
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun MySpatialContent(onRequestHomeSpaceMode: () -> Unit) {
    val session = checkNotNull(LocalSession.current)
    var modelEntity by remember { mutableStateOf<GltfModelEntity?>(null) }

    SpatialPanel(SubspaceModifier.width(1280.dp).height(800.dp).resizable().movable()) {
        Surface {
            Box(modifier = Modifier.fillMaxSize()) {
                MainContent(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 64.dp)
                )
            }
        }
        LaunchedEffect(key1 = Unit) {
            val model = GltfModel.create(session, "models/girl.gltf").await()
            val entity = GltfModelEntity.create(
                session = session,
                model = model,
                pose = Pose(
                    translation = Vector3(0f, -0.5f, 0.2f),
                    rotation = Quaternion.fromEulerAngles(0f, 0f, 0f)
                )
            )
            entity.setScale(0.5f)
//            entity.startAnimation(loop = true, animationName = "Animation")
            modelEntity = entity
        }
        DisposableEffect(Unit) {
            onDispose {
                modelEntity?.dispose()
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
