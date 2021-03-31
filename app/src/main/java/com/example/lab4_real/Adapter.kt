package com.example.lab4_real

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lab4_real.databinding.EntriesBinding
import name.ank.lab4.BibDatabase
import name.ank.lab4.Keys
import java.io.InputStream
import java.io.InputStreamReader

class Adapter(base: InputStream) : RecyclerView.Adapter<Adapter.ViewHolder>() {
    private val database = BibDatabase(InputStreamReader(base))

    class ViewHolder (binding: EntriesBinding) : RecyclerView.ViewHolder(binding.root) {
        val author = binding.author
        val title = binding.title
        val year = binding.year
        val pages = binding.pages
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = EntriesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = database.getEntry(position % database.size())
        holder.author.text = entry.getField(Keys.AUTHOR)
        holder.pages.text = "Pages: " + entry.getField(Keys.PAGES)
        holder.title.text = entry.getField(Keys.TITLE)
        holder.year.text = entry.getField(Keys.YEAR)
    }
}