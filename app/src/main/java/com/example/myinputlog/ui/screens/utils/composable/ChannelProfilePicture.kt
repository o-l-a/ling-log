package com.example.myinputlog.ui.screens.utils.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
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

@Composable
fun ChannelProfilePicture(
    modifier: Modifier = Modifier,
    pictureUrl: String
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Gray)
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize(),
            model = ImageRequest.Builder(context = LocalContext.current)
                .size(Size.ORIGINAL)
                .data(pictureUrl)
                .crossfade(true)
                .build(),
            error = painterResource(R.drawable.baseline_person_24),
            placeholder = painterResource(R.drawable.loading_img),
            contentDescription = stringResource(R.string.video_thumbnail_content_description),
            contentScale = ContentScale.FillWidth
        )
    }
}