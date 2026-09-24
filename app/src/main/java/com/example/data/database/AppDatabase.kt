package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.model.*

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        CartItemEntity::class,
        WishlistItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        ReviewEntity::class,
        UserEventEntity::class,
        AIExperimentEntity::class,
        AuditLogEntity::class,
        MerchantEntity::class,
        MerchantOfferEntity::class,
        PriceHistoryEntity::class,
        PriceAlertEntity::class,
        PromoCampaignEntity::class,
        MerchantCouponEntity::class,
        VoucherListingEntity::class,
        VoucherTransactionEntity::class,
        ExternalClickEntity::class,
        NotificationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun orderDao(): OrderDao
    abstract fun reviewDao(): ReviewDao
    abstract fun userEventDao(): UserEventDao
    abstract fun aiExperimentDao(): AIExperimentDao
    abstract fun auditLogDao(): AuditLogDao

    // New marketplace DAOs
    abstract fun merchantDao(): MerchantDao
    abstract fun merchantOfferDao(): MerchantOfferDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    abstract fun priceAlertDao(): PriceAlertDao
    abstract fun promoCampaignDao(): PromoCampaignDao
    abstract fun merchantCouponDao(): MerchantCouponDao
    abstract fun voucherListingDao(): VoucherListingDao
    abstract fun voucherTransactionDao(): VoucherTransactionDao
    abstract fun externalClickDao(): ExternalClickDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smartcart_ai.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
