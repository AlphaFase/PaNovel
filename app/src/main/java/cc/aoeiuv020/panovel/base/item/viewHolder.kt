package cc.aoeiuv020.panovel.base.item

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cc.aoeiuv020.panovel.local.Bookshelf
import cc.aoeiuv020.panovel.search.FuzzySearchActivity
import cc.aoeiuv020.panovel.sql.entity.NovelStatus
import cc.aoeiuv020.panovel.text.CheckableImageView
import cc.aoeiuv020.panovel.text.NovelTextActivity
import cn.lemon.view.adapter.BaseViewHolder
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.novel_item_big.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.text.SimpleDateFormat

/**
 *
 * Created by AoEiuV020 on 2017.11.22-11:19:03.
 */
abstract class SmallItemViewHolder<out T : SmallItemPresenter<*>>(protected val itemListPresenter: BaseItemListPresenter<*, T>,
                                                                  protected val ctx: Context, parent: ViewGroup?, layoutId: Int,
                                                                  listener: OnItemLongClickListener? = null)
    : BaseViewHolder<NovelItem>(parent, layoutId), SmallItemView, AnkoLogger {
    @Suppress("UNCHECKED_CAST")
    protected val presenter: T = itemListPresenter.subPresenter()
    private val image = itemView.imageView
    private val name = itemView.tvName
    private val author = itemView.tvAuthor
    private val site = itemView.tvSite
    private val last = itemView.tvLast
    /**
     * 书架页没有这个star按钮，
     */
    private val star: CheckableImageView? = itemView.ivStar
    protected lateinit var novelItem: NovelItem

    init {
        name.setOnClickListener {
            NovelDetailActivity.start(ctx, novelItem)
        }

        name.setOnLongClickListener {
            FuzzySearchActivity.start(ctx, novelItem.name, novelItem.author)
            true
        }

        last.setOnClickListener {
            NovelTextActivity.start(ctx, novelItem, -1)
        }

        itemView.setOnClickListener {
            NovelTextActivity.start(ctx, novelItem)
        }

        listener?.let { nonnullListener ->
            itemView.setOnLongClickListener {
                nonnullListener.onItemLongClick(layoutPosition, novelItem)
                true
            }
        }

        star?.apply {
            setOnClickListener {
                toggle()
                if (isChecked) {
                    Bookshelf.add(novelItem)
                } else {
                    Bookshelf.remove(novelItem)
                }
            }
        }
    }

    override fun showNewChapterDot() {
    }

    override fun hideProgressBar() {
    }

    override fun setData(data: NovelItem) {
        this.novelItem = data
        debug {
            "${this.hashCode()} $layoutPosition setData $data"
        }
        star?.isChecked = Bookshelf.contains(novelItem)
        @Suppress("UnnecessaryVariable")
        val novel = data
        name.text = novel.name
        author.text = novel.author
        site.text = novel.site

        // 清空残留数据，避免闪烁，
        last.text = ""
        image.setImageDrawable(null)

        presenter.attach(this)
        presenter.requestDetail(novel)
    }

    override fun showDetail(novelDetail: NovelDetail) {
        // 详情页的时间选择性无视，因为详情页是缓存的，
        // 目前没有获取到updateTime，比如章节还没获取，或者章节里没有更新时间，
        Glide.with(ctx).load(novelDetail.bigImg).into(image)
        presenter.requestChapters(novelDetail)
    }

    override fun showChapter(novelStatus: NovelStatus) {
        last.text = novelStatus.chapterNameLast
    }

    fun destroy() {
        presenter.detach()
    }
}

open class DefaultItemViewHolder<out T : BigItemPresenter<*>>(itemListPresenter: BaseItemListPresenter<*, T>,
                                                              ctx: Context, parent: ViewGroup?, layoutId: Int,
                                                              listener: OnItemLongClickListener? = null)
    : SmallItemViewHolder<T>(itemListPresenter, ctx, parent, layoutId, listener), BigItemView {
    companion object {
        @SuppressLint("SimpleDateFormat")
        private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }

    /**
     * 小型视图没有这两个，
     */
    private val update: TextView? = itemView.tvUpdate
    private val readAt: TextView? = itemView.tvReadAt

    override fun setData(data: NovelItem) {
        super.setData(data)

        // 清空残留数据，避免闪烁，
        update?.text = ""
        readAt?.text = ""
    }

    override fun showChapter(novelStatus: NovelStatus) {
        super.showChapter(novelStatus)
        if (update != null && novelStatus.updateTime != null) {
            update.text = sdf.format(novelStatus.updateTime)
        }
        readAt?.text = novelStatus.chapterNameReadAt
    }
}

