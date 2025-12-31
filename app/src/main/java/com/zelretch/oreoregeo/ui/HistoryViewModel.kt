package com.zelretch.oreoregeo.ui

import androidx.lifecycle.ViewModel
import com.zelretch.oreoregeo.domain.OreoregeoRepository

class HistoryViewModel(repository: OreoregeoRepository) : ViewModel() {
    val history = repository.history
}
