package tranhph26979.fpoly.app_bao_thuc.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AlarmViewModel:ViewModel() {
    val alarmTime = MutableLiveData<String>()
    val isRepeat = MutableLiveData<Boolean>()
}