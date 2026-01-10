package dev.girlz.drone_app.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.girlz.drone_app.data.local.DummyEntity
import dev.girlz.drone_app.data.local.DummyRepository
import dev.girlz.drone_app.data.local.LocalDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: DummyRepository
) : ViewModel() {
    val dummyItems: StateFlow<List<DummyEntity>> =
        repository.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun insert(name: String, value: String) {
        viewModelScope.launch {
            repository.insert(name, value)
        }
    }
}

class ProfileViewModelFactory(
    private val appContext: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val database = LocalDatabase.getInstance(appContext)
            val repository = DummyRepository(database.dummyDao())
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
