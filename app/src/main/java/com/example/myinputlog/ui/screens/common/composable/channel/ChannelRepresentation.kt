package com.example.myinputlog.ui.screens.common.composable.channel

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.myinputlog.R
import com.example.myinputlog.ui.theme.spacing

@Composable
fun ChannelRepresentation(
    title: String, url: String?, modifier: Modifier = Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (url == null) {
            Spacer(modifier = Modifier.size(MaterialTheme.spacing.large))
            Spacer(Modifier.size(MaterialTheme.spacing.medium))
            Text(stringResource(R.string.channel_other_channels))
        } else {
            ChannelThumbnail(
                modifier = Modifier.size(MaterialTheme.spacing.large), channelThumbnailUrl = url
            )
            Spacer(Modifier.size(MaterialTheme.spacing.medium))
            Text(title)
        }
    }
}