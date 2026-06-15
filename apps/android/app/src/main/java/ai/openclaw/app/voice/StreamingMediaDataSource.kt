package ai.openclaw.app.voice

import android.media.MediaDataSource
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.min

internal class StreamingMediaDataSource : MediaDataSource() {
  private data class Chunk(val start: Long, val data: ByteArray)

  private val lock = ReentrantLock()
  private val condition = lock.newCondition()
  private val chunks = ArrayList<Chunk>()
  private var totalSize: Long = 0
  private var closed = false
  private var finished = false
  private var lastReadIndex = 0

  fun append(data: ByteArray) {
    if (data.isEmpty()) return
    lock.withLock {
      if (closed || finished) return@withLock
      val chunk = Chunk(totalSize, data)
      chunks.add(chunk)
      totalSize += data.size.toLong()
      condition.signalAll()
    }
  }

  fun finish() {
    lock.withLock {
      if (closed) return@withLock
      finished = true
      condition.signalAll()
    }
  }

  fun fail() {
    lock.withLock {
      closed = true
      condition.signalAll()
    }
  }

  override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
    if (position >= totalSize) return -1
    lock.withLock {
      while (true) {
        if (closed) return -1
        val chunkIndex = findChunkIndex(position)
        if (chunkIndex >= 0) break
        if (finished) return -1
        condition.await()
      }
    }
    val chunkIndex = findChunkIndex(position)
    val chunk = chunks[chunkIndex]
    val chunkOffset = (position - chunk.start).toInt()
    val available = min(chunk.data.size - chunkOffset, size)
    val srcOffset = chunk.data.copyOfRange(chunkOffset, chunkOffset + available)
    srcOffset.copyInto(buffer, offset, 0, available)
    return available
  }

  override fun getSize(): Long = totalSize

  override fun close() {
    lock.withLock {
      closed = true
      condition.signalAll()
    }
  }

  private fun findChunkIndex(position: Long): Int {
    for (i in lastReadIndex until chunks.size) {
      val chunk = chunks[i]
      if (position >= chunk.start && position < chunk.start + chunk.data.size) {
        lastReadIndex = i
        return i
      }
    }
    for (i in 0 until lastReadIndex) {
      val chunk = chunks[i]
      if (position >= chunk.start && position < chunk.start + chunk.data.size) {
        lastReadIndex = i
        return i
      }
    }
    return -1
  }
}
