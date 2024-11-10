package com.example.fingerprint.Apis

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MyApis {
    @Multipart
    @POST("students/")
    suspend fun uploadData(
        @Part("First_name") firstName: RequestBody,
        @Part("Last_name") lastName: RequestBody,
        @Part("Father_name") fatherName: RequestBody,
        @Part("Email") email: RequestBody,
        @Part("Gender") gender: RequestBody,
        @Part("Date_of_birth") dateOfBirth: RequestBody,
        @Part("Registration_No") registrationNo: RequestBody,
        @Part("Contact_No") contactNo: RequestBody,
        @Part("Semester") semester: RequestBody?,
        @Part("Batch") batch: RequestBody?,
        @Part("Address") address: RequestBody?,
        @Part images: List<MultipartBody.Part>
    ): Response<ResponseBody>
}
