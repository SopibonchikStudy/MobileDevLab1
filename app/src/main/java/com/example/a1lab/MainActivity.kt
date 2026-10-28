package com.example.a1lab

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.a1lab.domain.RegisteredAccount
import com.example.a1lab.logging.LoggedActivity
import com.example.a1lab.navigation.ContentRoute
import com.example.a1lab.navigation.PunchlineRoute
import com.example.a1lab.navigation.RegistrationRoute
import com.example.a1lab.navigation.SettingsRoute
import com.example.a1lab.navigation.StartRoute
import com.example.a1lab.navigation.settings
import com.example.a1lab.ui.ContentFragment
import com.example.a1lab.ui.PunchlineTheme
import com.example.a1lab.ui.RegistrationFragment
import com.example.a1lab.ui.SettingsFragment
import com.example.a1lab.ui.StartFragment
import com.example.a1lab.viewmodel.ContentViewModel
import com.example.a1lab.viewmodel.RegistrationViewModel
import com.example.a1lab.viewmodel.SettingsViewModel
import com.example.a1lab.viewmodel.StartViewModel

class MainActivity : LoggedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val backStack = rememberNavBackStack(StartRoute())
            val current = requireNotNull(backStack.last() as? PunchlineRoute)
            PunchlineTheme(current.settings()) {
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier,
                    onBack = {
                        if (backStack.size > 1) backStack.removeLastOrNull()
                        else finish()
                    },
                    entryProvider = entryProvider {
                        entry<StartRoute> { route ->
                            val model: StartViewModel = viewModel(
                                key = "start-${route.account?.profile?.login}",
                                factory = viewModelFactory {
                                    initializer { StartViewModel(route.settings, route.account) }
                                }
                            )
                            StartFragment(
                                onRegister = { backStack.add(RegistrationRoute(route.settings)) },
                                onAuthenticated = { state ->
                                    backStack[0] = ContentRoute(state)
                                },
                                model = model
                            )
                        }
                        entry<RegistrationRoute> { route ->
                            val model: RegistrationViewModel = viewModel(
                                key = "registration",
                                factory = viewModelFactory { initializer { RegistrationViewModel() } }
                            )
                            RegistrationFragment(
                                model = model,
                                onBack = { backStack.removeLastOrNull() },
                                onRegistered = { account -> returnToStart(backStack, route, account) }
                            )
                        }
                        entry<ContentRoute> { route ->
                            val model: ContentViewModel = viewModel(
                                key = "content-${route.state.hashCode()}",
                                factory = viewModelFactory {
                                    initializer { ContentViewModel(route.state) }
                                }
                            )
                            ContentFragment(
                                model = model,
                                onOpenSettings = { state -> backStack.add(SettingsRoute(state)) },
                                onExit = ::finish
                            )
                        }
                        entry<SettingsRoute> { route ->
                            val model: SettingsViewModel = viewModel(
                                key = "settings-${route.state.hashCode()}",
                                factory = viewModelFactory {
                                    initializer { SettingsViewModel(route.state) }
                                }
                            )
                            SettingsFragment(
                                model = model,
                                onStateChanged = { state -> updateContent(backStack, state) },
                                onBack = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                )
            }
        }
    }

    private fun returnToStart(
        backStack: MutableList<NavKey>,
        route: RegistrationRoute,
        account: RegisteredAccount
    ) {
        if (backStack.isEmpty()) return
        backStack[0] = StartRoute(route.settings, account)
        while (backStack.size > 1) backStack.removeLast()
    }

    private fun updateContent(backStack: MutableList<NavKey>, state: com.example.a1lab.domain.PunchlineState) {
        val index = backStack.indexOfLast { it is ContentRoute }
        if (index >= 0) backStack[index] = ContentRoute(state)
    }
}
