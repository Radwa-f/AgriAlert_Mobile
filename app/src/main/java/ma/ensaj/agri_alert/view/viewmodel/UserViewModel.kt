package ma.ensaj.agri_alert.view.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val _loginResult = MutableStateFlow<Result<Boolean>?>(null)
    val loginResult: StateFlow<Result<Boolean>?> get() = _loginResult

    private val _registrationResult = MutableStateFlow<Result<Boolean>?>(null)
    val registrationResult: StateFlow<Result<Boolean>?> get() = _registrationResult

    private val users = mutableListOf(
        Pair("user@example.com", "password123"),
        Pair("test@example.com", "testpass"),
        Pair("douidy@gmail.com", "douidy123")
    )

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            val result = if (users.any { it.first == email && it.second == password }) {
                Result.success(true)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
            _loginResult.value = result
        }
    }

    fun registerUser(email: String, fullname: String, password: String) {
        viewModelScope.launch {
            val isEmailTaken = users.any { it.first == email }
            if (isEmailTaken) {
                _registrationResult.value = Result.failure(Exception("Email is already registered"))
            } else {
                users.add(Pair(email, password))
                _registrationResult.value = Result.success(true)
            }
        }
    }
}