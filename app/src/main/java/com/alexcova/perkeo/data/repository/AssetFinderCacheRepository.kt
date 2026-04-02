package com.alexcova.perkeo.data.repository

import android.content.Context
import com.alexcova.perkeo.domain.model.finder.CompressedSeed
import com.alexcova.perkeo.domain.model.finder.FinderDataItem
import com.alexcova.perkeo.domain.repository.FinderCacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.EOFException
import java.io.FileNotFoundException

class AssetFinderCacheRepository(
    private val context: Context,
) : FinderCacheRepository {
    private var cachedCompressed: List<CompressedSeed> = emptyList()
    private var cachedItems: List<FinderDataItem> = emptyList()

    override suspend fun readInstant(): List<CompressedSeed> = withContext(Dispatchers.IO) {
        if (cachedCompressed.isNotEmpty()) {
            return@withContext cachedCompressed
        }

        cachedItems = emptyList()
        cachedCompressed = readCompressedAsset("canio.jkr")
        cachedCompressed
    }

    override suspend fun readJokerData(): List<FinderDataItem> = withContext(Dispatchers.IO) {
        if (cachedItems.isNotEmpty()) {
            return@withContext cachedItems
        }

        cachedCompressed = emptyList()
        cachedItems = readFinderDataAsset("perkeo.jkr")
        cachedItems
    }

    private fun readCompressedAsset(assetName: String): List<CompressedSeed> {
        val result = mutableListOf<CompressedSeed>()
        val input = openAsset(assetName) ?: return emptyList()

        DataInputStream(BufferedInputStream(input)).use { stream ->
            while (true) {
                try {
                    val a = stream.readLong()
                    val b = stream.readLong()
                    val c = stream.readLong()
                    val d = stream.readLong()
                    result += CompressedSeed(longArrayOf(a, b, c, d))
                } catch (_: EOFException) {
                    break
                }
            }
        }

        return result
    }

    private fun readFinderDataAsset(assetName: String): List<FinderDataItem> {
        val result = mutableListOf<FinderDataItem>()
        val input = openAsset(assetName) ?: return emptyList()

        DataInputStream(BufferedInputStream(input)).use { stream ->
            while (true) {
                try {
                    val seedBytes = ByteArray(8)
                    stream.readFully(seedBytes)
                    val seed = String(seedBytes, Charsets.UTF_8)
                    require(seed.matches(Regex("^[a-zA-Z0-9]{8}$"))) {
                        "Invalid seed in finder data: $seed"
                    }

                    val score = stream.readInt()
                    val dataSize = stream.readInt()
                    val values = IntArray(dataSize)
                    for (i in 0 until dataSize) {
                        values[i] = stream.readInt()
                    }

                    result += FinderDataItem(seed = seed, score = score, data = values)
                } catch (_: EOFException) {
                    break
                }
            }
        }

        return result
    }

    private fun openAsset(assetName: String) = try {
        context.assets.open(assetName)
    } catch (_: FileNotFoundException) {
        null
    }
}
