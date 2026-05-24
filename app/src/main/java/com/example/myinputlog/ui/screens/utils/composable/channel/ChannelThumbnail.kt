package com.example.myinputlog.ui.screens.utils.composable.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Size
import com.example.myinputlog.R
import com.example.myinputlog.ui.screens.utils.ext.shimmerEffect

@Composable
fun ChannelThumbnail(
    modifier: Modifier = Modifier, channelThumbnailUrl: String
) {
    val context = LocalContext.current

    val imageRequest = remember(channelThumbnailUrl) {
        if (channelThumbnailUrl.isBlank()) null
        else ImageRequest.Builder(context).size(Size.ORIGINAL).data(channelThumbnailUrl)
            .crossfade(true).diskCachePolicy(CachePolicy.ENABLED).build()
    }

    Box(
        modifier = modifier
            .aspectRatio(1F)
            .fillMaxWidth()
            .clip(CircleShape)
            .background(Color.Gray),
        contentAlignment = Alignment.BottomEnd
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1F),
            model = imageRequest,
            error = painterResource(R.drawable.video_placeholder),
            placeholder = painterResource(R.drawable.loading_img),
            contentDescription = stringResource(R.string.channel_thumbnail_content_description),
            contentScale = ContentScale.FillBounds
        )
    }
}


@Composable
fun ChannelThumbnailPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1F)
            .clip(CircleShape)
            .shimmerEffect()
    )
}