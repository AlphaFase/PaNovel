package cc.aoeiuv020.panovel.refresher

import cc.aoeiuv020.panovel.server.ServerAddress
import cc.aoeiuv020.panovel.server.common.toBean
import java.io.File

/**
 * Created by AoEiuV020 on 2018.04.21-16:05:40.
 */
fun main(args: Array<String>) {
    val ite = args.iterator()
    var address: ServerAddress = ServerAddress.new("localhost:8080")
    var config = Config()
    val bookshelfList = mutableSetOf<String>()

    while (ite.hasNext()) {
        when (ite.next()) {
            "-a" -> {
                address = File(ite.next()).readText().toBean()
            }
            "-h" -> {
                address = ServerAddress.new(ite.next())
            }
            "-b" -> {
                bookshelfList.add(ite.next())
            }
            "-c" -> {
                config = File(ite.next()).readText().toBean()
            }
        }
    }
    Refresher().start(address = address, config = config, bookshelfList = bookshelfList)
}

