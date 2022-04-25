package vn.app.newstospeech.base.adapter

import android.util.SparseArray
import com.base.common.base.adapter.BaseAdapter
import vn.app.newstospeech.base.BaseItemViewModel

abstract class BaseVMAdapter<T, VM : BaseItemViewModel<*, *>>(list: ArrayList<T>) :
    BaseAdapter<T>(list) {
    val viewModelProvide = SparseArray<VM>()

}