/*
 * Copyright 2020-2022 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package com.goldenraven.padawanwallet.data.tutorial

import androidx.lifecycle.LiveData

class TutorialRepository(private val tutorialDao: TutorialDao) {
    val readAllData: LiveData<List<Tutorial>> = tutorialDao.readAllTutorial()

    val readAllInitialData: List<Tutorial>? = tutorialDao.readAllTutorial().value

    internal suspend fun getTutorial(id: Int): Tutorial {
        return tutorialDao.getTutorial(id = id)
    }

    internal suspend fun setCompleted(id: Int, completed: Boolean) {
        tutorialDao.setCompleted(id = id, completed = completed)
    }

    private suspend fun addTutorial(tutorial: Tutorial) {
        tutorialDao.addTutorial(tutorial = tutorial)
    }

    suspend fun initTutorial(tutorialList: List<Tutorial>) {
        tutorialList.forEach {
            addTutorial(tutorial = it)
        }
    }
}