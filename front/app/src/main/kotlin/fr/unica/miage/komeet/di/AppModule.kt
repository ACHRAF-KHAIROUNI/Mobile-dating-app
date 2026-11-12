package fr.unica.miage.komeet.di

import fr.unica.miage.komeet.data.local.SessionDataStore
import fr.unica.miage.komeet.data.remote.KoMeetRemoteSource
import fr.unica.miage.komeet.data.remote.createKtorClient
import fr.unica.miage.komeet.data.repository.AuthRepository
import fr.unica.miage.komeet.data.repository.AuthRepositoryImpl
import fr.unica.miage.komeet.data.repository.ProfileRepository
import fr.unica.miage.komeet.data.repository.ProfileRepositoryImpl
import fr.unica.miage.komeet.ui.auth.LoginViewModel
import fr.unica.miage.komeet.ui.auth.RegisterViewModel
import fr.unica.miage.komeet.ui.discovery.DiscoveryViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Session locale & Token DataStore
    single { SessionDataStore(androidContext()) }

    // Client HTTP Ktor 3
    single { createKtorClient(get()) }

    // Source distante (API KoMeet)
    single { KoMeetRemoteSource(get()) }

    // Dépôts (Repositories)
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }

    // ViewModels injectés avec Koin
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { DiscoveryViewModel(get(), get()) }
}
