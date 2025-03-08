package pw.checkers.game.presentation.gameScreen.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dshatz.composempp.AutoSizeText
import pw.checkers.game.domain.model.User

@Composable
fun UserPanel(
    user: User,
    isCurrentTurn: Boolean,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isCurrentTurn) MaterialTheme.colorScheme.secondaryContainer
    else MaterialTheme.colorScheme.background

    val textColor = if (isCurrentTurn) MaterialTheme.colorScheme.onSecondaryContainer
    else MaterialTheme.colorScheme.onBackground

    Box(modifier = modifier.background(color = backgroundColor)) {
        AutoSizeText(
            text = user.username,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(8.dp),
            fontWeight = if (isCurrentTurn) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}