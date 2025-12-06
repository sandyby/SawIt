package com.example.sawit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sawit.R
import com.example.sawit.ui.theme.BgPrimary500
import com.example.sawit.ui.theme.BgSecondary900
import com.example.sawit.ui.theme.TextPrimary900
import com.example.sawit.ui.theme.White

@Composable
fun EditPasswordScreen(
    onBack: () -> Unit,
    onPasswordChanged: (oldPassword: String, newPassword: String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var oldError by remember { mutableStateOf<String?>(null) }
    var newError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgSecondary900)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_filled_keyboard_backspace_24_text_primary_900),
                    contentDescription = "Back Button",
                    tint = TextPrimary900,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Text(
                text = "Edit Password",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.lato_bold)),
                color = TextPrimary900,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            InputTextWithLabel(
                label = "Current Password",
                value = oldPassword,
                onValueChange = { oldPassword = it; oldError = null },
                placeholder = "********",
                type = "textPassword",
                isError = oldError != null,
                errorMsg = oldError,
            )
            InputTextWithLabel(
                label = "New Password",
                value = newPassword,
                onValueChange = { newPassword = it; newError = null },
                placeholder = "********",
                type = "textPassword",
                isError = newError != null,
                errorMsg = newError
            )
            InputTextWithLabel(
                label = "Confirm New Password",
                value = confirmPassword,
                onValueChange = { confirmPassword = it; confirmError = null },
                placeholder = "********",
                type = "textPassword",
                isError = confirmError != null,
                errorMsg = confirmError
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                oldError = null; newError = null; confirmError = null

                when {
                    oldPassword.isEmpty() -> oldError = "Please fill in your current password!"
                    newPassword.isEmpty() -> newError = "Please fill in your new password!"
                    confirmPassword.isEmpty() -> confirmError = "Please confirm your new password!"
                    newPassword.length < 8 -> newError =
                        "Password must be at least 8 characters long!"

                    newPassword != confirmPassword -> confirmError = "Password do not match!"
                    else -> {
                        onPasswordChanged(oldPassword, newPassword)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BgPrimary500),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "CHANGE PASSWORD",
                color = White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.lato_bold))
            )
        }
    }
}