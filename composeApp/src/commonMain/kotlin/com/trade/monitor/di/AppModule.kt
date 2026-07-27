package com.trade.monitor.di

import com.trade.monitor.service.TradeApiService
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {
    single { TradeApiService() }
}
