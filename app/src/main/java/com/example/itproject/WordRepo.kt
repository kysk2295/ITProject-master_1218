package com.example.itproject

import com.google.gson.annotations.SerializedName

data class WordRepo(@SerializedName("entry") val entry:String,
                    @SerializedName("meaning") val meaning:Meaning)

data class Meaning(@SerializedName("korean") val korean:String,
                   @SerializedName("noun") val noun:String)
