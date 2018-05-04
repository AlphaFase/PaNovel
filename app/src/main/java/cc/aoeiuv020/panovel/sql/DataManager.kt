package cc.aoeiuv020.panovel.sql

import android.content.Context
import android.support.annotation.VisibleForTesting
import cc.aoeiuv020.panovel.api.DetailRequester
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.sql.entity.*
import cc.aoeiuv020.panovel.api.NovelDetail as NovelDetailApi

/**
 * 封装多个数据库的联用，
 * 隐藏所有数据库实体，这里进出的都是专用的kotlin数据类，
 *
 * Created by AoEiuV020 on 2018.04.28-16:53:14.
 */
object DataManager {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var app: AppDatabaseManager
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var cache: CacheDatabaseManager

    @Synchronized
    fun init(context: Context) {
        if (!::app.isInitialized) {
            app = AppDatabaseManager(context)
        }
        if (!::cache.isInitialized) {
            cache = CacheDatabaseManager(context)
        }
    }

    private fun getNovelDetail(detailRequester: DetailRequester): NovelDetail? {
        return cache.queryByDetailRequester(detailRequester)
    }

    fun getNovelDetailToApi(detailRequester: DetailRequester): NovelDetailApi? {
        // TODO: NovelDetailApi里的update要处理一下，
        return getNovelDetail(detailRequester)?.api
    }

    fun putNovelDetailFromApi(novelDetailApi: NovelDetailApi) {
        // TODO: novelDetailApi里的update要处理一下，
        return cache.putNovelDetail(novelDetailApi.sql)
    }

    fun getNovelStatus(detailRequester: DetailRequester): NovelStatus? {
        return cache.queryStatusByDetailRequester(detailRequester)
    }

    fun putChapters(detailRequester: DetailRequester, chapters: List<NovelChapter>) = cache.db.runInTransaction {
        cache.queryByDetailRequester(detailRequester)?.let { novelDetail ->
            val status = cache.queryOrNewStatus(detailRequester)
            chapters.last().update?.let {
                status.updateTime = it
            }
            cache.putChapters(novelDetail, chapters.mapIndexed { index: Int, novelChapter: NovelChapter ->
                Chapter(
                        novelDetailId = requireNotNull(novelDetail.id),
                        index = index,
                        name = novelChapter.name,
                        textRequesterType = novelChapter.requester.type,
                        textRequesterExtra = novelChapter.requester.extra
                )
            })
        }
    }
}