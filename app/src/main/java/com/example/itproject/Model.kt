package com.example.itproject

data class Model (val type : Int, val text : String, val text2 : String) {
    companion object {
        const val TITLE_TYPE = 0
        const val CARD_TYPE = 1
    }
}