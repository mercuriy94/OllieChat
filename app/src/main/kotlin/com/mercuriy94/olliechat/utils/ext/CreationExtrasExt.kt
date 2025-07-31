package com.mercuriy94.olliechat.utils.ext

import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras

fun creationExtras(block: MutableCreationExtras.() -> Unit): CreationExtras {
    val mutableCreationExtras = MutableCreationExtras()
    mutableCreationExtras.block()
    return mutableCreationExtras
}
