package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.AssistPlusNav
import com.example.ui.screens.LoadingConnectionScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.AssistBgDark
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppViewModel
import com.example.viewmodel.AuthState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AssistPlusApp()
            }
        }
    }
}

@Composable
fun AssistPlusApp(viewModel: AppViewModel = viewModel()) {
    val context = LocalContext.current
    val activity = context as? Activity
    val authState by viewModel.authState.collectAsState()
    val currentPlayable by viewModel.currentPlayable.collectAsState()

    // Control system bars (hide status & navigation bars when player is active)
    DisposableEffect(currentPlayable != null) {
        val window = activity?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            if (currentPlayable != null) {
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.statusBars())
                controller.hide(WindowInsetsCompat.Type.navigationBars())
            } else {
                controller.show(WindowInsetsCompat.Type.statusBars())
                controller.show(WindowInsetsCompat.Type.navigationBars())
            }
        }
        onDispose {
            val window = activity?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.show(WindowInsetsCompat.Type.statusBars())
                controller.show(WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    // When the video player is active, avoid adding safe drawing padding so video can go true edge-to-edge
    val contentModifier = if (currentPlayable != null) {
        Modifier.fillMaxSize()
    } else {
        Modifier.fillMaxSize().safeDrawingPadding()
    }

    Box(
        modifier = contentModifier.background(AssistBgDark)
    ) {
        when (val state = authState) {
            is AuthState.Loading -> {
                LoadingConnectionScreen(
                    message = state.message,
                    subMessage = state.subMessage,
                    onCancel = { viewModel.cancelLoading() }
                )
            }
            is AuthState.Authenticated -> {
                AssistPlusNav(
                    account = state.account,
                    viewModel = viewModel,
                    onLogout = { viewModel.logout() }
                )
            }
            else -> {
                val savedAccounts by viewModel.savedAccounts.collectAsState()
                LoginScreen(
                    authState = state,
                    savedAccounts = savedAccounts,
                    onSelectSavedAccount = { account ->
                        viewModel.loginWithSavedAccount(account)
                    },
                    onDeleteSavedAccount = { username ->
                        viewModel.removeSavedAccount(username)
                    },
                    onLoginXtream = { server, user, pass ->
                        viewModel.loginXtream(server, user, pass)
                    },
                    onLoginXtreamAuto = { user, pass ->
                        viewModel.loginXtreamAuto(user, pass)
                    },
                    onPaymentSuccess = { user, days, exp ->
                        viewModel.activatePaidSubscription(user, days, exp)
                    }
                )
            }
        }
    }
}

