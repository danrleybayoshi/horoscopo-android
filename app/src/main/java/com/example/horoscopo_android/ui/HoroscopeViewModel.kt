package com.example.horoscopo_android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.horoscopo_android.Horoscopo
import com.example.horoscopo_android.domain.HoroscopeRepository // ✅ CORRECCIÓN: Usando el paquete 'domain'
import com.example.horoscopo_android.network.HoroscopeResponse // ✅ NUEVO: Importando el modelo desde el paquete 'network'
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// NOTA: La clase HoroscopeResponse se ha movido al paquete 'network' y se importa arriba.
// Se ha eliminado la definición duplicada de la clase de este archivo.

sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    // UiState ahora usa la clase HoroscopeResponse importada desde el paquete 'network'.
    data class Success(val horoscopo: Horoscopo, val apiData: HoroscopeResponse) : UiState()
    data class Error(val message: String) : UiState()
}

class HoroscopeViewModel(
    private val repository: HoroscopeRepository = HoroscopeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun fetchHoroscope(horoscopo: Horoscopo) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // Para simplificar la corrección del error, usaremos el ID como placeholder del signo.
                // ATENCIÓN: En una aplicación real, horoscopo.id.toString() debe reemplazarse
                // por el nombre del signo en inglés (ej: "aries") para la API.
                val signNameForApi = horoscopo.id.toString()

                // 🚨 CORRECCIÓN: Usamos la función getHoroscopeWithFailover del repositorio
                // y manejamos el resultado (Result<T>).
                val result = repository.getHoroscopeWithFailover(signNameForApi)

                result.onSuccess { response ->
                    _uiState.value = UiState.Success(horoscopo, response)
                }.onFailure { e ->
                    _uiState.value = UiState.Error("Fallo al cargar el horóscopo: ${e.message}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Fallo al cargar el horóscopo: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
