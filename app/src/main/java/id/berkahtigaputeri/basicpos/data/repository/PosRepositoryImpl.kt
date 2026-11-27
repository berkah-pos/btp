package id.berkahtigaputeri.basicpos.data.repository

import id.berkahtigaputeri.basicpos.data.local.dao.PosDao
import id.berkahtigaputeri.basicpos.domain.model.DashboardSummary
import id.berkahtigaputeri.basicpos.domain.repository.PosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class PosRepositoryImpl(
    private val dao: PosDao
) : PosRepository {
    override fun observeDashboardSummary(): Flow<DashboardSummary> =
        combine(
            dao.observeProductCount(),
            dao.observeTransactionCount()
        ) { productCount, transactionCount ->
            DashboardSummary(
                productCount = productCount,
                transactionCount = transactionCount
            )
        }
}
