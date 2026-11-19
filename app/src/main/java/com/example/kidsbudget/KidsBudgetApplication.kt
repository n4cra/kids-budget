package com.example.kidsbudget

import android.app.Application
import com.example.kidsbudget.data.DefaultKidsRepository
import com.example.kidsbudget.data.KidsRepository
import com.example.kidsbudget.data.local.KidsDatabase

class KidsBudgetApplication : Application() {
  lateinit var repository: KidsRepository
    private set

  override fun onCreate() {
    super.onCreate()
    repository = DefaultKidsRepository(KidsDatabase.create(this))
  }
}
