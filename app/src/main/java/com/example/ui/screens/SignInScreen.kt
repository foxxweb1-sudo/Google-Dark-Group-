package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.CybeNodeViewModel

@Composable
fun SignInScreen(
    viewModel: CybeNodeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isRegisterTab by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Login Fields
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showLoginPassword by remember { mutableStateOf(false) }

    // Register Fields
    var regName by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var showRegPassword by remember { mutableStateOf(false) }

    // Reset Password Dialog
    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // App Logo / Symbol
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.linearGradient(listOf(CyberPurple, CyberPurpleLight)))
                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "DG",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Dark Group",
            color = CyberTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "منظومة الاتصال الصوتي السحابي والمراسلة الفورية",
            color = CyberTextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Tab Selector (Login / Register)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CyberCard)
                .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (!isRegisterTab) CyberPurple else Color.Transparent)
                    .clickable { isRegisterTab = false }
                    .testTag("tab_login"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "تسجيل الدخول",
                    color = if (!isRegisterTab) Color.White else CyberTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isRegisterTab) CyberPurple else Color.Transparent)
                    .clickable { isRegisterTab = true }
                    .testTag("tab_register"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "إنشاء حساب جديد",
                    color = if (isRegisterTab) Color.White else CyberTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Google Sign-In Primary Button
        Button(
            onClick = {
                isLoading = true
                viewModel.signInWithGoogle(context as Activity) {
                    isLoading = false
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_google_signin"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(20.dp),
                    color = Color.Transparent
                ) {
                    Text("G", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF4285F4))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isRegisterTab) "المتابعة بحساب Google" else "تسجيل الدخول عبر Google",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = CyberBorder)
            Text(
                text = " أو بالبريد الإلكتروني ",
                color = CyberTextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = CyberBorder)
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (!isRegisterTab) {
            // === LOGIN FORM ===
            OutlinedTextField(
                value = loginEmail,
                onValueChange = { loginEmail = it },
                label = { Text("البريد الإلكتروني") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = CyberCyan)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_login_email"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = authFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = loginPassword,
                onValueChange = { loginPassword = it },
                label = { Text("كلمة المرور") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = CyberCyan)
                },
                trailingIcon = {
                    IconButton(onClick = { showLoginPassword = !showLoginPassword }) {
                        Icon(
                            imageVector = if (showLoginPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = CyberTextMuted
                        )
                    }
                },
                visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_login_password"),
                colors = authFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "نسيت كلمة المرور؟",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { showForgotDialog = true }
                )
            }

            if (isLoading) {
                CircularProgressIndicator(color = CyberCyan)
            } else {
                Button(
                    onClick = {
                        val cleanEmail = loginEmail.trim()
                        val cleanPass = loginPassword.trim()
                        if (cleanEmail.isBlank()) {
                            viewModel.showToast("يرجى إدخال البريد الإلكتروني", com.example.viewmodel.ToastType.INFO)
                            return@Button
                        }
                        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
                            viewModel.showToast("يرجى إدخال بريد إلكتروني صالح (مثال: name@domain.com)", com.example.viewmodel.ToastType.ERROR)
                            return@Button
                        }
                        if (cleanPass.isBlank()) {
                            viewModel.showToast("يرجى إدخال كلمة المرور", com.example.viewmodel.ToastType.INFO)
                            return@Button
                        }
                        isLoading = true
                        viewModel.signInWithEmail(cleanEmail, cleanPass) {
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_email_login"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("تسجيل الدخول", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ليس لديك حساب بعد؟ ",
                        color = CyberTextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "إنشاء حساب جديد",
                        color = CyberCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                regEmail = loginEmail.trim()
                                isRegisterTab = true
                            }
                            .testTag("switch_to_register_link")
                    )
                }
            }
        } else {
            // === REGISTER FORM ===
            OutlinedTextField(
                value = regName,
                onValueChange = { regName = it },
                label = { Text("الاسم الكامل") },
                placeholder = { Text("مثال: أحمد محمد") },
                leadingIcon = {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = CyberCyan)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_reg_name"),
                colors = authFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = regUsername,
                onValueChange = { regUsername = it },
                label = { Text("اسم المستخدم للبحث (@username)") },
                placeholder = { Text("مثال: ahmed99 (للبحث عنك)") },
                leadingIcon = {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = CyberCyan)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_reg_username"),
                colors = authFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = regEmail,
                onValueChange = { regEmail = it },
                label = { Text("البريد الإلكتروني") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = CyberCyan)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_reg_email"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = authFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = regPassword,
                onValueChange = { regPassword = it },
                label = { Text("كلمة المرور (6 أحرف على الأقل)") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = CyberCyan)
                },
                trailingIcon = {
                    IconButton(onClick = { showRegPassword = !showRegPassword }) {
                        Icon(
                            imageVector = if (showRegPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = CyberTextMuted
                        )
                    }
                },
                visualTransformation = if (showRegPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_reg_password"),
                colors = authFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (isLoading) {
                CircularProgressIndicator(color = CyberCyan)
            } else {
                Button(
                    onClick = {
                        val cleanName = regName.trim()
                        val cleanUsername = regUsername.trim().lowercase().replace(Regex("[^a-z0-9_]"), "")
                        val cleanEmail = regEmail.trim()
                        val cleanPass = regPassword.trim()

                        if (cleanName.isBlank()) {
                            viewModel.showToast("يرجى إدخال الاسم الكامل", com.example.viewmodel.ToastType.INFO)
                            return@Button
                        }
                        if (cleanUsername.length < 3) {
                            viewModel.showToast("اسم المستخدم يجب أن يتكون من 3 أحرف على الأقل", com.example.viewmodel.ToastType.ERROR)
                            return@Button
                        }
                        if (cleanEmail.isBlank() || !cleanEmail.contains("@") || !cleanEmail.contains(".")) {
                            viewModel.showToast("يرجى إدخال بريد إلكتروني صالح", com.example.viewmodel.ToastType.ERROR)
                            return@Button
                        }
                        if (cleanPass.length < 6) {
                            viewModel.showToast("كلمة المرور يجب أن تتكون من 6 خانات على الأقل", com.example.viewmodel.ToastType.ERROR)
                            return@Button
                        }
                        isLoading = true
                        viewModel.signUpWithEmail(cleanName, cleanUsername, cleanEmail, cleanPass) {
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_email_register"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("إنشاء الحساب والبدء", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "لديك حساب بالفعل؟ ",
                        color = CyberTextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "تسجيل الدخول",
                        color = CyberCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                loginEmail = regEmail.trim()
                                isRegisterTab = false
                            }
                            .testTag("switch_to_login_link")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "نظام مشفر سحابياً بالكامل • اتصال صوتي عالمي مستقر",
            color = CyberTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }

    // Forgot Password Dialog
    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("إعادة ضبط كلمة المرور", color = CyberTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("أدخل بريدك الإلكتروني لإرسال رابط إعادة التعيين:", color = CyberTextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        label = { Text("البريد الإلكتروني") },
                        colors = authFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.sendPasswordReset(forgotEmail)
                        showForgotDialog = false
                    }
                ) {
                    Text("إرسال الرابط", color = CyberCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("إلغاء", color = CyberTextMuted)
                }
            },
            containerColor = CyberCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyberCyan,
    unfocusedBorderColor = CyberBorder,
    focusedLabelColor = CyberCyan,
    unfocusedLabelColor = CyberTextSecondary,
    focusedTextColor = CyberTextPrimary,
    unfocusedTextColor = CyberTextPrimary,
    cursorColor = CyberCyan
)
