package com.example.horoscopo_android.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.horoscopo_android.HoroscopoData
import com.example.horoscopo_android.domain.HoroscopeRepository
import com.example.horoscopo_android.network.HoroscopoResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Estado que representa la UI en DetalleHoroscopoActivity.
 */
sealed interface UiState {
    object Loading : UiState
    data class Success(val apiData: HoroscopoResponse) : UiState
    data class Error(val message: String) : UiState
}

class HoroscopeViewModel(private val repository: HoroscopeRepository = HoroscopeRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    /**
     * Función para iniciar la llamada a la API y actualizar el estado de la UI.
     * @param context Contexto necesario para obtener el nombre del signo.
     * @param horoscopoId ID del signo a buscar (0-11).
     * @param timeframe Periodo de consulta ('daily', 'weekly', 'monthly').
     */
    fun fetchHoroscope(context: Context, horoscopoId: Int, timeframe: String) {
        // Obtenemos el nombre del signo en el formato de la API (ej: 'aries')
        val signName = HoroscopoData.getSignNameById(context, horoscopoId)

        if (signName == null) {
            _uiState.value = UiState.Error("Signo no encontrado.")
            return
        }

        _uiState.value = UiState.Loading // Mostrar estado de carga

        viewModelScope.launch {
            val result = repository.getHoroscope(signName, timeframe)

            result.onSuccess { response ->
                _uiState.value = UiState.Success(response)
            }.onFailure { e ->
                _uiState.value = UiState.Error(e.message ?: "Error desconocido en la red.")
            }
        }
    }
}
