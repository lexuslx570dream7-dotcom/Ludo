package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "Auth"

@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Attempt silent auto-sign-in on initial composition
    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onSignInSuccess,
            onUnauthenticated = { /* show interactive button */ },
            scope = coroutineScope
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Uploaded background image kept exactly as is
        Image(
            painter = painterResource(id = R.drawable.login_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Main Login Content: Clean, minimal layout positioned according to the design specification
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Spacer to position the logo precisely in the upper-middle area
            Spacer(modifier = Modifier.weight(0.75f))

            // Upper middle area: Circular Ludo game logo with four colored quadrants,
            // golden crown in the center, and bold colorful letters below it spelling 'Ludo Club'.
            // No tagline or subtext below the logo.
            CircularLudoGameLogo()

            // Below that, in the empty space
            Spacer(modifier = Modifier.weight(1.0f))

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color(0xFFD32F2F),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // White pill-shaped button with official Google 'G' logo on the left and bold black text 'Continue with Google'
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    onGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            isLoading = false
                            onSignInSuccess()
                        },
                        onAuthError = { err ->
                            isLoading = false
                            errorMessage = err
                        },
                        scope = coroutineScope,
                        onAuthCancelled = {
                            isLoading = false
                        }
                    )
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color(0xFFFAFAFA)
                ),
                shape = RoundedCornerShape(50),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp,
                    disabledElevation = 0.dp
                ),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .widthIn(max = 340.dp)
                    .height(54.dp)
                    .shadow(8.dp, RoundedCornerShape(50), spotColor = Color(0x33000000))
                    .testTag("google_sign_in_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color(0xFF4285F4),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Directly below the button: small gray text showing only 'Terms and Conditions' and 'Privacy Policy'
            Text(
                text = "Terms and Conditions  •  Privacy Policy",
                color = Color(0xFF757575),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            // Bottom spacing keeping it gracefully balanced above the bottom pawns and dice
            Spacer(modifier = Modifier.weight(0.45f))
        }
    }
}

/**
 * Circular Ludo game logo with four colored quadrants, a golden crown in the center,
 * and bold colorful letters below it spelling 'Ludo Club'.
 */
@Composable
fun CircularLudoGameLogo(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
    ) {
        // Circular Ludo Board with 4 colored quadrants and central golden crown
        Box(
            modifier = Modifier
                .size(136.dp)
                .shadow(16.dp, CircleShape, spotColor = Color(0x55000000))
                .clip(CircleShape)
                .background(Color.White)
                .border(
                    width = 4.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFFFD54F),
                            Color(0xFFFFB300),
                            Color(0xFFFFF9C4),
                            Color(0xFFFF8F00),
                            Color(0xFFFFD54F)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // 4 Quadrants
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f)) {
                    // Top-Left: Red
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFFE53935)),
                        contentAlignment = Alignment.Center
                    ) {
                        LudoQuadrantCell(pawnColor = Color(0xFFE53935))
                    }
                    // Vertical divider
                    Box(modifier = Modifier.width(2.5.dp).fillMaxHeight().background(Color.White))
                    // Top-Right: Green
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFF2E7D32)),
                        contentAlignment = Alignment.Center
                    ) {
                        LudoQuadrantCell(pawnColor = Color(0xFF2E7D32))
                    }
                }
                // Horizontal divider
                Box(modifier = Modifier.fillMaxWidth().height(2.5.dp).background(Color.White))
                Row(modifier = Modifier.weight(1f)) {
                    // Bottom-Left: Blue
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFF1565C0)),
                        contentAlignment = Alignment.Center
                    ) {
                        LudoQuadrantCell(pawnColor = Color(0xFF1565C0))
                    }
                    // Vertical divider
                    Box(modifier = Modifier.width(2.5.dp).fillMaxHeight().background(Color.White))
                    // Bottom-Right: Yellow
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFFFBC02D)),
                        contentAlignment = Alignment.Center
                    ) {
                        LudoQuadrantCell(pawnColor = Color(0xFFFBC02D))
                    }
                }
            }

            // Central golden crown base
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFFDE7), Color(0xFFFFECB3), Color(0xFFFFD54F))
                        )
                    )
                    .border(2.dp, Color(0xFFFF8F00), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👑",
                    fontSize = 24.sp
                )
            }
        }

        // Bold colorful letters below it spelling 'Ludo Club'
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // L - u - d - o
            LetterCircleBadge(char = "L", color = Color(0xFF0288D1))
            LetterCircleBadge(char = "u", color = Color(0xFFE53935))
            LetterCircleBadge(char = "d", color = Color(0xFF2E7D32))
            LetterCircleBadge(char = "o", color = Color(0xFFF9A825))

            Spacer(modifier = Modifier.width(6.dp))

            // C - l - u - b
            LetterCircleBadge(char = "C", color = Color(0xFFE53935))
            LetterCircleBadge(char = "l", color = Color(0xFF0288D1))
            LetterCircleBadge(char = "u", color = Color(0xFF2E7D32))
            LetterCircleBadge(char = "b", color = Color(0xFFF9A825))
        }
    }
}

@Composable
private fun LudoQuadrantCell(pawnColor: Color) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .shadow(2.dp, RoundedCornerShape(6.dp))
            .background(Color.White, RoundedCornerShape(6.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.size(9.dp).background(pawnColor, CircleShape))
                Box(modifier = Modifier.size(9.dp).background(pawnColor, CircleShape))
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.size(9.dp).background(pawnColor, CircleShape))
                Box(modifier = Modifier.size(9.dp).background(pawnColor, CircleShape))
            }
        }
    }
}

@Composable
private fun LetterCircleBadge(char: String, color: Color) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .shadow(4.dp, CircleShape, spotColor = Color(0x35000000))
            .background(Color.White, CircleShape)
            .border(1.5.dp, color.copy(alpha = 0.85f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            color = color,
            fontWeight = FontWeight.Black,
            fontSize = 17.sp,
            textAlign = TextAlign.Center
        )
    }
}

// 1. Silent Auto-Sign-In on App Startup
fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }

    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

// 2. Interactive Sign-In Button Flow
fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

// 3. Sign-Out Flow
fun signOutUser(
    context: Context,
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}
