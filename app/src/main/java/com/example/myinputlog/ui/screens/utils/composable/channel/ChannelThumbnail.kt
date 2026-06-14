package com.example.myinputlog.ui.screens.utils.composable.channel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Size
import com.example.myinputlog.R
import com.example.myinputlog.ui.screens.utils.ext.shimmerEffect
import com.example.myinputlog.ui.theme.spacing

private val rankMap: Map<Int, Int> = mapOf(
    1 to R.drawable.img_emoji_first_place,
    2 to R.drawable.img_emoji_second_place,
    3 to R.drawable.img_emoji_third_place
)

@Composable
fun ChannelThumbnail(
    modifier: Modifier = Modifier, channelThumbnailUrl: String, rank: Int = 0
) {
    val context = LocalContext.current

    val imageRequest = remember(channelThumbnailUrl) {
        if (channelThumbnailUrl.isBlank()) null
        else ImageRequest.Builder(context).size(Size.ORIGINAL).data(channelThumbnailUrl)
            .crossfade(true).diskCachePolicy(CachePolicy.ENABLED).build()
    }

    val rankBadge = rankMap.getOrDefault(rank, null)

    Box(
        modifier = modifier
            .aspectRatio(1F)
            .fillMaxWidth(), contentAlignment = Alignment.TopEnd
    ) {
        rankBadge?.let {
            Image(
                modifier = Modifier
                    .size(MaterialTheme.spacing.large)
                    .zIndex(1F).offset(x = MaterialTheme.spacing.small),
                painter = painterResource(id = it),
                contentDescription = "Badge",
            )
        }
        Box(
            modifier = Modifier
                .aspectRatio(1F)
                .fillMaxWidth()
                .clip(CircleShape)
                .background(Color.Gray),
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