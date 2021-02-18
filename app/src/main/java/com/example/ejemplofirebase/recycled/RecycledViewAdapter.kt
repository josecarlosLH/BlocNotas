package com.example.ejemplorecicledview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.ejemplofirebase.Nota
import com.example.ejemplofirebase.R
import kotlinx.android.synthetic.main.item_nota.view.*
import java.io.InputStream
import java.net.URL

class RecycledViewAdapter(val context:Context, val listaNota:List<Nota>, private val itemClickListener: OnNotaClickListener): RecyclerView.Adapter<BaseViewHolder<*>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return NotasViewHolder(LayoutInflater.from(context).inflate(R.layout.item_nota, parent, false))
    }
    interface OnNotaClickListener{
        fun eliminar(item:Nota)
    }
    //Si la funcion solo devuelve un dato
    override fun getItemCount(): Int = listaNota.size

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        if(holder is NotasViewHolder){
            holder.bind(listaNota[position],position)
        }
    }

    inner class NotasViewHolder(itemView: View):BaseViewHolder<Nota>(itemView){
        override fun bind(item: Nota, position: Int) {
            itemView.titulo.text = item.titulo
            itemView.editTextMensaje.keyListener = null;
            itemView.editTextMensaje.text =  SpannableStringBuilder(item.mensaje)
            itemView.button_eliminar.setOnClickListener {
                itemClickListener.eliminar(item)
            }
        }
    }
}