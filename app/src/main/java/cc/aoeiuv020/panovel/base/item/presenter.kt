package cc.aoeiuv020.panovel.base.item

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.Cache
import cc.aoeiuv020.panovel.local.Progress
import cc.aoeiuv020.panovel.local.bookId
import cc.aoeiuv020.panovel.local.toJson
import cc.aoeiuv020.panovel.server.UpdateManager
import cc.aoeiuv020.panovel.sql.DataManager
import cc.aoeiuv020.panovel.sql.entity.NovelStatus
import cc.aoeiuv020.panovel.util.async
import cc.aoeiuv020.panovel.util.suffixThreadName
import io.reactivex.Observable
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
import org.jetbrains.anko.verbose
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2017.11.22-10:45:36.
 */
abstract class BaseItemListPresenter<V : BaseItemListView, out T : SmallItemPresenter<*>> : Presenter<V>() {
    var refreshTime = 0L

    abstract fun refresh()

    fun forceRefresh() {
        refreshTime = System.currentTimeMillis()
        refresh()
    }

    abstract fun subPresenter(): T
}

abstract class DefaultItemListPresenter<V : BaseItemListView>
    : BaseItemListPresenter<V, DefaultItemPresenter>() {
    override fun subPresenter(): DefaultItemPresenter = DefaultItemPresenter(this)
}

class DefaultItemPresenter(itemListPresenter: BaseItemListPresenter<*, *>)
    : BigItemPresenter<DefaultItemViewHolder<*>>(itemListPresenter)

abstract class SmallItemPresenter<T : SmallItemView>(protected val itemListPresenter: BaseItemListPresenter<*, *>) : Presenter<T>() {
    open val refreshTime: Long
        get() = itemListPresenter.refreshTime

    fun requestDetail(novelItem: NovelItem) {
        debug { "request detail $novelItem" }
        Observable.fromCallable {
            suffixThreadName("requestDetail")
            DataManager.getNovelDetailToApi(novelItem.requester)
                    ?: NovelContext.getNovelContextByUrl(novelItem.requester.url)
                            .getNovelDetail(novelItem.requester)
                            .also { DataManager.putNovelDetailFromApi(it) }
        }.async().subscribe({ novelDetail ->
            view?.showDetail(novelDetail)
        }, { e ->
            val message = "读取《${novelItem.bookId}》详情失败，"
            error(message, e)
            itemListPresenter.view?.showError(message, e)
        }).let { addDisposable(it, 0) }
    }

    fun requestChapters(detail: NovelDetail) {
        Observable.create<NovelStatus> { em ->
            // TODO: 这里真的非常糟糕了，
            // 还有其他地方有requestChapters，所以多加个后缀，
            suffixThreadName("requestChaptersItem")
            val novelItem = detail.novel
            val progress = Progress.load(novelItem).chapter
            val cachedStatus = DataManager.getNovelStatus(novelItem.requester)?.also {
                em.onNext(it)
            }
            var fromCache = true
            val novel = UpdateManager.query(novelItem.requester)
            verbose {
                "向服务器查询结果 ${novel?.toJson()}"
            }
            // 只对比长度，时间可空真的很麻烦，
            fun Pair<Date?, Int?>.newerThan(other: NovelStatus): Boolean {
                return (second ?: 0 > other.chaptersCount)
            }

            val refreshChapters = Cache.chapters.get(novelItem, refreshTime = refreshTime)
            // 如果服务器告知有更新，就刷新，否则这个留空，
            val chapters = if (refreshChapters == null ||
                    (novel != null
                            && cachedStatus != null
                            && novel.run { updateTime to chaptersCount }.newerThan(cachedStatus))) {
                debug {
                    "${novelItem.name} 要更新，${cachedStatus?.chaptersCount} -> ${novel?.chaptersCount}"
                }
                NovelContext.getNovelContextByUrl(novelItem.requester.url).also { fromCache = false }
                        .getNovelChaptersAsc(detail.requester).also {
                            DataManager.putChapters(detail.novel.requester, it)
                        }
            } else {
                null
            }
            chapters?.let {
                em.onNext(Pair(it, progress))
            }
            // 只对比长度，时间可空真的很麻烦，
            fun List<NovelChapter>.newerThan(other: List<NovelChapter>): Boolean {
                return (size > other.size)
            }
            if (chapters != null
                    && cachedChapters != null
                    && chapters.newerThan(cachedChapters)) {
                UpdateManager.uploadUpdate(novelItem.requester, chapters.size, chapters.last().update)
            } else if (chapters != null
                    && !fromCache) {
                // 只是从缓存中拿出来的就不要上传了，
                UpdateManager.touch(novelItem.requester, chapters.size, chapters.last().update)
            }
            em.onComplete()
        }.async().subscribe({ novelStatus ->
            debug { "展示章节 $novelStatus" }
            view?.showChapter(novelStatus)
        }, { e ->
            val message = "读取《${detail.novel.bookId}》章节失败，"
            error(message, e)
            itemListPresenter.view?.showError(message, e)
        }, {
            view?.hideProgressBar()
        }).let { addDisposable(it, 1) }
    }

}

abstract class BigItemPresenter<T : BigItemView>(itemListPresenter: BaseItemListPresenter<*, *>) : SmallItemPresenter<T>(itemListPresenter) {
}