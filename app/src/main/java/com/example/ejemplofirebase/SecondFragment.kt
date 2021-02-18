package com.example.ejemplofirebase

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ejemplorecicledview.RecycledViewAdapter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_second.*
import kotlinx.android.synthetic.main.item_nota.*

class SecondFragment : Fragment(),RecycledViewAdapter.OnNotaClickListener {
    lateinit var database : FirebaseDatabase
    lateinit var list : ArrayList<Nota>
    lateinit var myRef : DatabaseReference
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        myRef = database.getReference(FirebaseAuth.getInstance().currentUser?.uid+"")
        recyclerView.layoutManager = LinearLayoutManager(context)
        button_crear.setOnClickListener {
            if (editTextTextTitulo.text.isNotEmpty() && editTextTextMensaje.text.isNotEmpty()){
                crearNota(Nota(editTextTextTitulo.text.toString(),editTextTextMensaje.text.toString()))
            }
        }

        list = ArrayList()
        cargarNotas()
    }

    fun cargarNotas(){
        println(FirebaseAuth.getInstance().currentUser?.uid)
        myRef.get().addOnCompleteListener {
            val children = it.getResult()!!.children
            children.forEach {
                val e = Nota(it.child("titulo").value as String,it.child("mensaje").value as String)
                crearNota(e)
            }
            recargarLista()
        }
    }

    fun recargarLista(){
        println("recarga lista")
        for (n:Nota in list){
            println(n.mensaje)
            println(n.titulo)
        }
        recyclerView.adapter = context?.let { RecycledViewAdapter(it,
            list, this) }
    }

    fun crearNota(nota : Nota){
        println("entra")
        var cambiado = false
        if(list.isNullOrEmpty()){
            list = ArrayList()
        }
        for (n:Nota in list){
            if(n.titulo.equals(nota.titulo)) {
                list[list.indexOf(n)].mensaje = nota.mensaje
                cambiado = true
            }
            println(n)
        }
        if (!cambiado)
            list.add(nota)
        myRef.setValue(list)
        recargarLista()
    }

    override fun eliminar(not: Nota) {
        var aux : Nota? = null
        for (n:Nota in list){
            if(n.titulo.equals(not.titulo)) {
               aux = n
            }
        }
        if(aux != null)
            list.remove(aux)
        myRef.setValue(list)
        recargarLista()
    }
}