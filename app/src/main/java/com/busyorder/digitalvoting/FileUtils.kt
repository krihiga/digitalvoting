package com.busyorder.digitalvoting

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

fun fileToMultipartPart(fieldName: String, file: File): MultipartBody.Part {
    val reqFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
    return MultipartBody.Part.createFormData(fieldName, file.name, reqFile)
}
