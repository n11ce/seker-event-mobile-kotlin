package com.example.yeniproje

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    // Görünüm elemanları
    private lateinit var adSoyadEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var telefonEditText: EditText
    private lateinit var dogumTarihEditText: EditText
    private lateinit var sifreEditText: EditText
    private lateinit var kayitOlButton: Button
    private lateinit var geriDonButton: Button

    // Firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Firebase Authentication ve Realtime Database başlatma
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Görünüm elemanlarını bağlama
        adSoyadEditText = findViewById(R.id.register_adsoyad_txt)
        emailEditText = findViewById(R.id.register_email_txt)
        telefonEditText = findViewById(R.id.register_telefon_txt)
        dogumTarihEditText = findViewById(R.id.register_dogumtarih_txt)
        sifreEditText = findViewById(R.id.editTextTextPassword)
        kayitOlButton = findViewById(R.id.register_kayitol_btn)
        geriDonButton = findViewById(R.id.register_geridon_btn)

        // Kayıt Ol butonu tıklama dinleyicisi
        kayitOlButton.setOnClickListener {
            val adSoyad = adSoyadEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val telefon = telefonEditText.text.toString().trim()
            val dogumTarih = dogumTarihEditText.text.toString().trim()
            val sifre = sifreEditText.text.toString().trim()

            if (adSoyad.isNotEmpty() && email.isNotEmpty() && telefon.isNotEmpty() && dogumTarih.isNotEmpty() && sifre.isNotEmpty()) {
                registerUser(adSoyad, email, telefon, dogumTarih, sifre)
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            }
        }

        // Geri Dön butonu tıklama dinleyicisi
        geriDonButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Kullanıcıyı kaydetme
    private fun registerUser(adSoyad: String, email: String, telefon: String, dogumTarih: String, sifre: String) {
        mAuth.createUserWithEmailAndPassword(email, sifre)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    user?.let {
                        saveUserToDatabase(it, adSoyad, telefon, dogumTarih)
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Kayıt başarısız oldu."
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Kullanıcı bilgilerini veritabanına kaydetme
    private fun saveUserToDatabase(user: FirebaseUser, adSoyad: String, telefon: String, dogumTarih: String) {
        val userId = user.uid
        val defaultProfileImageUrl = "gs://androidprojeson.firebasestorage.app/profile_images/profile.png"

        // Kullanıcı verileri
        val userData = mapOf(
            "adSoyad" to adSoyad,
            "email" to user.email,
            "telefon" to telefon,
            "dogumTarih" to dogumTarih,
            "profilResmi" to defaultProfileImageUrl // Varsayılan profil resmi URL'si
        )

        // Veritabanına kaydetme
        database.child("users").child(userId).setValue(userData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Kayıt başarılı, giriş ekranına yönlendiriliyorsunuz.", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                } else {
                    val errorMessage = task.exception?.message ?: "Veritabanına kaydedilemedi."
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    // LoginActivity'ye yönlendirme
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
