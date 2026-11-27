package id.berkahtigaputeri.basicpos.data.backup

import id.berkahtigaputeri.basicpos.data.local.entity.CategoryEntity
import id.berkahtigaputeri.basicpos.data.local.entity.ProductEntity
import id.berkahtigaputeri.basicpos.data.local.entity.SalesTransactionEntity
import id.berkahtigaputeri.basicpos.data.local.entity.StoreProfileEntity
import id.berkahtigaputeri.basicpos.data.local.entity.TransactionItemEntity
import org.junit.Assert.assertThrows
import org.junit.Test

class PosBackupValidatorTest {
    private val profile = StoreProfileEntity(
        id = 1,
        storeName = "Toko Berkah",
        ownerName = "Pemilik",
        updatedAtEpochMillis = 100
    )
    private val category = CategoryEntity(
        id = 1,
        name = "Minuman",
        createdAtEpochMillis = 100
    )
    private val product = ProductEntity(
        id = 1,
        name = "Kopi",
        sku = "KOPI-01",
        priceRupiah = 12_500,
        stockQuantity = 10,
        categoryId = 1,
        updatedAtEpochMillis = 100
    )
    private val transaction = SalesTransactionEntity(
        id = 1,
        transactionNumber = "BTP-20261127-0001",
        createdAtEpochMillis = 200,
        subtotalRupiah = 25_000,
        discountRupiah = 2_000,
        taxRupiah = 1_500,
        totalRupiah = 24_500,
        paidRupiah = 30_000,
        changeRupiah = 5_500,
        storeNameSnapshot = "Toko Berkah",
        storeOwnerSnapshot = "Pemilik"
    )
    private val item = TransactionItemEntity(
        id = 1,
        transactionId = 1,
        productId = 1,
        productNameSnapshot = "Kopi",
        skuSnapshot = "KOPI-01",
        unitPriceRupiah = 12_500,
        quantity = 2,
        lineTotalRupiah = 25_000
    )

    @Test
    fun acceptsConsistentBackupWithStoreSnapshot() {
        PosBackupValidator.validate(
            profile,
            listOf(category),
            listOf(product),
            listOf(transaction),
            listOf(item)
        )
    }

    @Test
    fun acceptsLegacyTransactionWithoutStoreSnapshot() {
        val legacyTransaction = transaction.copy(
            storeNameSnapshot = null,
            storeOwnerSnapshot = null,
            storeAddressSnapshot = null,
            storePhoneSnapshot = null,
            receiptFooterSnapshot = null
        )

        PosBackupValidator.validate(
            profile,
            listOf(category),
            listOf(product),
            listOf(legacyTransaction),
            listOf(item)
        )
    }

    @Test
    fun rejectsProductWithUnknownCategory() {
        assertThrows(IllegalArgumentException::class.java) {
            PosBackupValidator.validate(
                profile,
                listOf(category),
                listOf(product.copy(categoryId = 99)),
                listOf(transaction),
                listOf(item)
            )
        }
    }

    @Test
    fun rejectsDuplicateCategoryNamesAndProductSkusAfterTrimming() {
        assertThrows(IllegalArgumentException::class.java) {
            PosBackupValidator.validate(
                profile,
                listOf(category, category.copy(id = 2, name = " Minuman ")),
                listOf(product),
                listOf(transaction),
                listOf(item)
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            PosBackupValidator.validate(
                profile,
                listOf(category),
                listOf(product, product.copy(id = 2, sku = " KOPI-01 ")),
                listOf(transaction),
                listOf(item)
            )
        }
    }

    @Test
    fun rejectsBlankSkuInsteadOfRestoringNonCanonicalValue() {
        assertThrows(IllegalArgumentException::class.java) {
            PosBackupValidator.validate(
                profile,
                listOf(category),
                listOf(product.copy(sku = "   ")),
                listOf(transaction),
                listOf(item)
            )
        }
    }

    @Test
    fun rejectsInconsistentPaymentTotalsAndStoreSnapshots() {
        assertThrows(IllegalArgumentException::class.java) {
            PosBackupValidator.validate(
                profile,
                listOf(category),
                listOf(product),
                listOf(transaction.copy(totalRupiah = 24_499)),
                listOf(item)
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            PosBackupValidator.validate(
                profile,
                listOf(category),
                listOf(product),
                listOf(transaction.copy(storeNameSnapshot = null)),
                listOf(item)
            )
        }
    }

    @Test
    fun rejectsOrphanItemsAndSubtotalMismatches() {
        assertThrows(IllegalArgumentException::class.java) {
            PosBackupValidator.validate(
                profile,
                listOf(category),
                listOf(product),
                listOf(transaction),
                listOf(item.copy(transactionId = 2))
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            PosBackupValidator.validate(
                profile,
                listOf(category),
                listOf(product),
                listOf(transaction),
                listOf(item.copy(quantity = 1, lineTotalRupiah = 12_500))
            )
        }
    }

    @Test
    fun rejectsArithmeticOverflowRatherThanWrappingTotals() {
        assertThrows(ArithmeticException::class.java) {
            PosBackupValidator.validate(
                profile,
                listOf(category),
                listOf(product),
                listOf(transaction),
                listOf(
                    item.copy(
                        unitPriceRupiah = Long.MAX_VALUE,
                        quantity = 2,
                        lineTotalRupiah = 0
                    )
                )
            )
        }
    }
}
