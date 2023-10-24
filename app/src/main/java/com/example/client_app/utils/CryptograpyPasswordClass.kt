package com.example.client_app.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class CryptograpyPasswordClass {

    private var key: String = "mysecretkey12345"
    private var secretKeySpec = SecretKeySpec(key.toByteArray(), "EAS")
    fun encrypt(password: String):String {

        var cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        var encryptBytes = cipher.doFinal(password.toByteArray(Charsets.UTF_8))

        return Base64.encodeToString(encryptBytes, Base64.DEFAULT)
    }

    fun decrypt(password: String):String {
        var cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        var decryptBytes = cipher.doFinal(Base64.decode(password, Base64.DEFAULT))

        return String(decryptBytes, Charsets.UTF_8)
    }

}