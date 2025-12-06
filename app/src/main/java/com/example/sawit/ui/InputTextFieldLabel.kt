package com.example.sawit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.sawit.ui.theme.TextPrimary500
import com.example.sawit.ui.theme.TextPrimary900

@Composable
fun InputTextLabel(
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Start,
            color = TextPrimary900,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InputTextLabelPreview() {
    InputTextLabel(
        label = "Label Title",
        modifier = Modifier
    )
}

