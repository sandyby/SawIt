package com.example.sawit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sawit.ui.theme.BgPrimary500
import com.example.sawit.ui.theme.BgSecondaryOverlay2
import com.example.sawit.ui.theme.Text600
import com.example.sawit.ui.theme.TextError900
import com.example.sawit.ui.theme.TextPrimary500

@Composable
fun InputTextWithLabel(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    type: String = "text",
    isError: Boolean = false,
    errorMsg: String? = null,
    placeholderStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = Text600
    ),
    modifier: Modifier = Modifier,
    inputTextFieldModifier: Modifier = Modifier.fillMaxWidth(),
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
    ) {
        InputTextLabel(
            label = label,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = placeholderStyle,
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
            isError = isError,
            visualTransformation = if (type == "textPassword") PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = trailingIcon,
            modifier = inputTextFieldModifier,
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
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InputTextWithLabelPreview() {
    InputTextWithLabel(
        label = "Current Password",
        value = "",
        onValueChange = { },
        placeholder = "********",
        type = "textPassword",
        isError = false,
        errorMsg = null,
    )
}
