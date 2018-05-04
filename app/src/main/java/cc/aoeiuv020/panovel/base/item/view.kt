package cc.aoeiuv020.panovel.base.item

import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.sql.entity.NovelStatus

/**
 *
 * Created by AoEiuV020 on 2017.11.22-10:48:04.
 */
interface BaseItemListView : IView {
    fun showError(message: String, e: Throwable)
}

interface SmallItemView : IView {
    fun showDetail(novelDetail: NovelDetail)
    fun showNewChapterDot()
    fun showChapter(novelStatus: NovelStatus)
    fun hideProgressBar()
}

interface BigItemView : SmallItemView