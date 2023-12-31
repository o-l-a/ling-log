package com.example.myinputlog.ui.screens.utils.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.myinputlog.R
import com.example.myinputlog.ui.screens.utils.ext.shimmerEffect
import com.example.myinputlog.ui.screens.utils.formatDuration
import com.example.myinputlog.ui.theme.spacing

@Composable
fun VideoThumbnail(
    modifier: Modifier = Modifier,
    videoUrl: String,
    duration: Long,
    isListItemLeading: Boolean = false
) {
    Box(
        modifier = modifier
            .aspectRatio(16F / 9F)
            .fillMaxWidth()
            .clip(
                shape = RoundedCornerShape(
                    if (isListItemLeading) {
                        MaterialTheme.spacing.small
                    } else
                        MaterialTheme.spacing.medium
                )
            )
            .background(Color.Gray),
        contentAlignment = Alignment.BottomEnd
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4F / 3F),
            model = ImageRequest.Builder(context = LocalContext.current)
                .size(Size.ORIGINAL)
                .data(videoUrl)
                .crossfade(true)
                .build(),
            error = painterResource(R.drawable.video_placeholder),
            placeholder = painterResource(R.drawable.loading_img),
            contentDescription = stringResource(R.string.video_thumbnail_content_description),
            contentScale = ContentScale.FillWidth
        )
        VideoDurationContainer(duration = duration, isListItemLeading = isListItemLeading)
    }
}

@Composable
fun VideoDurationContainer(
    modifier: Modifier = Modifier,
    duration: Long = 0L,
    isListItemLeading: Boolean = false
) {
    Card(
        modifier = modifier.padding(
            if (isListItemLeading) {
                MaterialTheme.spacing.extraSmall
            } else
                MaterialTheme.spacing.small
        ),
        shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
    ) {
        Text(
            modifier = Modifier.padding(MaterialTheme.spacing.extraSmall),
            text = formatDuration(duration),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun VideoThumbnailPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(16F / 9F)
            .fillMaxWidth()
            .clip(
                shape = RoundedCornerShape(
                    MaterialTheme.spacing.small
                )
            )
            .shimmerEffect()
    )
}