package id.berkahtigaputeri.basicpos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "store_profile")
data class StoreProfileEntity(
    @PrimaryKey val id: Int = 1,
    val storeName: String,
    val ownerName: String? = null,
    val address: String? = null,
    val phoneNumber: String? = null,
    val receiptFooter: String? = null,
    val updatedAtEpochMillis: Long
)

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAtEpochMillis: Long
)

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["sku"], unique = true)
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sku: String? = null,
    val priceRupiah: Long,
    val stockQuantity: Long,
    val categoryId: Long? = null,
    val updatedAtEpochMillis: Long
)

@Entity(
    tableName = "sales_transactions",
    indices = [
        Index(value = ["transactionNumber"], unique = true),
        Index(value = ["createdAtEpochMillis"])
    ]
)
data class SalesTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionNumber: String,
    val createdAtEpochMillis: Long,
    val subtotalRupiah: Long,
    val discountRupiah: Long,
    val taxRupiah: Long,
    val totalRupiah: Long,
    val paidRupiah: Long,
    val changeRupiah: Long,
    val storeNameSnapshot: String? = null,
    val storeOwnerSnapshot: String? = null,
    val storeAddressSnapshot: String? = null,
    val storePhoneSnapshot: String? = null,
    val receiptFooterSnapshot: String? = null
)

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = SalesTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["transactionId"]), Index(value = ["productId"])]
)
data class TransactionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,
    /** Product ID is a non-FK reference so historical receipts survive product deletion. */
    val productId: Long? = null,
    val productNameSnapshot: String,
    val skuSnapshot: String? = null,
    val unitPriceRupiah: Long,
    val quantity: Long,
    val lineTotalRupiah: Long
)
