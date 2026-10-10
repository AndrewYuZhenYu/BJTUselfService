package team.bjtuss.bjtuselfservice.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import team.bjtuss.bjtuselfservice.repository.NetworkRepository.getClassroomMap
import team.bjtuss.bjtuselfservice.repository.RoomOccupancyRepository
import team.bjtuss.bjtuselfservice.repository.RoomOccupancyWeek

sealed interface RoomOccupancyState {
    data object Idle : RoomOccupancyState
    data object Loading : RoomOccupancyState
    data class Ready(val week: RoomOccupancyWeek) : RoomOccupancyState
    data class Failed(val message: String) : RoomOccupancyState
}

class ClassroomViewModel : ViewModel() {
    private var _classroomMap =
        MutableStateFlow<MutableMap<String, List<Int>>>(mutableMapOf())
    val classroomMap: StateFlow<MutableMap<String, List<Int>>> = _classroomMap.asStateFlow()
    private val _roomOccupancy = MutableStateFlow<RoomOccupancyState>(RoomOccupancyState.Idle)
    val roomOccupancy: StateFlow<RoomOccupancyState> = _roomOccupancy.asStateFlow()
    private var roomOccupancyJob: Job? = null

    init {
        // refreshClassroomMap()
    }

    fun refreshClassroomMap() {
        viewModelScope.launch {
            val map = getClassroomMap() ?: emptyMap()
            _classroomMap.value = map.toMutableMap()
        }
    }

    fun loadRoomOccupancy(week: Int? = null, building: String = "", forceRefresh: Boolean = false) {
        roomOccupancyJob?.cancel()
        roomOccupancyJob = viewModelScope.launch {
            _roomOccupancy.value = RoomOccupancyState.Loading
            val result = RoomOccupancyRepository.load(week, building, forceRefresh)
            _roomOccupancy.value = result.fold(
                onSuccess = { RoomOccupancyState.Ready(it) },
                onFailure = { RoomOccupancyState.Failed(it.message ?: "教室数据加载失败") },
            )
        }
    }
}
