package com.example.yeniproje

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var resetPasswordButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var resetGeriButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Firebase Authentication başlat
        firebaseAuth = FirebaseAuth.getInstance()

        // Görünümleri başlat
        emailEditText = findViewById(R.id.forgot_password_email)
        resetPasswordButton = findViewById(R.id.reset_password_button)
        resetGeriButton = findViewById(R.id.reset_geri_button)

        // Şifre sıfırlama butonu tıklama olayı
        resetPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString()

            if (email.isEmpty()) {
                showToast("Lütfen e-posta adresinizi girin")
            } else {
                // Firebase şifre sıfırlama işlemi
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            showToast("Şifre sıfırlama bağlantısı e-postanıza gönderildi")
                        } else {
                            showToast("Şifre sıfırlama başarısız: ${task.exception?.message}")
                        }
                    }
            }
        }

        // Geri dön butonu tıklama olayı
        resetGeriButton.setOnClickListener {
            finish()  // Bu işlem aktiviteyi bitirir ve bir önceki ekrana döner
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
