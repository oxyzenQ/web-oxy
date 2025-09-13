/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.oxyzenq.kconvert.data.local.SettingsDataStore
import com.oxyzenq.kconvert.presentation.util.setImmersiveMode
import com.oxyzenq.kconvert.domain.engine.UpdateEngine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.oxyzenq.kconvert.presentation.navigation.KconvertNavigation
import com.oxyzenq.kconvert.presentation.screen.KconvertMainScreen
import com.oxyzenq.kconvert.ui.theme.CurrencyConverterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var updateEngine: UpdateEngine
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply persisted full screen setting ASAP
        val settingsStore = SettingsDataStore(this)
        lifecycleScope.launch {
            val full = settingsStore.fullScreenFlow.first()
            setImmersiveMode(this@MainActivity, full)
        }
        setContent {
            CurrencyConverterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    KconvertNavigation(navController = navController)
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Check for updates once per session when activity resumes
        lifecycleScope.launch {
            updateEngine.checkOnceAndNotifyIfNeeded()
        }
    }
}

@Composable
fun CurrencyConverterApp() {
    KconvertMainScreen()
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CurrencyConverterTheme {
        CurrencyConverterApp()
    }
}
