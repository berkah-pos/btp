package id.berkahtigaputeri.basicpos

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class PosCashierHappyPathTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private var testSku: String? = null

    private val testApplication: BerkahPosTestApplication
        get() = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as BerkahPosTestApplication

    @Test
    fun createProductCheckoutShowsReceiptAndTransactionHistory() {
        val suffix = UUID.randomUUID().toString().take(8).uppercase()
        val productName = "E2E POS $suffix"
        val sku = "E2E-$suffix"
        testSku = sku

        composeRule.onNodeWithTag("nav.products").performClick()
        composeRule.onNodeWithTag("products.add").performClick()
        composeRule.onNodeWithTag("product.name").performTextInput(productName)
        composeRule.onNodeWithTag("product.sku").performTextInput(sku)
        composeRule.onNodeWithTag("product.price").performTextInput("12500")
        composeRule.onNodeWithTag("product.stock").performTextInput("3")
        composeRule.onNodeWithTag("product.save").performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("product.card.$sku").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("nav.cashier").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("cashier.add.$sku").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("cashier.add.$sku").performClick()
        composeRule.onNodeWithTag("cashier.paid").performScrollTo().performTextInput("15000")
        composeRule.onNodeWithTag("cashier.checkout").performScrollTo().assertIsEnabled().performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("cashier.receipt.title").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("cashier.receipt.item.$sku").assertTextContains(productName)
        composeRule.onNodeWithTag("cashier.receipt.total").assertTextContains("12.500")

        val dao = testApplication.instrumentationDatabase.posDao()
        val sale = runBlocking { dao.getAllTransactionsForBackup().single() }
        assertEquals(1, runBlocking { dao.observeTransactionCount().first() }.toInt())
        val savedProduct = runBlocking {
            dao.getAllProductsForBackup().single { it.sku == sku }
        }
        assertEquals(2L, savedProduct.stockQuantity)
        assertEquals(1, runBlocking {
            dao.getAllTransactionItemsForBackup().count {
                it.transactionId == sale.id && it.skuSnapshot == sku
            }
        })

        composeRule.onNodeWithTag("cashier.receipt.done").performClick()
        composeRule.onNodeWithTag("nav.transactions").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("transactions.row").fetchSemanticsNodes().isNotEmpty()
        }
        val transactionRows = composeRule.onAllNodesWithTag("transactions.row")
        transactionRows.assertCountEquals(1)
        transactionRows.onFirst().performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("transactions.receipt.title").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("transactions.receipt.item.$sku").assertTextContains(productName)
        composeRule.onNodeWithTag("transactions.receipt.total").assertTextContains("12.500")
    }

    @After
    fun removeOnlyThisTestSaleAndProduct() = runBlocking {
        val sku = testSku ?: return@runBlocking
        val database = testApplication.instrumentationDatabase
        val dao = database.posDao()
        val saleIds = dao.getAllTransactionItemsForBackup()
            .filter { it.skuSnapshot == sku }
            .map { it.transactionId }
            .distinct()

        saleIds.forEach { saleId ->
            database.openHelper.writableDatabase.execSQL(
                "DELETE FROM transaction_items WHERE transactionId = ?",
                arrayOf(saleId)
            )
            database.openHelper.writableDatabase.execSQL(
                "DELETE FROM sales_transactions WHERE id = ?",
                arrayOf(saleId)
            )
        }

        dao.getAllProductsForBackup()
            .filter { it.sku == sku }
            .forEach { product -> dao.deleteProduct(product) }
    }
}
