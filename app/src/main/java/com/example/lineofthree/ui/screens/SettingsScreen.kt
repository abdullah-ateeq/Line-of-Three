package com.example.lineofthree.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.lineofthree.data.AppSettings
import com.example.lineofthree.data.BoardTheme
import com.example.lineofthree.data.GameStatistics
import com.example.lineofthree.data.ThemeMode
import com.example.lineofthree.ui.AppViewModel
import com.example.lineofthree.ui.components.ConfirmDialog
import com.example.lineofthree.ui.components.HairlineDivider
import com.example.lineofthree.ui.components.SecondaryButton
import com.example.lineofthree.ui.components.SectionLabel
import com.example.lineofthree.ui.components.SubScreen

@Composable
fun SettingsScreen(
    settings: AppSettings,
    stats: GameStatistics,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    SubScreen("Settings", onBack) {
        SectionLabel("Appearance")
        SettingLabel("Theme")
        Segmented(ThemeMode.entries, settings.themeMode, { it.label }, viewModel::setThemeMode)
        Spacer(Modifier.height(16.dp))
        SettingLabel("Board Theme")
        Segmented(BoardTheme.entries, settings.boardTheme, { it.label }, viewModel::setBoardTheme)

        SectionLabel("Feedback")
        ToggleRow("Sound Effects", settings.soundEnabled, viewModel::setSoundEnabled)
        HairlineDivider()
        ToggleRow("Haptic Feedback", settings.hapticsEnabled, viewModel::setHapticsEnabled)

        SectionLabel("Gameplay")
        ToggleRow("Show Legal Moves", settings.showLegalMoves, viewModel::setShowLegalMoves)
        HairlineDivider()
        ToggleRow("Highlight Completed Lines", settings.highlightCompletedLines, viewModel::setHighlightCompletedLines)

        SectionLabel("Statistics")
        Text(
            "${stats.gamesPlayed} games · Player 1 ${stats.player1Wins} wins · Player 2 ${stats.player2Wins} wins · Most lines in a win ${stats.mostLinesFormed}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        SecondaryButton("Reset Statistics", { confirmReset = true }, Modifier.fillMaxWidth(), enabled = stats.gamesPlayed > 0)
    }

    if (confirmReset) {
        ConfirmDialog(
            title = "Reset statistics?",
            message = "Games played and win totals will be cleared.",
            confirmLabel = "Reset",
            onConfirm = { confirmReset = false; viewModel.resetStatistics() },
            onDismiss = { confirmReset = false }
        )
    }
}

@Composable
private fun SettingLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun <T> Segmented(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(3.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.background)
                    .selectable(selected = isSelected, role = Role.RadioButton) { onSelect(option) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label(option).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}
