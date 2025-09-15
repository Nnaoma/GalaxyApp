package com.adikob.galaxychat.ui.views.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.adikob.galaxychat.R
import com.adikob.galaxychat.utility.shimmerBackground

@Composable
fun FullScreenLoadingDialog(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF858585).copy(alpha = 0.6f))
            .clickable(enabled = false) {} // Block touches
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.Center)
                .size(50.dp),
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun NetworkImageWidget(
    modifier: Modifier = Modifier,
    imageUrl: String
) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .build(),
        contentDescription = stringResource(R.string.profile_picture),
        loading = { Box(modifier = Modifier
            .fillMaxSize()
            .shimmerBackground()) },
        error = {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary))
        },
        modifier = modifier.clip(CircleShape)
    )
}

@Composable
fun CircularLoadingOverlay(modifier: Modifier) {
    Card(
        shape = CircleShape,
        modifier = modifier.size(32.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp),
                strokeWidth = 2.dp
            )
        }
    }
}