package vn.app.newstospeech.ui.dashboard

import android.os.Bundle
import android.view.View
import com.base.common.base.fragment.BaseMVVMFragment
import com.base.common.base.viewmodel.CommonEvent
import org.koin.androidx.viewmodel.ext.android.viewModel
import vn.app.newstospeech.R
import vn.app.newstospeech.databinding.FragmentDashboardBinding

class DashBoardFragment :
    BaseMVVMFragment<CommonEvent, FragmentDashboardBinding, DashBoardViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_dashboard
    override val viewModel: DashBoardViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewEvent()
    }

    private fun setupViewEvent() {

    }


}