package com.example.sawit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sawit.ui.theme.BgPrimary500
import com.example.sawit.ui.theme.BgSecondaryOverlay2
import com.example.sawit.ui.theme.Text600
import com.example.sawit.ui.theme.TextError900

@Composable
fun InputText(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    type: String = "text",
    isError: Boolean = false,
    errorMsg: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Text600,
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = when (type) {
                    "textEmailAddress" -> KeyboardType.Email
                    "textPassword" -> KeyboardType.Password
                    "numberDecimal" -> KeyboardType.Decimal
                    "numberInteger" -> KeyboardType.Number
                    else -> KeyboardType.Text
                }
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BgPrimary500,
                unfocusedBorderColor = if (isError) TextError900 else BgSecondaryOverlay2,
                errorBorderColor = TextError900,
                cursorColor = BgPrimary500
            )
        )
        if (isError && errorMsg != null) {
            Text(
                text = errorMsg,
                color = TextError900,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
