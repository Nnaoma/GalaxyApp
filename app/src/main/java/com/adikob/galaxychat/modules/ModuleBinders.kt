package com.adikob.galaxychat.modules

import com.adikob.galaxychat.network.ChatService
import com.adikob.galaxychat.network.ChatServiceImpl
import com.adikob.galaxychat.repository.ChatDataRepository
import com.adikob.galaxychat.repository.ChatDataRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class ModuleBinders {
    @ViewModelScoped
    @Binds
    abstract fun bindChatRepository(impl: ChatDataRepositoryImpl): ChatDataRepository

    @Binds
    abstract fun bindChatService(impl: ChatServiceImpl): ChatService
}