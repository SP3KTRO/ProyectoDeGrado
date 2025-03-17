package com.tupausa.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tupausa.model.Usuario
import com.tupausa.model.PausaActiva
import com.tupausa.model.Ejercicio
import com.tupausa.repository.TuPausaRepository

class TuPausaViewModel(private val repository: TuPausaRepository) : ViewModel() {
    private val _usuarios = MutableLiveData<List<Usuario>>()
    val usuarios: LiveData<List<Usuario>> get() = _usuarios

    private val _pausasActivas = MutableLiveData<List<PausaActiva>>()
    val pausasActivas: LiveData<List<PausaActiva>> get() = _pausasActivas

    private val _ejercicios = MutableLiveData<List<Ejercicio>>()
    val ejercicios: LiveData<List<Ejercicio>> get() = _ejercicios

    fun insertUsuario(usuario: Usuario) {
        repository.insertUsuario(usuario)
        loadUsuarios()
    }

    fun loadUsuarios() {
        _usuarios.value = repository.getAllUsuarios()
    }

    fun insertPausaActiva(pausaActiva: PausaActiva) {
        repository.insertPausaActiva(pausaActiva)
        loadPausasActivas()
    }

    fun loadPausasActivas() {
        _pausasActivas.value = repository.getAllPausasActivas()
    }

    fun insertEjercicio(ejercicio: Ejercicio) {
        repository.insertEjercicio(ejercicio)
        loadEjercicios()
    }

    fun loadEjercicios() {
        _ejercicios.value = repository.getAllEjercicios()
    }
}