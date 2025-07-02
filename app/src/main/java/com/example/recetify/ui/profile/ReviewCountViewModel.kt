// app/src/main/java/com/example/recetify/ui/profile/ReviewCountViewModel.kt
package com.example.recetify.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReviewCountViewModel(app: Application) : AndroidViewModel(app) {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count

    init { loadCount() }

    fun loadCount() = viewModelScope.launch {
        _count.value = try {
            RetrofitClient.api.countMyReviews()
        } catch (e: Exception) {
            0
        }
    }
}