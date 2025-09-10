/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.oxyzenq.kconvert.data.repository.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for settings screen with update repository access
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    val updateRepository: UpdateRepository
) : ViewModel()
