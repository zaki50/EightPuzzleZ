package org.zakky.eightpuzzlez

import android.app.Application

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        GameRepository.init(this)
    }
}