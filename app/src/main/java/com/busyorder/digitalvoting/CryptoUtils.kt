package com.busyorder.digitalvoting

import android.util.Base64
import org.json.JSONObject
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

object CryptoUtils {
    private const val AES_KEY = "12345678901234567890123456789012" // must match server

    fun encrypt(input: String): JSONObject {
        val secretKey: SecretKey = SecretKeySpec(AES_KEY.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

        // Generate random IV
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val encrypted = cipher.doFinal(input.toByteArray())

        val obj = JSONObject()
        obj.put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
        obj.put("ciphertext", Base64.encodeToString(encrypted, Base64.NO_WRAP))
        return obj
    }
}
