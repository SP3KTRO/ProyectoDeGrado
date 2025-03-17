package com.tupausa

import android.app.Application
import com.tupausa.database.DatabaseHelper
import com.tupausa.repository.TuPausaRepository

class TuPausaApplication : Application() {

    val databaseHelper by lazy { DatabaseHelper(this) }
    val repository by lazy { TuPausaRepository(databaseHelper) }
}