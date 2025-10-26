package me.memoryandthought.weighty.automerge

import android.content.Context
import kotlinx.coroutines.future.await
import org.automerge.ObjectId
import org.automerge.ObjectType
import org.automerge.kotlin.findDocument
import org.automerge.kotlin.withDocAsync
import org.automerge.repo.AutomergeUrl
import org.automerge.repo.DocHandle
import org.automerge.repo.Repo
import org.automerge.repo.RepoConfig
import org.automerge.repo.storage.FileSystemStorage

class WeightyRepo private constructor(context: Context) {

    companion object {
        private const val PREFS_NAME = "weighty_automerge"
        private const val KEY_DOC_URL = "doc_url"

        @Volatile
        private var instance: WeightyRepo? = null

        fun getInstance(context: Context): WeightyRepo {
            return instance ?: synchronized(this) {
                instance ?: WeightyRepo(context.applicationContext).also { instance = it }
            }
        }
    }

    val repo: Repo
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        val storageDir = context.filesDir.resolve("automerge")
        storageDir.mkdirs()
        val config = RepoConfig.builder()
            .storage(FileSystemStorage(storageDir.toPath()))
            .build()
        repo = Repo.load(config)
    }

    suspend fun getDocHandle(): DocHandle {
        val savedUrl = prefs.getString(KEY_DOC_URL, null)
        if (savedUrl != null) {
            val url = AutomergeUrl.parse(savedUrl)
            val handle = repo.findDocument(url)
            if (handle != null) return handle
        }
        return createDocument()
    }

    private suspend fun createDocument(): DocHandle {
        val handle = repo.create().await()
        handle.withDocAsync { doc ->
            doc.startTransaction().use { tx ->
                tx.set(ObjectId.ROOT, "exercises", ObjectType.LIST)
                tx.commit()
            }
            null
        }
        prefs.edit().putString(KEY_DOC_URL, handle.url.toString()).apply()
        return handle
    }

    fun close() {
        repo.close()
    }
}
