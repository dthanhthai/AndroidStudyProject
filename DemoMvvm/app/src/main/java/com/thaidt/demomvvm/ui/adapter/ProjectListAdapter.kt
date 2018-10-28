package com.thaidt.demomvvm.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.thaidt.demomvvm.R
import com.thaidt.demomvvm.data.model.Project
import com.thaidt.demomvvm.databinding.ItemProjectListBinding

class ProjectListAdapter : RecyclerView.Adapter<ProjectListAdapter.ProjectViewHolder>() {

    private var itemList: MutableList<Project>? = mutableListOf()

    fun setItemList(list: List<Project>) {
        itemList = list.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        var binding: ItemProjectListBinding =
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_project_list, parent, false)
        return ProjectViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return itemList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        itemList?.apply {
            holder.binding.project = this[position]
            holder.binding.executePendingBindings()
        }
    }

    inner class ProjectViewHolder(binding: ItemProjectListBinding) : RecyclerView.ViewHolder(binding.root) {
        var binding: ItemProjectListBinding = binding
    }
}