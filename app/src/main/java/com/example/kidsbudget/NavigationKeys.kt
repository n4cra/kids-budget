package com.example.kidsbudget

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object KidsList : NavKey

@Serializable data class KidDetail(val childId: Long) : NavKey

@Serializable data object AddChild : NavKey

@Serializable data class EditChild(val childId: Long) : NavKey

@Serializable data class AddAccount(val childId: Long) : NavKey

@Serializable data class AddTransaction(val childId: Long, val accountId: Long? = null) : NavKey
