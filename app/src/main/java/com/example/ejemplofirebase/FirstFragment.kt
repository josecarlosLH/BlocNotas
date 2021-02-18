package com.example.ejemplofirebase

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_first.*
import kotlin.system.exitProcess


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    lateinit var mGoogleSignInClient : GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var provider: OAuthProvider.Builder
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this.requireContext(), gso)
        auth = Firebase.auth
        provider = OAuthProvider.newBuilder("github.com")
        setup()
        estaIniciada()
        FirebaseAuth.getInstance().addAuthStateListener {
            mGoogleSignInClient.signOut()
        }
    }
    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, 3)
    }
    fun loginGithub(){
        val pendingResultTask: Task<AuthResult>? =
            FirebaseAuth.getInstance().pendingAuthResult
        if (pendingResultTask != null) {
            // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                .addOnSuccessListener {
                        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
                }
                .addOnFailureListener {
                    // Handle failure.
                }
        } else {
            activity?.let {
                FirebaseAuth.getInstance()
                    .startActivityForSignInWithProvider( /* activity= */it, provider.build())
                    .addOnSuccessListener(
                        OnSuccessListener<AuthResult?> {
                            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
                        })
                    .addOnFailureListener(
                        OnFailureListener {
                            // Handle failure.
                        })
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 3) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // ...
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this.requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(this.requireView(), "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                }

                // ...
            }
    }
    private fun setup(){
        b_reg.setOnClickListener {
            if(editTextTextEmailAddress.text.isNotEmpty() && editTextTextPassword.text.isNotEmpty()){
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    editTextTextEmailAddress.text.toString(),
                    editTextTextPassword.text.toString()
                ).addOnCompleteListener {
                    if(it.isSuccessful) {
                        Toast.makeText(context, "Registrado Correctamente", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
                        sesionIniciada()
                    }else
                        Toast.makeText(context,"Ocurrio un error", Toast.LENGTH_SHORT).show()
                }
            }
        }
        b_login.setOnClickListener {
            if(editTextTextEmailAddress.text.isNotEmpty() && editTextTextPassword.text.isNotEmpty()){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    editTextTextEmailAddress.text.toString(),
                    editTextTextPassword.text.toString()
                ).addOnCompleteListener {
                    if(it.isSuccessful) {
                        Toast.makeText(context, "Logueado Correctamente", Toast.LENGTH_SHORT).show()
                        sesionIniciada()
                        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
                    }else
                        Toast.makeText(context,"Ocurrio un error el loguearse", Toast.LENGTH_SHORT).show()
                }
            }
        }
        sign_in_button.setOnClickListener {
            signIn()
        }
        b_login_github.setOnClickListener {
            loginGithub()
        }
    }

    private fun sesionIniciada(){
        val pref: SharedPreferences.Editor = requireActivity().getSharedPreferences(getString(R.string.prefs_name), Context.MODE_PRIVATE).edit()
        pref.putString("email",editTextTextEmailAddress.text.toString())
        pref.putString("pass",editTextTextPassword.text.toString())
        pref.apply()
    }
    private fun estaIniciada(){
        val pref: SharedPreferences =
            requireActivity().getSharedPreferences(getString(R.string.prefs_name), Context.MODE_PRIVATE)
        val email = pref.getString("email","")
        val pass = pref.getString("pass","")
        if(!email!!.isEmpty() && !pass!!.isEmpty()){
            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email,
                pass
            ).addOnCompleteListener {
                if(it.isSuccessful) {
                    Toast.makeText(context, "Logueado Correctamente", Toast.LENGTH_SHORT).show()
                    sesionIniciada()
                    findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
                }else
                    Toast.makeText(context,"Ocurrio un error el loguearse", Toast.LENGTH_SHORT).show()
            }
        }
        /*if(email == null) {
            b_reg.isEnabled = true
            b_login.isEnabled = true
            b_salir.isEnabled = false
        } else {
            b_reg.isEnabled = false
            b_login.isEnabled = false
            b_salir.isEnabled = true
        }*/
    }
}