package com.example.myinputlog.ui.screens.utils.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.myinputlog.R
import com.example.myinputlog.ui.theme.MyInputLogTheme
import com.example.myinputlog.ui.theme.spacing

@Composable
fun MyInputLogAppIconWithoutBackground(
    modifier: Modifier = Modifier
) {
    Image(
        modifier = modifier,
        painter = painterResource(R.drawable.app_logo),
        contentDescription = null
    )
}

@Composable
fun MyInputLogAppIcon(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(R.drawable.app_logo_white),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Preview
@Composable
fun AppIconPreview() {
    MyInputLogTheme {
        MyInputLogAppIcon(Modifier.size(MaterialTheme.spacing.extraLarge))
    }
}

@Preview
@Composable
fun AppIconWithoutBackgroundPreview() {
    MyInputLogTheme {
        MyInputLogAppIconWithoutBackground(Modifier.size(MaterialTheme.spacing.extraLarge))
    }
}