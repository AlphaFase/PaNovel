package cc.aoeiuv020.panovel.sql.dao

import android.arch.persistence.room.*
import android.support.annotation.VisibleForTesting
import cc.aoeiuv020.panovel.sql.entity.NovelDetail

/**
 * 操作缓存数据库中的表，
 * Created by AoEiuV020 on 2018.04.25-21:47:54.
 */
@Dao
abstract class NovelDetailDao {
    @Insert
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    abstract fun insertNovels(vararg novelDetails: NovelDetail)

    @Insert
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    abstract fun insertNovel(novelDetails: NovelDetail): Long

    @Query("delete from NovelDetail")
    abstract fun deleteAll(): Int

    @Update
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    abstract fun updateNovel(novelDetail: NovelDetail)

    @Query("select * from NovelDetail" +
            " where detailRequesterType = :type" +
            " and detailRequesterExtra = :extra")
    abstract fun queryByDetailRequester(type: String, extra: String): NovelDetail?

    @Query("select count(*) from NovelDetail")
    abstract fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrUpdate(novelDetails: NovelDetail): Long

}