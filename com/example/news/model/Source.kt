package com.example.news.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Source(
    val name: String? = null,
    val id: @RawValue Any? = null
) : Parcelable
