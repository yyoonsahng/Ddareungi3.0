package com.example.ddareungi.data

import com.example.ddareungi.data.source.DataRepository
import java.util.*

class DataRepositoryHolder {
    companion object {
        private val mDataRepositoryHolder = mutableMapOf<String, DataRepository>()

        fun putDataRepository(dataRepository: DataRepository): String {
            val holderId = UUID.randomUUID().toString()
            mDataRepositoryHolder[holderId] = dataRepository

            return holderId
        }

        fun popDataRepository(key: String): DataRepository {
            val dataRepository = mDataRepositoryHolder[key]
            mDataRepositoryHolder.remove(key)

            return dataRepository!!
        }
    }
}