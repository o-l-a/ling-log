package com.example.myinputlog.ui.screens.utils.composable.channel

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myinputlog.R
import java.io.File

@Composable
fun ProfilePicture(modifier: Modifier = Modifier, imageFile: File?) {
    val context = LocalContext.current
    val timestamp = imageFile?.lastModified() ?: 0L

    AsyncImage(
        model = ImageRequest.Builder(context).data(imageFile)
            .setParameter("last_modified", timestamp, memoryCacheKey = timestamp.toString())
            .crossfade(true).build(),
        contentDescription = "User Profile Photo",
        placeholder = painterResource(R.drawable.baseline_person_24),
        error = painterResource(R.drawable.baseline_person_24),
        modifier = modifier.clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}