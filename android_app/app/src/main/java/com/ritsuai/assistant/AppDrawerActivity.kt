package com.ritsuai.assistant

import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ritsuai.assistant.databinding.ActivityAppDrawerBinding

class AppDrawerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppDrawerBinding
    private lateinit var apps: List<AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadApps()

        binding.appsRecyclerView.layoutManager = GridLayoutManager(this, 4)
        binding.appsRecyclerView.adapter = AppsAdapter(apps)
    }

    private fun loadApps() {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        apps = resolveInfos.map { AppInfo(
            label = it.loadLabel(pm),
            icon = it.loadIcon(pm),
            packageName = it.activityInfo.packageName
        ) }
    }

    data class AppInfo(
        val label: CharSequence,
        val icon: Drawable,
        val packageName: String
    )

    private inner class AppsAdapter(private val apps: List<AppInfo>) : RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.app_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = apps[position]
            holder.appName.text = app.label
            holder.appIcon.setImageDrawable(app.icon)
            holder.itemView.setOnClickListener {
                val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName.toString())
                startActivity(launchIntent)
            }
        }

        override fun getItemCount(): Int {
            return apps.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val appName: TextView = itemView.findViewById(R.id.app_name)
            val appIcon: ImageView = itemView.findViewById(R.id.app_icon)
        }
    }
}
