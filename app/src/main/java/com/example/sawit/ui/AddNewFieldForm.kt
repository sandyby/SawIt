package com.example.sawit.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sawit.ui.theme.*

@Composable
fun AddFieldDataForm(
    onSubmit: (Map<String, String>) -> Unit
) {
    val scrollState = rememberScrollState()
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Option 1", "Option 2", "Option 3")
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InputTextWithLabel(
            label = "Field Name",
            value = "",
            onValueChange = {},
            placeholder = "Enter your field name here",
            type = "text",
            isError = false,
            errorMsg = null,
            placeholderStyle = MaterialTheme.typography.bodyMedium.copy(
//                fontSize = 12.sp,
                color = Text600
            ),
            modifier = Modifier,
//                .height(60.dp),
            inputTextFieldModifier = Modifier.height(IntrinsicSize.Min)
        )
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
            ) {
                InputTextLabel(
                    label = "Field Area",
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    InputText(
                        value = "",
                        onValueChange = {},
                        placeholder = "127",
                        type = "numberDecimal",
                        isError = false,
                        errorMsg = null,
                        modifier = Modifier.width(88.dp)
                    )
                    Text(
                        text = "Ha",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary500
                    )
                }
            }
        }
        InputTextWithLabel(
            label = "Palm Oil Type",
            value = "",
            onValueChange = {},
            placeholder = "Enter your palm oil type here",
            type = "text",
            isError = false,
            errorMsg = null,
        )
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
            ) {
                InputTextLabel(
                    label = "Average Palm Oil Age",
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    InputText(
                        value = "",
                        onValueChange = {},
                        placeholder = "127",
                        type = "numberInteger",
                        isError = false,
                        errorMsg = null,
                        modifier = Modifier.width(88.dp)
                    )
                    Text(
                        text = "month(s)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary500
                    )
                }
            }
        }
        InputTextWithLabel(
            label = "Notes",
            value = "",
            onValueChange = {},
            placeholder = "",
            type = "text",
            isError = false,
            errorMsg = null,
            modifier = Modifier
        )
        Button(
            onClick = {

            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = BgPrimary500,
                contentColor = White
            )
        ) {
            Text("Add Field")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddFieldDataFormPreview() {
    AddFieldDataForm { }
}

