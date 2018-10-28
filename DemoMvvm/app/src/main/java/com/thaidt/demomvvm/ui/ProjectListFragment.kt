package com.thaidt.demomvvm.ui


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thaidt.demomvvm.R
import com.thaidt.demomvvm.data.callback.OnDataReceiveResult
import com.thaidt.demomvvm.data.model.Project
import com.thaidt.demomvvm.databinding.FragmentProjectListBinding
import com.thaidt.demomvvm.ui.adapter.ProjectListAdapter
import com.thaidt.demomvvm.ui.viewholder.ProjectListViewModel
import kotlinx.android.synthetic.main.fragment_project_list.*


//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

class ProjectListFragment : Fragment(), LifecycleOwner {
//    private var param1: String? = null
//    private var param2: String? = null

    lateinit var mBinding:FragmentProjectListBinding
    lateinit var projectListListAdapter:ProjectListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_project_list, container, false)

        mBinding.isLoading = true

        projectListListAdapter = ProjectListAdapter()
        val linearLayoutManager:LinearLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        mBinding.projectListRv.adapter = projectListListAdapter
        mBinding.projectListRv.layoutManager = linearLayoutManager

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel:ProjectListViewModel = ViewModelProviders.of(this).get(ProjectListViewModel::class.java)

        viewModel.loadProjectList(object : OnDataReceiveResult<List<Project>>{
            override fun onResponse(data: List<Project>) {

            }

            override fun onError(message: Throwable) {

            }

        })
        observeViewModel(viewModel)
    }

    fun observeViewModel(viewModel: ProjectListViewModel){
        viewModel.getProjectListObservable().observe(this, Observer {
            it?.apply {
                mBinding.isLoading = false
                projectListListAdapter.setItemList(this)
            }
        })
    }

    companion object {

//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            ProjectListFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
        val TAG = ProjectListFragment::class.java.simpleName
        fun newInstance() = ProjectListFragment()

    }
}
