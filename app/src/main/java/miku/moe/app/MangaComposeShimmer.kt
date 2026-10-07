package miku.moe.app

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.ImageView

@Composable
internal fun rememberMangaShimmerProgress(): Float {
    val transition = rememberInfiniteTransition(label = "manga-shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "manga-shimmer-progress",
    )
    return progress
}

@Composable
internal fun MangaShimmerBlock(modifier: Modifier, progress: Float, radius: Dp = 6.dp) {
    val base = MaterialTheme.colorScheme.surfaceContainerHigh
    val luminance = 0.2126f * base.red + 0.7152f * base.green + 0.0722f * base.blue
    val amount = if (luminance > 0.5f) 0.7f else 0.14f
    val highlight = Color(
        red = base.red + (1f - base.red) * amount,
        green = base.green + (1f - base.green) * amount,
        blue = base.blue + (1f - base.blue) * amount,
        alpha = base.alpha,
    )
    Box(modifier.drawBehind {
        val band = size.width * 0.9f
        val left = -band + progress * (size.width + band * 2f)
        drawRoundRect(
            brush = Brush.linearGradient(
                colorStops = arrayOf(0f to base, 0.5f to highlight, 1f to base),
                start = Offset(left, 0f),
                end = Offset(left + band, size.height),
            ),
            cornerRadius = CornerRadius(radius.toPx()),
        )
    })
}

@Composable
internal fun MangaBrokenImage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text("Gambar Rusak", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun MangaDetailSkeleton(modifier: Modifier = Modifier, manga: MangaPost? = null) {
    val progress = rememberMangaShimmerProgress()
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).then(modifier),
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Box(Modifier.fillMaxWidth().height(278.dp)) {
                MangaShimmerBlock(Modifier.fillMaxSize(), progress, 0.dp)
                Row(
                    Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 30.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Box(Modifier.width(126.dp).aspectRatio(0.68f).clip(RoundedCornerShape(14.dp))) {
                        if (!manga?.coverImage.isNullOrBlank()) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { context -> ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
                                update = { image -> MangaImageLoader.loadShimmerCover(image, manga?.coverImage.orEmpty(), manga?.getSourceId().orEmpty()) },
                            )
                        } else {
                            MangaShimmerBlock(Modifier.fillMaxSize(), progress, 14.dp)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f).padding(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (!manga?.title.isNullOrBlank()) {
                            Text(manga?.title.orEmpty(), color = Color.White, style = MaterialTheme.typography.titleLarge, maxLines = 3)
                        } else {
                            MangaShimmerBlock(Modifier.fillMaxWidth(0.9f).height(22.dp), progress)
                            MangaShimmerBlock(Modifier.fillMaxWidth(0.68f).height(22.dp), progress)
                        }
                        val knownLabels = listOf(manga?.getTypeLabel().orEmpty(), manga?.status.orEmpty()).filter { it.isNotBlank() }
                        if (knownLabels.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                knownLabels.forEach { label ->
                                    Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.16f)) {
                                        Text(label, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White, maxLines = 1)
                                    }
                                }
                            }
                        } else {
                            MangaShimmerBlock(Modifier.fillMaxWidth(0.78f).height(20.dp), progress, 12.dp)
                        }
                    }
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MangaShimmerBlock(Modifier.fillMaxWidth(0.32f).height(20.dp), progress)
                if (!manga?.synopsis.isNullOrBlank()) {
                    Text(manga?.synopsis.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 4)
                } else {
                    repeat(4) { MangaShimmerBlock(Modifier.fillMaxWidth(if (it == 3) 0.68f else 1f).height(14.dp), progress) }
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MangaShimmerBlock(Modifier.fillMaxWidth(0.42f).height(20.dp), progress)
                val genres = manga?.genre.orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() }
                if (genres.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        genres.take(4).forEach { label ->
                            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                Text(label, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.onSecondaryContainer, maxLines = 1)
                            }
                        }
                    }
                } else {
                    repeat(4) { MangaShimmerBlock(Modifier.fillMaxWidth(if (it % 2 == 0) 0.82f else 0.94f).height(14.dp), progress) }
                }
            }
        }
        items(8) {
            MangaShimmerBlock(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp), progress, 8.dp)
        }
    }
}

@Composable
internal fun MangaRelatedSkeletonCard(progress: Float) {
    Column(Modifier.fillMaxWidth()) {
        MangaShimmerBlock(Modifier.fillMaxWidth().aspectRatio(2f / 3f).clip(RoundedCornerShape(8.dp)), progress, 8.dp)
        Spacer(Modifier.height(8.dp))
        MangaShimmerBlock(Modifier.fillMaxWidth(0.88f).height(12.dp), progress)
        Spacer(Modifier.height(5.dp))
        MangaShimmerBlock(Modifier.fillMaxWidth(0.58f).height(12.dp), progress)
    }
}