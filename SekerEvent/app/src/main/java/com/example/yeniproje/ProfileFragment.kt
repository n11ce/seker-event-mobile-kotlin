package com.example.yeniproje

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Firebase bağlantıları
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()

        val profileImage: ImageView = view.findViewById(R.id.editProfileImageView)
        val changeImageButton: Button = view.findViewById(R.id.change_profile_image_button)
        val nameEditText: EditText = view.findViewById(R.id.edit_name)
        val notificationCheckBox: CheckBox = view.findViewById(R.id.notification_checkbox)
        val updateButton: Button = view.findViewById(R.id.update_profile_button)
        val logoutButton: Button = view.findViewById(R.id.logout_button)

        val userId = auth.currentUser?.uid

        // Kullanıcı resmini yüklemek için bir ActivityResultContracts kullanıyoruz
        val getProfileImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imageUri = uri
                profileImage.setImageURI(uri)
            }
        }

        // Profil resmini değiştirmek için buton tıklama işlemi
        changeImageButton.setOnClickListener {
            getProfileImage.launch("image/*")  // Kullanıcının resim seçmesini sağlıyor
        }

        // Firebase'den kullanıcı bilgilerini çekme
        userId?.let {
            database.child("users").child(it).get().addOnSuccessListener { snapshot ->
                val adSoyad = snapshot.child("adSoyad").value?.toString() ?: ""
                val notificationEnabled = snapshot.child("notificationEnabled").value as? Boolean ?: true
                val profileImageUrl = snapshot.child("profileResmi").value?.toString()

                // Kullanıcı adı ve bildirim tercihini güncelleme
                nameEditText.setText(adSoyad)
                notificationCheckBox.isChecked = notificationEnabled

                // Kullanıcı profil resmini yükleme (Firebase Storage'dan)
                profileImageUrl?.let { url -> Picasso.get().load(url).into(profileImage) }
            }
        }

        // Profil bilgilerini güncelleme
        updateButton.setOnClickListener {
            val updatedName = nameEditText.text.toString()
            val notificationEnabled = notificationCheckBox.isChecked

            val userUpdates = mapOf(
                "adSoyad" to updatedName,
                "notificationEnabled" to notificationEnabled
            )

            // Eğer resim değiştirilmişse, Firebase Storage'a yükle
            imageUri?.let { uri ->
                val storageRef = storage.reference.child("profile_images/${userId}.jpg")
                storageRef.putFile(uri).addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        // Firebase Realtime Database'e resim URL'sini kaydetme
                        database.child("users").child(userId!!).updateChildren(userUpdates)
                        database.child("users").child(userId).child("profileResmi").setValue(downloadUrl.toString())
                        Toast.makeText(requireContext(), "Profil güncellendi!", Toast.LENGTH_SHORT).show()
                    }
                }
            } ?: run {
                // Eğer resim değiştirilmemişse sadece profil bilgilerini güncelle
                database.child("users").child(userId!!).updateChildren(userUpdates)
                Toast.makeText(requireContext(), "Profil güncellendi!", Toast.LENGTH_SHORT).show()
            }
        }

        // Çıkış yap işlemi
        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()  // Aktif activity'yi kapatma
        }
    }

    // Fragment'a geçerken verileri sıfırlamak için onDestroyView() metodu
    override fun onDestroyView() {
        super.onDestroyView()
        val nameEditText: EditText? = view?.findViewById(R.id.edit_name)

        nameEditText?.text?.clear()  // Ad soyad text'i sıfırla
    }
}
