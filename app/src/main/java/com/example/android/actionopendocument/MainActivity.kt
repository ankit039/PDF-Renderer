/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.actionopendocument

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.transaction

const val DOCUMENT_FRAGMENT_TAG = "com.example.android.actionopendocument.tags.DOCUMENT_FRAGMENT"

/**
 * Simple activity to host [ActionOpenDocumentFragment].
 */
class MainActivity : AppCompatActivity() {

    private lateinit var noDocumentView: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_real)

        noDocumentView = findViewById(R.id.no_document_view)
        findViewById<Button>(R.id.open_file).setOnClickListener {
            openDocumentPicker()
        }

        getSharedPreferences(TAG, Context.MODE_PRIVATE).let { sharedPreferences ->
            if (sharedPreferences.contains(LAST_OPENED_URI_KEY)) {
                val documentUri =
                    sharedPreferences.getString(LAST_OPENED_URI_KEY, null)?.toUri() ?: return@let
                openDocument(documentUri)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open -> {
                openDocumentPicker()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == OPEN_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { documentUri ->
                contentResolver.takePersistableUriPermission(
                    documentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                openDocument(documentUri)
            }
        }
    }

    private fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, OPEN_DOCUMENT_REQUEST_CODE)
    }

    private fun openDocument(documentUri: Uri) {

        getSharedPreferences(TAG, Context.MODE_PRIVATE).edit {
            putString(LAST_OPENED_URI_KEY, documentUri.toString())
        }

        val fragment = ActionOpenDocumentFragment.newInstance(documentUri)
        supportFragmentManager.transaction {
            add(R.id.container, fragment, DOCUMENT_FRAGMENT_TAG)
        }

        // Document is open, so get rid of the call to action view.
        noDocumentView.visibility = View.GONE
    }
}

private const val OPEN_DOCUMENT_REQUEST_CODE = 0x33
private const val TAG = "MainActivity"
private const val LAST_OPENED_URI_KEY =
    "com.example.android.actionopendocument.pref.LAST_OPENED_URI_KEY"

