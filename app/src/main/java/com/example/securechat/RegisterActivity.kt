package com.example.securechat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerPhoto.setOnClickListener {
            Log.d("RegisterActivity", "Uploading photo")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        registerButton.setOnClickListener {
            performRegistration()
        }

        registerAlreadyHaveAccount.setOnClickListener {
            Log.d("RegisterActivity", "Try to show login activity")

            // Launch login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }
    }

    var selectedPhotoUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RegisterActivity", "Photo was selected")

            selectedPhotoUri = data.data
            registerPhoto.setImageURI(selectedPhotoUri)
        }
    }

    /**
     * Register/Create user function
     */
    private fun performRegistration() {
        val username = registerUsername.text.toString()
        val email = registerEmail.text.toString()
        val password = registerPassword.text.toString()

        // To avoid fatal crash error when all fields are blank while register button is clicked
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity", "Username is: $username")
        Log.d("RegisterActivity", "Email is: $email")
        Log.d("RegisterActivity", "Password is: $password")

        // Firebase authentication to create a user with email and password. Possibly lambda function is used.
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d("RegisterActivity", "Successfully created user with uid: ${it.result?.user?.uid}")

                uploadImageToFirebaseStorage()

            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Failed to create user: ${it.message}")
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()

            }

    }


    private fun uploadImageToFirebaseStorage() {
        if(selectedPhotoUri == null) return


        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl
                    .addOnSuccessListener {
                        Log.d("RegisterActivity", "File location: $it")

                        saveUserToFirebaseDatabase(it.toString())
                    }
                    .addOnFailureListener {
                        Log.d("RegisterActivity", "Image URL not found: ${it.message}")
                    }
            }

    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, registerUsername.text.toString(), profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Successfully added user to firebase database")
            }
            .addOnFailureListener {
                /**
                 * Can happen due to permission denied error. If so, go to realtime database and change rules to
                 * read = "auth!=null"
                 * write = "auth!=null"
                 */
                Log.d("RegisterActivity", "Failed to add user to firebase database: ${it.message}")
            }
    }
}

class User(val uid: String, val username: String, val profileImageUrl: String)