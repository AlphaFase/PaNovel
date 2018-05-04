package cc.aoeiuv020.panovel.sql.entity

import cc.aoeiuv020.panovel.api.ChaptersRequester
import cc.aoeiuv020.panovel.api.DetailRequester
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.api.Requester
import java.util.*
import cc.aoeiuv020.panovel.api.NovelDetail as NovelDetailApi

/**
 * Created by AoEiuV020 on 2018.04.29-18:48:10.
 */

val NovelMini.detailRequester: DetailRequester
    get() = Requester.deserialization(detailRequesterType, detailRequesterExtra) as DetailRequester
val NovelDetail.detailRequester: DetailRequester
    get() = Requester.deserialization(detailRequesterType, detailRequesterExtra) as DetailRequester
val NovelDetail.chaptersRequester: ChaptersRequester
    get() = Requester.deserialization(detailRequesterType, detailRequesterExtra) as ChaptersRequester

val NovelDetailApi.sql: NovelDetail
    get() = NovelDetail(
            name = this.novel.name,
            author = this.novel.author,
            site = this.novel.site,
            imageUrl = this.bigImg,
            introduction = this.introduction,
            detailRequesterType = this.novel.requester.type,
            detailRequesterExtra = this.novel.requester.extra,
            chaptersRequesterType = this.requester.type,
            chaptersRequesterExtra = this.requester.extra
    )
val NovelDetail.api: NovelDetailApi
    get() = NovelDetailApi(
            NovelItem(this.name, this.author, this.site, this.detailRequester),
            this.imageUrl,
            Date(0),
            this.introduction,
            this.chaptersRequester
    )
