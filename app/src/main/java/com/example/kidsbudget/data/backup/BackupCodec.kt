package com.example.kidsbudget.data.backup

import kotlinx.serialization.json.Json

object BackupCodec {
  private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
  }

  fun encode(backup: BackupFile): String = json.encodeToString(BackupFile.serializer(), backup)

  fun decode(raw: String): BackupFile {
    val parsed =
      try {
        json.decodeFromString(BackupFile.serializer(), raw)
      } catch (error: Exception) {
        throw BackupException("This file is not a Kids Budget backup.", error)
      }
    if (parsed.format != BackupFile.FORMAT) {
      throw BackupException("This file is not a Kids Budget backup.")
    }
    if (parsed.version > BackupFile.VERSION) {
      throw BackupException("This backup was made with a newer version of the app.")
    }
    parsed.children.forEach { child ->
      if (child.name.isBlank()) throw BackupException("A child in this backup is missing a name.")
      child.accounts.forEach { account ->
        if (account.name.isBlank()) {
          throw BackupException("An account for ${child.name} is missing a name.")
        }
      }
    }
    return parsed
  }
}
