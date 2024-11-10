package com.example.fingerprint.Apis

import com.google.gson.annotations.SerializedName

data class UploadFile(
    val name: String,
    val type: String,
    val content: String
)


data class UploadResponse(
    @SerializedName("First_name") val First_name: String,
    @SerializedName("Last_name") val Last_name: String,
    @SerializedName("Father_name") val Father_name: String,
    @SerializedName("Email") val Email: String,
    @SerializedName("Gender") val Gender: String,
    @SerializedName("Date_of_birth") val Date_Of_birth: String,
    @SerializedName("Registration_No") val Registration_No: String,
    @SerializedName("Contact_No") val Contact_No: String,
    @SerializedName("Semester") val Semester: String,
    @SerializedName("Batch") val Batch: String,
    @SerializedName("Address") val Address: String,
    @SerializedName("images") val images: List<UploadFile>,
)

