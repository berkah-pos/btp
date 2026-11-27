package id.berkahtigaputeri.basicpos.domain.repository

import id.berkahtigaputeri.basicpos.domain.model.DashboardSummary
import kotlinx.coroutines.flow.Flow

interface PosRepository {
    fun observeDashboardSummary(): Flow<DashboardSummary>
}
