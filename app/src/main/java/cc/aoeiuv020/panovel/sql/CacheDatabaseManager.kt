package cc.aoeiuv020.panovel.sql

import android.content.Context
import android.support.annotation.VisibleForTesting
import cc.aoeiuv020.panovel.api.DetailRequester
import cc.aoeiuv020.panovel.sql.db.CacheDatabase
import cc.aoeiuv020.panovel.sql.entity.Chapter
import cc.aoeiuv020.panovel.sql.entity.NovelDetail
import cc.aoeiuv020.panovel.sql.entity.NovelStatus

/**
 * 封装一个数据库多个表多个DAO的联用，
 *
 * Created by AoEiuV020 on 2018.04.28-16:55:41.
 */
class CacheDatabaseManager(context: Context) {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val db: CacheDatabase = CacheDatabase.build(context)

    fun putChapters(novelDetail: NovelDetail, chapters: List<Chapter>) = db.runInTransaction {
        val id = requireNotNull(novelDetail.id) {
            "require novelDetail.id was null,"
        }
        db.chapterDao().removeChaptersByNovelId(id)
        db.chapterDao().insertChapters(chapters)
    }

    fun getChapters(novelDetail: NovelDetail): List<Chapter> {
        val id = requireNotNull(novelDetail.id) {
            "require novelDetail.id was null,"
        }
        return db.chapterDao().queryChaptersByNovelDetailId(id)
    }

    fun queryByDetailRequester(novelDetail: NovelDetail): NovelDetail? {
        val type = novelDetail.detailRequesterType
        val extra = novelDetail.detailRequesterExtra
        return queryByDetailRequester(type, extra)
    }

    fun queryByDetailRequester(detailRequester: DetailRequester): NovelDetail? {
        return queryByDetailRequester(detailRequester.type, detailRequester.extra)
    }

    fun queryByDetailRequester(type: String, extra: String): NovelDetail? {
        return db.novelDetailDao().queryByDetailRequester(type, extra)
    }

    fun putNovelDetail(novelDetail: NovelDetail) {
        // TODO: 测试下更新时会不会跟着删除章节什么的，
        db.novelDetailDao().insertOrUpdate(novelDetail)
    }

    fun queryStatusByDetailRequester(detailRequester: DetailRequester): NovelStatus? {
        return queryStatusByDetailRequester(detailRequester.type, detailRequester.extra)
    }

    fun queryStatusByDetailRequester(type: String, extra: String): NovelStatus? {
        return db.novelStatusDao().queryStatusByDetailRequester(type, extra)
    }

    fun queryOrNewStatus(detailRequester: DetailRequester): NovelStatus {
        return queryOrNewStatus(detailRequester)
    }
}