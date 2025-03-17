package com.tupausa.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tupausa.repository.TuPausaRepository
import com.tupausa.viewModel.TuPausaViewModel

class TuPausaViewModelFactory(private val repository: TuPausaRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TuPausaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TuPausaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}